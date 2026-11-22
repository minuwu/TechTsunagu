# Forum pain points: 4chan archives + Nagasaki/Kyushu local sources

Researched 2026-11-22. Read-only. For TSUNAGU hackathon (build Nov 22-24 2026).

## Method and source access (read this first)

| Source | Access result |
|---|---|
| desuarchive.org (/int/ etc.) | HTTP 403 on search URL. Not searchable. |
| archived.moe (/int/) | HTTP 403. Not searchable. |
| archive.4plebs.org (/trv/) | HTTP 403. Not searchable. |
| WebSearch `site:` filters on the above | Returned zero archive hits (domains not indexed by the search tool). |
| warosu.org (/jp/, /biz/) | Worked. Search pages fetched. Most hits are pre-2023. |
| Yahoo!知恵袋 search | Worked (chiebukuro.yahoo.co.jp/search). Best forum source for Nagasaki. |
| 長崎市「市民の声」(city.nagasaki.lg.jp/teian/) | Worked. Resident complaints + city replies, 2025-2026. Strongest recent primary source. |
| ジモティー Nagasaki (jmty.jp/nagasaki/com) | Worked. Community board snapshot Nov 2026. |
| reddit (r/nagasaki, r/fukuoka, r/japanlife) | Blocked ("unable to fetch from www.reddit.com"). Search snippets gave no threads. GAP. |
| 5ch (find.5ch.net -> find.5ch.io) | Search returned only the service page, no threads. GAP. |
| 長崎新聞 reader comments | Not attempted (time). GAP. |
| note / personal blogs "長崎 住んでみて 不便" | Searches returned only city pages and a travelogue, no note posts. GAP. |

Caveats:
- Every WebFetch output was produced by a summarizing model. Quotes marked "(fetch summary)" are near-verbatim but not checked against raw HTML. The one source read in full raw form is the 九州運輸局 PDF on 西肥自動車 route abolition (marked VERIFIED).
- Part A (4chan) is corroboration only. It is not primary evidence and is mostly generic Japan-wide griping, not Nagasaki-specific.
- Online evidence for shipyard/fishery/care foreign-worker daily problems is thin. Mostly government documents. Not inflated here.

---

## Part A: 4chan (warosu /jp/, /biz/ only)

Search URLs (each a results page; individual post numbers listed):

1. Rural car dependence / no shops: https://warosu.org/jp/?task=search2&search_text=inaka+car
   - No.29928385 (2021-12-29): "He walked two hours TO a station." / "Owning a car is an option, especially if you live in chou inaka..." (fetch summary)
   - No.27674161 (2020-10-14): "...have to walk 50 minutes to the closest konbini." (fetch summary)
   - No.20375028 (2019, date malformed): "no train or bus service. You'll have to have a car." (fetch summary)
   - No.19199838 (2018-06-11): "you would need a car or a scooter at the very least to commute to buy provisions and food from the nearest town." (fetch summary)
2. Garbage sorting burden: https://warosu.org/jp/?task=search2&search_text=garbage+sorting
   - No.49702470 (2025-07-12): "You love sorting your garbage 4 times a week" (fetch summary)
   - No.25949907 (2020-09-03): "no landlords to get mad at you for sorting your garbage wrong." (fetch summary)
   - No.20336200 (2019-12-30): "sorting your garbage is really complicated" (fetch summary)
3. Guarantor / housing for foreigners: https://warosu.org/jp/?task=search2&search_text=guarantor+apartment
   - No.8031673 (2011-10-24): "Pay with your blood to get a guarantor, insurance..." (old)
   - No.45147171 (2024-12-04), No.45166490 (2024-12-06): replies disputing difficulty (company can be guarantor). Mixed signal.
4. Nagasaki-specific: https://warosu.org/jp/?task=search2&search_text=nagasaki
   - No.51107919 (2026-07-13): "In Nagasaki where there's only one place to play, Tokyo has so many options." (fetch summary; paraphrase of a vtuber, weak)
   - No.51269343 (2026-09-19): "not meant be stuck on an island in nagasaki." (fetch summary, weak)
5. /biz/ Japan jobs: https://warosu.org/biz/?task=search2&search_text=japan+job+salary+foreigner -> no relevant complaints.
6. Bureaucracy: https://warosu.org/jp/?task=search2&search_text=city+hall+pension -> "No posts found".

Takeaway: 4chan confirms only generic themes (car dependence in inaka, garbage-sorting stress, guarantor friction). Nothing actionable that is Nagasaki-specific.

---

## Part B: Nagasaki / Kyushu pain points (ranked by evidence strength)

### 1. Bus routes dying from driver shortage; residents left to piece together replacement modes

**Problem:** Rural/suburban bus lines in Nagasaki Prefecture are being abolished at ~6 months notice; replacements are a patchwork (reservation shared taxi, public rideshare, community bus, MR rail transfer) that residents and students must figure out themselves.

**Evidence:**
- VERIFIED (read raw PDF): 九州運輸局「一般乗合旅客自動車運送事業の路線一部廃止届出に係る意見聴取結果」. 西肥自動車 filed abolition of **14 routes (sections)** on 2024-09-18 to the prefectural council; hearing 2025-01-17; abolition 2025-04-01. https://wwwtb.mlit.go.jp/kyushu/content/000345816.pdf
  - 佐世保市: route abolished faster than expected "急速な運転士不足の進展により"; municipal cost of replacements (デマンドタクシー、ライドシェア) "増大の一途". Council member: next generation feels anxious about living in the area. 5 districts / 7 routes switched to 区域運行型予約制乗合タクシー.
  - 平戸市: 7 北松農業高校 students affected; some would need to leave home ~1 hour earlier; transfer fare higher. No new replacement.
  - 川棚町/波佐見町: route served 4 schools incl. 2 special-needs schools; "6ヶ月という短い期間で代替手段の確保を図ることは…非常に難しい"; replaced by 公共ライドシェア / 自家用有償旅客運送.
- 長崎市 市民の声: 千々線 (Nagasaki Bus, Nomo peninsula east coast) abolition. Resident (Sep 2026) calls it a "生命線" for 通院・通勤・通学・買い物; proposes 3 runs/day, minibus, community bus; fears villages become "廃墟村". City (2026-10-14) will first assess usage and mobility needs, then work with residents on alternatives; no commitment to keep the line. https://www.city.nagasaki.lg.jp/teian/93232.html (fetch summary)
- 長崎市 市民の声: 三重町 (Aug 2026, resident 60s) "町内に店舗が1店もない", few vending machines, limited transport. City (2026-10-05): shared taxis run in 5 districts but driver shortage threatens them; Mie demand too small for shared taxi; points residents to community-led options (volunteer transport, public rideshare) and a workshop「『ついで』のチカラ」. https://www.city.nagasaki.lg.jp/teian/92836.html (fetch summary)
- 知恵袋 2026-10-22: Sunday evening bus cut (中浦→新地中華街); answer: "減便ならまだ良い方で、路線廃止も相次いでますので。" https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q12332283692
- 知恵袋 2023-03-23: 長崎県営バス timetable revision for Apr 1 announced only the day before. https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q12277414657
- 知恵袋 2024-04-29: large spring 2024 Nagasaki Bus service cut. https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10297273150
- 知恵袋 2022-06-03: 県営バス major cut, fuel + chronic staff shortage. https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11262768346
- UNVERIFIED (search summary only, PDF not read): Nagasaki Bus withdrew from 16 routes on 2024-04-01 and cut 25 weekday shifts. Candidate sources: https://www.city.nagasaki.lg.jp/uploaded/attachment/36501.pdf , https://www.mlit.go.jp/sogoseisaku/transport/content/001595740.pdf

**Recurrence:** High. Independent sources 2022-2026, multiple municipalities, ongoing Oct 2026.

**Current workaround / why it fails:** Each town picks a different replacement (reservation taxi, public rideshare, community bus, rail transfer). Information is scattered across town sites and paper notices; reservation-based modes need phone calls; ~6 months notice is too short (川棚/波佐見). Usage data the city says it needs before acting ("assess usage and needs") is collected ad hoc.

**Who suffers:** High-school and special-needs students, elderly without cars, hospital commuters, peninsula/outlying villages; municipalities paying rising replacement costs.

**3-day app idea: "Route Pledge / ノリツナギ"**
- Residents on a threatened route log the trips they would actually take (who, when, purpose: school/hospital/shopping) as lightweight "ride pledges", by tap or by a family member on behalf of an elder.
- Output for the town: a demand heatmap by hour and stop, to size a reservation taxi or rideshare; output for residents: one page per abolished route showing "how to make your old trip now" (which replacement mode, how to book, fare).
- TSUNAGU fit: data↔data (scattered resident needs to planning data), past↔future (old route to new mode), people↔community.
- Why Nagasaki judges care: directly feeds the step the city itself named in its 千々線 reply; prefecture is the coordinating body for 西肥's 14 routes. Industrial judges: same pattern as workforce shortage.
- Collision check vs. evaluated list: not civic issue reporting (it is demand aggregation, not defect reports); not freight return-load matching; not island group ordering. Avoid drifting into generic ride-matching (overlaps foreign-worker↔elderly mutual help).
- Originality 7/10, Feasibility 8/10 (forms + map + charts; seed with real 西肥 route list).

### 2. Garbage sorting is complex; help channels are limited in hours/format

**Problem:** Residents (and especially newcomers/foreigners) struggle with sorting rules and the 8 a.m. put-out deadline; questions can only be answered by phone in limited hours.

**Evidence:**
- 長崎市 市民の声 (Oct 2026): "分別が複雑で" ; resident asks "ゴミ分別AIの導入をお願いします"; can only call after work. City (2026-10-26): "現時点ではAIの導入予定はございません"; points to PDF guide, 50音 list, official LINE search, あじさいコール 095-822-8888 (weekdays to 19:00). https://www.city.nagasaki.lg.jp/teian/94192.html (fetch summary)
- 長崎市 市民の声 (Aug 2026): bags left uncollected when trucks came before 8 a.m.; erodes neighbour trust. City promised unannounced inspections. https://www.city.nagasaki.lg.jp/teian/91349.html (fetch summary)
- 4chan /jp/ garbage posts 2019-2025 (Part A item 2).
- Slope context: city says garbage collection on slopes uses sledges under baskets and costs "数億円" more than similar cities (old document; date unverified). https://www.city.nagasaki.lg.jp/sumai/660000/664000/p005617.html

**Workaround / why it fails:** City LINE keyword search and PDFs exist; they require knowing the Japanese item name. No photo-based or multilingual lookup.

**Who suffers:** Newcomers, foreign residents and interns in company dorms, busy workers.

**App idea:** Photo-to-category sorter tuned to Nagasaki city's actual list, multilingual (incl. Bengali/Vietnamese/Indonesian), with a "my station, next pickup" reminder.
- TSUNAGU: people↔things, data↔data.
- Why judges care: a resident literally asked the city for this in Oct 2026 and the city said no plans.
- Originality 4/10 (sorting apps exist in other cities; LINE search exists), Feasibility 9/10.

### 3. Newcomer isolation (people who just moved to Nagasaki/Sasebo)

**Problem:** People who move to Nagasaki for work or marriage report having no local friends; they post on classifieds boards to find any connection.

**Evidence (ジモティー Nagasaki community board, Nov 2026, page 1 snapshot):** https://jmty.jp/nagasaki/com
- "長崎に引っ越して来た方" (Nov 14) https://jmty.jp/nagasaki/com-fri/article-1r75dp
- "長崎他県人会！今日雑談しませんか？" (Nov 14) https://jmty.jp/nagasaki/com-fri/article-1razah
- "ゆっくり話しながらご飯食べませんか！？" (Nov 21) https://jmty.jp/nagasaki/com-fri/article-1tcyo2
- "新しい友達を作りたい！" Sasebo newcomer (Nov 21) https://jmty.jp/nagasaki/com-fri/article-1tdh39
- "20代後半〜30代のママ友を募集しています" moved after marriage (Nov 15) https://jmty.jp/nagasaki/com-mot/article-1sz99p
- "仲良くしてください" twin mother (Nov 20) https://jmty.jp/nagasaki/com-mot/article-1r8pu5
- "サッカー友達" with foreign participants (Nov 13) https://jmty.jp/nagasaki/com-fri/article-1t6qth
- Related: 自治会 face "担い手の不足、役員の高齢化"; city trialled digital circulation board app "いちのいち" from Sep 2024 with 24 associations (so a digital 回覧板 idea is NOT novel). https://www.city.nagasaki.lg.jp/uploaded/attachment/10215.pdf (fetch summary; doc date unverified)

**Workaround / why it fails:** Generic classifieds and meetup apps; no tie to the neighbourhood/自治会 that actually needs new members.

**App idea:** Newcomer-to-自治会 bridge: new arrivals get matched to small neighbourhood tasks or events (festival prep, slope cleanup) as a low-pressure entry; 自治会 gets new hands.
- Must be sharply differentiated from "foreign-worker↔elderly mutual help" (already evaluated). Angle: domestic + foreign newcomers, onboarding into the 自治会 itself.
- Originality 5/10, Feasibility 8/10. Evidence is a single-week snapshot; moderate strength.

### 4. Slope and stair living (坂・階段)

**Problem:** Hillside homes are unreachable by car; groceries, garbage, moving furniture and outings are physically hard; young people leave, elders remain.

**Evidence:**
- 長崎市 official: narrow stair paths "縦横無尽"; slope living drives outflow of the young and ageing ("若い世代を中心に人口の流出"). https://www.city.nagasaki.lg.jp/sumai/660000/664000/p005617.html (date unverified)
- City 斜面地 transport support capped at 16 uses/month; ふれあい訪問収集 door-to-door garbage collection for those who cannot reach stations (doc dates unverified). https://www.city.nagasaki.lg.jp/uploaded/attachment/66593.pdf etc.
- 知恵袋 2024-05-07: visitor calls steep narrow slopes "不便極まりない場所". https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q14297667514
- 知恵袋 2026-02-12: student moving near 長崎大学 asks whether a bicycle is usable given "階段が多いイメージ". https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q12325274062
- 知恵袋 2015-09-21: stair-top flats near 長崎大学 from ¥20,000 ("激坂・階段上の物件は2万円～"). https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13150572563 (old)
- 知恵袋 2017-07-29: city very inconvenient without a motorbike. https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11177300901 (old)
- 水の浦 district: residents who gave up cars cannot shop alone; social-welfare corporation shuttles them in spare van time. https://www.city.nagasaki.lg.jp/uploaded/attachment/46264.pdf (fetch summary, date unverified)
- 2025 urban-planning paper on 斜面地モビリティ: https://pub-files.atlas.jp/fs/public/annuconfcpij2025/ver_15/abstract/ja/30913.pdf

**Workaround / why it fails:** とくし丸 mobile supermarket, city delivery list, monthly-capped transport support, door-to-door garbage for qualifying elders. Coverage is partial and eligibility-gated; nothing helps non-elderly residents or one-off heavy tasks.

**App idea:** "Stair-aware" micro-errand board: maps stair counts/steps from the nearest road for each home; students living on the slopes (cheap stair-top flats) take paid micro-jobs carrying groceries or oversized garbage from the road point to the door.
- TSUNAGU: people↔people (students↔elders), people↔things (stair map).
- Collision risk: overlaps foreign-worker↔elderly mutual help and food/goods delivery ideas; the novel bit is the stair/step dataset (data). Originality 5/10, Feasibility 7/10.

### 5. Island access to hospitals (五島, 小値賀, 宇久 etc.) — government docs only

**Problem:** Small islands depend on one ferry per day or the afternoon boat; specimens, patients and supplies move on ferry schedules.
**Evidence (no forum discussion found):**
- National doc: on small islands, blood drawn in the morning can only reach the 福江島 lab by the afternoon ferry ("定期船の午後便しかなく"). https://www.mlit.go.jp/kokudoseisaku/chirit/content/14nagasaki_gotoushi_medical.pdf
- Prefecture: 神浦港～寺島港～柳港 route is the only link for 通院・通勤・生活物資. https://www.pref.nagasaki.jp/shared/uploads/2024/03/1711701421.pdf (approx; fetch summary)
- Drone medical delivery trials: Shin-Kamigoto Level 4 approval Nov 2025 https://www.lnews.jp/2025/11/r1120701.html ; Sasebo fish drone Mar 2025 https://www.logi-today.com/739232
- 知恵袋 search "長崎 離島 通院" returned nothing relevant: https://chiebukuro.yahoo.co.jp/search?p=長崎+離島+通院
**Assessment:** real but weak online signal; also near "island group-ordering" (evaluated). Originality 5, Feasibility 6. Skip unless pairing with ferry-cancellation-aware hospital appointment rebooking.

### 6. Foreign workers / technical interns in Kyushu (shipyards, fisheries, care) — thin online evidence

**Evidence:**
- 長崎労働局: foreign workers 6,951 at end Oct 2022 (+20.2%, record); Vietnam largest group (2,051 in 2019). https://jsite.mhlw.go.jp/nagasaki-roudoukyoku/content/contents/gaikokujin-koyo-23013110.pdf
- Prefecture: 特定技能 shipbuilding 191 people (Oct 2022), second after agriculture 335. https://www.pref.nagasaki.jp/shared/uploads/2023/05/1684473494.pdf (fetch summary)
- Fisheries: Indonesian 特定技能 dispatched to 雲仙市 anchovy net fishing; work starts ~01:30 summer; 170-180 h/month busy season. 南串山 boat owner arranged calligraphy-class exchanges so interns could fit into the community. https://www.pref.nagasaki.jp/shared/uploads/2024/02/1708488928.pdf (fetch summary)
- Prefectural council doc: "ことばの壁 日本語の問題（日常会話〇、読み書き×）". https://www.pref.nagasaki.jp/gikai/houkatsu/img07/07-1.pdf (fetch summary)
- Care: prefecture subsidizes housing for firms hiring foreign care workers (1/2, up to ¥200k) and offers 10-min on-demand Japanese videos. https://www.pref.nagasaki.lg.jp/shared/uploads/2025/07/1751343674.pdf (fetch summary)
- 知恵袋 2013-04-04 (OLD): Bangladeshi woman at a Nagasaki textile firm reportedly paid ¥10,000 for ~400 h/month. https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10105112605
- No 2024-2026 forum threads found on Nagasaki shipyard/fishery interns' daily problems (searches in EN and JP).
**Assessment:** "can talk, can't read" plus night-shift fisheries schedules suggests an idea: read-aloud + translate for workplace/town documents (safety notices, payslips, ward notices). But overlaps "foreign-resident admin checklist" (evaluated). Originality 4-5, Feasibility 8. Judge fit high (Bangladeshi IT talent pipeline) but evidence weak.

### 7. 西九州新幹線 武雄温泉 face-to-face transfer — noisy, not app-able

Many 知恵袋 threads 2022-2026 call it 不便 (e.g., https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11267070991 2022-08-27, 15 answers; https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11302064301 2024-08-04; https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10325080170 2026-02-06). Political infrastructure problem; no 3-day app lever. Skip.

### Minor: tourism traffic vs residents
- 市民の声 (Oct 2026): tour buses released in a row from 松が枝町 lot block the national road near Glover Garden; city to revise guidance (2026-11-16). https://www.city.nagasaki.lg.jp/teian/95855.html (fetch summary). Single report; tourism stamp rally already evaluated, but resident-side congestion is a different angle. Weak.

---

## Other URLs consulted
- https://chiebukuro.yahoo.co.jp/search?p=長崎+坂+不便&flg=3
- https://chiebukuro.yahoo.co.jp/search?p=長崎+階段+引越し&flg=3
- https://chiebukuro.yahoo.co.jp/search?p=長崎+バス+減便&flg=3
- https://chiebukuro.yahoo.co.jp/search?p=長崎+仕事+ない+若者&flg=3 (youth outflow, low wages; all threads 2010-2015 except q11312561436 2025-03-20 asking about job availability for a migrant)
- https://chiebukuro.yahoo.co.jp/search?p=西九州新幹線+対面乗換+不便&flg=3
- https://chiebukuro.yahoo.co.jp/search?p=長崎+外国人+実習生&flg=3
- https://www.city.nagasaki.lg.jp/teian/ (市民の声 listing, 85 pages)
- https://trafficnews.jp/post/121872/2 (Nagasaki buses on steep narrow roads)
- https://www.businessinsider.jp/article/193327/ (五島 migrants: rent ¥50-60k, dense neighbour relations; ~2019)
- https://meetsmore.com/services/furniture-luggage-carrier/nagasaki (handyman rates ¥3,000-5,000/h)
