"""End-to-end smoke check against a running API with the demo seed (backend/README.md demo accounts).

    uv run python scripts/smoke.py https://<api-host> [--origin https://<frontend-origin>]

Read-only apart from signing in, so it is safe on the live demo: it never answers, sends or acknowledges anything.
Prints PASS/FAIL per check and exits 1 if any check failed. Standard library only."""

import argparse
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from collections.abc import Callable
from datetime import UTC, datetime, timedelta
from typing import Any

Json = dict[str, Any]
TIMEOUT = 20  # seconds per request; a free database can be slow on the first query after a pause


class Response:
    def __init__(self, status: int, headers: dict[str, str], body: bytes) -> None:
        self.status, self.headers, self.body = status, {k.lower(): v for k, v in headers.items()}, body

    def json(self) -> Json:
        data: Json = json.loads(self.body)
        return data


class Api:
    def __init__(self, base: str) -> None:
        self.base = base.rstrip("/")

    def call(self, method: str, path: str, body: Json | None = None, headers: dict[str, str] | None = None) -> Response:
        data = json.dumps(body).encode() if body is not None else None
        req = urllib.request.Request(self.base + path, data=data, method=method, headers=headers or {})  # noqa: S310
        if data is not None:
            req.add_header("Content-Type", "application/json")
        try:
            with urllib.request.urlopen(req, timeout=TIMEOUT) as res:  # noqa: S310  # the URL is the operator's
                return Response(res.status, dict(res.headers), res.read())
        except urllib.error.HTTPError as err:
            return Response(err.code, dict(err.headers), err.read())

    def login(self, code: str, role: str, name: str, pin: str) -> dict[str, str]:
        query = urllib.parse.urlencode({"code": code, "role": role})
        roster = self.call("GET", f"/api/auth/roster?{query}").json()["data"]
        user = next(u for u in roster if u["displayName"] == name)
        token = self.call("POST", "/api/auth/login", {"userId": user["id"], "pin": pin}).json()["data"]["token"]
        return {"Authorization": f"Bearer {token}"}


def _require(condition: object, detail: object) -> None:
    """A check that also runs under `python -O` (unlike assert)."""
    if not condition:
        raise AssertionError(str(detail))


def _ok(res: Response, status: int = 200) -> Json:
    if res.status != status:
        raise AssertionError(f"HTTP {res.status}: {res.body[:120]!r}")
    return res.json()


def run(api: Api, origin: str | None) -> list[tuple[str, bool, str]]:
    who: dict[str, dict[str, str]] = {}
    today = datetime.now(UTC).date()

    def sign_in() -> str:
        who["foreman"] = api.login("SASEBO01", "foreman", "山口 隆", "246810")
        who["worker"] = api.login("SASEBO01", "worker", "Rahim Uddin", "2468")
        who["poster"] = api.login("MINATO26", "poster", "自治会 事務局", "482615")
        return "Foreman, Worker and Poster signed in"

    def guarded() -> str:
        status = api.call("GET", "/api/board").status
        _require(status == 401, f"got {status}")
        return "401 without a token"

    def board() -> str:
        machines = _ok(api.call("GET", "/api/board", headers=who["foreman"]))["data"]["machines"]
        _require(len(machines) >= 4, machines)
        return f"{len(machines)} machines"

    def matrix() -> str:
        rows = _ok(api.call("GET", "/api/skill-matrix", headers=who["foreman"]))["data"]["rows"]
        _require(rows, "no rows")
        return f"{len(rows)} workers"

    def notices() -> str:
        res = api.call("GET", "/api/notices", headers=who["worker"])
        items = _ok(res)["data"]["items"]
        _require(items and b"correctIndex" not in res.body, "no Notices, or the answer key reached a Worker")
        return f"{len(items)} published, no answer key"

    def events() -> str:
        path = f"/api/events?from={today}&to={today + timedelta(days=60)}"
        return f"{len(_ok(api.call('GET', path, headers=who['worker']))['data'])} dates in 60 days"

    def chat() -> str:
        threads = _ok(api.call("GET", "/api/threads", headers=who["worker"]))["data"]
        path = f"/api/threads/{threads[0]['id']}/messages"
        first = api.call("GET", path, headers=who["worker"])
        _ok(first)
        again = api.call("GET", path, headers=who["worker"] | {"If-None-Match": first.headers["etag"]})
        _require(again.status == 304, f"an unchanged poll gave {again.status}")
        return f"{len(threads)} threads, unchanged poll is 304"

    def tasks() -> str:
        return f"{len(_ok(api.call('GET', '/api/tasks', headers=who['foreman']))['data']['items'])} tasks"

    def senpai() -> str:
        return str(_ok(api.call("GET", "/api/senpai/demo-tanaka-7f3a9c"))["data"]["senpaiName"])

    def support_log() -> str:
        path = f"/api/support-log.csv?from={today - timedelta(days=30)}&to={today}"
        res = api.call("GET", path, headers=who["foreman"])
        _require(res.status == 200 and res.headers.get("content-type", "").startswith("text/csv"), res.status)
        return f"{len(res.body.decode('utf-8-sig').splitlines())} lines"

    def near_misses() -> str:
        return f"{len(_ok(api.call('GET', '/api/near-misses', headers=who['foreman']))['data']['items'])} reports"

    def cors() -> str:
        res = api.call(
            "OPTIONS", "/api/health", headers={"Origin": str(origin), "Access-Control-Request-Method": "GET"}
        )
        allowed = res.headers.get("access-control-allow-origin")
        _require(allowed == origin, f"allow-origin is {allowed!r}")
        return f"{origin} allowed"

    checks: list[tuple[str, Callable[[], str]]] = [
        ("health/ready", lambda: str(_ok(api.call("GET", "/api/health/ready"))["data"])),
        ("sign in (demo accounts)", sign_in),
        ("protected route needs a token", guarded),
        ("Foreman board", board),
        ("Skill Matrix", matrix),
        ("Worker Notices", notices),
        ("Worker Events", events),
        ("Chat poll + ETag", chat),
        ("Task Board", tasks),
        ("Senpai link", senpai),
        ("Support Log CSV", support_log),
        ("Near-miss list", near_misses),
    ]
    if origin:
        checks.append(("CORS preflight", cors))
    results: list[tuple[str, bool, str]] = []
    for name, test in checks:
        try:
            results.append((name, True, test()))
        except Exception as exc:  # noqa: BLE001  # a smoke check reports every failure and carries on
            results.append((name, False, f"{type(exc).__name__}: {exc}"[:200]))
    return results


def main() -> None:
    parser = argparse.ArgumentParser(description="Smoke-check a running Wakatta? API that has the demo seed")
    parser.add_argument("base_url")
    parser.add_argument("--origin", help="frontend origin that CORS must allow, e.g. https://app.example")
    args = parser.parse_args()
    results = run(Api(args.base_url), args.origin)
    for name, passed, detail in results:
        print(f"{'PASS' if passed else 'FAIL'}  {name}: {detail}")  # noqa: T201
    failed = sum(not passed for _, passed, _ in results)
    print(f"{len(results) - failed}/{len(results)} passed")  # noqa: T201
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
