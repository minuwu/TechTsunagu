# Map data

`japan-prefectures.topo.json`: Japan's 47 prefectures as TopoJSON (object `prefectures`; properties `id` = ISO 3166-2 code such as `JP-42`, `ja` = 長崎県, `en` = Nagasaki).

- **Source:** [Natural Earth](https://www.naturalearthdata.com/) 1:10m Cultural Vectors, Admin 1 – States, Provinces (`ne_10m_admin_1_states_provinces`, downloaded 2026-11-23 from `naciscdn.org`).
- **License:** public domain. "No permission is needed to use Natural Earth" ([terms of use](https://www.naturalearthdata.com/about/terms-of-use/)); credit is optional and given here.
- **Built with** [mapshaper](https://github.com/mbloch/mapshaper) 0.6.113 (MPL-2.0), not a project dependency:

```sh
npx mapshaper@0.6.113 ne_10m_admin_1_states_provinces.shp name=prefectures \
  -filter "adm0_a3 === 'JPN'" \
  -each "id=iso_3166_2, ja=name_ja, en=(name_en || name).replace(/ Prefecture$/, '')" \
  -filter-fields id,ja,en -simplify 45% keep-shapes -clean \
  -o format=topojson quantization=1e5 japan-prefectures.topo.json
```

Natural Earth draws boundaries at small scale; the map is decorative and for orientation, not a survey map.

`east-asia-50m.topo.json`: Japan's neighbours, drawn around the prefecture map in the landing's "Journey to Nagasaki" (object `countries`; property `id` = ISO 3166-1 alpha-3).

- **Source:** Natural Earth 1:50m Cultural Vectors, Admin 0 – Countries (`ne_50m_admin_0_countries`). Public domain, as above.
- **Built with** mapshaper 0.6.113 (Japan left out, clipped to East Asia):

```sh
npx mapshaper@0.6.113 ne_50m_admin_0_countries.shp name=countries -filter "ADM0_A3 !== 'JPN'"   -clip bbox=112,18,160,52 -each "id=ADM0_A3" -filter-fields id -simplify 60% keep-shapes -clean   -o format=topojson quantization=1e5 east-asia-50m.topo.json
```

`prefecture-highlights.json`: one famous place per prefecture for the journey's panel (`id`; `ja`, `en` = the place; `image`, `license`, `artist`, `page` = the photo and its credit; `desc`, `fact` = a short description and a fact, each `{ en, ja }`). Written for this project from well-established public facts; photos and credits: see `public/media/README.md`.
