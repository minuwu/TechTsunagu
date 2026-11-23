"""Which models answer, in which order, and how many calls each may make (AI_PLAN.md §2–§4, XREQ-003).

Changing a model is one line here. Budgets stay just under each free limit; on the paid tier they become a cost cap."""

from typing import Literal

Chain = Literal["dev", "demo"]
Tier = Literal["free", "paid"]

GEMINI_CHAINS: dict[Chain, tuple[str, ...]] = {
    # prompt work, the eval, anyone testing real AI: the 500-a-day models only
    "dev": ("gemini-3.5-flash-lite", "gemini-3.1-flash-lite"),
    # filmed takes and the live demo. 3.5 Flash-Lite first: 30/30 in the eval at 1–3 s (docs/ai-eval.md), while 3.8
    # Flash answered 503 "high demand" all of 2026-11-23; a busy model costs seconds before the chain moves on.
    "demo": ("gemini-3.5-flash-lite", "gemini-3.8-flash", "gemini-3.6-flash", "gemini-3.1-flash-lite"),
}

# Last in every chain when GROQ_API_KEY is set: Whisper turns audio into text, then this model writes the draft.
# Chosen by the 2026-11-23 benchmark (5/6 drafts passed our checks, ignored the planted instruction).
GROQ_MODEL = "openai/gpt-oss-120b"

# model -> (calls per day, calls per minute)
BUDGETS: dict[Tier, dict[str, tuple[int, int]]] = {
    "free": {
        "gemini-3.8-flash": (18, 4),  # free limit 20/day, 5/min
        "gemini-3.6-flash": (18, 4),
        "gemini-3.5-flash-lite": (450, 12),  # free limit 500/day, 15/min
        "gemini-3.1-flash-lite": (450, 12),
        GROQ_MODEL: (40, 1),  # free limit is 200K tokens/day, 8K/min; one draft uses about 5K
    },
    "paid": {
        # about $0.004 a call on 3.8 Flash: 1,500 calls is $6 a day at most
        "gemini-3.8-flash": (1500, 60),
        "gemini-3.6-flash": (1500, 60),
        "gemini-3.5-flash-lite": (3000, 120),
        "gemini-3.1-flash-lite": (3000, 120),
        GROQ_MODEL: (3000, 60),
    },
}
