# Board brief: Wakatta? (わかった？)

You cannot see the conversation this came from. Everything you need is here.

## The idea

- What it is: a web app that turns a foreman's 20-second Japanese voice note into a plain-Japanese (やさしい日本語), Bengali and English instruction pinned to one machine, and keeps that machine's tablet panel LOCKED for a foreign worker until they pass a short quiz on it; wrong answers alert the foreman with the exact shop word (玉掛け "slinging", 段取り "setup"…) that was misunderstood. The same app turns neighbourhood notices (garbage rules, disaster drills, festivals) into plain steps with calendar dates, answers workers' questions only from those notices (never inventing a rule), and pairs each worker with a Japanese "Senpai" (a coworker or neighbourhood-association volunteer).
- Who it is for: small and mid-sized manufacturers in Nagasaki / Kyushu, Japan, that employ foreign workers on the 特定技能 (Specified Skilled Worker) or 技能実習 (Technical Intern) visas, often from Bangladesh, Vietnam or Indonesia. The factory owner or plant manager decides to adopt it; foremen (班長) and workers use it daily.
- What it sells, at what price: free for employers and workers. Paid by 登録支援機関 (registered support organisations): for 特定技能1号 workers, the employer or its registered support organisation must by law provide life orientation, consultation and support for exchange with Japanese people. The app's "Support Log" export is evidence of that support. The support organisation's price is not set yet.
- Where and how: Japan, starting in Nagasaki Prefecture. A phone/tablet web app: a QR sticker on each machine opens it, no install. Distribution is planned through support organisations and the prefecture's foreign-talent programmes.
- Budget and constraints: a student hackathon project (BJIT "TSUNAGU" hackathon; build window ends 2026-11-24, final presentation 2026-11-26). Judges: Socio Future Inc., Daishin Giken Co., and Nagasaki Prefecture. Team of 5. Hosted on Azure student credits; the AI (Google Gemini) runs on a free tier of about 20 calls a day on the better model. No customers, no pilots and no revenue yet.

## How a day looks

1. A foreman records a voice note at a machine ("how to change the blade"). The app drafts the instruction in three languages with a back-translation into Japanese (so the foreman can check the Bengali without reading Bengali), safety icons and 2–3 quiz questions. The foreman approves it; every older sign-off for that machine becomes invalid.
2. A worker scans the machine's QR code with their own phone (signed in with their own account and PIN). The panel beside the machine shows LOCK. The worker reads the instruction in their language and answers the quiz. All correct → a sign-off valid for 90 days, and the panel turns READY. Any wrong answer → the foreman is alerted with the exact term.
3. The foreman's board shows every machine's lock state, who is assigned today without a valid sign-off, a worker × machine skill matrix and a heatmap of confusing terms.
4. At home, the same worker gets the neighbourhood's garbage rules as plain steps with "add to calendar", can ask questions answered only from posted notices, and can forward an unanswered question to their Senpai.

## Evidence the team has

- Japan's labour ministry (MHLW) 2025 survey of employers of foreign workers: 46.2% name "communication is hard because of Japanese ability" as a challenge, the most-cited one (up from 43.9%). https://www.mhlw.go.jp/stf/newpage_75722.html
- Injury rates per 1,000 workers, as quoted in the team's plan: 3.98 (技能実習), 3.91 (特定技能), 2.3 (all workers). The final table is still to be verified.
- Existing tools already translate manuals and run quizzes: tebiki, Teachme Biz, カミナシ. The team's claimed difference: the quiz gates one specific machine through its panel (a software lock, not a hardware interlock), and every wrong answer reaches the foreman with the exact term.

## Not known yet

Market size, the number of target factories in Nagasaki, what support organisations pay today for the same duties, what they would pay for this, and whether any factory would let a software panel stand in front of a machine.
