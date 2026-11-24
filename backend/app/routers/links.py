"""Android App Links on the API host (XREQ-026, MOBILE_PLATFORM_CONTRACT §2): the Digital Asset Links file, and an
"open in the app" page for phones without the app. Outside /api and the contract; no database."""

from html import escape

from fastapi import APIRouter
from fastapi.responses import HTMLResponse, JSONResponse

from app.api.auth import AppSettings

router = APIRouter(include_in_schema=False)
PACKAGE = "com.techtsunagu.wakatta"
# One page for every link shape; the link itself is never echoed (no reflected input, nothing to escape).
PAGE = """<!doctype html>
<html lang="ja"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="robots" content="noindex"><title>Wakatta?</title>
<style>body{{font-family:system-ui,sans-serif;max-width:32rem;margin:3rem auto;padding:0 1rem;line-height:1.6}}</style>
</head><body><h1>Wakatta?</h1>
<p>このリンクは Wakatta? アプリで開きます。アプリを入れてから、もう一度読み取るかタップしてください。</p>
<p lang="en">This link opens in the Wakatta? app. Install the app, then scan or tap the link again.</p>
{download}</body></html>
"""


@router.get("/.well-known/assetlinks.json")
def asset_links(settings: AppSettings) -> JSONResponse:
    """Empty until ANDROID_CERT_SHA256 is set: Android then simply does not verify the links."""
    fingerprints = settings.android_cert_sha256
    statements = [
        {
            "relation": ["delegate_permission/common.handle_all_urls"],
            "target": {"namespace": "android_app", "package_name": PACKAGE, "sha256_cert_fingerprints": fingerprints},
        }
    ]
    return JSONResponse(statements if fingerprints else [])


@router.get("/m/{rest:path}")
@router.get("/c/{rest:path}")
@router.get("/t/{rest:path}")
@router.get("/n/{rest:path}")
@router.get("/b/{rest:path}")
@router.get("/nm/{rest:path}")
@router.get("/i/{rest:path}")
def open_in_app(rest: str, settings: AppSettings) -> HTMLResponse:  # noqa: ARG001  # the path is never used
    url = settings.app_download_url
    download = f'<p><a href="{escape(url)}">Download the app / アプリをダウンロード</a></p>' if url else ""
    return HTMLResponse(PAGE.format(download=download))
