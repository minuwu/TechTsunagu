import { useState } from 'react'
import { HANDBOOK, PANELS, REEL, INSPECT } from '@/domain/courseContent'
import { plain, rubyParts } from '@/domain/course'
import { pick, type Tri } from '@/domain/wakatta'
import { useReel } from '@/features/course'
import { m } from '@/i18n/paraglide/messages'
import { Card } from '@/ui/v2/components/Card'
import { Button } from '@/ui/v2/components/ui/button'
import { uiLang } from '@/ui/v2/lib/format'
import { cn } from '@/ui/v2/lib/utils'

/** Japanese with optional furigana. */
function Ja({ s, furi }: { s: string; furi: boolean }) {
  return (
    <span lang="ja">
      {rubyParts(s).map(([base, reading], i) =>
        reading && furi ? (
          <ruby key={i}>
            {base}
            <rt>{reading}</rt>
          </ruby>
        ) : (
          base
        ),
      )}
    </span>
  )
}

function Toggle({
  on,
  onClick,
  children,
}: {
  on: boolean
  onClick: () => void
  children: string
}) {
  return (
    <Button
      size="sm"
      variant={on ? 'default' : 'outline'}
      aria-pressed={on}
      onClick={onClick}
    >
      {children}
    </Button>
  )
}

/** The line in Japanese, then its translation in the reader's language. */
function Line({
  t,
  furi,
  trans,
  big,
}: {
  t: Tri
  furi: boolean
  trans: boolean
  big?: boolean
}) {
  const lang = uiLang()
  return (
    <>
      <p
        className={cn(
          'font-bold',
          big ? 'text-2xl leading-loose' : 'text-xl leading-loose',
        )}
      >
        <Ja s={t.ja} furi={furi} />
      </p>
      {trans && lang !== 'ja' && (
        <p className="text-muted-foreground">{pick(t, lang)}</p>
      )}
    </>
  )
}

/** Door 1: Comic or Handbook, with furigana and translation toggles. */
export function ReadDoor({
  done,
  onFinish,
}: {
  done: boolean
  onFinish: () => void
}) {
  const [mode, setMode] = useState<'comic' | 'handbook'>('comic')
  const [furi, setFuri] = useState(true)
  const [trans, setTrans] = useState(true)
  const lines = mode === 'comic' ? PANELS.map((p) => p.text) : HANDBOOK
  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2">
        <Toggle on={mode === 'comic'} onClick={() => setMode('comic')}>
          {m.course_mode_comic()}
        </Toggle>
        <Toggle on={mode === 'handbook'} onClick={() => setMode('handbook')}>
          {m.course_mode_handbook()}
        </Toggle>
        <Toggle on={furi} onClick={() => setFuri(!furi)}>
          {m.course_furigana()}
        </Toggle>
        <Toggle on={trans} onClick={() => setTrans(!trans)}>
          {m.course_translation()}
        </Toggle>
      </div>
      <ol className="space-y-3">
        {lines.map((t, i) => (
          <li
            key={i}
            className={cn(
              'card-surface p-4',
              mode === 'comic' && PANELS[i]?.who === 'worker' && 'sm:ml-12',
              mode === 'comic' && PANELS[i]?.who === 'foreman' && 'sm:mr-12',
            )}
          >
            <Line t={t} furi={furi} trans={trans} />
          </li>
        ))}
      </ol>
      <Button onClick={onFinish} disabled={done}>
        {done ? m.course_done() : m.course_finish_read()}
      </Button>
    </div>
  )
}

const REEL_JA = REEL.map((r) => plain(r.ja))

/** Door 2: pointing-and-calling reel with karaoke lines and a slow toggle. */
export function WatchDoor({
  done,
  onFinish,
}: {
  done: boolean
  onFinish: () => void
}) {
  const r = useReel(REEL_JA, onFinish)
  return (
    <div className="space-y-4">
      <p className="text-muted-foreground">{m.course_watch_hint()}</p>
      <Card>
        <div aria-live="polite" className="min-h-28">
          <Line t={REEL[r.i]!} furi trans big />
        </div>
        <ol className="mt-4 flex gap-2" aria-hidden="true">
          {REEL.map((_, i) => (
            <li
              key={i}
              className={cn(
                'h-2 flex-1 rounded-full',
                i <= r.i && (r.playing || done) ? 'bg-primary' : 'bg-muted',
              )}
            />
          ))}
        </ol>
      </Card>
      <div className="flex flex-wrap gap-2">
        <Button onClick={r.toggle}>
          {r.playing ? m.course_pause() : m.course_play()}
        </Button>
        <Button variant="outline" onClick={r.replay}>
          {m.course_replay()}
        </Button>
        <Toggle on={r.slow} onClick={r.toggleSlow}>
          {m.course_speed()}
        </Toggle>
        {done && (
          <span className="self-center font-bold text-success">
            {m.course_done()}
          </span>
        )}
      </div>
    </div>
  )
}

// Two parts per item so the exploded-view slider can pull them apart; `bad` is drawn only on a defective sample
const SHAPES: Record<string, { body: string; cap: string; bad: string }> = {
  harness: {
    body: 'M10 8h20l20 32 20-32h20v6H74L50 52 26 14H10z',
    cap: 'M42 26h16v10H42z',
    bad: '',
  },
  wheel: {
    body: 'M50 10a22 22 0 1 0 .01 0zM50 26a6 6 0 1 1-.01 0z',
    cap: 'M38 6h24v5H38z',
    bad: 'M44 14l6 10-4 6 8 12',
  },
  clamp: {
    body: 'M18 24h64v16H18z',
    cap: 'M46 6h8v18h-8z',
    bad: 'M44 8l12 6-12 6 12 6',
  },
}

/** Door 4: tap the check points, pull the parts apart, then pass or reject. */
export function InspectDoor({ onFinish }: { onFinish: () => void }) {
  const lang = uiLang()
  const [i, setI] = useState(0)
  const [gap, setGap] = useState(0)
  const [seen, setSeen] = useState<Record<string, number[]>>({})
  const [right, setRight] = useState<Record<string, boolean>>({})
  const [wrong, setWrong] = useState(false)
  const item = INSPECT[i]!
  const shape = SHAPES[item.id]!
  const looked = seen[item.id] ?? []

  const judge = (reject: boolean) => {
    const ok = reject === item.defective
    setWrong(!ok)
    if (!ok) return
    const next = { ...right, [item.id]: true }
    setRight(next)
    if (INSPECT.every((x) => next[x.id])) onFinish()
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2">
        {INSPECT.map((x, n) => (
          <Toggle
            key={x.id}
            on={n === i}
            onClick={() => {
              setI(n)
              setWrong(false)
              setGap(0)
            }}
          >
            {`${right[x.id] ? '✓ ' : ''}${pick(x.name, lang)}`}
          </Toggle>
        ))}
      </div>
      <Card title={pick(item.name, lang)}>
        <svg
          viewBox="0 0 100 60"
          className="w-full max-w-md"
          role="img"
          aria-label={pick(item.name, lang)}
        >
          <g
            transform={`translate(0 ${gap / 4})`}
            className="fill-muted stroke-foreground"
            strokeWidth="0.8"
            fillRule="evenodd"
          >
            <path d={shape.body} />
          </g>
          <g
            transform={`translate(0 ${-gap / 2})`}
            className="fill-card stroke-foreground"
            strokeWidth="0.8"
          >
            <path d={shape.cap} />
          </g>
          {item.defective && (
            <path
              d={shape.bad}
              className="fill-none stroke-destructive"
              strokeWidth="1.6"
            />
          )}
          {item.spots.map((s, n) => (
            <foreignObject
              key={n}
              x={s.x - 5}
              y={s.y - 5}
              width="10"
              height="10"
            >
              <button
                type="button"
                aria-label={pick(s.check, lang)}
                className={cn(
                  'size-full rounded-full border-2 border-primary',
                  looked.includes(n) ? 'bg-primary' : 'bg-primary/20',
                )}
                onClick={() =>
                  setSeen({ ...seen, [item.id]: [...new Set([...looked, n])] })
                }
              />
            </foreignObject>
          ))}
        </svg>
        <label className="mt-2 flex items-center gap-3">
          {m.course_inspect_explode()}
          <input
            type="range"
            min={0}
            max={20}
            value={gap}
            onChange={(e) => setGap(+e.target.value)}
          />
        </label>
        <p className="mt-3 text-muted-foreground">{m.course_inspect_tap()}</p>
        <p className="text-sm">
          {m.course_inspect_checked({
            n: looked.length,
            total: item.spots.length,
          })}
        </p>
        <ul className="mt-2 space-y-1">
          {looked.map((n) => (
            <li key={n} className="font-bold">
              {pick(item.spots[n]!.check, lang)}
            </li>
          ))}
        </ul>
        <div className="mt-4 flex flex-wrap items-center gap-2">
          <Button
            disabled={
              looked.length < item.spots.length || right[item.id] === true
            }
            onClick={() => judge(false)}
          >
            {m.course_inspect_pass()}
          </Button>
          <Button
            variant="danger"
            disabled={
              looked.length < item.spots.length || right[item.id] === true
            }
            onClick={() => judge(true)}
          >
            {m.course_inspect_reject()}
          </Button>
          {right[item.id] && (
            <span className="font-bold text-success">
              {m.course_inspect_right()}
            </span>
          )}
          {wrong && (
            <span role="alert" className="font-bold text-destructive">
              {m.course_inspect_wrong()}
            </span>
          )}
        </div>
      </Card>
    </div>
  )
}
