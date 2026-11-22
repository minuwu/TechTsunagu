# Business pain points — Japanese small shops, restaurants, inns, clinics, salons, direct-selling producers, local services

Research date: 2026-11-22. Method: WebSearch (standard) plus one WebFetch. Read-only.

## How to read this file

- **Fetched** means I read the page myself.
- **Relayed** means the figure came from a search-result summary and I have not opened the source. Treat it as **unverified** until someone opens the URL.
- **Tiers:** A = fetched or official survey. B = several independent owner anecdotes plus one secondary survey. C = vendor content, old data, or one thin source.
- **Gaps in the sources:**
  - 5ch threads (自営業板, 飲食店板) and Reddit r/japanlife returned **no hits** through WebSearch.
  - The 知恵袋 hits were all **customer-side** questions about キャンセル料. They show that the issue keeps coming up, seen from the other side, not owner pain.
  - The real owner and UGC voices found are: note.com posts by owners, Girls Channel topics, and an owner post on X reported by the press.
- **Avoid list.** I did not count these as new: food-surplus routing, lunch pooling, stamp rally, generic AI chatbot, menu translation, SME tool connector, and the shift-cover board.

---

## Tier A

### 1. 無断キャンセル・直前キャンセル(ノーショー)と、キャンセル料を回収できない問題
- **JP:** 予約が「ディナー売上の柱」なのに、無断キャンセルされてもキャンセル料はほぼ取れず泣き寝入り。
- **EN:** Shops depend on bookings for revenue, yet no-shows go unpaid and owners just absorb the loss.

**Evidence**
- **飲食店ドットコム (シンクロ・フード) survey** — fetched. Published 2026-05-20, fieldwork 2026-04-15〜21, n=207, 71.5% single-store owners, 48.3% in Tokyo. https://prtimes.jp/main/html/rd/p/000000728.000001049.html
  - 76% call no-shows and last-minute cancellations an important problem.
  - 48.3% collect too little in fees: 30.3% collect nothing and 18.0% collect less than half. Only 11.2% collect in full.
  - The average cancelled party was 10.5 people (max 60). The largest loss averaged about ¥80,000 (max ¥800,000).
  - Countermeasures: written policy 45.9%, confirmation call 24.2%, SMS/LINE/email 12.9%, **advance payment only 4.6%**, nothing at all 32.5%.
  - 42.5% of shops get half or more of their dinner sales from bookings.
  - Owner quotes:
    - 「小さな店舗の為、キャンセルは死活問題」
    - 「電話に出ないケースがほとんどなので対策が出来ません」
    - 「泣き寝入り」
- **Related research page:** https://www.inshokuten.com/research/result/584
- **Inbound tourists, 木挽町よしや (Ginza wagashi shop):** the owner posted on X that 2–3 tourist groups a day do not pick up their reserved orders. Short-shelf-life どら焼き are thrown away, and the shop said prepayment is too costly to set up.
  - SoraNews24, 2025-11-18: https://soranews24.com/2025/11/18/many-foreign-tourists-not-picking-up-paying-for-their-orders-at-100-year-old-tokyo-sweets-shop/
  - 弁護士ドットコム: https://www.bengo4.com/c_18/n_19641/
  - Unseen Japan: https://unseen-japan.com/japan-restaurant-no-shows-tourists/
- **Girls Channel (UGC):**
  - Fugu restaurant, same-day no-show, the customer refused an ¥80,000 fee (2024-12): https://girlschannel.net/topics/5442277/
  - Bento shop, 200 boxes cancelled without notice and the customer then blocked the shop's calls (2026-06): https://girlschannel.net/topics/6157186/
  - Commenters overwhelmingly say 「全額前払いしてもらわないと」.
- **Salons and 整体:**
  - 弁護士ドットコム: salons often 泣き寝入り because it is hard to name the loss. https://www.bengo4.com/c_18/n_16198/
  - Vendor content describes one-person 整体院 that cannot answer the phone mid-treatment and suffer no-shows (C-grade): https://atsoho.com/blog/manual-therapy-booking-system
- **Customer side on 知恵袋** (recurrence evidence only). Repeated questions of the form "I no-showed, do I have to pay?":
  - https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10324083050
  - https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q13235958918
  - https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11269738836
  - https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q10314194875
- **Unverified (relayed):**
  - METI is said to put no-show losses at about ¥200 billion a year. Forbes JP repeats it, but the original was not checked. https://forbesjapan.com/articles/detail/81993
  - ぐるなび: 66% of shops are reluctant to take bookings from inbound tourists (2017, old): https://honichi.com/news/2017/07/24/tablesolution/
- **Older precedent:** 全日本飲食店協会 ran a phone-number blacklist in 2018. https://www.j-cast.com/kaisha/2018/02/15321348.html?p=all

**Why existing tools fail**
- TableCheck and トレタ do support card guarantees, but they are monthly-fee systems built for mid-to-high-end restaurants.
- Most small shops still take bookings by phone, which leaves no card on file.
- 食べログ and ホットペッパー bookings cannot enforce payment on a phone booking.
- Takeout and pre-order shops (bento, wagashi) are not served by restaurant booking tools at all.

**Who suffers:** single-store restaurants, takeout and pre-order shops, salons, 整体, and small inns.

**Three-day app idea — 「約束チケット」 (Promise Ticket)**
- When the owner takes a phone or walk-in booking, they send the customer a link by SMS or LINE.
- The customer opens it and taps once to confirm, with an optional small deposit through a Stripe test link.
- The link has a sliding reminder (T-24h and T-2h) and a one-tap "release my slot" button.
- A released slot goes to a waitlist of nearby regulars, or the item goes up as "pickup available now."
- **TSUNAGU link:** it turns a one-way phone call into a two-way, mutual commitment between people. The released slot reconnects with another customer.
- **How it differs from TableCheck:** the deposit sits on top of a phone booking, so no booking platform is needed. It covers takeout and pre-order items, not only tables.
- **Originality:** 5/10 (deposit features exist in big systems; small-shop phone-first and pickup-orders is the angle).
- **Feasibility:** 9/10.
- **Demo data** (synthetic, must be labelled fictional):
  - A fictional Nagasaki izakaya plus a wagashi shop, each with 30 bookings drawn from the survey's distribution (party size around 10, roughly 1 in 10 no-showing).
  - Run a "before vs after" simulation.

### 2. 後継者不在・黒字廃業 (succession gap, closures)
- **JP:** 黒字でも後継ぎがおらず店をたたむ。常連・技・レシピ・取引先が消える。
- **EN:** Profitable shops close because nobody takes over, and the regulars, skills, recipes, and supplier ties disappear with them.

**Evidence** (all relayed from search summaries of TDB data; the numbers are consistent across outlets)
- 帝国データバンク counted 67,949 休廃業・解散 in 2025. 49.1% had been profitable in their last accounts, the first time that share fell below half.
- The 後継者不在率 for 2025 was 50.1% overall and 57.3% for small firms.
  - ITmedia, 2026-01: https://www.itmedia.co.jp/business/spv/2601/07/news040.html
  - zaikei: https://www.zaikei.co.jp/releases/3194399/
- Kumamoto saw a record 876 closures in 2025, described as a 「静かな退場」: https://kumanichi.com/articles/1970109
- **Nagasaki:** 後継者不在率 is 59.2%, above both the Kyushu (50.6%) and national averages. **Unverified** — the figure comes from an M&A vendor citing TDB: https://masouken.com/%E9%95%B7%E5%B4%8E%E3%81%AEM&A%E3%81%A8%E4%BA%8B%E6%A5%AD%E6%89%BF%E7%B6%99
- 壱岐: a company takes over operation of inns and restaurants from owners who have no successor (ryukyushimpo/PR TIMES): https://ryukyushimpo.jp/prtimes/entry-2599215.html
- **Gap:** I found no forum threads from owners on this topic. All the evidence is statistical.

**Why existing tools fail:** M&A platforms (TRANBI, BATONZ) sell the company. They do nothing to keep the know-how or the regulars once a shop closes or changes hands.

**Who suffers:** elderly owners, regulars, local suppliers, and 商店街.

**Three-day app idea — 「味のバトン」 (Shop Legacy Passport)**
- The owner records recipes, supplier contacts, regulars' preferences, and seasonal routines through short voice and photo prompts. The result is a structured handover file.
- A matching board lists closing shops alongside would-be successors, such as UI-turn applicants and 地域おこし協力隊 members.
- **TSUNAGU link:** past↔future and people↔people.
- **Originality:** 7/10.
- **Feasibility:** 7/10. This is a demo of the handover data model, not real M&A.
- **Demo data:** one fictional Nagasaki ちゃんぽん shop with about 15 knowledge cards.

### 3. キャッシュレス手数料の負担 (cashless fees)
- **JP:** 3%前後の決済手数料が薄利の小店を圧迫、導入をためらう・やめる店も。
- **EN:** Card fees of about 3% squeeze thin margins. Some shops refuse cashless payments or drop them.

**Evidence**
- JFC (日本政策金融公庫) research paper, 2025-02: some businesses drop cashless payments because the fees feel too heavy. https://www.jfc.go.jp/n/findings/pdf/ronbun2502_02.pdf
- METI study-group report: the cashless ratio reached 42.8% in 2024, but terminal and fee costs and slow SME uptake are now flagged as problems. https://www.meti.go.jp/shingikai/mono_info_service/cashless_promotion/pdf/003_04_00.pdf
- **Relayed and unverified:**
  - StorePro survey (2025-08, n=1,007 owners without cashless): 77.2% said 「手数料がもったいない」. https://www.commercepick.com/archives/72841
  - GRADMIN survey (2025-09, n=125): about 20% said card fees squeeze profits. https://zaikei.co.jp/releases/3096749/

**Why existing tools fail:** Square and Airペイ fees are around 2.5–3.25%. Free QR promotions have ended. Nothing gives small shops a cheaper channel.

**Three-day app idea:** a 地域 prepaid wallet or 商店街 coin that settles once a month (a points ledger with no per-transaction card fee).
- **Caveat:** this runs into the regulation-heavy space of the 資金決済法. A demo is fine, but the idea is not original (many 地域通貨 schemes exist: さるぼぼコイン, etc.).
- **Originality:** 3/10.
- **Feasibility:** 8/10 for a demo.
- **Recommendation:** use this as context only, not as the main pick.

---

## Tier B

### 4. 予約・問い合わせ電話が仕込み/施術/診療を中断する (phone interruption)
- **JP:** 一人・少人数で回す店や院で、作業中の電話が流れを止め、取り逃しも出る。
- **EN:** In one- or two-person shops and clinics, the phone breaks up the prep, treatment, or consultation, and calls that go unanswered are lost bookings.

**Evidence**
- note.com owner and staff posts:
  - 「今日も「全部やる！」と宣言して5分後に電話が鳴る件」: https://note.com/wisearrowltd/n/n18f17d6a2642
  - 「小さな飲食店に電話は必要か？」 (wine bar owner): https://note.com/marino_wine/n/n8b9e64feb519
  - 「「電話が鳴らない店」は、予約導線がうまい」: https://note.com/_novita_/n/nf020bca2fe3e
  - 「これから電話予約を止めるための4ステップ」 (one-person salon): https://note.com/shimada_atsushi/n/ne46b6609f80b
  - A counter-voice from a rural one-person bar that **gained 1.5×** bookings by publishing its phone number: https://note.com/shikatadatsuma/n/n0fb144e26804
- 飲食店ドットコム, 2024-02, n=375: the phone is still the most common booking channel. https://www.inshokuten.com/foodist/article/7376/ (relayed)
- Clinics: one-person front desks where calls interrupt consultations (vendor content only, C-grade): https://www.oflight.co.jp/ja/columns/clinic-reservation-system-2026
- **Warning sign:** AI phone-booking services have already backfired. Shops reported 「誤った情報が掲載」 and 「30分ずっと電話が鳴る」 (ITmedia, 2023-05): https://www.itmedia.co.jp/business/spv/2305/18/news165_2.html
  - **So do not pitch an AI phone bot.** It is on the avoid list, and its failure is already on record.

**Why existing tools fail:** online booking sits beside the phone, not in place of it. Older customers and locals keep calling, and owners cannot move them over.

**Three-day app idea:** fold this into #1 as the same flow. A "busy now" mode replies to a missed call by SMS with a link showing real-time free slots. The owner sets the state with one tap: 仕込み中, 満席, or あと30分.
- **Originality:** 5/10.
- **Feasibility:** 8/10. A Twilio trial or a simulated SMS is enough.

### 5. 小規模旅館・民宿の OTA 依存 (OTA dependence)
- **JP:** 楽天・じゃらん・Booking 経由が大半で手数料10〜25%、リピーターも OTA 経由で戻ってくる。
- **EN:** Most bookings come through OTAs at 10–25% commission, and even repeat guests come back through the OTA.

**Evidence**
- note.com posts by owners:
  - 「OTAだけに頼らない宿づくりを考える」 (宿主): https://note.com/shin_iwate/n/nedb6398b4624
  - 「OTA依存から脱却。気づけば自社予約が一番に」 (small-inn owner): https://note.com/houvuntree/n/n4bb7e17408eb
  - 民泊 operator moving to direct Stripe payments: https://note.com/yuiiiiiiino/n/ncbb84675efd2
  - A writer notes that 10% is not small for a little inn: https://note.com/solo_onsen/n/nece55eb54afa
- 観光庁 study material (2025-02): regional OTAs have low commission but struggle to attract guests, and moving the second and later stays off the OTA is the stated way out. https://www.mlit.go.jp/kankocho/content/001868679.pdf (relayed)
- The 15–25% commission figures come from vendor blogs (relayed).

**Why existing tools fail:**
- Booking engines such as Beds24 need a website and enough traffic.
- An official LINE account requires the guest to add the inn.
- No tool captures a guest *at checkout* and carries them into a direct rebooking.

**Three-day app idea — 「また来てね」 Return Pass**
- A QR code in the room or at checkout issues a 「次回直予約特典」 pass.
- The pass links into a lightweight direct-booking page.
- Inns in the same area cross-refer guests, so a fully booked inn hands the guest to a neighbour instead of losing them to an OTA.
- **TSUNAGU link:** community (inns cooperating) and past↔future (the guest coming back).
- **Originality:** 6/10.
- **Feasibility:** 8/10.
- **Demo data:** three fictional 五島/雲仙 inns.

### 6. 仕入れ高騰・価格転嫁 (rising costs, passing them on to customers)
- **JP:** 食材・光熱・最低賃金が上がり続け、値上げの根拠を示せない。
- **EN:** Ingredient, utility, and minimum-wage costs keep rising, and owners cannot back a price rise with data.

**Evidence (relayed)**
- 飲食店ドットコム, 2025-10: about 70% faced cost increases of more than 10%. https://www.inshokuten.com/foodist/article/8054/
- 2025 minimum-wage survey (n=299): 35.8% said the rise was higher than expected, and about 16% said it threatens survival. https://www.oricon.co.jp/pressrelease/3004517/
- Infomart survey (n=309), set against the 食料システム法 taking effect in 2026-04: under 30% of restaurants can show objective grounds when negotiating prices with suppliers, and only 25.6% order ingredients digitally. https://ryukyushimpo.jp/prtimes/entry-5278518.html
  - A "28.8%" figure appeared in one summary but could **not** be traced. Do not use it.
- MAFF 食品等取引実態調査: https://www.maff.go.jp/j/press/shokuhin/kikaku/260331.html

**Why existing tools fail:** POS systems (スマレジ, Airレジ) track sales, not the cost of each menu item over time. Spreadsheets are what shops actually use.

**Three-day app idea:** a "menu cost tracker." Photograph a supplier invoice to log prices, track recipe cost per dish, and show a margin-erosion alert. It produces a 「値上げ説明カード」 for customers explaining why a price went up.
- **TSUNAGU link:** data↔data and people↔people (being open with customers).
- **Originality:** 5/10.
- **Feasibility:** 7/10, since OCR is the risky part.

---

## Tier C (thin evidence)

### 7. 悪質口コミ・口コミを使った脅し (review harassment, カスハラ)
- **JP:** 星1や虚偽口コミを店が消せない、「低評価つけるぞ」と脅される。
- **EN:** Shops cannot remove 1-star or false reviews, and some customers threaten bad reviews to get their way.

**Evidence**
- TKC (2022): owners cannot delete Google reviews themselves, and companies that promise deletion are probably scams. https://tkc.jp/cc/senkei/202208_consult01
- 飲食店ドットコム (2023, n=360): 71.7% have had trouble with customers (not specific to reviews). https://www.inshokuten.com/research/magazine/article/28
- 弁護士ドットコム: an owner who feared being stabbed got no police report accepted and closed the shop. https://www.bengo4.com/c_1009/n_17287/
- Laws:
  - The 東京都カスハラ条例 took effect in 2025-04.
  - National employer duties take effect in 2026-10 (relayed, unverified).

**Why existing tools fail:** Google Business Profile only offers "reply" and "report."

**App idea:** "verified-visit reviews," where only receipt- or QR-verified customers can post feedback directly to the owner.
- **Caveat:** this does not fix Google itself.
- **Originality:** 4/10.
- **Feasibility:** 8/10.

### 8. 産直(農家・漁師)の売り切り・連絡の手間 (direct-selling producers)
- **JP:** 獲れ高・収穫が日々変わり、LINEやSNSで個別に注文受付・取り置き・売れ残り連絡をさばくのが重い。
- **EN:** The catch and harvest change every day, and handling orders, holds, and leftover notices one by one over LINE or SNS is heavy work.

**Evidence (relayed, mostly vendor or government material)**
- A Wakayama prefecture document describes using LINE オープンチャット to share leftover produce: https://www.pref.wakayama.lg.jp/prefg/020100/data/competition/2021_5_d/fil/umemikan.pdf
- A vendor article on LINE direct sales for farms (holding limited items for customers): https://aurant-technologies.com/blog/line-official-account-for-farm-direct/
- A 対馬 fisher grew sales through SNS. Under the traditional route, the fisher keeps only 20–30% of the retail price: https://atsoho.com/blog/fisherman-fresh-fish-direct-sales
- Shizuoka fisheries station on how competitive online sales are: https://fish-exp.pref.shizuoka.jp/izu/0006/364/364-1.pdf
- **Gap:** I found no forum voices from producers.

**Why existing tools fail:**
- BASE and ポケマル assume a fixed stock list.
- The real problem is variable daily supply matched to a standing group of buyers.

**App idea — 「今日の水揚げ」:**
- The fisher or farmer posts today's catch from a photo in about 30 seconds.
- Subscribed local restaurants and households claim portions.
- The leftover is automatically offered to the next ring of buyers.
- **TSUNAGU link:** people↔things.
- **Overlap:** it partly overlaps **food-surplus routing** (avoid list). It is only distinct if framed as *pre-sale of a variable catch*, not surplus.
- **Originality:** 5/10.
- **Feasibility:** 8/10.

### 9. Google マップ上の営業時間・臨時休業が不正確 (local shop invisibility, stale information)
**Evidence**
- An SEO firm explains that a shop's GBP listing can be changed without the owner's knowledge (relayed): https://www.sungrove.co.jp/google-business-profile-katteni/
- 訪日ラボ, 2025-06, on business hours: https://honichi.com/news/2025/06/23/businesshours/
- **Thin:** I found no owner forum threads.

**App idea:** a one-tap 「今日の営業状況」 broadcast to GBP, Instagram, and LINE together.
- **Overlap:** this collides with the **SME tool connector** on the avoid list. Skip it.

---

## Negative findings (also worth knowing)

- **インボイス:** B2C restaurants and retailers are barely affected, because consumers do not ask for invoices. The pain sits with B2B sole traders. A weak fit for this segment. Sources:
  - https://biz.moneyforward.com/invoice/basic/invoice-restaurant
  - https://www.jftc.go.jp/dk/guideline/unyoukijun/invoice_qanda.html
- **長崎 商店街 decline:** the search turned up only subsidy programmes (empty-shop subsidies of ¥1–2M, a prefectural 80% subsidy rate), not evidence of decline or owner voices. Sources:
  - https://www.city.nagasaki.lg.jp/uploaded/attachment/41349.pdf
  - https://www.pref.nagasaki.lg.jp/shared/uploads/2025/10/1760926010-1.pdf
- **人手不足, タイミー:**
  - 飲食店ドットコム (2023-06): 40.7% are short of staff.
  - Spot-work platforms carry fees and legal risk. A 2025-12 ruling held the shop liable for wages once a match was made. https://www.inshokuten.com/foodist/article/8168/ (relayed)
  - This is the **same space as the shift-cover board** already found, so it is not counted as new.
- **Inbound allergen and halal:**
  - JNTO says shops "feel the need but don't know where to start." 2025 arrivals reached 42.68M. https://www.jnto.go.jp/projects/regional-support/resources/3950.html
  - I found no quantitative survey of owners. Any app here must be **ingredient-level declarations the owner verifies**, not translation (avoid list).
- **Local service businesses** (handymen, 工務店, 整体): only one vendor source, on 整体 phone and no-show trouble (folded into #1 and #4). Otherwise **not covered**.
- **5ch and Reddit:** no usable threads were found through WebSearch. Other notes files in this folder (`5ch_matome.md`, `japanese_qa_forums.md`) may cover them.

## Recommendation for the team
- **Strongest combination:** #1 + #4 as one app, 「約束チケット」. Turn a phone or walk-in booking into a two-way commitment (confirm, deposit, release), with released slots and orders routed to waitlisted locals.
  - It has the best evidence (a fetched survey, an inbound wagashi case, UGC).
  - It can be built in 3 days, and the demo is clear: before/after no-show loss on synthetic data.
- **Most original but evidence is statistical only:** #2, 「味のバトン」 (past↔future fits the theme perfectly).
- **Good Nagasaki angle:** #5 Return Pass for 五島/雲仙 inns.
