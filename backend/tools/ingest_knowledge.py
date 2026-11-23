"""Build app/seed_data/knowledge.json: the public guides the Wakatta? chatbot may answer from (RAG_PLAN.md).

    cd backend; uv run --with pypdf python tools/ingest_knowledge.py <folder with the PDFs>

One-off tool, not part of the app (pypdf is not an app dependency). Both sources are under the Public Data License
1.0: reuse and editing are allowed with the source named and the editing stated, which `attribution` carries. Text
only: no logos, images or character designs are copied."""

import json
import re
import sys
from pathlib import Path

from pypdf import PdfReader

SOURCES = [
    {
        "id": "mhlw-safety",
        "file": "mhlw-safety-en.pdf",
        "kind": "safety",
        "title": "For Work Safety and Health: To those working in the manufacturing industry",
        "publisher": "Ministry of Health, Labour and Welfare (MHLW)",
        "url": "https://www.mhlw.go.jp/file/06-Seisakujouhou-11300000-Roudoukijunkyokuanzeneiseibu/0000148471.pdf",
        "attribution": "出典：厚生労働省「未熟練労働者に対する安全衛生教育マニュアル（製造業・英語）」"
        "(https://www.mhlw.go.jp/stf/seisakunitsuite/bunya/0000118557.html) をもとに Wakatta? が抜粋・要約",
        "skip_pages": {1, 2},  # cover, blank
    },
    {
        "id": "isa-guide",
        "file": "isa-guide-en.pdf",
        "kind": "life",
        "title": "Guidebook on Living and Working (7th edition)",
        "publisher": "Immigration Services Agency of Japan (ISA)",
        "url": "https://www.moj.go.jp/isa/content/001450885.pdf",
        "attribution": "出典：出入国在留管理庁「生活・就労ガイドブック（英語版）」"
        "(https://www.moj.go.jp/isa/support/portal/guidebook_all.html) をもとに Wakatta? が抜粋・要約",
        "skip_pages": set(range(1, 12)),  # cover, foreword, table of contents
    },
]
MAX_CHARS = 1200  # one retrieved piece; a long page becomes two or three
_NAV = re.compile(r"(Chapter \d+){2,}")  # the side tabs repeated on every page of the guide


def clean(text: str) -> str:
    text = _NAV.sub(" ", text)
    return " ".join(text.split())


def split(text: str) -> list[str]:
    if len(text) <= MAX_CHARS:
        return [text]
    parts, current = [], ""
    for sentence in re.split(r"(?<=[.!?。])\s+", text):
        if current and len(current) + len(sentence) > MAX_CHARS:
            parts.append(current)
            current = ""
        current = f"{current} {sentence}".strip()
    return [*parts, current] if current else parts


def main(folder: Path) -> None:
    sources, chunks = [], []
    for s in SOURCES:
        reader = PdfReader(folder / s["file"])
        for number, page in enumerate(reader.pages, start=1):
            if number in s["skip_pages"]:
                continue
            text = clean(page.extract_text() or "")
            if len(text) < 80 or text.count("....") > 3:  # blank or a table-of-contents page
                continue
            for n, part in enumerate(split(text), start=1):
                chunks.append({"id": f"{s['id']}:p{number}:{n}", "source": s["id"], "page": number, "text": part})
        sources.append({k: v for k, v in s.items() if k not in ("file", "skip_pages")})
    out = Path(__file__).resolve().parents[1] / "app" / "seed_data" / "knowledge.json"
    data = {"_comment": "Built by tools/ingest_knowledge.py; edit the tool, not this file.", "sources": sources}
    out.write_bytes((json.dumps(data | {"chunks": chunks}, ensure_ascii=False, indent=0) + "\n").encode("utf-8"))
    by_source = {s["id"]: sum(c["source"] == s["id"] for c in chunks) for s in sources}
    sys.stdout.write(f"wrote {out}: {len(chunks)} chunks {by_source}\n")


if __name__ == "__main__":
    main(Path(sys.argv[1]))
