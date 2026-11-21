# Claude Code Backend Setup — Skills & Plugins

Checked against each repo on 21 Nov 2026.

**How to use this file:** put it in any folder, open Claude Code in VS Code, and say:
> Read `claude-code-backend-setup.md` and run **Part A** step by step. Stop and tell me if a step fails.

---

## PART A — Setup instructions (for Claude Code to run)

> Claude: run these with Bash, using the `claude plugin ...` CLI. You can't type `/plugin` slash commands yourself. Detect the OS. On Windows, use PowerShell equivalents for `mkdir`/`cp`. Skip any step whose tool is already installed. Do **not** install anything twice through two different methods. That causes duplicate skills and hooks.

### A0. Check prerequisites
```bash
claude --version      # need 2.1+
node --version        # need 20.19+ or 22.12+ (claude-mem needs this)
git --version
python --version      # need 3.10+
uv --version || pip install uv
```

### A1. Official Anthropic marketplace (core quality tools)
```bash
claude plugin marketplace add anthropics/claude-plugins-official
claude plugin install claude-code-setup@claude-plugins-official
claude plugin install mattpocock-skills@claude-plugins-official
claude plugin install feature-dev@claude-plugins-official
claude plugin install code-review@claude-plugins-official
claude plugin install pr-review-toolkit@claude-plugins-official
claude plugin install code-simplifier@claude-plugins-official
claude plugin install commit-commands@claude-plugins-official
claude plugin install security-guidance@claude-plugins-official
claude plugin install claude-security@claude-plugins-official
claude plugin install hookify@claude-plugins-official
claude plugin install claude-md-management@claude-plugins-official
claude plugin install context7@claude-plugins-official
claude plugin install github@claude-plugins-official
```
**Language server:** install the plugin for the user's backend language and its binary.
- Python: `npm i -g pyright` + `claude plugin install pyright-lsp@claude-plugins-official`
- Node/TS: `npm i -g typescript-language-server typescript` + `claude plugin install typescript-lsp@claude-plugins-official`
- Go: `gopls-lsp`; Java: `jdtls-lsp`

**GitHub plugin:** ask the user to set `GITHUB_PERSONAL_ACCESS_TOKEN` as a user environment variable. Never ask them to paste the token into chat.

### A2. Community plugins
```bash
# Ponytail: always-on "write only what's needed" + strong reviews
claude plugin marketplace add DietrichGebert/ponytail
claude plugin install ponytail@ponytail

# Karpathy guidelines: think first, simple, surgical changes
claude plugin marketplace add multica-ai/andrej-karpathy-skills
claude plugin install andrej-karpathy-skills@karpathy-skills

# Supabase + Postgres best practices
claude plugin marketplace add supabase/agent-skills
claude plugin install postgres-best-practices@supabase-agent-skills
claude plugin install supabase@supabase-agent-skills

# pstack (poteto-mode): rigorous bug/perf/refactor playbooks
claude plugin marketplace add cursor/plugins
claude plugin install pstack@cursor-plugins

# Everything Claude Code (ECC): agents, build-fixers, backend skills
claude plugin marketplace add https://github.com/affaan-m/ECC
claude plugin install ecc@ecc

# claude-mem: memory across sessions
claude plugin marketplace add thedotmack/claude-mem
claude plugin install claude-mem@thedotmack
npx claude-mem telemetry disable

# task-observer: improves your skills over time
claude plugin marketplace add rebelytics/one-skill-to-rule-them-all
claude plugin install task-observer@one-skill-to-rule-them-all
```

### A3. Manual installs (not available as Claude Code plugins)
```bash
# Thermo-nuclear code quality review (single SKILL.md from Cursor's team kit)
mkdir -p ~/.claude/skills/thermo-nuclear-code-quality-review
curl -fsSL https://raw.githubusercontent.com/cursor/plugins/main/cursor-team-kit/skills/thermo-nuclear-code-quality-review/SKILL.md \
  -o ~/.claude/skills/thermo-nuclear-code-quality-review/SKILL.md

# ECC rules (plugins can't ship rules): common + ONE language pack only
git clone --depth 1 https://github.com/affaan-m/ECC.git /tmp/ECC
mkdir -p ~/.claude/rules/ecc
cp -R /tmp/ECC/rules/common ~/.claude/rules/ecc/
cp -R /tmp/ECC/rules/python ~/.claude/rules/ecc/    # or typescript / golang / java

# graphify: knowledge graph of your codebase
uv tool install graphifyy
graphify install

# Playwright: Microsoft recommends the CLI+skills over MCP for coding agents (fewer tokens)
npm install -g @playwright/cli@latest
```

### A4. Install the global engineering manual
Copy `CLAUDE.md` (which sits next to this file) to `~/.claude/CLAUDE.md` (Windows: `C:\Users\<you>\.claude\CLAUDE.md`). Claude Code loads it automatically in **every** session and project. If a `~/.claude/CLAUDE.md` already exists, show the user the diff and ask before replacing or merging it.

### A5. Verify
Ask the user to **restart Claude Code**, then run `/plugin`. Every plugin above should show as enabled. Then run `/context` to check how much context the skills use.

---

## PART B — Run once per project
| Command | Why |
|---|---|
| `/init` | Creates `CLAUDE.md`, the project memory |
| "recommend automations for this project" | `claude-code-setup` suggests hooks and MCP servers that fit your stack |
| `/setup-matt-pocock-skills` | **Required** before Matt's engineering skills will work |
| `/pstack:setup-pstack` | Pick **Opus/Sonnet only**. Its default panel uses Gemini, which Claude Code doesn't have |
| `/graphify .` then `graphify hook install` | Builds the code graph and keeps it updated on every commit |
| `playwright-cli install --skills` | Only in projects with web UI/E2E tests |

---

## PART C — Which to use, when

`★` = most important  ·  `⚡` = runs multiple agents, so it uses a lot of your **Pro** limit. Save these for big changes.

### 1. Plan (before writing code)
- ★ `/grill-me`: Claude asks you questions until the design is clear. Use it for every non-trivial feature.
- `/to-spec` → `/to-tickets`: turns the conversation into a spec, then into small tickets.
- `/ecc:plan`: quick plan that waits for your OK before touching code.
- `/prototype`: throwaway code to settle a design question.

### 2. Build
- ★ **Ponytail** is always on. Set strength with `/ponytail full` (default). Use `ultra` for tight code, `off` to disable.
- ★ `/tdd`: red → green → refactor. Use it for any logic (services, validators, DB queries).
- `/implement`: builds from a spec or tickets with TDD, then reviews.
- ⚡ `/feature-dev`: bigger features that need codebase exploration and architecture.
- Ask about any library and add "use context7" so Claude reads up-to-date docs instead of guessing APIs.
- Postgres/SQL/RLS work: the postgres-best-practices skill loads automatically. You can also say "check against Postgres best practices".

### 3. Debug
- ★ `/diagnosing-bugs`: phased bug hunt that ends with a regression test.
- `/pstack:poteto-mode <bug>`: for hard bugs, perf problems, and refactors. Its rule is "reproduce first, then fix with evidence".
- `/ecc:build-fix`: build or type errors.
- `graphify query "how does auth reach the DB?"` (terminal), or just ask Claude. It checks the graph first. Use it to understand unfamiliar code fast.

### 4. Review (before every commit / PR)
| Situation | Use |
|---|---|
| ★ Every change (fast) | `/ponytail-review` (catches bugs, missing tests, slow paths, extra code) |
| Before opening a PR | ⚡ `/pr-review-toolkit:review-pr` (tests, error handling, types, comments) |
| Big/messy change | `/thermo-nuclear-code-quality-review` (very strict, structure-focused) |
| Clean up after it works | `code-simplifier` agent: "simplify the code I just changed" |
| Security (auth, payments, uploads, SQL) | ⚡ `/claude-security scan my branch at medium` |
| Whole repo health | `/ponytail-audit` (ranked: fix this first) |

`security-guidance` runs on its own. It warns about injection, XSS, SSRF, and secrets while Claude edits.

### 5. Ship
- ★ `/commit-commands:commit`, or `/commit-commands:commit-push-pr` for everything at once.
- ⚡ `/code-review --comment`: reviews the PR on GitHub and posts the findings.
- `/ecc:test-coverage`: finds untested code and writes the missing tests.

### 6. After the session
- `/claude-md-management:revise-claude-md`: saves what Claude learned into `CLAUDE.md`.
- `/retro`: suggests improvements to your setup.
- `/handoff`: when context is nearly full, saves a summary you can paste into a new session.
- `/hookify "never edit .env files"`: turns a repeated mistake into a permanent rule.
- **claude-mem** works on its own from your 2nd session. Wrap secrets in `<private>…</private>`.

### Name clashes
`code-review`, `pr`, and `plan` exist in several plugins. If the wrong one runs, type the full name: `/code-review:code-review`, `/ecc:code-review`, `/mattpocock-skills:code-review`.

---

## PART D — Extras worth adding later (official marketplace)
Install with `claude plugin install <name>@claude-plugins-official`, only when you actually use the tool:
- `semgrep`: real-time security scanning (static analysis)
- `deepeval`: evals and tracing for **AI/agent apps** (fits your agentic-AI goal)
- `sentry`: debug production errors from Claude
- `terraform`: infrastructure as code
- `redis-development`, `prisma`, `neon`, `mongodb`: when your stack uses them
- `codspeed`: performance benchmarks
- `chrome-devtools-mcp`: network/perf debugging when your API serves a frontend

---

## PART E — Honest warnings (read once)
1. **Too many always-on tools = worse output.** Ponytail, Karpathy, and ECC rules all inject instructions. If Claude feels slow or confused, run `/context`. Then disable **ECC** first in `/plugin`, because it has 300+ skills. Re-enable it when you need its build-fixers or backend skills.
2. **Pro limits:** `⚡` commands, `/implement-spec`, pstack `swarm`/`arena`, and the claude-mem observer all use your quota fast. Use them on important changes only.
3. **claude-mem** may offer a paid 30-day trial during setup. Skip it. It works on your Anthropic plan.
4. **task-observer** only runs after you add its session-start hook and CLAUDE.md block (see its `references/environments.md`). It's worth activating once you write your own skills.
5. **Skipped on purpose:**
   - **headroom**: it's a proxy in front of Claude, and its own docs flag proxies as a risk for subscription (Pro) accounts.
   - **Playwright MCP**: the CLI does the same job with fewer tokens. Install only one of the two.
   - **superpowers**: overlaps with Matt Pocock + ECC.
6. Only install plugins you trust. They can run hooks and commands on your machine.
