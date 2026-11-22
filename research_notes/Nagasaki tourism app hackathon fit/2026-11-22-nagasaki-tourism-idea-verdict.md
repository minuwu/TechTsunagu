# Nagasaki tourism app: hackathon fit verdict

Research date: 2026-11-22. Judging axes are taken from [docs/FRONTEND_REGULATIONS.md](../../docs/FRONTEND_REGULATIONS.md): 実用性, 創造性, ユーザビリティ, 完成度 and プレゼン.

## Idea as pitched

The pitch has six parts:
- a 3D model of the Peace Park area (heiwa)
- a community where locals and tourists share local specialities
- a souvenir map
- a Poké Lid stamp collection
- gamified maps
- an AI chatbot that finds shops and hotels

## Verdict

As pitched, the idea is not novel. Every part already exists, and most parts are run by incumbents with far more resources and official data.

| Part | Existing solution | Source |
|---|---|---|
| 3D peace and atomic-bomb model | The Nagasaki Archive, a 3D map of the bombing with survivor portraits, has run since 2010. | [Media Arts Festival](https://www.bunka.go.jp/j-mediaarts-festival/en/award/single/nagasaki-archive/index.html), [Bentley 2025](https://www.bentley.com/en/blog/the-future-of-memory-japanese-professor-uses-3d-technology-to-redefine-how-we-experience-history/) |
| Poké Lid stamps | Pokémon GO has had a "GO Stamp Rally" for Poké Lids since 2025-04-13. | [ITmedia](https://www.itmedia.co.jp/mobile/spv/2504/10/news103.html), [Pokémon GO](https://pokemongo.com/ja/news/pokelid_full) |
| Gamified Nagasaki stamp rally | The prefecture runs 「ながさき照らす旅3」 from 2026-08-27 to 2027-02-28. It covers Denryu Poké Lids and about 40 spots across 21 municipalities, with GPS check-in, quiz and photo missions, and prizes of local goods. It runs in the browser, so no install is needed. | [Kyodo PR](https://kyodonewsprwire.jp/release/202608264772), [pref PDF](https://www.pref.nagasaki.jp/shared/uploads/2025/11/1763107646.pdf) |
| Other rallies | Zenrin's STLOCAL runs a Nagasaki City rally from 2026-08 to 2027-03. "my route" ran the 2025 culture festival rally. | [Travel Voice](https://www.travelvoice.jp/20250122-157002), [sakigake](https://www.sakigake.jp/news/article/20260827PR0008/) |
| AI chatbot for shops and hotels | Google Maps "Ask Maps" (Gemini) has been live in Japan since 2026-08-07. Since November 2026 it also builds itineraries and checks availability in real time. JNTO runs a GenAI travel chatbot. | [Google JP blog](https://blog.google/intl/ja-jp/products/connect-communicate/japanese-ask-maps/), [JNTO](https://www.japan.travel/en/terms-of-use-travel-chatbot) |
| Locals-to-tourists community | Ask Japan, TOMOGO! and JeePe already exist. Ask Japan has only 3 App Store ratings, which shows the two-sided cold-start problem. | [Ask Japan](https://apps.apple.com/us/app/ask-japan-local-travel-q-a/id6748002901), [TOMOGO](https://www.tokyoweekender.com/tw-collabs/tomogos-local-experts-tokyo-best-hidden-gems/) |
| "Discover Japan" | This is DENTSU's magazine app, which publishes editorial content. It is not a navigation tool. | [App Store](https://apps.apple.com/us/app/-/id972428725) |

## Why someone would not switch from Google Maps

For generic discovery of shops and hotels, nobody switches. Google has the place data, the reviews, live transit, and now Gemini itineraries in Japanese. A hackathon chatbot built on scraped data loses on every axis.

## Blocking risk: Pokémon IP

Poké Lids, Denryu and the Pokémon art belong to The Pokémon Company and Niantic. The prefecture already runs the licensed rally. Do not use Pokémon assets in the build or the demo. At most, link out to the official rally.

## Score against the judging axes (estimate)

- プレゼン: strong. A 3D model and maps demo well.
- 創造性: weak. Every part has a precedent.
- 実用性: weak. Google, Pokémon GO and the prefecture already cover the use case.
- 完成度: at risk. The pitch lists six features plus "and many more", which is feature soup for 1 to 2 days of work.

## Where novelty might exist (candidates to test, not conclusions)

Run this test on each candidate: will Google, Niantic or the prefecture ship it by demo day? If yes, cut it.

1. **Fragmentation.** Official rallies are time-boxed campaigns spread across 3 or 4 apps: 照らす旅 (web), STLOCAL, my route, and Pokémon GO. A single permanent "Nagasaki passport" that persists across campaigns is a gap.
2. **Curation by locals, not tourist reviews.** Map reviews mostly come from visitors. Picks by verified locals (for example, "where locals buy kasutera and karasumi") is a narrow and demoable wedge.
3. **Spending at small shops.** Few tools send visitors to small specialty and souvenir shops. That is TOMOGO's stated thesis, so it is validated but still open in Nagasaki. Cruise growth is pushing visitor numbers up ([pref 2025 Q2 report](https://www.pref.nagasaki.lg.jp/shared/uploads/2025/09/1757982394.pdf)).

None of the sources searched gave cruise-passenger dwell time. Any claim that short stays are a problem is unverified.
