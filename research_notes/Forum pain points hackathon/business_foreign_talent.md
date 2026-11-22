# Business pain points: hiring, onboarding and retaining foreign workers (IT engineers, 特定技能/技能実習) in Japan, with a regional and Bangladesh focus

Researched 2026-11-22 for the TSUNAGU hackathon (build Nov 22-24 2026). Read-only desk research.

## Method and source access (read this first)

- Forum access was poor. Reddit (www.reddit.com) refused automated fetches. Two Redlib mirror links that surfaced in search failed (connection refused, 404). `site:` searches for r/japanlife, r/cscareerquestionsjapan and Yahoo!知恵袋 returned no forum threads, only blogs and guides. Japan Today's "have your say" thread was blocked by Akamai. Several Nagasaki prefecture and university PDFs were 404 or password-protected.
- As a result, the evidence spine is official and survey data, not forum posts. Forum-style evidence is limited to anecdotal blog and article accounts (dev.to, Cybozu, freee note, TokyoDev). Treat "recurring in forums" as weakly verified for every item.
- Evidence tags used below:
  - **[fetched]**: I read the page itself.
  - **[snippet]**: the figure comes from a search-engine summary of the page; I did not read the page. Verify before using in the proposal.
  - **[anecdote]**: single first-person account or opinion piece.
- Time window: 2023-2026 preferred. Older items are flagged with their date.

## The spine: MHLW 2025 外国人雇用実態調査 (national, both sides) [fetched]

Source: JIL summary of the MHLW survey, https://www.jil.go.jp/kokunai/blt/backnumber/2026/11/kokunai_04.html (survey as of 2025-09-30, fieldwork Oct-Nov 2025, results announced 2026-08-31).

Employer-side challenges (share of establishments):
- Communication difficult because of Japanese ability: **46.2%** (up 2.3 pts, the top item)
- Paperwork for residence-status applications: 24.8%
- Trouble from cultural, value and lifestyle differences: 22.4% (up from 20.9%)
- Cost of improving living conditions (housing etc.): 21.9% (up from 19.9%)

Worker side:
- 10.0% reported trouble at their current job; 88.5% reported none.
- Among that 10% only: agency/sending-organisation fees too high 22.2% (about 2.2% of all workers); did not know where to seek advice 15.8% (about 1.6% of all); more Japanese required than explained beforehand 11.9% (about 1.2% of all). The base is small, so do not quote these as "22% of workers".
- 81.2% want to keep working at their current employer; 6.1% want to change employer in Japan. This is all statuses combined.
- Status mix: 専門的・技術的分野 858,085 workers (42.1%), of which 技術・人文知識・国際業務 377,886 and 特定技能 365,042. Average monthly pay: 専門的・技術的 ¥306,500; 特定技能 ¥254,100.

Context numbers:
- Foreign workers 2,571,037 and employing establishments 371,215 at end-October 2025, both record highs (MHLW 外国人雇用状況, https://www.mhlw.go.jp/stf/newpage_68794.html) [snippet].
- 特定技能1号 holders with at least one employer change: 22.4% by end-August 2025 (ISA material, https://www.moj.go.jp/isa/content/001450286.pdf) [snippet]. This shows job-hopping is real for 特定技能 even though the all-status "want to stay" figure is 81.2%.

## Nagasaki, Miyazaki and the Bangladesh pipeline (judge-relevant)

- **Nagasaki model** [snippet, from a search summary of prefecture PDFs; verify]:
  - 6 job offers at 5 companies in the prefecture as of a 2026-05-19 prefecture document. Breakdown: IT company in Nagasaki City 1; survey/design consultancy 1; civil works in Isahaya 1; manufacturer in Sasebo 2; civil/pipe works in Kawatana 1. So 4 or 5 of 6 are at non-IT firms.
  - Candidates come through B-JET (Japanese and business manners in Dhaka), then company interviews. About 2 months of online Japanese after joining, free to the company.
  - Recruitment fee about **¥1.28M per Bangladeshi IT hire** (2024-fiscal prefecture document).
  - Nagasaki University started a programme on 2026-06-01 for 3 Bangladeshi hires with offers from prefecture companies (Japanese, intercultural training, and a September internship at the offering company to build the relationship before work starts).
  - The prefecture's company tour to Bangladesh runs **2026-11-24 to 11-26**, with BJIT named as planner/operator. That is the same week as the hackathon.
  - URLs (several 404 or locked when fetched): https://www.pref.nagasaki.lg.jp/shared/uploads/2025/09/1757637732.pdf ; https://www.pref.nagasaki.jp/shared/uploads/2025/07/1751619742.pdf ; https://www.pref.nagasaki.lg.jp/fs/1/7/6/2/0/_/2._______________________.pdf ; https://www.nagasaki-u.ac.jp/ja/guidance/kouhou/press/pdf/1388file1_20260529115715.pdf ; prefecture speaker deck at JP MIRAI (password-protected): https://jp-mirai.org/wp-content/uploads/講演02_長崎県未来人材課企画監_高見誠氏_compressed.pdf
- **Miyazaki precedent** (the model Nagasaki copies):
  - B-JET: 280 completed by 2020, 186 (nearly 70%) became engineers in Japan; 24 Miyazaki businesses employed 50+ Bangladeshi IT engineers 2017-2022 (JICA, 2023-02-22, https://www.jica.go.jp/english/information/topics/2022/20230222_11.html) [fetched]. JICA also notes some residents at first "mistook these engineers for unskilled labor".
  - Why some left Miyazaki (data-max, 2022-11-13, https://data-max.co.jp/article/50309) [fetched; older than window]: young people prefer cities; **family remittance pressure** pushes them toward higher-paying cities (named the biggest factor); **salary information is shared on Facebook**; companies "cannot legally prohibit" job changes; 54 hires in Miyazaki City, departure count not given.
- **Bangladesh side**:
  - Prothom Alo, 2025-10-03, https://en.prothomalo.com/business/qh7xo2wbs2 [fetched]: "without knowledge of the Japanese language, it is impossible to find work there"; BMET's 32 language centres lack skilled teachers; 27 licensed recruiting agencies, one licence cancelled for overcharging; Bangladesh "has not been able to show much success" since the 2019 agreement. About 26,000 Bangladeshis live in Japan versus 250,000 from Nepal.
  - Daily Star counts differ: about 35,000 working in Japan (2024) and 18,000 to 41,000 "working or studying" (2025). They measure different things; do not compare directly [snippet]. https://online-d11.thedailystar.net/opinion/views/open-dialogue/news/japan-has-opened-its-door-bangladesh-lets-not-ruin-it-4017886
  - JagoNews24 quotes an unnamed source: people hear it costs 4-5 lakh taka plus a language requirement and would rather pay more to skip the language step (https://www.jagonews24.com/en/biz-econ/news/83304) [snippet; page returned 403].
  - Daily Star: an IT firm fined Tk 10.5 lakh for irregular recruitment/training for Japan jobs (https://www.thedailystar.net/backpage/news/jobs-japan-it-firm-fined-tk-105-lakh-irregularities-recruitment-1820248) [snippet; date not confirmed].
  - BJIT: founded 2001, 600+ engineers in Bangladesh, Japan office with 30+ mostly Bangladeshi engineers; Marubeni alliance April 2023 (https://www.marubeni.com/en/brand_media/scope/bjit/) [snippet]. B-JET batch listings, 30 seats per batch, "final job placement is not guaranteed" (https://hotjobs1.bdjobs.com/b-jet--bangladesh-japan-ict-engineers--training-program-/bjet27.html) [snippet].
  - Japan's plan to take 100,000 Bangladeshi workers over five years; Bangladesh set up a "Japan Cell" (Dhaka Tribune, Pressenza 2025-07) [snippet].

## Ranked pain points (strongest evidence first)

### 1. Instructions in Japanese are not understood, and workers nod instead of asking
- **Problem:** Supervisors give high-context Japanese instructions; foreign staff pretend to understand; mistakes follow.
- **Evidence:**
  - MHLW 2025: 46.2% of employers, top challenge, rising [fetched].
  - TCJ/日本語総合研究所 survey 2026 (n=300 Japanese staff working with foreign workers, fieldwork Aug 2026): 75.4% find Japanese communication difficult; 44.3% struggle with technical terms and instructions; food/lodging 85.8%; 77.7% say ongoing Japanese support is needed. https://www.logi-today.com/991467 [fetched]
  - Kyoto prefecture survey: 30.2% of employers name Japanese communication as the top hardship (https://www.pref.kyoto.jp/rosei/gaikokujinnzai/documents/56-58.pdf) [snippet].
  - Yamaguchi Prefectural University 2025 paper: trainees struggle with vocabulary, fast speech and dialect; when lost they ask for an interpreter, **pretend to understand, or nod** (https://ypir.lib.yamaguchi-u.ac.jp/yp/1884/files/168433) [snippet].
  - JETRO: Takasago Electric runs 2-hour quiz-style "暗黙知" training so foreign staff learn to ask when intent is unclear (https://www.jetro.go.jp/biz/areareports/special/2024/0304/e5e85d8970142638.html) [snippet]. Consultants (Lifehacker, Indeed) blame vague words, katakana and "言わなくてもわかる" (https://www.lifehacker.jp/article/191124-how-to-communicate-with-foreign-subordinate/ ; https://jp.indeed.com/hire/c/info/ways-to-communicate-with-foreign-workers-to-prevent-misinterpretation) [snippet].
- **Workaround and why it fails:** Machine translation, やさしい日本語 guides (MHLW, ISA), training videos (METI). Translation converts words but never checks understanding; guides are one-off training, not part of each task.
- **Who suffers:** Site supervisors in manufacturing, logistics, care, construction; the worker who gets blamed.
- **App idea: "Wakatta? (わかった？) teach-back loop."** A supervisor sends a task in Japanese. The app rewrites it into やさしい日本語 plus the worker's language, then asks the worker to **teach it back**: pick the right photo/step order, or say it back in their own words. An LLM compares the teach-back with the original and flags the gap ("deadline missed", "which machine?") before work starts. Unclear company terms go into a shared team glossary that grows over time.
- **Why not a translation app:** It measures understanding, not wording. The output is a per-instruction "understood / gap" signal and a company jargon glossary.
- **TSUNAGU link:** people-to-people (supervisor and worker), data-to-data (instructions and team glossary).
- **Judge angle:** Nagasaki hires are mostly at non-IT site firms (civil, manufacturing); Daishin Giken as a technical firm would recognise the site-instruction problem (Daishin Giken's business not verified).
- **Originality 6/10. Feasibility 8/10** (one form, one LLM call, one dashboard).
- **Honest demo data:** 10 invented but realistic site instructions (safety check, deadline, reporting) written by the team, labelled as synthetic.

### 2. Costly hires walk away to cities (regional retention, 特定技能 job-hopping)
- **Problem:** Regional firms pay heavily to hire, then lose the person to Tokyo/Osaka or to a higher bidder.
- **Evidence:**
  - 22.4% of 特定技能1号 have changed employer (ISA, end-Aug 2025) [snippet].
  - Miyazaki departures driven by family remittance pressure and Facebook salary sharing [fetched, 2022].
  - JFC research (2025-11) flags concern that 育成就労 transfer rights (from 2027-04) will drain regional areas toward higher-wage cities (https://www.jfc.go.jp/n/findings/pdf/ronbun2511_01.pdf) [snippet; PDF locked]. RIETI column makes the same point (https://www.rieti.go.jp/jp/columns/a01_0758.html) [snippet].
  - Nagasaki fee about ¥1.28M per hire [snippet]. Treegp article: employers must be "chosen" rather than lock people in (https://treeglobalpartners.com/articles/tokutei-ginou-tenshoku-jiyuu-ronten/) [snippet].
  - Counterweight: 81.2% of all foreign workers want to stay (MHLW 2025) [fetched]; JETRO says "foreign staff do not settle" is not always true for highly skilled hires (https://www.jetro.go.jp/biz/areareports/special/2024/0304/47b45d8a63947705.html) [snippet].
- **Workaround and why it fails:** Exit happens with no warning; employers compete on gross salary only; families abroad see only the headline Tokyo salary.
- **Who suffers:** Regional SMEs, the prefecture's ROI on its programme, and the worker if the move to Tokyo leaves them worse off after rent.
- **App idea: "Real Pay, Real Life" (本当の手取り).** The worker enters an offer. The app computes take-home after tax, social insurance, rent, commute and remittance capacity for Nagasaki vs Tokyo/Osaka from public data. It generates a one-page **Bengali/English card for the family**: "You can send home ¥X per month here vs ¥Y there". The employer side gets an anonymous monthly 3-question pulse (workload, relationships, money worry) in the worker's language and an early-warning flag.
- **Why novel:** No found tool aims at the family, which Miyazaki named the biggest factor in departures.
- **TSUNAGU link:** people-to-people across borders (worker and family in Bangladesh), data-to-data (cost of living, pay).
- **Judge angle:** Directly protects Nagasaki's per-hire investment; BJIT's placement reputation depends on retention.
- **Originality 8/10. Feasibility 7/10** (tax/insurance math needs care; use simplified, clearly labelled formulas).
- **Honest demo data:** Public rent averages (e-Stat/SUUMO market pages), national tax/insurance rates; mark the results as estimates, not advice.

### 3. The Bangladesh-to-Nagasaki pipeline converts very few hires
- **Problem:** Nagasaki has run its Bangladesh IT programme since about 2024 but had only 6 offers at 5 firms by May 2026.
- **Evidence:** Nagasaki documents [snippet]; Miyazaki took 5 years to reach 50+ hires across 24 firms [fetched]; JICA notes local misperception of engineers as unskilled [fetched]; Prothom Alo on language and teacher shortage [fetched]; B-JET "placement not guaranteed" [snippet]. Why so few is not stated in any source I could read: the following causes are inferred and unverified: ¥1.28M fee risk for an SME, no way to picture a Dhaka candidate doing the firm's actual work, and a one-shot interview after the B-JET course.
- **Workaround and why it fails:** Prefecture tours (Nov 24-26 2026), interviews, a September internship for 3 hires. These are expensive, episodic and few.
- **Who suffers:** Nagasaki SMEs that hesitate; B-JET graduates waiting for offers; the prefecture.
- **App idea: "Trial Sprint Bridge."** A Nagasaki firm posts one small, real task (e.g. digitise a paper inspection form). Shortlisted B-JET candidates do it remotely in 1 week with a shared board and daily やさしい日本語 check-ins. Both sides rate the experience; the firm sees work and communication, not just a CV. After hire, the same board becomes the pre-arrival onboarding plan.
- **Why not the portfolio/skill-gap ideas already evaluated:** It is a live, two-way trial for one specific employer, not a static portfolio or gap map.
- **TSUNAGU link:** people-to-people (Dhaka-Nagasaki), communities (prefecture, firm, university), past-to-future (trial becomes onboarding).
- **Judge angle:** Highest. It addresses the exact programme the Nagasaki judges run, BJIT's sourcing, and is timely with the tour week.
- **Originality 7/10. Feasibility 7/10.**
- **Honest demo data:** 3 sample task briefs written by the team based on the real job categories (survey design, civil works, manufacturing); candidate profiles marked fictional.

### 4. IT graduates placed in non-IT regional firms have no defined first project (role/career mismatch)
- **Problem:** Most Nagasaki offers are at surveying, civil and manufacturing firms. An IT hire risks being used as general labour or interpreter, which hurts career growth and retention.
- **Evidence:** Nagasaki placement breakdown [snippet]; Miyazaki young engineers moving to Tokyo [fetched]; Cybozu: experienced foreign staff often "don't last more than a few years" (https://cybozushiki.cybozu.co.jp/articles/m006245.html) [anecdote]; HackerNoon on 年功序列 frustration (https://hackernoon.com/what-foreign-engineers-should-know-about-japans-nenko-joretsu-system) [anecdote]. No direct survey found. **Weak, inferred.**
- **App idea: "DX First 90 Days."** Staff at a non-IT firm list manual pain spots (paper forms, Excel re-typing, photo reports). The app turns them into a ranked backlog of small DX projects matched to the engineer's stack, with visible milestones the president can see.
- **Distance from skill-gap mapper:** It maps the company's process gaps, not the worker's skill gaps.
- **TSUNAGU link:** people-to-things, past-to-future (paper processes to digital).
- **Originality 7/10. Feasibility 7/10.** Demo data: team-written sample process list for a fictional civil-works firm.

### 5. Cultural friction: ホウレンソウ, unwritten rules, "flat vs hierarchical" expectations
- **Evidence:** MHLW 2025: 22.4% of employers report culture/values trouble, rising [fetched]. TCJ media article: "not reporting" and "doing it differently" are usually cultural, not skill, problems (https://gaikoku-jinzai.tcj-education.com/posts/workplace_communication) [snippet]. Kenyan engineer at freee on unwritten workplace rules (https://note.freee.co.jp/n/nd51a2a46bc95) [anecdote]. METI video kit on high-context miscommunication (https://www.meti.go.jp/policy/economy/jinzai/gaikokujin/20210426003-1.pdf) [snippet].
- **Workaround and why it fails:** One-off seminars and videos (Nagasaki also made videos, https://www.pref.nagasaki.jp/shared/uploads/2025/11/1764130751-2.pdf [snippet]). They are generic, not about this team's rules.
- **App idea: "Unwritten Rules board."** Japanese and foreign staff each post one rule they assume ("tell me before 17:00 if late", "silence in a meeting is not agreement"). Both sides vote "I knew / I did not know". The gaps become a daily 3-tap ホウレンソウ check-in (done / stuck / will be late) with ready Japanese phrases.
- **Overlap note:** Pairs naturally with idea 1; could be one product.
- **Originality 6/10. Feasibility 8/10.**

### 6. Residence-status paperwork and mandatory support records
- **Evidence:** MHLW 2025: 24.8% of employers [fetched]. **Correction to the brief:** the quarterly 定期届出 ended; since 2025-04-01 it is **annual** with one unified form (参考様式第3-6号) (https://www.moj.go.jp/isa/applications/ssw/nyuukokukanri10_00002.html) [snippet]. The 3-monthly 定期面談 is still required; online is allowed with consent since 2025-04; reports are attached only if a problem was found (office-tree: https://office-tree.jp/blog/immigration/teiki-todokede-guide/) [snippet]. 登録支援機関 fees average about ¥20-30k per worker per month plus ¥20-50k setup (advertiser sources: https://office-tree.jp/blog/immigration/registration-support-costs/ ; https://treeglobalpartners.com/articles/torokushien-kikan-hiyo/) [snippet].
- **Why existing tools already cover it:** SMILEVISA, ビザマネ, Linkus and about 16 products already offer expiry alerts and form generation (comparison: https://kigyolog.com/service.php?id=791 ; https://www.itmedia.co.jp/itselect/employment-management-system-for-foreigners/) [snippet].
- **Verdict:** Real pain but a crowded market and low novelty. **Originality 3/10, Feasibility 8/10.** Only worth it if folded into idea 2 (the 3-monthly 面談 as the pulse check, saved as the record the rules require).

### 7. Housing and living set-up cost falls on the employer
- **Evidence:** MHLW 2025: 21.9% of employers cite living-environment cost, rising [fetched]. MOJ 2017 foreign-resident survey: 39.3% refused housing for being foreign, 41.2% for having no Japanese guarantor (https://roumu.com/archives/51260520.html ; https://www.japantimes.co.jp/community/2017/07/02/social-issues/no-foreign-tenants-not-much-can-you-can-do) [snippet; **2017, outside window**; Toyo Keizai gives 39.8%]. Guarantor-company workarounds are widely documented in blogs (https://e-housing.jp/post/why-your-apartment-rental-application-is-getting-rejected-as-a-foreigner) [snippet].
- **App idea: "Akiya Match for Talent."** A prefecture-endorsed board where landlords of vacant regional homes (akiya) opt in to "employer-guaranteed" foreign tenants; the employer's guarantee and the worker's verified profile go with the request.
- **Risk:** Close to the "admin life-event checklist" space and needs real landlord partners. **Originality 6/10, Feasibility 5/10.**

### 8. Recruitment fees and brokers on the sending side
- **Evidence:** MHLW 2025: high agency fees are the top trouble among troubled workers (22.2% of the 10%) [fetched]. Prothom Alo: an agency licence cancelled for overcharging [fetched]. JagoNews24 "4-5 lakh taka" quote [snippet]. Daily Star Tk 10.5 lakh fine [snippet]. ISA names entry-fee debt as a main reason for trainee 失踪 (6,510 in 2024 vs 9,753 in 2023) (https://www.moj.go.jp/isa/content/001425159.pdf) [snippet]. 育成就労 caps what workers pay sending agencies at 2 months' pay (https://global-saponet.mgl.mynavi.jp/visa/18276) [snippet].
- **App idea:** Fee-transparency ledger where each cost item is confirmed by worker, agency and employer. **Risk:** sensitive, needs real agency data, and BJIT is itself a placement company. **Originality 6/10, Feasibility 5/10.** Weak hackathon fit.

### 9. Isolation and "nowhere to ask" in regional cities
- **Evidence:** 15.8% of troubled workers did not know where to ask (about 1.6% of all) [fetched]. JICA: Miyazaki residents unsure how to relate to the engineers at first [fetched]. Cybozu / Japan Times sponsored pieces on lonely workplaces (https://cybozushiki.cybozu.co.jp/articles/m006253.html ; https://info.japantimes.co.jp/jt-with-kintopia/02) [anecdote]. EY: local governments lack resources to receive foreign residents (https://www.ey.com/ja_jp/insights/government-public-sector/retention-of-foreign-talent-supports-the-regional-economy-part1) [snippet].
- **Note:** Close to the already-evaluated "foreign-worker and elderly mutual help". Low priority. **Originality 4/10.**

### 10. Engineers in Japanese-only meetings
- **Evidence:** Anecdotes only: dev.to "the Japanese word arrives 3 business days late... meetings where I feel lost" (https://dev.to/itsugo/working-with-japanese-clients-humbled-me-faster-than-i-expected-49da); N2 as a hiring filter (https://www.tokyodev.com/articles/job-hunting-as-a-junior-developer-in-japan); TokyoDev 2025 survey (n about 900, Tokyo-heavy, 65% use English "always", job happiness 2.9/5 with no remote work vs 3.8 fully remote) (https://2025.surveys.tokyodev.com/) [fetched]. TokyoDev's sample is well paid and Tokyo-centred; it does not represent B-JET graduates in regional firms.
- **Idea:** After each meeting, a 3-line やさしい日本語 decisions summary that the foreign engineer confirms. This is a sub-feature of idea 1. **Originality 4/10 alone.**

## Recommendation

Strongest combined pitch for these judges: **idea 3 (Trial Sprint Bridge) feeding idea 2 (retention pulse + family pay card)**, with idea 1's teach-back check as the communication layer. It covers hiring, onboarding and retention for exactly the Nagasaki-BJIT programme, and each part has a clear demo. Avoid idea 6 (crowded SaaS market) and idea 9 (overlaps an evaluated idea).

## Gaps / unverified

- No first-hand Reddit, 知恵袋, 5ch or note.com threads were retrievable. Forum recurrence is not demonstrated.
- Nagasaki programme numbers (6 offers, 5 firms, ¥1.28M fee, placement mix) come from search summaries of PDFs I could not open. Confirm with 長崎県未来人材課 (095-895-2733) or the PDFs.
- No source states why Nagasaki conversion is low; causes in item 3 are inferred.
- Daishin Giken's business line was not checked.
- ISA 令和6年度 在留外国人に対する基礎調査 (7,621 respondents plus 1,000 employing organisations) likely has stronger worker-side numbers but the result tables were not retrieved: https://www.moj.go.jp/isa/content/001436042.pdf

## Other URLs consulted

- https://www.mhlw.go.jp/content/11655000/001646130.pdf (MHLW 外国人雇用状況 full report)
- https://www.moj.go.jp/isa/content/001416410.pdf (ISA 定期届出 change)
- https://www.miyazaki-u.ac.jp/sdgs/mediafile/6012_ot_ito.pdf (B-JET 265 completed, 186 in Japan, 53 in Miyazaki) [snippet]
- https://www.jri.co.jp/report/researchfocus/detail/16663/ (JRI: regional settlement depends on system design, not only wages) [snippet]
- https://www.tokyodev.com/ja/articles/multinational-teams-and-the-challenges-facing-japanese-companies [snippet]
- https://jp-mirai.org/wp-content/uploads/資料２：長崎県20251112登壇資料（JP-MIRAI宍戸）.pdf (not opened)
- https://www.chisou.go.jp/sousei/about/gaikokujinzai/pdf/r6_gaikokujinzai_report.pdf (Cabinet Office local-government survey, not opened)
- https://talenthub.jp/interviews/anik-barua/ ; https://insideout.paypay.ne.jp/en/2024/07/10/lifeinjapan-vol13-en/ (Bangladeshi engineers in Tokyo, positive accounts) [snippet]
