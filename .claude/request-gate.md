TechTsunagu request gate (local to this repo). Apply to this prompt in order:

1. graphify first. If graphify-out/graph.json exists, run `graphify query "<question>"` (or `graphify path` / `graphify explain`) from the repo root before any Grep, Glob or source Read. Read raw files only for lines the graph located. After code changes run `graphify update .`; after doc changes run `/graphify . --update`. No graph yet: build it with `/graphify .` before exploring.
2. ponytail. If the ponytail skill is not active in this session yet, invoke it (Skill "ponytail", level full) before any coding, doc or config work.
3. Grill with docs. Before starting any request that changes code, docs or config, invoke Skill "mattpocock-skills:grilling" and Skill "mattpocock-skills:domain-modeling" (together they are grill-with-docs). Ask through AskUserQuestion, recommended option first. Record resolved terms in GLOSSARY.md and hard-to-reverse decisions in docs/adr/ as the domain-modeling skill says.
   Skip step 3 when: the prompt is a pure question that changes nothing, or it answers a grilling round already in progress, or the plan for this exact task was already settled by grilling in this session.
