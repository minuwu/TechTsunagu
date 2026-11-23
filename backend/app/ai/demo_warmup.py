"""Warm the AI cache with every filmed demo take, through the running API (AI_PLAN.md §7, XREQ-006 item 6).

    cd backend; uv run python -m app.ai.demo_warmup --base-url https://<api host>      # or http://127.0.0.1:8000

Logs in as the seeded demo users and sends each take once (real AI, answer saved in ai_cache), then sends it again to
show it now comes from the cache in about a second. Run it after the last deploy and before filming: the demo reset
at container start keeps ai_cache (app/seed.py), so the drafts it leaves behind are cleared and the answers stay.

A take only replays from the cache when the input is the same, so film these exact texts (typed, or the same
recording file). A new live recording is a new input and calls the model again."""

import argparse
import sys
import time
import uuid
from collections.abc import Sequence
from typing import Any

import httpx2

# The filmed takes: the prototype's die-change message (prototypes/wakatta/parts/skeleton.html DATA.script), the
# frontend mock's crane Briefing, the plasma-cutter note, the garbage flyer (eval cases), and an Ask that a
# published demo Notice answers.
CHAT_MESSAGE = (
    "明日の朝8時から、2号プレスの金型交換をします。"
    "ラヒムさんとグエンさんは、7時45分に集合してください。"
    "交換の前に、必ず電源をロックアウトして、安全靴と手袋をつけてください。"
    "終わったら点検表を書いて、私に報告してください。"
)
BRIEFING = (
    "今日15時から天井クレーンの点検です。15時から16時まで、クレーンは使えません。"
    "吊り荷の作業は午前中に終わらせてください。"
)
NOTE = (
    "いいか、プラズマは段取りが命だ。保護メガネ。切ったらバリ取り。"
    "寸法はプラスマイナス1ミリ、ノギスで2か所測れ。外れてたら次を切るな、俺を呼べ。"
)
FLYER = (
    "可燃ごみと不燃ごみは、市の指定袋に入れて出してください。資源物は透明の袋に入れてください。"
    "収集日の朝8時までに出してください。"
    "粗大ごみは、クリーンセンターに持ち込むか、有料の戸別収集を申し込んでください。"
)
ASK = "Where do I go for the disaster drill, and what time?"  # answered by the seeded published drill Notice

Json = dict[str, Any]


class Api:
    def __init__(self, base_url: str) -> None:
        self.http = httpx2.Client(base_url=base_url.rstrip("/"), timeout=120)

    def login(self, code: str, role: str, pin: str, name: str) -> dict[str, str]:
        roster: list[Json] = self._data(self.http.get("/api/auth/roster", params={"code": code, "role": role}))
        user = next((u for u in roster if u["displayName"] == name), None)
        if user is None:
            sys.exit(f"no {role} named {name!r} in {code}; is the demo seed loaded?")
        token = self._data(self.http.post("/api/auth/login", json={"userId": user["id"], "pin": pin}))["token"]
        return {"Authorization": f"Bearer {token}"}

    def get(self, path: str, headers: dict[str, str]) -> Any:
        return self._data(self.http.get(path, headers=headers))

    def post_form(self, path: str, headers: dict[str, str], form: dict[str, str]) -> tuple[Json, float]:
        start = time.monotonic()
        r = self.http.post(path, headers=headers | {"Idempotency-Key": str(uuid.uuid4())}, data=form)
        return self._data(r), time.monotonic() - start

    @staticmethod
    def _data(r: httpx2.Response) -> Any:
        if r.status_code >= 400:
            sys.exit(f"{r.request.method} {r.request.url.path}: HTTP {r.status_code} {r.text[:300]}")
        return r.json()["data"]


def _verdict(take: str, data: Json) -> str:
    """What the draft says about the AI: a fallback draft means the take is not safe to film yet."""
    if take == "ask":
        return "covered" if data.get("covered") else "NOT covered"
    if data.get("needsEdit"):
        return "FALLBACK (AI did not answer)"
    return f"ok ({data['aiOutcome']})" if "aiOutcome" in data else "ok"


def main(argv: Sequence[str] | None = None) -> None:
    p = argparse.ArgumentParser(description="Warm the AI cache with every filmed demo take.")
    p.add_argument("--base-url", default="http://127.0.0.1:8000")
    p.add_argument("--shop", default="SASEBO01")
    p.add_argument("--district", default="MINATO26")
    p.add_argument("--foreman", default="山口 隆")
    p.add_argument("--foreman-pin", default="246810")
    p.add_argument("--worker", default="Rahim Uddin")
    p.add_argument("--worker-pin", default="2468")
    p.add_argument("--poster", default="自治会 事務局")
    p.add_argument("--poster-pin", default="482615")
    args = p.parse_args(argv)

    api = Api(args.base_url)
    foreman = api.login(args.shop, "foreman", args.foreman_pin, args.foreman)
    worker = api.login(args.shop, "worker", args.worker_pin, args.worker)
    poster = api.login(args.district, "poster", args.poster_pin, args.poster)
    machines: list[Json] = api.get("/api/machines", foreman)["items"]
    plasma = next(m for m in machines if m["code"] == "plasma-1")
    thread = api.get("/api/threads", foreman)[0]

    takes = [
        ("note", f"/api/machines/{plasma['id']}/notes", foreman, {"text": NOTE}),
        ("chat", f"/api/threads/{thread['id']}/messages", foreman, {"text": CHAT_MESSAGE}),
        ("briefing", "/api/briefings/draft", foreman, {"text": BRIEFING}),
        ("notice", "/api/notices", poster, {"sourceKind": "text", "text": FLYER}),
        ("ask", "/api/asks", worker, {"text": ASK}),
    ]
    safe = True
    for take, path, who, form in takes:
        first, t1 = api.post_form(path, who, form)
        again, t2 = api.post_form(path, who, form)
        verdict = _verdict(take, again)
        safe = safe and not verdict.startswith(("FALLBACK", "NOT"))
        sys.stdout.write(f"{take:9} first {t1:5.1f} s · again {t2:5.1f} s · {_verdict(take, first)} → {verdict}\n")
    sys.stdout.write("All takes are warm.\n" if safe else "Some takes are not safe to film yet: fix them and rerun.\n")
    sys.exit(0 if safe else 1)


if __name__ == "__main__":
    main()
