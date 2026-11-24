"""The client IP behind the rate limits (login, roster, register, find) cannot be chosen by the caller."""

from starlette.requests import Request

from app.api.auth import client_ip


def _request(forwarded: str | None, peer: str = "10.0.0.5") -> Request:
    headers = [(b"x-forwarded-for", forwarded.encode())] if forwarded else []
    return Request({"type": "http", "headers": headers, "client": (peer, 1234)})


def test_the_last_forwarded_hop_wins_because_the_caller_writes_the_first() -> None:
    """Azure's ingress appends the real address last; whatever the caller sent stays in front of it."""
    assert client_ip(_request("6.6.6.6, 203.0.113.9")) == "203.0.113.9"
    assert client_ip(_request("1.1.1.1, 2.2.2.2, 203.0.113.9")) == "203.0.113.9"


def test_without_a_proxy_the_peer_address_is_used() -> None:
    assert client_ip(_request(None)) == "10.0.0.5"
