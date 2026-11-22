# Forum pain points: 5ch / matome / Girls Channel sweep

Date of sweep: 2026-11-22. Read-only, nothing posted.

## Method and source caveats

- **Main source used: Girls Channel (girlschannel.net).** Its site search (`https://girlschannel.net/topics/search/?q=<kw>`) works through WebFetch. It lists topic title, date and comment count. Five high-volume threads were read in full (top-voted comments summarized).
- **5ch itself was not reachable for search.** `find.5ch.net` redirects to `find.5ch.io`, which returned no results to the fetcher. `find.open2ch.net` returned 403. Matome site searches (kijosoku.com refused connection; hamusoku.com `?s=` ignored the query). WebSearch with `site:` filters returned no 5ch/matome hits.
- So evidence below is mostly Girls Channel (anonymous board, mostly women 30s–50s, many parents). This skews toward household / school / neighborhood issues. Workplace and male-dominated pain points are under-sampled.
- Comment counts are as shown on the search page on 2026-11-22. "Recurrence" = number of separate threads on the same issue.
- Many GC topics are reposts of news articles; the comments are the grievance evidence. Comment quotes come from WebFetch summaries of the thread; treat exact wording as **near-verbatim, not verified character-by-character**.
- Nagasaki-specific context comes from city/academic PDFs, not forums (marked).

All GC URLs below are `https://girlschannel.net/topics/<id>/`.

---

## Ranked pain points (strongest evidence first)

### 1. 旗当番・登下校見守り当番 (school crossing flag duty rotation)

**Problem:** Parents are forced into rotating morning crossing-guard duty. Swaps, absences, toddler exemptions and flag handoff are all handled by paper and doorstep visits.

**Evidence (GC):**
- 下の子がいて小学校の旗当番ができない！免除なし、預かり費は2000円 — 2023-07-05, 1925 comments — /topics/4649207/
- 千葉県流山の小学校PTAが「旗振り当番」…「外注」を選択した結果 — 2023-03-14, 1697 — /topics/4443045/
- 旗当番の不満 — 2024-04-12, 1017 — /topics/5058159/ (read)
- 冷え込む朝の「旗当番」が大変すぎる問題 — 2023-12-12, 777 — /topics/4914774/
- 【小学校】旗振り当番が憂鬱 — 2025-03-25, 445 — /topics/5602382/
- 旗当番について！頻度や不明点 — 2025-01-28, 273 — /topics/5519318/
- 旗当番どうしよう【2歳児連れ】 — 2023-10-18, 260 — /topics/4794342/
- 旗振り当番が苦手な人 — 2025-12-05, 154 — /topics/5948861/
- 【PTA】旗振当番 欠席の場合 — 2026-01-08, 95 — /topics/5987088/
- Recurrence: 14 threads 2022–2026, ~7,000 comments total.

**Quotes (from /topics/5058159/):**
- 「月1くらいならまだしも週1で多すぎます」 — "Monthly I could stand, weekly is too much."
- 「今年度ペアの人が1度も来なかった。」 — "My pair partner never came once this year."
- 「手渡しと決まってるから玄関に置いておくのもダメ」 — "Flag must be handed over in person; can't even leave it at the door."
- 「ただじゃないのよ。お金かかるの。」 — on outsourcing: "It's not free, it costs money."
- Retiree volunteers: 「頼み込まれて断れない場合もある」 — "Seniors sometimes get begged and can't refuse."

**Current practice / why tools fail:** Paper rota from PTA, LINE for swaps, physical flag/vest handoff. Outsourcing (Nagareyama) costs money. Senior volunteer pools have waitlists and burnout. No tool handles swap + fairness + who-is-actually-standing-there.

**Who suffers:** Working parents, parents with toddlers, single parents; retirees pressured to help.

**3-day app idea — "みまもりシフト" (crossing-duty swap market):** Web app per school district. Map of crossing points; rota auto-generated with caps per household. One-tap swap request broadcast to eligible households; points ledger for fairness. Opt-out families can pay into a pool that pays community volunteers (retirees, local company staff on commute) who sign up for specific corners. QR at each corner: volunteer scans in, so parents see "covered" live. No physical flag handoff needed (flag stays in a box at the corner).
TSUNAGU: people↔people (parents↔retirees↔local firms), community, past↔future (retirees protecting kids).
**Originality 7/10. Feasibility 9/10.**

---

### 2. 回覧板 (paper neighborhood circulars)

**Problem:** Paper circulars must be carried door-to-door, often by hand, weekly. Digital attempts stall because elders block LINE and some residents lack smartphones.

**Evidence (GC, from search "回覧板", 32 topics 2017–2026):**
- 「回覧板をLINEで？ふざけるな」田舎の情報共有、デジタル化を許さない自治会長老 — 2026-04-03, 493 — /topics/6081752/ (read)
- 加入率が最低 町内会のなり手不足が深刻 回覧板よりインターネット — 2023-06-26, 503 — /topics/4614797/
- 回覧板をペーパレスにしてほしい！と発表しましたが、却下されました — 2023-11-13, 450 — /topics/4865050/
- 町内会の旧態依然の運営スタイルに疲弊する人たち…「回覧板」と「現金での集金」で潰れる休日 — 2024-07-01, 377 — /topics/5210475/ (read)
- 回覧板がしんどい — 2023-03-21, 277 — /topics/4495055/
- 回覧板が煩わし過ぎる — 2024-08-26, 249 — /topics/5297150/
- 紙か電子化か…変わる回覧板「スマホで良い」「年配は使いこなせない」 — 2025-08-28, 213 — /topics/5819443/
- コロナ禍を経て復活した「回覧板」…「回す手間が大変」 — 2023-11-16, 190 — /topics/4876647/
- 回覧板の日付記入 — 2025-09-08, 183 — /topics/5832236/
- “令和の回覧板事情” — 2025-12-29, 156 — /topics/5960841/
- 回覧板がめんどくさい — 2025-07-20, 128 — /topics/5765762/
- 助けて 回覧板が毎週回ってくる — 2025-02-27, 121 — /topics/5565736/
- Recurrence: ~13 threads since 2023, ~3,300 comments.

**Quotes (/topics/6081752/):**
- 「回覧板隣にもってくの地味にだるいもん」(+579) — "Carrying it next door is quietly exhausting."
- 「PDF形式でメールで送ってくれや…経費の無駄」(+620) — "Just email a PDF; waste of money."
- 「田舎では回覧板で安否確認ってところもある」(+403) — "In the countryside the circular doubles as a welfare check."
- 「回覧板のためにLINEの有料サービス入れって言われたらそりゃ嫌だわ」(+358) — against paid LINE.
- 「高齢者にLINEなんて無理だよ」(+161) — "Elderly can't do LINE."
- /topics/5210475/: 「回覧板は面倒。ペーパーレスにしようよ。」 / 「町内会費もさっさと振り込みにしてほしい」

**Non-forum corroboration:** Saitama City e-circular survey: digital + paper both arrive, "手間は変わらない" (https://www.city.saitama.lg.jp/001/013/007/p125740_d/fil/r7densikairanbananke-toma.pdf). Iwaki pilot: paper chain city→嘱託員→班長→household takes weeks (https://www.city.iwaki.lg.jp/www/contents/1747102986512/simple/r7jissihoukokussyo.pdf). Toyama and Kagoshima subsidize e-circulars.

**Why existing services fail:** e-circular apps (結ネット, Lorcle, My自治会, LINE open chat) are all-digital. Mixed neighborhoods end up running paper and digital in parallel (double work). Elders defend paper because the doorstep handoff is the de facto 安否確認. LINE raises privacy objections.

**3-day app idea — "ハイブリッド回覧":** Leader uploads a notice once. Smartphone households read in-app with a "見ました" tap. App auto-prints a slim paper version only for opted-out households, each with a per-household QR / tick box. The rider (or the household itself, via a cheap button or a family member) marks read. Read-confirmations feed a soft welfare signal: household unconfirmed for N days → gentle nudge to the assigned neighbor. This keeps the 安否 function elders value while dropping the walking chain.
TSUNAGU: community, digital↔analog, people↔people.
Overlap check: different from "voice bulletin for elderly" (that is broadcast only); this one is the two-way read-receipt + welfare angle. Note it may be judged close; pitch the 安否 loop.
**Originality 6/10. Feasibility 9/10.**

---

### 3. PTA・町内会・子ども会の役員決めと現金集金 (officer lottery and cash dues collection)

**Problem:** Officer roles assigned by lottery or "1 per child" rule; dues collected door-to-door in cash; no transparency on who did what.

**Evidence (GC):**
- なんでPTAってなくならないの？？ — 2026-03-02, 754 — /topics/6033893/
- 小学校PTA役員の逃げられない現実。「子ども1人につき1回ルール」 — 2026-03-15, 527 — /topics/6045985/ (read)
- PTAの役員選出のアンケートについて — 2025-12-11, 437 — /topics/5950438/
- 町内会・自治会"辞めたいと思った"は8割 — 2025-02-06, 412 — /topics/5526536/
- 町内会費払わないと夜にピンポン — 2025-08-23, 333 — /topics/5786737/
- 【PTA】来年度の役員決め【子ども会】 — 2025-12-06, 314 — /topics/5919732/
- PTA・子供会・自治会役員の愚痴 — 2026-05-21, 312 — /topics/6132313/
- 町内会行事欠席で罰金を取られる — 2026-06-24, 252 — /topics/6176892/
- 子ども会役員を断る口実を私に下さい — 2026-11-10, 191 — /topics/6331646/
- 町内会の集金どんなものがありますか？ — 2024-05-22, 137 — /topics/5160191/
- 【PTA】来年度の役員決め — 2026-11-17, 89 — /topics/6325976/ (fresh, this week)
- Exclusion of non-member kids: 「お前に食わせる豚汁はない」 — 2026-01-28, 2511 — /topics/6009786/; 「子ども会入ってないくせにプレゼント貰うな」 — 2025-12-17, 1301 — /topics/5945888/
- Recurrence: 20+ threads, every officer-selection season (Nov–Apr).

**Points from /topics/6045985/:** most hated = lottery, graduation committee, evening meetings, 広報/バザー; tools = paper sign-up forms, LINE, Excel. Suggested fixes: volunteer sign-up instead of lottery, online meetings, handover manuals, cut tasks.
**Points from /topics/5210475/:** 「現金の集金は正直無理筋」, 「集金の効率悪すぎ」.

**Why existing tools fail:** Generic PTA apps (e.g. PTA向けの連絡アプリ) handle notices, not fair task allocation. Cashless dues apps exist but elders keep cash. Handover knowledge lives in one person's head.

**3-day app idea — "タスク分解型 役員":** Break a big officer role into micro-tasks (print 1 newsletter, staff bazaar 2 h, collect 10 houses' dues). Members pick tasks; points accrue per household; nobody gets a full role by lottery. Built-in handover wiki per task. Dues: QR payment with cash fallback logged by collector.
TSUNAGU: community, past↔future (handover knowledge).
**Originality 6/10. Feasibility 8/10.** (Can merge with #1 as one "community duty fairness" platform.)

---

### 4. 免許返納後の移動・買い物難民 (mobility after giving up license / grocery access)

**Problem:** Elderly who surrender licenses cannot shop or reach hospitals; buses cut, taxis scarce, mobile supermarkets thin.

**Evidence (GC):**
- 宅配サービスは高い、タクシー移動もつらい…(免許返納後) — 2025-09-05, 1122 — /topics/5828803/ (read)
- 「あと10年は乗りたい」進まぬ免許返納…6割超「不便になった」 — 2024-06-22, 905 — /topics/5201810/
- 「返す年齢も決めてほしい」和歌山県 90歳男性 — 2024-10-26, 656 — /topics/5368932/
- 99歳が“逆走”…「バスがない」「手放せない」 — 2025-06-30, 434 — /topics/5738740/
- 親or祖父母に免許返納してもらった人のその後 — 2025-04-03, 262 — /topics/5605382/
- 車必須の地域の方、老後は免許返納などどのようにお考えですか？ — 2025-11-24, 169 — /topics/5934529/
- 田舎暮らしの親の免許返納について — 2024-08-16, 158 — /topics/5278107/
- 「自分はまだ大丈夫」免許返納で“過疎地は負担増” — 2024-06-09, 123 — /topics/5186360/
- 買い物難民: 県民の1割が「買い物難民」、移動スーパー広がる — 2023-12-09, 109 — /topics/4913032/; 食料品アクセス困難人口は900万人超 — 2024-09-07, 189 — /topics/5314606/
- Recurrence: 162 results for 免許返納; 50+ on page 1 alone since mid-2024.

**Quotes (/topics/5828803/):** 「移動スーパーは品数が少なく、値段も高い」 — "Mobile supermarket: few items, expensive." Taxi for wheelchair husband = 20–30 min wait. Comments: taxis booking-only, rural taxis shut nights/holidays; 乗合タクシー fixed return times don't match hospital end times; sidewalks too narrow/steep for senior cars.

**Nagasaki context (non-forum):** 43% of Nagasaki City is 斜面市街地, aging rate there >35%; residents who give up cars can't shop alone; a welfare corp uses idle shuttle time for supermarket runs (https://www.mlit.go.jp/common/001272004.pdf, https://www.city.nagasaki.lg.jp/sumai/660000/664000/p005617.html).

**3-day app idea — "通院ついで乗合":** Match elderly with the same clinic/hospital day and nearby addresses into one shared taxi; the return leg is flexible (app pings when the last rider's 会計 is done). Bundle a supermarket stop. Family members book on behalf of parents.
Overlap check: close to "island group-ordering" and "freight return-load matching" (already evaluated). Angle differs: passenger pooling keyed to hospital schedules.
**Originality 5/10. Feasibility 7/10.**

---

### 5. ゴミステーション問題 (garbage-station access, duty and rule disputes)

**Problem:** Garbage stations are run by 町内会; leaving the association can mean losing access. Rule-breakers, station cleaning duty and confusing rules cause neighbor conflict.

**Evidence (GC):**
- 自治会入ってないとゴミ出し禁止…脅しをかけられています — 2023-10-04, 520 — /topics/4808854/
- 町内会抜けた後のゴミ出し問題どうしてますか？ — 2026-05-12, 509 — /topics/6128528/ (read)
- 自治会に入らずゴミ出し 上田市で住民から「不公平」の声 — 2025-11-01, 417 — /topics/5888657/
- ゴミ出しのルールを守らない人にイライラする — 2024-02-23, 281 — /topics/5026340/
- 「朝のゴミ出し」が難しい人々の悩み — 2024-05-23, 236 — /topics/5154736/
- ゴミ出し警察 — 2026-10-04, 218 — /topics/6266774/
- 【ゴミ出し】揉めますか？？【ご近所】 — 2023-09-20, 205 — /topics/4743652/
- 退会でゴミ出し禁止 — 2024-12-03, 170 — /topics/5423972/
- 「ゴミ出しボランティア」中学生に働かせるな — 2024-08-03, 381 — /topics/5234932/
- Recurrence: 80 results for ゴミ出し; ~10 dispute threads since 2023.

**From /topics/6128528/:** Workarounds: private paid collectors (「数千円でその煩わしさから解放される」), driving to クリーンセンター (booking, limited hours), own bins, paying a 協力金, new stations refused by city, sneaking at night → cameras and notes. City says "settle it between residents."

**Nagasaki context (non-forum):** Slope districts need sled-basket collection costing several 億円 more than peer cities (https://www.city.nagasaki.lg.jp/sumai/660000/664000/p005617.html, via search summary — **unverified detail**).

**3-day app idea — "ステーション会計":** Each garbage station becomes its own tiny unit, decoupled from 町内会. Users (member or not) join the station, pay a per-station cost share, take cleaning turns. Photo-report misplaced bags with auto-guess of the wrong category; rota and fees transparent. Slope variant: "ゴミ出しついで" — neighbor heading downhill carries an elderly household's bag, logged as a duty credit.
TSUNAGU: community, people↔things.
**Originality 7/10. Feasibility 7/10.**

---

### 6. 予約しても病院で待たされる (hospital waits despite booking)

**Problem:** Appointment slots are overbooked; reservation time is only a check-in window; no live ETA.

**Evidence (GC):**
- 【予約】病院の待ち時間長すぎ問題 — 2026-05-05, 664 — /topics/6120614/ (read)
- 病院での待ち時間最高どれくらい待ちましたか？ — 2023-04-18, 471 — /topics/4512317/
- そこのあなた！病院の待ち時間どれくらいなら待てる？ — 2023-08-05, 306 — /topics/4715306/
- 病院で“有料ファストパス”ついに導入 — 2026-10-29, 295 — /topics/6317712/
- 病院での待ち時間、最高どれくらいでしたか？ — 2023-06-29, 242 — /topics/4656427/
- 病院の待ち時間、どのくらいなら待てますか？ — 2024-11-25, 224 — /topics/5422283/
- 病院の待ち時間、どれくらいなら我慢できる？ — 2026-10-24, 178 — /topics/6307484/
- Recurrence: 13 threads 2023–2026 (~3,000 comments); reappears every few months.

**From /topics/6120614/:** Causes: overbooked slots, check-in order beats appointment time, emergencies, test-result waits, then 会計 and pharmacy waits. Apps that ping "almost your turn" are praised, but only some clinics have them, and ETAs are unreliable.

**Why existing fails:** Clinic-side queue systems (e.g. 順番予約 services) exist but are per-clinic, and large hospitals rarely expose live progress. Patient side has no data.

**3-day app idea:** Patient-side crowd ETA: patients in the waiting room tap "called" / "still waiting" for a department; app shows live median wait and lets family know when to pick up (ties into #4).
TSUNAGU: data↔data, people↔people. Demo needs simulated data.
**Originality 5/10. Feasibility 7/10.**

---

### 7. 部活の地域移行・送迎 (school club transition to community clubs, parent driving)

**Problem:** National reform moves junior-high clubs to community clubs (2026→). Parents now drive kids to distant venues; coaches are scarce; info scattered.

**Evidence (GC):**
- 《京都市・部活を廃止》「地域クラブへ移行というけれど…」 — 2026-09-15, 607 — /topics/6256245/ (read)
- 「保護者送迎」が支える部活やスポーツの遠征 — 2026-05-21, 317 — /topics/6140843/
- 子供の部活の送迎したくない — 2026-03-20, 148 — /topics/6069751/
- 当番制の部活送迎「事故の責任は保険の範囲に限る」誓約書 — 2025-10-14, 116 — /topics/5884733/
- 【車必須の田舎】共働きの方は塾や習い事の送迎どうしてますか？ — 2026-11-11, 96 — /topics/6334022/
- 習い事 送迎 つらい — 2025-10-22, 157 — /topics/5886432/
- 送迎疲れ — 2026-09-01, 162 — /topics/6252206/
- Recurrence: 282 results for 送迎; strong upward trend in 2026.

**From /topics/6256245/:** Top issues: 送迎 most common complaint (NPO: "送迎が必要になること" is the top voice from poorer families), coach shortage, schedule clash, unclear venue/safety/assignment info, costs.

**3-day app idea — "地域クラブ相乗り＋指導者マッチ":** Club calendar + carpool matching with liability checklist; separate pool where local adults (retirees, company staff — e.g. Daishin Giken engineers for a robotics club) offer coaching slots.
TSUNAGU: community, school↔local industry, past↔future. Fits Nagasaki judges (local industry involvement).
**Originality 7/10. Feasibility 7/10.**

---

### 8. 夏休みの学童弁当 (after-school care summer lunch boxes)

**Problem:** Gakudō requires a home-made bento every day of summer break; delivered options are expensive or rejected.

**Evidence (GC):**
- 「もうあのお弁当は持っていきたくない」…学童での出来事 — 2026-09-07, 894 — /topics/6256838/
- 〈夏休み“学童弁当”地獄〉 — 2025-07-17, 581 — /topics/5759548/ (read)
- 夏休みの学童、親が直面する「宿題の壁」 — 2025-07-22, 397 — /topics/5764442/
- 学童で、昼食に、買ったおにぎりや惣菜パンは可哀想ですか？ — 2026-03-27, 251 — /topics/6078102/
- 親を悩ませる夏休みのお昼ごはん問題 — 2026-07-31, 157 — /topics/6212801/
- 【学童】お弁当問題【夏休み】 — 2025-06-26, 114 — /topics/5728902/
- Related 小1の壁: 1203 — /topics/6090572/; 794 — /topics/6108558/
- Recurrence: seasonal, every summer.

**From /topics/5759548/:** Delivered bento ¥550–600/meal and tastes bad, containers smell; gakudō lunch ¥7,000/month; no microwave; peer comparison shame. "Retort day" (parents send rice, staff heat retort) liked.

**3-day app idea:** Gakudō group-order from local bento shops / 子ども食堂 with per-child allergy flags and a weekly vote menu; bulk pricing unlocked by group size.
Overlap check: group-ordering mechanic close to "island group-ordering"; food-surplus routing also nearby. Weakish originality.
**Originality 5/10. Feasibility 8/10.** Seasonal; judging in November weakens urgency.

---

### 9. 病児保育の予約 (sick-child daycare booking)

**Problem:** Child gets a fever; parent must phone at dawn for a few slots, after paper pre-registration and a doctor's note.

**Evidence (GC):**
- 子の感染症流行 病児保育も「異常事態」 — 2023-12-20, 649 — /topics/4917503/
- 正社員でいたかったら病児保育を使えという職場 — 2025-09-11, 233 — /topics/5838604/
- 「37.5度の壁」で離職も…病児保育の利用率は約“3割” — 2024-11-29, 152 — /topics/5436096/ (read)
- 病児保育が使えない【相談】 — 2024-04-12, 76 — /topics/5102444/
- Recurrence: 7 threads 2022–2026.

**From /topics/5436096/:** 「事前に紙で利用登録が必要」; 「当日の朝に数席しかないのを朝から電話待機して取り合い」; doctor's note needed. Existing digital booking (あずかるこちゃん) removes paper but not capacity; one area went free and slots vanished.

**3-day app idea:** Parent-to-parent "sick-day backup circle": pre-agreed trusted parents/grandparents/retirees offering backup care slots, plus live vacancy aggregation of local facilities. Safety/liability is a hard problem for a demo.
**Originality 5/10. Feasibility 6/10.**

---

### 10. 学校プリントの管理 (school paper handouts)

**Problem:** Stacks of paper handouts with deadlines, items to bring, and events get lost.

**Evidence (GC):**
- 子のプリントを紛失、保護者会や行事を忘れる… — 2023-06-08, 327 — /topics/4612191/
- 中学生 学校からのプリントを出さない — 2026-07-18, 270 — /topics/6205775/
- 小中学校の96％がいまだにFAX使用 — 2023-12-31, 223 — /topics/4943728/
- 「紙のプリント保管は諦めろ」新小1の親に伝えたいアドバイス — 2023-04-10, 142 — /topics/4532323/
- 学校からの連絡がアプリの方 — 2025-06-27, 127 — /topics/5733529/
- 子供関係の提出書類やプリントチェックを父親がやる家庭… — 2026-03-27, 118 — /topics/6079929/
- 子供のテストやプリントの整理どうしてる？ — 2023-07-31, 94 — /topics/4697146/
- Recurrence: steady, moderate counts.

**3-day app idea:** Photo of a handout → OCR + LLM extracts dates, 持ち物, 提出物 → shared family calendar with both parents' reminders.
Weakness: several apps already do this (unverified which ones are popular in Japan); low originality.
**Originality 4/10. Feasibility 9/10.**

---

### 11. 遠距離介護 (long-distance care of parents) — weak evidence

- 「この年で遠距離介護はきつい」片道8時間 — 2026-08-31, 140 — /topics/6255483/
- 遠距離介護 — 2024-10-24, 149 — /topics/5350194/
- 柴田理恵 94歳の母親の遠距離介護 — 2023-11-25, 130 — /topics/4892396/
- 遠距離介護 — 2023-11-30, 116 — /topics/4900607/
- 一人っ子の遠距離介護問題 — 2026-01-11, 100 — /topics/5990384/
- 親の通院に付き添いしてる方語りませんか？ — 2023-12-02, 141 — /topics/4902698/
- Threads not read in full. Overlaps already-evaluated caregiver QR passport / dementia alerts. **Skip unless a sharp angle appears.**

---

## Checked, little signal

- 草刈り (community weeding duty): no dedicated grievance thread found; one 782-comment thread on early-morning mowing noise (/topics/6195426/) is a newcomer–local clash, not a tool gap.
- 見守り 親 (elderly check-in): no matching titles.
- 粗大ゴミ: 222-comment thread on giving furniture away on Jimoty because bulky-waste booking is a hassle (/topics/5202459/); otherwise thin.
- 長崎 坂: no GC threads. Nagasaki slope evidence only from official/academic PDFs (see #4, #5).

## Sources outside forums

- https://www.city.saitama.lg.jp/001/013/007/p125740_d/fil/r7densikairanbananke-toma.pdf (Saitama e-circular survey)
- https://www.city.iwaki.lg.jp/www/contents/1747102986512/simple/r7jissihoukokussyo.pdf (Iwaki e-circular pilot)
- https://www.city.toyama.lg.jp/_res/projects/default_project/_page_/001/012/291/kairanbanr8.pdf (Toyama subsidy)
- https://www.city.kagoshima.lg.jp/shimin/shiminbunka/chiikishinko/chiikicommunity/documents/dejitarukatuyou1.pdf (Kagoshima)
- https://www.mlit.go.jp/common/001272004.pdf (Nagasaki slope districts)
- https://www.city.nagasaki.lg.jp/sumai/660000/664000/p005617.html (Nagasaki slope living)
- https://www.moneypost.jp/1081458 (circular as welfare check)
- https://note.com/binchan2266/n/na0c0404a40bc, https://note.com/otomo_ni/n/n485225c37d0c (blogs: running e-circulars in practice; paper+LINE double work)
