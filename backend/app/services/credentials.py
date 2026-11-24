"""PIN hashing and session tokens (architecture §12, ADR 0010).

PINs are short, so the server pepper does the real work: a leaked table without PIN_PEPPER cannot be brute-forced
offline. Tokens are 256-bit random values; only their SHA-256 is stored."""

import hashlib
import hmac
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


def token_digest(token: str) -> bytes:
    return hashlib.sha256(token.encode()).digest()
