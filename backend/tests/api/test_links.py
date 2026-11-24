"""XREQ-026: Android App Links on the API host: assetlinks.json and the "open in the app" fallback page."""

from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session, sessionmaker

from app.ai.fake import FakeAiClient
from app.config import Settings
from app.main import create_app

RELEASE = ":".join(["AB"] * 32)
DEBUG = ":".join(["01"] * 32)
APK = "https://example.blob.core.windows.net/apk/wakatta-1.0.0.apk"


@pytest.fixture
def configured(settings: Settings, db: Session, fake_ai: FakeAiClient) -> Iterator[TestClient]:
    factory: sessionmaker[Session] = db.info["factory"]
    s = settings.model_copy(update={"android_cert_sha256": [RELEASE, DEBUG], "app_download_url": APK})
    with TestClient(create_app(s, session_factory=factory, ai_client=fake_ai)) as c:
        yield c


def test_assetlinks_lists_the_app_and_every_fingerprint(configured: TestClient) -> None:
    res = configured.get("/.well-known/assetlinks.json", follow_redirects=False)

    assert res.status_code == 200 and res.headers["content-type"].startswith("application/json")
    assert res.json() == [
        {
            "relation": ["delegate_permission/common.handle_all_urls"],
            "target": {
                "namespace": "android_app",
                "package_name": "com.techtsunagu.wakatta",
                "sha256_cert_fingerprints": [RELEASE, DEBUG],
            },
        }
    ]


def test_assetlinks_is_empty_until_a_fingerprint_is_set(client: TestClient) -> None:
    assert client.get("/.well-known/assetlinks.json").json() == []


@pytest.mark.parametrize("path", ["/m/12?s=SASEBO01", "/c/some-token", "/t/7?m=3", "/n/2", "/b/4", "/nm/9", "/i/due"])
def test_every_app_link_has_a_fallback_page(configured: TestClient, path: str) -> None:
    res = configured.get(path, follow_redirects=False)

    assert res.status_code == 200 and res.headers["content-type"].startswith("text/html")
    assert APK in res.text and "Wakatta?" in res.text
    assert "some-token" not in res.text and "SASEBO01" not in res.text  # the link itself is never echoed


def test_without_a_release_the_page_has_no_download_link(client: TestClient) -> None:
    res = client.get("/c/x")
    assert res.status_code == 200 and "href=" not in res.text


def test_api_paths_are_untouched(client: TestClient) -> None:
    assert client.get("/api/no-such-route").status_code == 404


def test_a_bad_fingerprint_fails_at_startup(settings: Settings) -> None:
    with pytest.raises(ValueError):
        Settings.model_validate({**settings.model_dump(), "android_cert_sha256": "not-a-fingerprint"})
