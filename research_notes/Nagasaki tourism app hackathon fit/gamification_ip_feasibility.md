# Gamified Nagasaki tourism app: evidence, IP/ethics risk, 3-day build feasibility

Research date: 2026-11-22. ~19 tool calls. Several sources were seen only via search-engine summaries (not opened); these are marked "(search summary)". Claims from background knowledge with no fetched source are marked **[UNVERIFIED]**.

## (a) Does gamification / stamp-rally / location-based play measurably change tourist behaviour in Japan?

### Takeaway
Big, one-off Pokémon GO *events* produce large, short spikes of visitors and spend (Tottori 2017 ~87–89k visitors; Fukuoka 2024 ~¥4.2B claimed impact), but almost all figures come from organisers or local governments, not independent studies. For digital stamp rallies, ポケふた and マンホールカード, no peer-reviewed or government study with measured dispersion/repeat/spend effects was found; the evidence is promotional or expectation-only. The one natural experiment found says real mobility effects were smaller than media coverage implied.

### Cited Findings
**Pokémon GO events (Japan)**
- Tottori Sand Dunes "Safari Zone", Nov 24–26 2017: organisers expected ~30,000 visitors; 15,000 came on day 1; total reported as ~87,000 (media) or 89,000+ (prefecture figures). — [Anime News Network](https://www.animenewsnetwork.com/interest/2017-11-28/tottori-city-sand-dunes-become-pokemon-go-safari-zone/.124593); [FutureGameReleases](https://www.futuregamereleases.com/pokemon-go-safari-zone-event-tottori-sand-dunes-japan-attracted-87-000-players); [Game Developer](https://gamedeveloper.com/game-platforms/japan-s-tottori-sand-dunes-see-tourism-boost-from-i-pokemon-go-i-event) (search summary)
- Tottori economic effect: prefecture estimate ~¥1 billion (~US$9M); PocketGamer.biz reported ~US$16M. The two estimates conflict. — [PocketGamer.biz](https://www.pocketgamer.biz/pokemon-go-travel-tottori-prefecture/) (search summary)
- Tohoku (Iwate/Miyagi/Fukushima) Lapras event, late 2016: >10,000 visitors; headline figure ~US$17M tourism revenue (headline only; article body not checked). — [Nintendo Life](https://www.nintendolife.com/news/2016/12/pokemon_go_lapras_event_raises_usd17_million_in_tourist_revenue_for_tsunami-hit_japanese_region); campaign launch: [Anime News Network](https://animenewsnetwork.com/news/2016-08-10/pokemon-go-launches-campaign-to-attract-tourists-to-quake-hit-japanese-prefectures/.105218) (search summary)
- Pokémon GO Wild Area: Fukuoka 2024: >36,000 in-person attendees, ¥4.2B (US$28M) local economic impact — publisher's own figure (Scopely), citing a Statista+ report not checked. — [Scopely](https://www.scopely.com/en/news/pokemon-go-wild-area-fukuoka-showcases-the-power-of-play-generating-42-billion-for-local-economy) (search summary)
- Niantic publishes yearly "economic impact" posts for GO events (vendor-produced). — [Niantic 2024](https://nianticlabs.com/news/pgo-economic-impact-2024/); [Niantic 2023](https://nianticlabs.com/news/pgo-economic-impact-2023)

**Nagasaki-specific**
- Pokémon GO Wild Area: Nagasaki ran Nov 7–9 2025 in Nagasaki City; tickets ¥3,600/day, sold out. No published visitor count or economic-impact figure found. — [pokemongo.com Nagasaki](https://pokemongo.com/en/gowildarea/nagasaki); [SoftBank news (network ops)](https://www.softbank.jp/en/sbnews/entry/20260525_01) (search summary)
- Nagasaki Prefecture tourism survey for Oct–Dec 2025: overnight guests up year-on-year; the prefecture lists the Wild Area Nagasaki event, Pokémon museum exhibits, the ながさきピース文化祭2025, Huis Ten Bosch and extra international flights/ferries as factors. The Pokémon effect is not isolated from the other causes. — [長崎県 PDF 2026/03](https://www.pref.nagasaki.jp/shared/uploads/2026/03/1772515804.pdf) (search summary; exact PDF-to-claim mapping not verified)
- Nagasaki Prefecture has a partnership agreement with The Pokémon Company under "ポケモンローカルActs"; Ampharos (デンリュウ) is the "ながさき未来応援ポケモン"; デンリュウ ポケふた are installed in 15 municipalities. — [長崎県 PDF 2024/08](https://www.pref.nagasaki.jp/shared/uploads/2024/08/1723602378.pdf); [長崎県 PDF 2024/12](https://www.pref.nagasaki.jp/shared/uploads/2024/12/1733388903.pdf) (search summary)
- An official digital stamp rally "ながさき照らす旅3" runs Aug 27 2026 – Feb 28 2027. It covers ポケふた plus ~40 tourist facilities, with prize draws by stamp count, aimed at tourists and residents. **The thing the team wants to build already exists officially and is live during the hackathon.** — [Kyodo PR Wire PDF 2026/08](https://cdn.kyodonewsprwire.jp/prwfile/release/M108642/202608264772/_prw_PR1fl_e0SYH3rJ.pdf); [さきがけ](https://www.sakigake.jp/news/article/20260827PR0008/) (search summary). An earlier edition existed (2025 PR: [Kyodo PR Wire 2025/07](https://cdn.kyodonewsprwire.jp/prwfile/release/M108642/202507222532/_prw_PR1fl_5qy1V1un.pdf)).
- No quantitative measure of visitors caused by ポケふた alone was found. — (gap; see above searches)

**Digital stamp rallies (general)**
- No peer-reviewed study proving dispersion (回遊) effects was found. The material found is vendor columns and a student thesis. The thesis cites a 2018 private survey: ~60% of participants said their interest in the theme rose. That measures interest, not movement. — [広瀬研 卒論 2023/24](https://www.yatex.org/gitbucket/HiroseLabo./2023-koki/raw/master/paper/c120020-thesis.pdf)
- Yokohama Chinatown 春節 digital stamp rally 2023 (NTT East): supports QR, quiz/keyword, GPS and NFC check-in, with footfall analytics. — [マイナビニュース](https://news.mynavi.jp/article/20230510-2659415/)
- Vendor claim: the main measurable benefit of a digital rally is the log (who/when/where/sequence), which supports route design. — [issoh.co.jp column](https://www.issoh.co.jp/md/column/19085)
- Suginami ward publishes a project result report that includes a stamp-rally item (not opened). — [杉並区 R6 事業結果](https://www.city.suginami.tokyo.jp/documents/20566/r6jigyoukekka.pdf)

**マンホールカード**
- Cumulative: 407 bodies, 478 card types, ~2.9M cards issued (MLIT figure; date of the snapshot not confirmed). Cards are free and handed out at sewer facilities and tourist information centres, so getting one requires a physical visit. — [MLIT PDF](https://www.mlit.go.jp/common/001264427.pdf); [MLIT press](https://www.mlit.go.jp/report/press/content/001414953.pdf) (search summary)
- The 2016 launch created a "boom" that is spreading to inbound tourists via SNS. — [nippon.com](https://www.nippon.com/ja/views/b06304/)
- Tokyo describes anime manhole lids and cards as *expected* to attract tourists. This is an expectation, not a measurement. — [東京都 財務局](https://www.zaimu.metro.tokyo.lg.jp/documents/d/zaimu/tomin9__2)

**Academic / failure modes**
- A natural experiment with mobile-phone data found that the statistical effect of Pokémon GO on urban mobility did *not* match the "massive change" shown in the media. — [arXiv 1610.08098](https://arxiv.org/pdf/1610.08098)
- "The Geography of Pokémon GO": beneficial and problematic effects on places and movement. It finds uneven spatial distribution of game resources. — [arXiv 1903.12041](https://arxiv.org/pdf/1903.12041)
- 神田孝治 (Ritsumeikan): gathering places created by Pokémon GO are temporary and shift easily. This is relevant to novelty decay. — [立命館 紀要](https://www.ritsumei.ac.jp/acd/cg/lt/rb/656/656PDF/kanda.pdf)
- 岡本健: Pokémon GO fieldwork used to redefine "tourism" as media communication. — [北大 HUSCAP](https://eprints.lib.hokudai.ac.jp/repo/huscap/all/43891/okamoto7083.pdf) (search summary; attribution of this PDF to 岡本 is from the search engine)
- 小池ら (2016, 日本地理学会): tourism impact of Pokémon GO measured from geotagged tweets. — [J-STAGE](https://www.jstage.jst.go.jp/article/ajg/2016a/0/2016a_100082/_article/-char/en)
- A location-game walking event found that the presence of fellow walkers sustained activity more than prizes did. This is a social-retention lever. — [newswitch](https://newswitch.jp/index.php/p/6163) (search summary)

### Inferences
- The defensible claim for a pitch: "time-boxed, event-style gamification reliably produces visit spikes; persistent stamp apps have weak public evidence." Do not claim measured dispersion or repeat-visit lift from stamp rallies; no source supports it.
- The strongest *mechanism* argument for digital rallies is the data they create (check-in logs give a dispersion heatmap), not proven behaviour change. A demo that shows a live check-in heatmap is more honest and more convincing than claiming lift.
- Retention levers seen in the evidence: time limits/events, physical collectibles (manhole cards need a visit), and social presence. Prizes alone are weaker (newswitch, weak source).
- Because "ながさき照らす旅3" already covers ポケふた + ~40 spots, a student clone of the stamp rally adds little. Differentiate on what it lacks, e.g. the locals↔tourists connection, which fits the "TSUNAGU / connect" theme.

### Gaps
- No J-STAGE peer-reviewed paper with measured dispersion/repeat/spend effects of a Japanese digital stamp rally was found (limited search budget; try CiNii "スタンプラリー 回遊 実証").
- No figure for Dragon Quest Walk collaborations was searched or found.
- No participation numbers for "ながさき照らす旅" 1/2 were found.
- No visitor or economic figure was found for Wild Area Nagasaki 2025.
- No longitudinal novelty-decay data (retention curves) for municipal stamp apps was found.

## (b) IP, legal and ethical risks

### Takeaway
Using Pokémon names, character art or ポケふた imagery in an unlicensed app is high-risk: The Pokémon Company forbids secondary and commercial use of its materials and enforces actively. Gamifying the Nagasaki Peace Park is an ethical red line with a direct local precedent: in July 2016 the park asked Niantic to remove Pokémon GO content, saying it is a place to pray, not play. Hiroshima made the same request.

### Cited Findings
**Pokémon IP**
- Official Pokémon Center digital-material terms: downloads are for personal use only. Modification, transfer, adaptation, lending, secondary use and commercial use are prohibited "by any means or form". — [Pokémon Center デジタルコンテンツ利用規約](https://www.pokemoncenter-online.com/feature/digital-contents-terms.html)
- Official ポケふた site footer: "©Pokémon. ©Nintendo/Creatures Inc./GAME FREAK inc."; Pokémon names are trademarks of Nintendo/Creatures/GAME FREAK. No photo-use policy for ポケふた is published there. — [local.pokemon.jp/manhole](https://local.pokemon.jp/manhole/) (fetched)
- The Pokémon Company is an active enforcer. It won a copyright and unfair-competition judgment in China. — [corporate.pokemon.co.jp news 345](https://corporate.pokemon.co.jp/media/news/detail/345.html) (search summary)
- Unlicensed "Pokémon"-named apps in the App Store drew criticism as suspected infringement. — [AUTOMATON 2022](https://automaton-media.com/articles/newsjp/20220120-189316/) (search summary)
- March 2026: The Pokémon Company publicly stated it had "not given any permission" for the US White House's Pokémon-style promo images. This shows the company will publicly disown even high-profile unlicensed use. — [GameBusiness.jp](https://www.gamebusiness.jp/article/2026/03/07/26363.html); [AUTOMATON](https://automaton-media.com/articles/newsjp/pokemon-pokopia-20260306-426822/)
- No official fan-art or secondary-creation guideline that would cover an app was found. — (search summary of [AUTOMATON](https://automaton-media.com/articles/newsjp/20240514-293297/), [inside-games](https://www.inside-games.jp/article/2024/05/14/155390.html))

**Memorial-site ethics**
- July 2016: Nagasaki Peace Park curators formally emailed Niantic to remove the park from Pokémon GO, saying it is a place to pray and games are inappropriate. Niantic reportedly complied (secondary reports; no primary Niantic statement found). — [SoraNews24](https://soranews24.com/2016/07/27/nagasakis-peace-park-asks-pokemon-go-developer-to-remove-game-functionality-on-park-grounds/); [Kotaku](https://kotaku.com/you-can-no-longer-catch-pokemon-at-hiroshima-1784985508)
- Hiroshima city asked Niantic to remove gyms/PokéStops from Peace Memorial Park before the Aug 6 ceremony. It named the Cenotaph and the Children's Peace Monument as particular concerns. Niantic removed them. — [SoraNews24](https://soranews24.com/2016/07/29/hiroshima-requests-removal-of-pokemon-go-facilities-from-peace-memorial-park/); [Global News](https://globalnews.ca/news/2870350/pokemon-go-removes-hiroshima-atomic-bomb-memorial-site-from-game/amp); [phys.org/AFP](https://phys.org/news/2016-07-hiroshima-unhappy-atomic-bomb-pokemon-site.html)
- The US Holocaust Memorial Museum and Arlington National Cemetery made similar requests; the museum confirmed removal. — [CBS News](https://www.cbsnews.com/sanfrancisco/news/pokemon-go-hiroshima-atomic-bomb-memorial) (search summary)

### Inferences
- Safe IP pattern: never ship Pokémon names, sprites, logos or ポケふた photos as app assets. Do not call the feature "ポケふた". Public facts (a lid exists at lat/lng X) are lower risk, but scraping location lists from the official site, plus the trademarked name, invites a takedown. Use generic "manhole lid / マンホール" spots, original team-drawn characters, or link out to official pages. **Not legal advice; [UNVERIFIED] as legal analysis.**
- A public GitHub repo + demo video = public distribution. Any Pokémon asset in the repo is a visible IP problem for judges and sponsors.
- Peace Park / Peace Statue: present it as a quiet "reflection/learning" stop with no points, streaks, confetti, leaderboard or reward animation. Better still, exclude memorial zones from game mechanics entirely (mirror the 2016 Niantic exclusions). A 3D Peace Statue "trophy" or collectible would be read as disrespectful in Nagasaki.
- AI chatbot risk: it may hallucinate shop hours, prices or allergy info. It must answer only from the team's dataset and say "unknown" otherwise. Keep personal data out of the check-in logs (location history is personal information under Japan's APPI). **[UNVERIFIED] — APPI not researched here.**

### Gaps
- No official Pokémon Company page specifically on ポケふた photo use, or a general fan-work guideline, was found.
- No current (2026) statement from Nagasaki City on games/apps in the Peace Park, and no Niantic primary confirmation of the 2016 Nagasaki removal, was found.
- Whether マンホールカード *designs* (sewerage association / municipality copyright) may be reproduced in an app was not checked. Assume no without permission.

## (c) Open data, 3D assets, maps, check-in, chatbot: building blocks for a credible 3-day build

### Takeaway
There is **no PLATEAU 3D city model for Nagasaki City**: only Sasebo (2022), Matsuura (2024) and Hasami (2024) are in Nagasaki Prefecture. No free, licensed 3D model of the Peace Statue was found on Sketchfab. A 3D Peace Park/Statue feature is therefore either a placeholder, a licensing problem, or a 3-day photogrammetry side-project: cut it. Maps should use OSM/MapLibre (free) or Google with per-SKU free tiers. POI data for Nagasaki City does not appear as a clean open CSV, so expect to hand-curate ~20–40 POIs.

### Cited Findings
**3D**
- The PLATEAU open-data page lists, for Nagasaki Prefecture, only 波佐見町 (2024), 松浦市 (2024) and 佐世保市 (2022). **長崎市 is not listed.** No licence is shown on that page; data links go to G空間情報センター (CKAN). — [MLIT PLATEAU open data](https://www.mlit.go.jp/plateau/open-data/) (fetched)
- PLATEAU data is generally published under open licences such as CC BY 4.0. — [MLIT press 2021](https://www.mlit.go.jp/report/press/toshi03_hh_000078.html) (search summary)
- Sasebo: LOD1 for the whole city (426.01 km²), plus LOD2 in the FY2022 project. — [MLIT PDF](https://www.mlit.go.jp/toshi/daisei/content/001767353.pdf) (search summary)
- G空間情報センター CKAN search returned HTTP 403 to automated fetch. — [geospatial.jp](https://www.geospatial.jp/ckan/dataset?q=%E9%95%B7%E5%B4%8E%E5%B8%82+PLATEAU)
- Sketchfab "Nagasaki 3D Project" (nagasaki_3d_museum): ~43 models, mostly Gunkanjima, heritage and industrial sites, plus the former Urakami Cathedral bell tower. No Peace Statue appeared. Licences per model were not checked. — [Sketchfab nagasaki_3d_museum](https://sketchfab.com/nagasaki_3d_museum/models) (search summary)
- Other results ("Peace sculpture" by chojmeister; CGTrader "Peace statue") are not confirmed to be the Nagasaki statue. — [Sketchfab chojmeister](https://sketchfab.com/chojmeister); [CGTrader](https://cgtrader.com/3d-models/exterior/other/peace-statue)

**POI / open data**
- No Nagasaki City tourism-facility CSV with lat/lng was found. Nagasaki City publishes some open data on the BODIK portal (e.g. a preschool list), so a tourism set may exist there but was not confirmed. — [BODIK 422011 example](https://odm.bodik.jp/en/dataset/422011_preschool_h/resource/a0693e2e-5ef8-4556-851f-9b8617306852)
- Nagasaki Prefecture publishes tourism statistics as PDFs, including per-facility visitor counts for R3–R5 and record tourism spend in R6, but not lat/lng datasets. — [長崎県 観光統計 PDF](https://www.pref.nagasaki.jp/shared/uploads/2025/10/1761792218.pdf); [長崎県 PDF](https://www.pref.nagasaki.jp/shared/uploads/2023/12/1701762446.pdf) (search summary; exact mapping unverified)
- Reference schema (Ishikawa tourism-facility CSV): name, address, lat, lng, open days, fee, description, access, parking. This is a useful template for a hand-built dataset. — [石川県 CKAN](https://ckan.opendata.pref.ishikawa.lg.jp/en/dataset/172065_tourism/resource/68ac06ea-619d-4662-8861-299897ad8828)
- Nagasaki DMO publishes quarterly inbound analyses (e.g. 2025-2Q). — [at-nagasaki.jp](https://www.at-nagasaki.jp/storage/files/shares/dmo/Inbound/2025-2Q.pdf)

**Maps APIs**
- Since Mar 1 2025, Google replaced the $200/month credit with per-SKU free monthly thresholds: Essentials 10,000, Pro 5,000, Enterprise 1,000 billable events; Map Tiles 2D/Street View tiles get 100,000. Places API (legacy), Directions and Distance Matrix are now "Legacy". — [Google March 2025 changes](https://developers.google.com/maps/billing-and-pricing/march-2025); [Google billing FAQ](https://developers.google.com/maps/billing-and-pricing/faq); [pricing overview](https://developers.google.com/maps/billing/overview/)
- A Google page mentions "1k free calls per SKU", which conflicts with the FAQ's 10,000. Check the pricing sheet for the exact SKU. Google also advertises a no-credit-card "Maps Demo Key". — [Google Maps Platform blog](https://mapsplatform.google.com/intl/hi_in/resources/blog/build-more-for-free-and-access-more-discounts-online-with-google-maps-platform-updates/) (search summary)
- OpenStreetMap data is ODbL (attribution + share-alike on the database); MapLibre GL JS is open source (BSD-3). The public OSM tile server has a usage policy that forbids heavy app use, so use a free-tier vector-tile host or self-host. **[UNVERIFIED] — background knowledge, not fetched this session.**
- Nagasaki-specific OSM POI completeness: **not checked**.

**Geofenced check-in and anti-cheat** — no sources fetched; all **[UNVERIFIED]** design notes:
- Browser Geolocation API + server-side haversine distance check (radius ~30–80 m to allow for GPS error in Nagasaki's hilly, dense streets).
- GPS spoofing is trivial on Android (mock-location apps). Practical defences for an MVP:
  - Combine GPS with a second factor: a QR/NFC code or a rotating one-time code shown at the partner shop (Yokohama Chinatown supports QR/NFC/keyword — [マイナビ](https://news.mynavi.jp/article/20230510-2659415/)).
  - Rate-limit and run a server-side "impossible travel" speed check.
  - Require `accuracy` below a threshold.
- Play Integrity / App Attest are overkill for 3 days.

**AI chatbot over a shop dataset (RAG)** — no sources fetched; **[UNVERIFIED]** design notes:
- Needs:
  - a small curated JSON/CSV of shops (name, category, hours, lat/lng, specialities, languages, source URL);
  - embeddings or plain keyword search (≤100 docs needs no vector DB);
  - an LLM call with a system prompt that answers only from the retrieved rows, cites the shop, and says "I don't know" otherwise.
- JA/EN bilingual output is a cheap win for inbound tourists.
- Shop data must be real and consented, or clearly labelled as sample data. Fake shops in a demo undermine credibility.

### Inferences — realistic, non-fake 3-day MVP
**Keep (one coherent loop, fits "TSUNAGU / connect"):**
1. Map (MapLibre + OSM, or Google free tier) with 15–30 hand-verified Nagasaki POIs: generic manhole-lid spots, shops, viewpoints. Each POI carries a real source URL.
2. Check-in "stamp" collection: GPS radius plus a QR fallback. Server logs feed a live heatmap/dashboard to show dispersion data (the evidence-backed value).
3. Locals↔tourists "speciality" posts: locals pin a recommendation; tourists collect it by visiting. This is the connection story and the differentiator vs the official ながさき照らす旅3 rally.
4. Optional, if time: a RAG chatbot over the same POI JSON, constrained to cite entries.

**Cut:**
- 3D Peace Park / Statue: no PLATEAU coverage for Nagasaki City, no licensed model, ethically fraught.
- Any Pokémon / ポケふた branding or assets.
- Points or rewards at memorial sites. Treat the Peace Park as a non-game "learn" pin, or exclude it.
- Souvenir-shop map as a separate product: fold it into POI categories.
- Hotel chatbot integrations: no data.

**Demo honesty:**
- Label sample data.
- Show real check-ins done by the team in Nagasaki during the event.
- Claim "data for dispersion", not "proven behaviour change".

### Gaps
- Nagasaki City POI open-data availability (BODIK / 長崎県オープンデータ portal) was not confirmed. The CKAN site blocked fetch.
- Licences of individual Sketchfab Nagasaki models were not checked.
- The exact Google Places (New) Essentials SKU threshold has conflicting figures (10k vs 1k).
- OSM licence, tile policy, MapLibre licence, anti-spoofing and RAG notes are from background knowledge, not verified this session.
