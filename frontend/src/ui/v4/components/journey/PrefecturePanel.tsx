import type { Feature, Geometry } from 'geojson'
import type { PrefectureHighlight, PrefectureProps } from '@/features/map'
import { m } from '@/i18n/paraglide/messages'
import type { Lang } from '@/domain/wakatta'

type Pref = Feature<Geometry, PrefectureProps>

/** The chosen prefecture as a museum label: both names, its famous place with a copyright-free photo,
 *  a short description and a fact. Previous / next walk the 47 in order; the list is the keyboard route. */
export function PrefecturePanel({
  sorted,
  selected,
  highlight,
  lang,
  onSelect,
}: {
  sorted: Pref[]
  selected: string
  highlight: PrefectureHighlight | undefined
  lang: Lang
  onSelect: (id: string) => void
}) {
  const i = sorted.findIndex((p) => p.properties.id === selected)
  const pref = sorted[i]?.properties
  const prev = sorted[(i - 1 + sorted.length) % sorted.length]?.properties
  const next = sorted[(i + 1) % sorted.length]?.properties
  const local = (p: { ja: string; en: string }) => (lang === 'ja' ? p.ja : p.en)

  return (
    <aside className="jp" aria-labelledby="jp-name" data-lenis-prevent>
      {/* always mounted, so each choice is announced */}
      <p className="sr-only" aria-live="polite">
        {pref && highlight ? `${local(pref)}: ${local(highlight)}` : ''}
      </p>

      <div className="jp-choose">
        <label htmlFor="jp-select" className="journey-label">
          {m.map_choose()}
        </label>
        <select
          id="jp-select"
          value={selected}
          onChange={(e) => onSelect(e.target.value)}
        >
          {sorted.map((p) => (
            <option key={p.properties.id} value={p.properties.id}>
              {local(p.properties)}
            </option>
          ))}
        </select>
      </div>

      {pref && highlight && (
        <article key={selected} className="jp-card">
          <header className="jp-names">
            <p className="journey-label tnum">
              {String(i + 1).padStart(2, '0')} / {sorted.length}
            </p>
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
              decoding="async"
            />
            <figcaption>
              {m.map_credit({
                artist: highlight.artist,
                license: highlight.license,
              })}
              {' · '}
              <a
                href={highlight.page}
                target="_blank"
                rel="noopener noreferrer"
              >
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
      )}

      {prev && next && (
        <nav className="jp-step" aria-label={m.journey_step_label()}>
          <button type="button" onClick={() => onSelect(prev.id)}>
            <span aria-hidden="true">←</span>{' '}
            {m.journey_prev({ name: local(prev) })}
          </button>
          <button type="button" onClick={() => onSelect(next.id)}>
            {m.journey_next({ name: local(next) })}{' '}
            <span aria-hidden="true">→</span>
          </button>
        </nav>
      )}
    </aside>
  )
}
