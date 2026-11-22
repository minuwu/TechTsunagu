# Japanese Q&A / forum pain-point mining (TSUNAGU hackathon)

Research date: 2026-11-22. Read-only web research (WebSearch with domain filters + WebFetch of individual threads).

## Method and evidence-strength rule

- Sources: Yahoo!知恵袋 (detail.chiebukuro.yahoo.co.jp), ママスタコミュニティ (mamastar.jp / select.mamastar.jp), はてな匿名ダイアリー (anond.hatelabo.jp), はてなブックマーク (b.hatena.ne.jp), note.com. 発言小町 and OKWAVE returned nothing useful through the search tool (not indexed for these queries); ジモティー complaints not searched.
- Evidence strength = (number of distinct threads found on the same problem) + (view counts on fetched threads) + (recency, 2023-2026 weighted up) + (はてブ users count where available).
- "Fetched" = numbers read directly from the page on 2026-11-22. "Search-only" = title seen in search results; date/views not verified.
- Chiebukuro question IDs (q1..., q10..., q14...) do NOT encode the date reliably (two q14 threads turned out to be 2021 and 2023). Dates below are only given where fetched.
- Most strong threads are 2013-2021. Recurrence across years is itself evidence the problem is unsolved, but 2023-2026 data points are fewer.

## Cross-cutting design constraint (from evidence)

The 829-user はてブ entry "PTAをDXしようとして挫折した話" (2022-04-09) shows why digital fixes for PTA/自治会 fail: school gave paper rosters only, officers distrusted Excel logic, the helper had no authority. Any app for PTA / 自治会 / 回覧板 should therefore:
- work alongside paper (QR on the printout, no login, no install for the reader),
- need only one enthusiastic person to adopt,
- make rules explicit and visible (fairness is the real fight, not the tool).

This is a strong talking point for the proposal ("we designed for the people who refuse apps").

---

## Ranked pain points

### 1. 部活・少年団の配車当番 / 車出しの偏り (Kids' club carpool duty: unfair, risky, opaque)

- Problem: Youth sports clubs and school clubs rely on parents to drive kids to away games. Duty falls on the same few families, costs (gas, tolls) are split ad hoc, and liability on accidents is unclear. Parents without a car or with toddlers cannot take turns and feel guilty or excluded.
- Evidence:
  - スポーツ少年団の親の配車について — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10130943110 — fetched: 2014-06-23, 6 answers, **53,438 views**. Asker afraid of highway driving and liability; club refuses a written agreement or bus.
  - 部活の配車当番について — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13228801388 — fetched: 2020-07-22, 5 answers, **8,098 views**. Parent with two preschoolers cannot do the full-day driving duty.
  - 少年サッカーチームの車出し、車代はどうしています — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10138721695 — search-only. Gas cost split by distance/headcount; some teams exempt drivers from other duties.
  - 少年野球の配車について — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q1395749543 — search-only.
  - スポ少の親の負担 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13298745608 — search-only (送迎, お茶当番, 見守り当番).
- Current workaround: paper or LINE 配車表 drawn up by a 世話人; gas money collected in cash; informal "drivers skip お茶当番" rules.
- Why apps fail: TeamHub / スポーツ系 apps do attendance and messaging; 車割作成 app (https://apps.apple.com/jp/app/id1627910277) only groups passengers into cars. No found app tracks cumulative driving burden, auto-proposes a fair rota, or computes gas split. (Existing-app check: one search, 2026-11-22.)
- Who suffers: 共働き parents, single parents, families without cars, the 1-2 parents who always drive, 世話人 who build the table.
- 3-day app idea: **"のりあいノート"** — team creates a match; parents tap seats available / need seats; app proposes a car assignment weighted by each family's cumulative driven km and duty count (fairness ledger), lets non-drivers "pay back" with other duties, shows gas split per family, and keeps a season history. Web app + shared link, no login for parents.
- TSUNAGU link: people↔people (families pooling cars), data↔data (driving ledger + duty ledger), community (team).
- Originality 7/10, Feasibility 9/10.

### 2. バイト「休むなら代わりを自分で探せ」(Part-time shift cover you must find yourself)

- Problem: Many shops make the worker find their own replacement. Workers DM coworkers one by one on LINE; managers have no visibility; students and part-timers feel trapped.
- Evidence:
  - バイト休む時に代わりを自分で探さないと休めないのって労働基準法に反してますよね？ — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q12284823092 — fetched: **2023-08-22, 38,212 views**, 2 answers, 6 共感.
  - アルバイトで休みをもらう時に代わりを自分で探すシステム — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13317333143 — fetched: **2025-07-11**, 8 answers, 2,054 views.
  - Search-only, same theme: q13171473363, q11280431026 (told to connect personal LINE with coworkers), q10312404150, q12251779632, q10322170680, q1277060257, q12288916942, q12178020647 — all https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/<id>. At least 10 distinct threads.
- Current workaround: DMing coworkers individually on personal LINE; asking manager to post in a store LINE group.
- Why apps fail: シェアフルシフト (旧Sync Up, https://apps.apple.com/app/id1282333147) and similar are employer-installed, company-code-gated, and show open shifts, not worker-to-worker swap requests. Small shops (individual restaurants, pharmacies, cram schools) do not buy shift SaaS. (Existing-app check: one search; dedicated peer swap app not found, mark partially unverified.)
- Who suffers: students (the team's own peers), part-timers, small-shop managers.
- 3-day app idea: **"シフト代わってボード"** — store owner prints one QR at the back room; worker posts "Sat 17-22 cover needed"; coworkers get push/LINE notify and tap "I'll take it"; manager approves with one tap; ledger shows who covered whom (credit, so covering is rewarded and repaid). No personal LINE exchange needed (privacy).
- TSUNAGU link: people↔people at work; small business ↔ staff; data (who-covered-whom ledger).
- Originality 6/10, Feasibility 9/10. Note: weaker "regional" angle; strong student relatability for the demo.

### 3. 役員の引き継ぎ資料がない (PTA / 自治会 / 子供会 officer handover with no records)

- Problem: Officers rotate yearly (often by lottery). Handover is oral, notes are lost, every year re-learns the same tasks, deadlines, contacts and tricks. This is also why 子供会 and PTA roles are feared and associations collapse.
- Evidence:
  - PTA役員、引継ぎ資料なしで口頭のみって普通ですか？ — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q14240063299 — fetched: 2021-03-10, 7 answers, **8,109 views**.
  - PTAをDXしようとして挫折した話 — https://anond.hatelabo.jp/20220409133020 (2022-04-09) — はてブ **829 users**: https://b.hatena.ne.jp/entry/s/anond.hatelabo.jp/20220409133020 . Paper-only roster, undocumented exemption rules for the role lottery.
  - Search-only: q13277230528 (前任者の杜撰さ), q12294346072, q10137803480 (仕事がわからな過ぎて困って), q12220447637, q10281395230 (会計 handover) — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/<id>.
  - Related 子供会 collapse threads: https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10238021705 , https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11303381736 , https://mamastar.jp/bbs/topic/3947678 , https://mamastar.jp/bbs/topic/3837408 (search-only).
  - 2026 data point: https://anond.hatelabo.jp/20260828105443 (2026-08-28): "PTAも自治会も必要だけどな。役員の引き受け義務が嫌がられてるだけで" (0 reactions itself; sits in a larger thread).
- Current workaround: paper binder passed on (often incomplete), oral handover, Google Drive if someone is IT-savvy.
- Why apps fail: general tools (Drive, Notion, LINE ノート) have no structure; PTA apps focus on broadcasting to parents, not on the officer's year-plan memory. (Existing-app check not run for "PTA 引き継ぎ アプリ"; mark unverified.)
- Who suffers: incoming officers (mostly working mothers), school teachers in charge, 自治会 班長.
- 3-day app idea: **"引き継ぎ年表"** — a month-by-month timeline per role (e.g. 4月 名簿更新, 6月 草刈り申請 deadline). Each year's officer checks tasks, attaches photos of the paper forms, contacts and "what I wish I knew" notes. Next year's officer inherits the whole timeline plus reminders. A printable QR handout lets non-app members view it.
- TSUNAGU link: past↔future (strongest fit of all items), people↔people across years, data↔data.
- Originality 7/10, Feasibility 9/10.

### 4. 回覧板が回らない・訃報が通夜の後に届く (Circular board stuck; urgent notices arrive too late)

- Problem: Paper 回覧板 takes weeks to months to go round; urgent items (deaths, funerals, disaster info) arrive after the event; 班長 cannot see where it is stuck.
- Evidence:
  - 至急の回覧板が回ってくるのが遅く訃報などの知らせがお通夜の後に — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q14241862949 — fetched: 2021-04-17, 6 answers, 2,837 views.
  - 回覧板が回ってくるのが遅く…最低１ヶ月 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q14275752284 — fetched: **2023-02-18**, 3 answers, 2,220 views (1-2 months per round).
  - Search-only: q1411352589 (回覧板を早く回す良い方法), q13302796768, q10258639665, q1063789865, q10263356540, q11312887591, q14245883309 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/<id>. 10 threads in one search.
- Current workaround: two copies from both ends; date stamps on a route slip; 班長 knocking on doors.
- Why apps fail: e-回覧板 apps exist (Saitama city pilot, Toyama subsidy, example app 1,100 yen/month per association: https://www.city.toyama.lg.jp/_res/projects/default_project/_page_/001/012/291/kairanbanr8.pdf ; Saitama: https://www.city.saitama.lg.jp/001/013/007/p125740_d/fil/gaiyou.pdf). They need every household to install, and elderly members do not, so paper continues anyway.
- OVERLAP: close to the already-evaluated "voice bulletin for elderly" and "civic issue reporting". Only pursue with the sharply different paper-hybrid angle below.
- 3-day app idea (different angle): **"回覧板トラッカー"** — keep the paper board, add a QR sticker. Each household scans (no login) to mark "read, passed on". 班長 sees a live map of where it is stuck; "urgent" pages (訃報) also go instantly to households who opted in. Paper stays primary.
- TSUNAGU link: people↔things (the board itself), community.
- Originality 5/10, Feasibility 9/10.

### 5. 親の介護の兄弟間の不公平 (Sibling imbalance in parent care)

- Problem: The nearby or co-resident sibling does almost all the care (visits, hospital escort, paperwork, money); distant siblings do little and do not see the load; resentment leads to family breakdown.
- Evidence:
  - 兄弟間で介護の負担に不公平を感じています — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10109485160 — fetched: 2013-06-28, 6 answers, **18,011 views**.
  - Search-only: q11293685256 (兄弟で仲違い), q13298574573 (成功例を聞かせて), q14248080704 (兄弟格差に苛立ち), q10289620960 (費用分担), q10296816470 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/<id>.
  - Related: hospital escort vs work — q1182827926, q14254010039, q10210901120 (search-only).
  - note essays on remote-care monitoring via LINE (2025-2026): https://note.com/novel_bear2760/n/na60f65843cab , https://note.com/heartlink_group/n/n556685b858a9 (search-only).
- Current workaround: family LINE group; arguments; settling at inheritance (not legally enforceable per answers).
- Why apps fail: care-diary apps (MydayAI list of 16: https://note.com/mydayai/n/nde58312dbc03) record the parent's condition, not who did what and who paid what.
- OVERLAP check: different from "caregiver medical QR passport" (that is medical info for responders). This is a family fairness ledger.
- 3-day app idea: **"きょうだい介護帳"** — log each visit, escort, call, expense per sibling; a task board where the main carer posts needs ("Tue 10:00 hospital escort") and distant siblings claim or pay instead; monthly fairness summary (hours + yen). 
- TSUNAGU link: people↔people (family across distance), data↔data.
- Originality 6/10, Feasibility 9/10.

### 6. 自治会の出役・当番と出不足金 (Mandatory community work days and absence fines)

- Problem: 草刈り, 清掃, ゴミ当番 are compulsory; absentees pay 出不足金 (500-3,000 yen); 共働き and elderly single residents cannot attend; resentment over who skips.
- Evidence:
  - 自治会の出不足料は、いくらですか？ — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10240315580 — fetched: 2021-03-15, 8 answers, 1,528 views.
  - Search-only: q1444460729, q13241256888 (罰金3000円), q10238366935 (elderly mother alone fined), q10129240470, q13304165663 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/<id>.
  - ゴミ当番 / 班長 by rotation: https://mamastar.jp/bbs/topic/3935355 (2021, search-only).
  - One association lets absentees make up time on another day (from q10240315580 answers).
- Current workaround: fines, make-up work, paper roster.
- Why apps fail: no app found for duty swapping / make-up scheduling in 自治会 (not formally checked; unverified).
- 3-day app idea: **"おたがいさま当番"** — duty calendar where residents can swap slots or do a make-up task (e.g. carry an elderly neighbour's bulky item) instead of paying the fine; time-credit ledger visible to the 班長.
- TSUNAGU link: community, people↔people, generations.
- Originality 7/10, Feasibility 8/10.

### 7. LINEグループが多すぎ・分断 (Too many fragmented LINE groups for school/club/lessons)

- Problem: grade, team, parents, committee LINE groups; important changes get buried; reply pressure; info does not reach the right person.
- Evidence:
  - note: LINEグループが多すぎて疲れる？いや、分断されてるから疲れるんです — https://note.com/shimao555/n/nff05d52749e1 — fetched: **2026-01-09** (likes count not shown). Lists 学年LINE, チームLINE, 保護者LINE, 係LINE.
  - グループLINEって、面倒な時ないですか？ — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13275774068 (search-only).
  - 習い事のグループライン、いつから主流になったの？ — https://mamastar.jp/bbs/topic/3677105 (search-only).
  - LINEが原因で小学校の保護者会が真っ二つに割れた話 — https://anond.hatelabo.jp/20180706142226 (2018, search-only).
  - 部活の保護者連絡網 LINE トラブル — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10146664070 (search-only).
- Why apps fail: LINE API cannot read group chats, so an aggregator is not buildable; dedicated apps (TeamHub, スポスル) require everyone to switch.
- 3-day app idea: fold into #1 as a "single source of truth" schedule page per team with a change log, linked from every LINE group.
- Originality 4/10, Feasibility 7/10. Better as a feature than a standalone.

### 8. 学校プリントが多すぎて管理できない (School handouts overload, missed deadlines)

- Evidence:
  - 小学校からのプリント類が多すぎて、管理に困ってます — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11188244151 — fetched: 2018-03-29, 7 answers, 1,783 views.
  - Search-only: q12174811032, q11227109146, q12324514757 (発達障害の中学生), q10305979995 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/<id>; ママスタセレクト article https://select.mamastar.jp/531080 .
- Why low priority: crowded. プリゼロ (https://apps.apple.com/jp/app/id1549139568) and ポストック (https://apps.apple.com/jp/app/id6443837941, with AI summary) already do photo + calendar; many schools moved to tetoru-type apps.
- Originality 3/10, Feasibility 9/10. Avoid.

### 9. 自治会費の集金で何度も訪問 (Cash collection of association fees)

- Evidence:
  - 自治会費の集金方法について — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q12185166837 — fetched: 2018-01-24, 11 answers, 5,590 views.
  - 自治会費の集金に何度行っても出ない — https://mamastar.jp/bbs/topic/3034489 — fetched: 2018-04-14, 10 replies.
  - Search-only: q10274426930, q10181449300, q10314831525.
- Why low priority: largely solved since 2024. PayPay allows 自治会 accounts from 2024-06 (https://paypay.ne.jp/store/jichikai/), 楽天ペイ from 2025-07-14 (https://webtan.impress.co.jp/n/2025/07/15/49732); Tokyo subsidises.
- Originality 2/10. Avoid.

### 10. 足腰が悪くてゴミ出しできない / 長崎の坂 (Garbage-out for elderly on slopes; Nagasaki)

- Evidence (regional bonus, weak forum volume):
  - 足腰が悪い人、ゴミ捨てどうしたら？ — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q14270559069 — fetched: 2022-11-05, 5 answers, 431 views.
  - 長崎のリアルな住み心地 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q14300548519 — fetched: 2024-07-03, 5 answers, 528 views (slopes make shopping trips exhausting; bikes impractical; last buses early).
  - なぜ長崎県は住みにくい評価 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13175969533 ; 長崎 坂道や災害などで家を手放す — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10319070085 ; 車椅子と階段 — https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11254074976 (search-only).
  - note on 長崎 斜面地 shopping refugees + mobile supermarket recruitment: https://note.com/ogasawarakikaku/n/na9cdcf31a728 (search-only).
  - Policy context: 環境省 高齢者のごみ出し支援制度導入の手引き https://www.env.go.jp/content/900536110.pdf ; NIES case book https://www-cycle.nies.go.jp/jp/report/aging3.pdf .
- OVERLAP: close to "foreign-worker↔elderly mutual help" and "island group-ordering". Only distinct if framed narrowly as a slope-specific "ゴミ出しついで便" (neighbours already walking down take a bag; QR on the bag station) — unverified demand from Nagasaki residents themselves.
- Originality 4/10, Feasibility 8/10.

### Dropped / folded (thin evidence or solved)

- 農家 収穫 人手 — served by デイワーク, タイミー, おてつたび, あぐりナビ (https://note.com/yamakubo/n/n43718dd10dcf ; https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10274358085). Avoid.
- 漁師 未利用魚 販路 — Fishlle!, E-Fish, 対馬 direct sites exist (https://note.com/sustainablesf/n/n08e0c810b799 ; https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11216402186). Overlaps food-surplus routing. Avoid.
- 制服お下がり — real but mostly handled by school/city 学生服リユース and bazaars (https://select.mamastar.jp/802686 ; https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q14142032774). Possible side idea only.
- 遠方の墓参り — answers point to temple services and 墓じまい (https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13307404878). Low forum heat.
- 公民館予約, 実家の通帳, 離島通院 — thin forum evidence in this pass.
- 小1の壁 送迎 — real (q11260515631, q14257934149) but solved via ファミサポ/子育てタクシー; overlaps #1 if anything.
- 地方移住 車がない — very common (q1246733142, q13192935223, note essays) but not app-solvable in 3 days beyond ride sharing, which hits 白タク legal issues.

## Gaps / unverified

- 発言小町 and OKWAVE produced no hits through the search tool; their volume is unknown.
- Most view counts come from 2013-2021 threads; 2023-2026 numbers exist only for #2 (2023, 2025), #4 (2023), #10 (2022, 2024), #7 (2026 note).
- Existing-app checks were one search each for #1, #2, #9; not run for #3, #5, #6. Treat originality scores for those as estimates.
