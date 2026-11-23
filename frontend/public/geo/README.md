# Map data

`japan-prefectures.topo.json`: Japan's 47 prefectures as TopoJSON (object `prefectures`; properties `id` = ISO 3166-2 code such as `JP-42`, `ja` = 長崎県, `en` = Nagasaki).

- **Source:** [Natural Earth](https://www.naturalearthdata.com/) 1:10m Cultural Vectors, Admin 1 – States, Provinces (`ne_10m_admin_1_states_provinces`, downloaded 2026-11-23 from `naciscdn.org`).
- **License:** public domain. "No permission is needed to use Natural Earth" ([terms of use](https://www.naturalearthdata.com/about/terms-of-use/)); credit is optional and given here.
- **Built with** [mapshaper](https://github.com/mbloch/mapshaper) 0.6.113 (MPL-2.0), not a project dependency:

```sh
npx mapshaper@0.6.113 ne_10m_admin_1_states_provinces.shp name=prefectures \
  -filter "adm0_a3 === 'JPN'" \
  -each "id=iso_3166_2, ja=name_ja, en=(name_en || name).replace(/ Prefecture$/, '')" \
  -filter-fields id,ja,en -simplify 12% keep-shapes -clean \
  -o format=topojson quantization=1e5 japan-prefectures.topo.json
```

Natural Earth draws boundaries at small scale; the map is decorative and for orientation, not a survey map.
