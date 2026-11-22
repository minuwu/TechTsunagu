# Analog Pain Points in Japan: Hackathon MVP Candidates

Research date: 2026-11-22. Scope: analog workflows in Japan (paper, hanko, FAX, in-person visits, phone, cash, handwritten forms, 回覧板) that a small team could digitize as a web-app MVP in 1–2 days.

## How to read this

- Every number links inline to its source. Sources are Japanese government or official first-party documents unless the text says "(secondary)".
- Survey dates matter. Each figure carries its survey date or reference date, because several datasets are one to three years old.
- "Pilot" means 実証 (a trial) and not national operation.
- Some areas overlap with the existing plan [docs/superpowers/plans/2026-11-22-mimamori-pass-single-qr.md](../superpowers/plans/2026-11-22-mimamori-pass-single-qr.md) (Mimamori Pass: a cross-institution medical/care QR passport). Those areas are tagged **[overlaps Mimamori Pass]**. The ranked shortlist includes options that do not overlap.

---

## 1. Cross-cutting context

### 1.1 Elderly digital divide (constrains every MVP)

- Internet use in 2024 (survey date: end of August 2024) was 90.4% for ages 60–69, 69.8% for ages 70–79, and 33.1% for ages 80 and over. The 80+ rate fell from 36.4% the year before ([総務省 令和6年通信利用動向調査, p.3](https://www.soumu.go.jp/johotsusintokei/statistics/data/250530_1.pdf)).
- Smartphone internet use in the same survey was 53.0% for ages 70–79 and 18.7% for ages 80 and over ([same source, p.3](https://www.soumu.go.jp/johotsusintokei/statistics/data/250530_1.pdf)).
- The share of people aged 65+ who live alone was 15.0% (men) and 22.1% (women) in 2020. It is projected to reach 26.1% and 29.3% by 2050 ([内閣府 令和8年版高齢社会白書 概要 第1章](https://www8.cao.go.jp/kourei/whitepaper/w-2026/html/gaiyou/s1_1.html)).
- Design implication: any MVP that touches residents aged 70+ needs a paper or proxy fallback, such as a printed QR code, a family or staff proxy, or a kiosk mode. A smartphone-only design excludes most people aged 80+.

### 1.2 Government de-analog push (policy tailwind)

- **Hanko.** In November 2020 the government found 14,992 procedure types from the public to government that required a seal. It decided to abolish, or prepare to abolish, the seal for 14,909 of them (over 99%). The 83 that remain involve 印鑑証明, registration, or bank seals ([河野大臣記者会見 2020-11-13](https://www.cao.go.jp/minister/2009_t_kono/kaiken/20201113kaiken.html)).
- **Analog regulations.** The デジタル臨時行政調査会 (2021–2023) set review policies for about 10,000 clauses (約1万条項) that require visual inspection, on-site presence, posted paper notices, floppy disks, and similar. Digital Agency stopped updating its tracking dashboard on 2026-09-28 and moved status reporting to a successor page ([デジタル庁 アナログ規制ダッシュボード](https://www.digital.go.jp/resources/govdashboard/administrative_research)).
- **Cash.** Cashless payments were 58.0% of consumer spending in 2025 (162.7兆円). The government targets 65% by 2030 and 80% in the long run ([経済産業省 2026-03-31](https://www.meti.go.jp/press/2025/03/20260331006/20260331006.html)). By value, roughly 42% of consumer payments are still non-cashless.
- **2025年の崖.** METI's 2018 DX Report used the term "cliff" for the economic loss Japanese firms face if legacy systems block DX. In May 2025 METI said the cliff had arrived and that legacy-system migration still lacked speed ([経済産業省 2025-05-28 レガシーシステムモダン化委員会総括レポート](https://www.meti.go.jp/press/2025/05/20250528003/20250528003.html)). The often-quoted "最大12兆円/年" loss figure comes from the 2018 report. This research did not fetch that report, so the figure is **(secondary)**.

---

## 2. Domain findings

Each domain covers: what is analog today, who suffers and how much, why it persists, policy and deadlines, existing solutions and gaps, and an MVP idea.

### 2.1 Schools and parents (プリント, 欠席連絡, 提出物, 面談日程, 集金)

**What is analog today.** These figures come from MEXT's FY2025 self-check of 28,049 public compulsory schools (98.6% response, run 2025-11-05 to 2025-12-10) ([文部科学省 校務DXチェックリスト自己点検結果 令和8年3月9日 別紙1, p.2–3, p.7](https://www.mext.go.jp/content/20260306-mxt_shuukyo01-000048038_3.pdf)).

| Item (school ↔ parent) | Fully digital | Not at all |
|---|---|---|
| Absence/late contact via cloud (欠席・遅刻・早退連絡) | 55.5% | 12.1% |
| School fees collected without cash (学校徴収金) | 45.0% | 12.2% |
| Surveys to parents via cloud | 29.4% | 5.1% |
| おたより/handouts sent via cloud | 13.1% | 5.1% |
| **Documents parents submit to school via cloud (提出資料)** | **1.4%** | **34.1%** (a further 46.0% do it for less than half) |
| **Scheduling with parents via cloud (日程調整)** | **7.2%** | **60.9%** |
| After-hours parent contact accepted via cloud | 34.6% have it | 65.4% do not |
| Online info sessions / parent meetings | 0.3% | 73.1% |

- **FAX and hanko inside schools.** 71.7% of schools still use FAX for daily work. That count excludes crisis-management and court procedures, where FAX is treated as an accepted exception. In addition, 91.0% of schools have work documents that require a seal (押印). This question asks about documents in school work generally, not specifically about parents ([same source, p.7](https://www.mext.go.jp/content/20260306-mxt_shuukyo01-000048038_3.pdf)).
- **Trend.** The previous round (FY2024, run September–November 2024) showed parent-submitted documents fully digital at 0.7% and FAX use at 76.8% ([文部科学省 令和7年3月26日 確定値 別紙1, p.3, p.6](https://www.mext.go.jp/content/20250326-mxt_jogai01-000033278_1.pdf)). So progress is real but slow for submissions and scheduling.

**Who suffers.** Teachers collect and tally paper forms, and parents fill in the same details again and again. No source found here measures this burden in hours. The best proxy: the top reason schools give for not progressing is "no time in school to consider it," at 39.9% ([same R7 source, p.23](https://www.mext.go.jp/content/20260306-mxt_shuukyo01-000048038_3.pdf)).

**Why it persists.** Schools gave these reasons ([same R7 source, p.23](https://www.mext.go.jp/content/20260306-mxt_shuukyo01-000048038_3.pdf)):
- no time to consider it (39.9%)
- anxiety about using ICT (33.0%)
- the board of education has not set up tools or security (27.4%)
- many staff dislike ICT (26.7%)
- no one knows where to start (18.8%)

Board rules prohibit the change in only 8.8% of cases. This is a habit-and-capacity problem more than a regulatory one.

**Policy.** MEXT runs the yearly checklist and publishes results ([MEXT checklist page](https://www.mext.go.jp/a_menu/shotou/zyouhou/detail/mext_02597.html)). Digital Agency publishes a per-municipality dashboard that tracks "教員と保護者間の連絡のデジタル化," FAX abolition, and hanko abolition ([デジタル庁 校務DXダッシュボード](https://www.digital.go.jp/resources/govdashboard/school-affairs-dx)).

**Existing solutions and gaps.** Absence contact and broadcast おたより are already well served: 55.5% of schools have absence contact fully digital, and only 5.1% send no おたより via cloud. The gap is two-way workflows:
- forms parents must return, such as 同意書, 調査票, and 引き取り票, possibly with a seal or signature
- scheduling, such as 個人面談 and 家庭訪問

These are where "not at all" stays high (34.1% and 60.9%). No primary source was found on why generic form tools leave this gap. A likely reason is that teachers need per-student tracking of who has not submitted, plus reminders. That is an inference, not a sourced finding.

**MVP idea: 「おかえりポスト」 (return-form and meeting-slot hub)**

- **Teacher side:**
  - Create a 提出物 from a template: 同意書, アンケート, 面談希望.
  - Pick a class roster.
  - Get one link or QR code to paste into the existing おたより channel.
- **Parent side:**
  - Open the link on a phone with no account, using a per-child token.
  - Fill in the form and sign with a finger signature or a typed name instead of a 押印.
  - Pick a 面談 slot from a grid.
- **Teacher dashboard:**
  - Submitted / not submitted per child.
  - One-tap reminder text for the 未提出 parents.
  - CSV and PDF export for filing.
- **Paper fallback.** A printable form with a QR code. The teacher ticks paper returns manually, so the dashboard stays complete.
- **Out of scope:** authentication with school accounts and LINE integration.
- **3-minute demo:**
  1. The teacher creates a 面談 form.
  2. Two "parents" submit on phones.
  3. The dashboard updates live.
  4. A slot conflict is prevented.
  5. A reminder goes to the remaining parent.

### 2.2 Neighborhood associations (回覧板, 町内会, 安否確認)

**What is analog today.** 回覧板 (paper circulars passed house to house), paper member lists, and in-person or phone 安否確認 after disasters. Municipalities also route much administrative work through 自治会: posting circulars, recommending commissioners, disaster drills, and streetlight upkeep. The 総務省 study group calls this "行政協力業務" and asks municipalities to take stock of it ([総務省 地域コミュニティに関する研究会報告書 概要 R4.4](https://www.soumu.go.jp/main_content/000807501.pdf)).

**Who suffers and how much.**
- Membership is falling. In designated cities (指定都市), average 自治会 membership fell 6.9 points from FY2010 to FY2020. The data covers 15 cities, from a survey of all 1,741 municipalities ([総務省 自治会・町内会の活動の持続可能性について R3.10.25, p.5](https://www.soumu.go.jp/main_content/000777270.pdf)). No verified national membership rate is given here.
- When asked which area of 自治会 digitization would help most, municipalities answered "安否確認 in disasters" most often (72.1%). Their top concern was "many residents are unfamiliar with operating devices" (41.2%). Only 15.1% of municipalities (263 of 1,741) had supported or planned support for 自治会 digitization, as of August 2021 ([総務省 研究会報告書 概要 R4.4](https://www.soumu.go.jp/main_content/000807501.pdf)).

**Why it persists.** The leaders are elderly volunteers, and many residents are unfamiliar with devices (41.2%, above). Municipal support is rare (15.1%). Associations are small and have no budget.

**Policy.** 総務省 recommends promoting 電子回覧板 apps at federation level, with information delivery, event RSVPs, and disaster 安否確認. It also recommends three-way agreements between the municipality, the federation, and a vendor. It notes that the Personal Information Protection Act (個人情報保護法) applies to 自治会: they must state the purpose of use and take security measures ([same source](https://www.soumu.go.jp/main_content/000807501.pdf)).

**Existing solutions and gaps.** The study group examined municipal cases including Kanazawa's 電子回覧板 「結ネット」, Shimada's 自治会DX, Okayama's 電子町内会, and Sasebo's local SNS ([総務省 研究会ページ](https://www.soumu.go.jp/main_sosiki/kenkyu/chiiki_community/index.html)). The gap, inferred from the 41.2% figure: most tools assume every household is on a smartphone. Few handle mixed paper-and-digital circulation, where the printed 回覧板 itself carries a QR code that records "read" status.

**MVP idea: 「デジタル回覧板＋ワンタップ安否」 (hybrid)**

- **Leader side:** Post a circular (text, PDF, or photo). The app prints a paper version with a QR code per household.
- **Resident side:**
  - Open the link to mark 既読. A family member can mark it on behalf of an elderly parent.
  - Answer a yes/no RSVP.
- **Disaster mode:** The leader triggers a 安否確認. Each household taps 無事 / 支援が必要. Households that do not answer appear in red on the street list for door-knocking.
- **Data:** Household name, block (班), and an optional phone number. No sensitive data.
- **3-minute demo:**
  1. Post a circular.
  2. Show the printed version.
  3. Three households mark it read.
  4. Switch to disaster mode.
  5. Two respond, and one non-responder is highlighted for a visit.

### 2.3 Disaster: evacuation shelter reception (避難所受付) [partially overlaps Mimamori Pass: QR identity]

**What is analog today.** Shelters register evacuees on handwritten paper forms. Staff then tally the forms by hand for the disaster HQ.
- Edogawa Ward states that shelter reception is paper-based and caused confusion during Typhoon No. 19 in 2019. Paper tallying delayed situational awareness at HQ ([江戸川区 避難所受付DX化](https://www.city.edogawa.tokyo.jp/documents/65179/hinanjodx.pdf)).
- In Digital Agency's design, people with no card fill in a handwritten form, receive a "white card," and staff convert the form by OCR ([デジタル庁 避難者支援業務のデジタル化に関する実証 2024-12-25, p.10](https://www.digital.go.jp/assets/contents/node/basic_page/field_ref_resources/f7339476-4afc-42d8-a574-a06bb8843fb5/7297b15d/20241225_policies_disaster_prevention_outline_01.pdf)).

**How much.** Digital Agency **pilots** (FY2022–2023, with Fukuoka, Kobe, Niigata, and Kanagawa) cut shelter check-in time by about 80% with an app. Check-in with My Number Card took about one-tenth of the time. Report-writing work fell by about 50% ([same source, p.2](https://www.digital.go.jp/assets/contents/node/basic_page/field_ref_resources/f7339476-4afc-42d8-a574-a06bb8843fb5/7297b15d/20241225_policies_disaster_prevention_outline_01.pdf)).

**Why it persists.** Shelters are opened by a few municipal staff with no equipment. Evacuees may have no phone or card. Systems must work offline and for people aged 80+, whose internet use is 33.1% (section 1.1).

**Policy.** Digital Agency is running multi-year pilots on My Number Card and transit-IC check-in. Its design includes a fallback card for people who hold nothing (same source).

**Existing solutions and gaps.** National pilots focus on card readers and municipal systems. One gap for a hackathon: a zero-install, offline-tolerant web app for small or volunteer-run shelters, with a live headcount and needs board.

**MVP idea: 「避難所QR受付ボード」**

- **Before a disaster:** A household pre-registers members and stores the QR code on a phone or as a printout.
- **At the shelter:** A tablet scans the QR code, or staff key in a quick manual entry, then check the household in or out.
- **HQ view:** A live board with headcount per shelter and aggregate needs (food, water, medicine, care).
- **Reuse:** The Mimamori Pass plan's QR encoding and offline approach could be reused.
- **Risk:** Health, allergy, or disability fields are 要配慮個人情報 (special-care personal information). The MVP should store only counts and non-sensitive needs, or use synthetic data only.
- **3-minute demo:**
  1. Scan three QR codes.
  2. The HQ board ticks up.
  3. A "needs" chart shows 2 × 乳児用ミルク.
  4. Check one household out.

### 2.4 Foreign residents (在留手続, multilingual paperwork, letters)

**Scale.** At the end of 2025, 4,125,395 foreign residents lived in Japan, up 9.5% year on year. This was the first time the number passed 4 million ([出入国在留管理庁 令和7年末現在における在留外国人数](https://www.moj.go.jp/isa/publications/press/13_00062.html)).

**Pain points.** These come from 出入国在留管理庁 「令和7年度 在留外国人に対する基礎調査」 (8,874 valid responses, October–November 2025) ([source](https://www.moj.go.jp/isa/content/001472177.pdf)):
- When trying to consult public bodies, residents reported:
  - "didn't know where to consult" (16.6%)
  - "had to take time off work or school to consult" (16.6%)
  - "no or few interpreters" (10.0%)
- At hospitals, 14.3% could not convey their symptoms accurately.
- 19.4% of those who experienced discrimination met it "when looking for housing," the most common setting.
- About 80% did not know of foreigner-support web pages or programs.
- One free-text response: pension-related letters arrive in Japanese that is too hard to read, so the person cannot tell whether a letter is genuine, and fears that mishandling it could affect their residence status. This is a single respondent's comment, not a statistic.

**Online residence procedures.** Among people who know about the 在留申請オンラインシステム, more than 30% do not use it. Their reasons ([出入国在留管理庁 オンラインによる在留手続に関するアンケート調査結果 令和8年2月, p.5](https://www.moj.go.jp/isa/content/001457393.pdf)):
- don't understand how to use it (19.3%)
- applications are too infrequent to see a benefit (10.2%)
- no PC or IC card reader (9.5%)
- prefer the counter to consult staff (7.0%)

**Why it persists.** Language barriers, low awareness of services, and procedures done at counters during working hours.

**Existing solutions and gaps.** National and local multilingual consultation centers exist, such as Shimane's 「Go-enしまね」 one-stop desk ([島根県サイト header link](https://www.pref.shimane.lg.jp/industry/suisan/kanri/iji/houkokugimu.html)). The MOJ survey shows awareness is the gap (about 80% unaware).

**MVP idea: 「この手紙なに？」 letter explainer + where-to-go finder**

- The user photographs or pastes a Japanese administrative letter, such as a 年金, 住民税, or 国保 notice.
- The app returns:
  - an やさしい日本語 summary plus a summary in the user's language
  - detected deadlines and amounts
  - a "what to bring" checklist
  - the official contact office for the user's municipality
- **Guardrails (required):**
  - Never state that a letter is genuine. Instead, show the official phone number from a curated list, so the user can check with the sender.
  - Do not give legal advice.
  - Do not fill in 申請書 for others. Japan's 行政書士法 reserves paid preparation of documents for government offices to licensed 行政書士 ([e-Gov 行政書士法](https://laws.e-gov.go.jp/law/326AC1000000004)). This team has not verified the exact boundary, so confirm it before pitching.
- The app sends text to an LLM. The demo must use synthetic letters only.
- **3-minute demo:**
  1. Paste a sample 国保税 notice.
  2. Show a Vietnamese and やさしい日本語 explanation.
  3. Show a deadline added to the calendar.
  4. Show a map link to the 区役所 counter.

### 2.5 SMEs: FAX ordering, paper invoices, promissory notes, e-bookkeeping

**FAX and phone ordering.** 中小企業庁 states that a certain number of SMEs still place and receive orders by FAX or phone. In 2021, 48.5% of order-receiving firms supported electronic ordering ([中小企業庁 中小企業の受発注デジタル化](https://www.chusho.meti.go.jp/keiei/gijut/digitalization/index.html)). This report found no newer national figure.

**Digitalization stage.** The 2025 SME White Paper reports a large drop in firms at "段階1" (paper and verbal work, no digitalization) between the 2023 and 2024 surveys. It cautions that the two samples differ. It also notes that about half of 段階1 firms have no DX plans at all ([2025年版 中小企業白書 第1部第1章第5節](https://www.chusho.meti.go.jp/pamflet/hakusyo/2025/chusho/b1_1_5.html)). This research did not extract the exact percentages, which are in that page's Excel files.

**Promissory notes (約束手形).**
- 1,967万 notes and cheques were cleared at electronic clearing houses in 2024.
- At the 2024 pace of reduction (5.01M, 61% of target), about 780,000 per month would remain at the end of FY2026, against a government goal of zero ([中小企業庁 取引適正化 研究会資料 r7_003/005](https://www.chusho.meti.go.jp/koukai/kenkyukai/kigyo_torihiki/r7_003/005.pdf)).
- Banks reportedly decided to abolish note and cheque clearing from the start of FY2027 **(secondary)**.
- This is a real analog pain, but it is a banking-rail problem and not a 1–2 day MVP.

**E-bookkeeping (電子帳簿保存法).** Since January 2024, businesses that send or receive invoices and receipts electronically must keep the electronic data under set requirements. Printing them out is no longer enough. A 猶予措置 (grace measure) applies where the tax office accepts there is good reason ([国税庁 電子帳簿保存法 パンフレット一覧](https://www.nta.go.jp/law/joho-zeikaishaku/sonota/jirei/08.htm)). This report took these details from NTA-hosted documents found by search; the details should be read in the pamphlets themselves.

**Why it persists.** Business partners dictate the format. Small firms cite "cost burden is large" and "lack of DX talent" ([2025年版 中小企業白書](https://www.chusho.meti.go.jp/pamflet/hakusyo/2025/chusho/b1_1_5.html)).

**Existing solutions and gaps.** METI's 中小企業共通EDI standard exists, backed by the つなぐITコンソーシアム ([中小企業庁](https://www.chusho.meti.go.jp/keiei/gijut/digitalization/index.html)). It requires both sides to adopt it. The gap is a small supplier whose customers keep faxing.

**MVP idea: 「FAXいらず受注フォーム」**

- A small supplier (for example a bento shop, a parts maker, or a farm) publishes a mobile order form built from its product list.
- Customers order on the web. The supplier sees a daily order board and can export an order PDF in its familiar layout, including a FAX-style sheet for staff who still want paper.
- A stretch goal: an invoice PDF with the T-number field (インボイス).
- **3-minute demo:**
  1. Build a form from a CSV.
  2. Place two orders.
  3. Show the order board.
  4. Print a picking list.

### 2.6 Agriculture and fisheries

**Agriculture.** MAFF says "most current administrative procedures are on paper, need many documents, and require visits to counters" ([農林水産省 eMAFF](https://www.maff.go.jp/j/kanbo/dx/emaff.html)).
- **Usage.** eMAFF launched in 2022 with a goal of 60% online use by FY2025. 総務省 found usage "低調" and the system under fundamental review. The baseline online rate in FY2020 was 0.3% ([総務省行政評価局 行政手続のDX推進に関する調査－農林水産関係－ 令和8年7月](https://www.soumu.go.jp/main_content/001084722.pdf)).
- **Proxy entry.** For livestock 農場台帳登録 under eMAFF, prefectures entered 98% of registrations on owners' behalf in FY2024. Prefectures say many owners are elderly and cannot use digital tools, so the owners submit paper and staff re-key it ([same source, №29](https://www.soumu.go.jp/main_content/001084722.pdf)). This 98% applies to that one procedure, not to all procedures.
- **Planning support.** Among 認定農業者 (certified farmers), 42.5% were aged 65+ at the end of March 2024. Municipal staff spend about one hour per farmer helping write the 農業経営改善計画 ([same source](https://www.soumu.go.jp/main_content/001084722.pdf)).
- **Demographics.** In 2025 the average age of core farm workers (基幹的農業従事者) was 67.7 ([農林水産白書 令和7年度](https://www.maff.go.jp/j/wpaper/w_maff/r7/pdf/1-2-02.pdf)).
- **Policy.** eMAFF 2.0 started in October 2026, with about 360 procedures online. The old eMAFF ends in mid-March 2027 ([農林水産省 eMAFF](https://www.maff.go.jp/j/kanbo/dx/emaff.html)).

**Fisheries.**
- Since the 2020-12-01 Fisheries Act amendment, catch reports for TAC-managed species are mandatory ([島根県 漁獲量等の報告について](https://www.pref.shimane.lg.jp/industry/suisan/kanri/iji/houkokugimu.html)).
- For minister-licensed fisheries, the Fisheries Agency built a web system that ingests fishers' Excel 漁獲成績報告書 ([水産庁 R03 委託事業報告](https://www.jfa.maff.go.jp/j/gyosei/supply/seika/attach/pdf/R03_ippan-180.pdf)).
- **No primary statistic was found on the paper vs. electronic share of fisheries reports.**

**MVP idea (lower priority): 「代理入力アシスト」**

- A form tool for prefecture or municipal staff, mirroring a paper form 1:1.
- Bulk entry with validation, and a printable pre-filled form that a farmer checks and signs next year.
- It fits the 総務省 recommendation to design for proxy entry for now.
- It is less demo-friendly.

### 2.7 Healthcare (paper charts, prescriptions, referral letters) [overlaps Mimamori Pass]

- **Paper charts.** Electronic medical record adoption in 2023 was 65.6% of general hospitals and 55.0% of general clinics (57,662 of 104,894). So about 45% of clinics still used paper charts ([厚生労働省 電子カルテの普及について 2025-12-10, p.1](https://www.mhlw.go.jp/content/10808000/001608407.pdf)).
- **Clinics that say they cannot adopt.** A Japan Medical Association survey of paper-chart clinics (5,466 responses, April–June 2025) found 54.2% said adoption is "impossible." They cited unfamiliarity with IT, high cost, and expecting to use it only a few years. The "impossible" share rises with the doctor's age ([same source, p.2](https://www.mhlw.go.jp/content/10808000/001608407.pdf)).
- **Target.** The government aims for "nearly all medical institutions" to have EMRs able to share necessary information by 2030 at the latest ([same source, p.3](https://www.mhlw.go.jp/content/10808000/001608407.pdf)).
- **Electronic prescriptions.** As of June 2026, adoption was 41.3% overall: hospitals 20.9%, medical clinics 29.5%, dental clinics 10.7%, pharmacies 90.5% ([厚生労働省 電子処方箋の進捗状況 2026-08-27, p.1](https://www.mhlw.go.jp/content/12401000/001742903.pdf)).
- **Gap.** Pharmacies are digital, but prescribers lag. Referral letters (診療情報提供書) are planned for the 電子カルテ情報共有サービス. This research did not verify a national go-live date.
- **お薬手帳.** No primary statistic was found on paper vs. electronic お薬手帳 use.
- **Hackathon fit.** The Mimamori Pass plan already covers a patient-held, cross-institution summary, which is the realistic hackathon angle here. This report does not propose a separate healthcare MVP.

### 2.8 Long-term care (介護: care-plan exchange by FAX, designation paperwork) [adjacent to Mimamori Pass]

- **What is analog.** Care managers (居宅介護支援) send monthly care plans and service-use tickets (提供票) to service providers by printing, sorting per office, and then FAX, post, or hand delivery. MHLW tells offices to review exactly these "印刷→振り分け→FAX・郵送・持参" tasks ([厚生労働省 ケアプランデータ連携を円滑に行うための業務改善のポイント集](https://www.mhlw.go.jp/content/12300000/001255601.pdf)).
- **Existing system.** MHLW and 国保中央会 run the ケアプランデータ連携システム. As of 2026-11-01, 124,751 offices were listed on WAM NET's usage map ([WAM NET](https://www.wam.go.jp/wamappl/kpdrsys.nsf/top)). This count is a listing, not an adoption rate against a defined denominator. A "free pass" campaign made the licence free from 2025-06-01 ([厚生労働省 資料](https://www.mhlw.go.jp/content/12300000/001505201.pdf)).
- **Designation paperwork.** From April 2026, the 電子申請・届出システム became the default channel for care-provider designation filings in all local governments. This comes from a municipal notice found via search and not fetched by this research ([新宿区](https://www.city.shinjuku.lg.jp/fukushi/korei02_000001_00004.html)).
- **Hackathon fit.** The official exchange runs on a defined standard spec, and network effects drive it. A rival tool is low value. A better MVP angle is the family side, such as sharing a care schedule with relatives, which overlaps Mimamori Pass.

### 2.9 Municipal procedures (転入届, 住民票, 窓口)

- **Moving.** Since 2023-02-06, the 転出届 can be filed online through マイナポータル in every municipality. However, the 転入届 still requires one visit to the new municipality with the My Number Card. Digital Agency lists residents' burdens: notifying many bodies one by one, missed procedures, and multiple counter visits ([デジタル庁 引越し手続オンラインサービス](https://www.digital.go.jp/policies/moving_onestop_service)).
- **Counters.** Residents "write the same items again and again, wait each time, and get sent between counters." Staff explain every form and check entries by hand. Digital Agency's 「書かないワンストップ窓口」 program and 窓口DXSaaS address this ([デジタル庁 自治体窓口DX](https://digital.go.jp/policies/cs-dx)).
- **Gap and MVP.** マイナポータル already shows required procedures and items to bring after a move ([デジタル庁](https://www.digital.go.jp/policies/moving_onestop_service)). So a "moving checklist" MVP adds little. A stronger angle is the counter-side: a pre-fill QR code that a resident creates at home and staff scan to print the form. But 窓口DXSaaS vendors already target this ([デジタル庁](https://digital.go.jp/policies/cs-dx)).

### 2.10 Real estate (契約書面)

- Since 2022-05-18, the 重要事項説明書 and contract documents can be delivered electronically, and the 宅地建物取引士 seal is abolished ([国土交通省 報道発表](https://www.mlit.go.jp/report/press/tochi_fudousan_kensetsugyo16_hh_000001_00036.html)).
- **No primary adoption statistic was found** on how many transactions now use electronic documents.
- Housing is also where foreign residents most often meet discrimination (19.4%; see 2.4).
- With weak evidence and incumbent e-contract vendors, this domain is not shortlisted.

### 2.11 Nursery schools (保育所: 連絡帳, 登降園, 集金)

- A national survey of 16,709 facilities found:
  - 71.5% use ICT for parent contact and 71.3% for attendance
  - only 11.7% have all four subsidized functions (records, parent contact, attendance, cashless)
  - 48.7% have the three non-cashless functions
  - facilities that adopted cashless report less collection work (89.1%)

  Source: [内閣官房 省力化投資促進プラン―保育―](https://www.cas.go.jp/jp/seisaku/atarashii_sihonsyugi/shouryokukatousi/11.pdf).
- **Target.** At least 20% of facilities with all four functions by FY2026 ([same source](https://www.cas.go.jp/jp/seisaku/atarashii_sihonsyugi/shouryokukatousi/11.pdf)).
- Most nurseries already use commercial ICT, so a remaining gap is cash collection. Lower priority.

---

## 3. Scoring and ranked shortlist

Scale is 1–5 on every criterion. **Higher is always better**, so for "Privacy/regulatory risk," 5 means the lowest risk.

| # | Candidate | Pain severity | Evidence strength | Demo in 3 min | Build in 1–2 days | Privacy/reg. risk (5 = low) | Total |
|---|---|---|---|---|---|---|---|
| A | School return-forms + 面談 scheduling hub (2.1) | 4 | 5 | 5 | 5 | 4 | **23** |
| B | Hybrid デジタル回覧板 + ワンタップ安否 (2.2) | 4 | 3 | 5 | 5 | 4 | **21** |
| C | Foreign-resident letter explainer + where-to-go (2.4) | 4 | 4 | 5 | 4 | 2 | **19** |
| D | Shelter QR reception board (2.3) | 4 | 4 | 5 | 4 | 2 | **19** |
| E | SME FAX-replacement order form (2.5) | 3 | 3 | 4 | 4 | 4 | **18** |
| – | Moving checklist / counter pre-fill (2.9) | 2 | 4 | 3 | 5 | 4 | 18 |
| – | Farm proxy-entry assist (2.6) | 3 | 4 | 2 | 3 | 4 | 16 |
| – | Care-plan exchange (2.8) | 4 | 3 | 2 | 2 | 3 | 14 |

### Top 5

1. **A. School 提出物・面談日程 hub.** It has the strongest evidence of any domain: a national MEXT census of 28,049 schools, refreshed in March 2026. It targets the two items with the worst digitization, parent submissions (1.4% fully digital) and scheduling (60.9% not at all). The barriers are time and confidence (39.9% and 33.0%), not rules (8.8%), so a simple tool fits. The demo is easy and needs only synthetic data. It does not overlap Mimamori Pass.
2. **B. Hybrid 回覧板 + 安否確認.** Municipalities named disaster 安否確認 the most useful area to digitize (72.1%), and resident unfamiliarity with devices the main blocker (41.2%). The paper-QR hybrid answers that blocker directly. Evidence is weaker: the survey is from 2021, and there is no national 回覧板 statistic. 個人情報保護法 applies but the data is light.
3. **C. Foreign-resident letter explainer.** The population is large (4.13M) and growing 9.5% per year. The MOJ 2025 survey documents the pain. Risk is the lowest score, for three reasons: LLM processing of personal letters, the need to avoid certifying authenticity or giving legal advice, and the 行政書士法 boundary. Ranked above D because it does not overlap Mimamori Pass.
4. **D. Shelter QR reception.** Digital Agency pilots show about 80% time savings, and Edogawa documents the paper failure. The demo is strong. But 要配慮個人情報 needs careful scoping, and it overlaps Mimamori Pass's QR stack. That overlap helps reuse but weakens differentiation.
5. **E. SME FAX-replacement order form.** The pain is real and FAX ordering is officially acknowledged. However, the best number is from 2021 (48.5% e-ordering), and the network effect, where customers keep faxing, limits the impact a single-sided tool can show.

The moving checklist ties E on total score. It is left out because マイナポータル already provides the checklist, so the remaining gap is small.

---

## 4. Claims without full primary verification

- 「2025年の崖」 loss of up to 12兆円/year. From the 2018 METI DX Report, which was not fetched. Marked **(secondary)**.
- Banks abolishing note and cheque clearing from the start of FY2027. **(secondary)**. The clearing volumes themselves are primary.
- National 自治会 membership rate. Not stated. Only the 指定都市 decline (−6.9 points) is verified.
- 2025 SME White Paper 「段階1」 percentages. Trend verified; exact numbers are in the white paper's Excel file, not extracted.
- 電子帳簿保存法 grace-measure details. Taken from NTA-hosted documents found by search, not read in full.
- Care designation 電子申請 becoming the default from April 2026. From a municipal page surfaced by search, not fetched.
- 電子カルテ情報共有サービス national go-live date. Not verified.
- 行政書士法 boundary for a letter-explainer tool. The law is cited, but its application to this MVP is not verified.
- Paper vs. electronic share of fisheries catch reports, お薬手帳, and real-estate e-contracts. No primary statistic found.
- ケアプランデータ連携 adoption rate. Only a listing count (124,751) is verified. An older "4.6% (2023)" figure from a 国保中央会 document could not be retrieved and is not used.
- My Number Card holding rate. Not used: the 総務省 statistics page could not be retrieved.
