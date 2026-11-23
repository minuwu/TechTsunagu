"""Operator commands (architecture §3.4), run inside the API container, which has DATABASE_URL and PIN_PEPPER:

    python -m app.cli reset-pin --code SASEBO01 --role foreman --name "山口 隆"

The last resort when nobody in the shop can reset the PIN. The new first PIN is made here and printed once, so it is
never in the shell history; its owner must choose their own at the next sign-in."""

import argparse
from collections.abc import Sequence
from datetime import UTC, datetime

from app.api.errors import ApiError
from app.config import Settings
from app.db import make_engine, make_session_factory
from app.domain.pins import PIN_LENGTH
from app.services import users


def main(argv: Sequence[str] | None = None) -> None:
    parser = argparse.ArgumentParser(prog="app.cli", description="Wakatta? operator commands")
    reset = parser.add_subparsers(dest="command", required=True).add_parser(
        "reset-pin", help="set a new first PIN: clears a lockout and signs the person out everywhere"
    )
    reset.add_argument("--code", required=True, help="the shop code, or the district code for a Poster")
    reset.add_argument("--role", required=True, choices=sorted(PIN_LENGTH))
    reset.add_argument("--name", required=True, help="the display name shown on the sign-in roster")
    args = parser.parse_args(argv)

    settings = Settings()  # pyright: ignore[reportCallIssue]  # values come from the environment
    engine = make_engine(settings.database_url)
    try:
        with make_session_factory(engine)() as db:
            pepper = settings.pin_pepper.get_secret_value()
            pin = users.reset_pin(db, args.code, args.role, args.name, pepper, datetime.now(UTC))
    except ApiError as exc:
        raise SystemExit(exc.message) from exc
    finally:
        engine.dispose()
    print(f"New first PIN for {args.name}: {pin}")  # noqa: T201


if __name__ == "__main__":
    main()
