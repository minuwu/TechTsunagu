"""PIN hashing and session tokens (architecture §12, ADR 0010).

PINs are short, so the server pepper does the real work: a leaked table without PIN_PEPPER cannot be brute-forced
offline. Tokens are 256-bit random values; only their SHA-256 is stored."""

import hashlib
import hmac
import re
import secrets

from argon2 import PasswordHasher
from argon2.exceptions import InvalidHashError, VerificationError, VerifyMismatchError

from app.domain.pins import PIN_LENGTH, pin_problem

_hasher = PasswordHasher()


def _peppered(pepper: str, pin: str) -> str:
    return hmac.new(pepper.encode(), pin.encode(), hashlib.sha256).hexdigest()


def hash_pin(pepper: str, pin: str) -> str:
    return _hasher.hash(_peppered(pepper, pin))


def verify_pin(pepper: str, pin_hash: str, pin: str) -> bool:
    try:
        return _hasher.verify(pin_hash, _peppered(pepper, pin))
    except (VerifyMismatchError, VerificationError, InvalidHashError):
        return False


def new_pin(role: str) -> str:
    while True:
        pin = "".join(secrets.choice("0123456789") for _ in range(PIN_LENGTH[role]))
        if pin_problem(role, pin) is None:
            return pin


def new_token() -> tuple[str, bytes]:
    token = secrets.token_urlsafe(32)
    return token, token_digest(token)


QR_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"  # no 0/O, 1/I/L: read aloud and typed without mix-ups
_SHORT_QR = re.compile(rf"^WK([{QR_ALPHABET}]{{6}})$")


def new_qr_code() -> tuple[str, bytes]:
    """My QR code, WK-7KQ4M2: 31^6 (about 887 million) values; resolving is rate-limited, so it cannot be guessed."""
    code = "WK-" + "".join(secrets.choice(QR_ALPHABET) for _ in range(6))
    return code, token_digest(code)


def normalize_qr(token: str) -> str:
    """A typed `wk 7kq4m2` is `WK-7KQ4M2`; an older long token is kept as it is (it is case-sensitive)."""
    squeezed = re.sub(r"[\s-]", "", token).upper()
    m = _SHORT_QR.match(squeezed)
    return f"WK-{m.group(1)}" if m else token.strip()


def token_digest(token: str) -> bytes:
    return hashlib.sha256(token.encode()).digest()
