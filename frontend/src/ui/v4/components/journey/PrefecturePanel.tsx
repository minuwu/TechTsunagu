import type { PrefectureHighlight, PrefectureProps } from '@/features/map'
import { m } from '@/i18n/paraglide/messages'
import type { Lang } from '@/domain/wakatta'

/** Nagasaki as a museum label: both names, its famous place with a copyright-free photo, a short
 *  description and one fact. A single card, no list to browse: the section tells one story. */
export function PrefecturePanel({
  pref,
  highlight,
  lang,
}: {
  pref: PrefectureProps
  highlight: PrefectureHighlight
  lang: Lang
}) {
  const local = (p: { ja: string; en: string }) => (lang === 'ja' ? p.ja : p.en)

  return (
    <aside className="jp" aria-labelledby="jp-name">
      <article className="jp-card">
        <header className="jp-names">
          <h3 id="jp-name" className="jp-ja" lang="ja">
            {pref.ja}
          </h3>
          <p className="jp-en" lang="en">
            {pref.en}
          </p>
        </header>
        <figure className="jp-photo">
          <img
            src={highlight.image}
            alt={local(highlight)}
            width={640}
            height={427}
            loading="lazy"
            decoding="async"
          />
          <figcaption>
            {m.map_credit({
              artist: highlight.artist,
              license: highlight.license,
            })}
            {' · '}
            <a href={highlight.page} target="_blank" rel="noopener noreferrer">
              {m.map_source()}
            </a>
          </figcaption>
        </figure>
        <p className="journey-label">{m.map_famous()}</p>
        <h4 className="jp-place">{local(highlight)}</h4>
        <p className="jp-desc">{highlight.desc[lang]}</p>
        <div className="jp-fact">
          <p className="journey-label">{m.journey_fact()}</p>
          <p>{highlight.fact[lang]}</p>
        </div>
      </article>
    </aside>
  )
}
