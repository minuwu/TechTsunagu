import { useMemo, useState } from 'react'
import type { CourseData } from '@/domain/courses/types'
import { plain, rubyParts } from '@/domain/course'
import { pick, type Tri } from '@/domain/wakatta'
import { useQuiz, useReel } from '@/features/course'
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
  course,
  done,
  onFinish,
}: {
  course: CourseData
  done: boolean
  onFinish: () => void
}) {
  const { panels: PANELS, handbook: HANDBOOK } = course
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
      <Glossary course={course} />
    </div>
  )
}

/** Door 2: pointing-and-calling reel with karaoke lines and a slow toggle. */
export function WatchDoor({
  course,
  done,
  onFinish,
}: {
  course: CourseData
  done: boolean
  onFinish: () => void
}) {
  const REEL = course.reel
  const reelJa = useMemo(() => REEL.map((r) => plain(r.ja)), [REEL])
  const r = useReel(reelJa, onFinish)
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
const GENERIC_SHAPE = {
  body: 'M16 20h68v22H16z',
  cap: 'M40 8h20v12H40z',
  bad: 'M36 22l10 8-8 6 10 6',
}

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
export function InspectDoor({
  course,
  onFinish,
}: {
  course: CourseData
  onFinish: () => void
}) {
  const INSPECT = course.inspect
  const lang = uiLang()
  const [i, setI] = useState(0)
  const [gap, setGap] = useState(0)
  const [seen, setSeen] = useState<Record<string, number[]>>({})
  const [right, setRight] = useState<Record<string, boolean>>({})
  const [wrong, setWrong] = useState(false)
  const item = INSPECT[i]!
  const shape = SHAPES[item.id] ?? GENERIC_SHAPE
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

/** Words to know: Japanese with reading, then English and Bengali. */
function Glossary({ course }: { course: CourseData }) {
  return (
    <Card title={m.course_glossary()}>
      <dl className="grid gap-x-4 gap-y-2 sm:grid-cols-2">
        {course.glossary.map((g) => (
          <div key={g.ja}>
            <dt className="font-bold">
              <span lang="ja">{g.ja}</span>{' '}
              <span lang="ja" className="text-muted-foreground">
                {g.reading}
              </span>
            </dt>
            <dd className="text-muted-foreground">{g.en}</dd>
          </div>
        ))}
      </dl>
    </Card>
  )
}

/** Extra Door: multiple choice, one question at a time. */
export function QuizDoor({
  course,
  best,
  onScore,
}: {
  course: CourseData
  best: number
  onScore: (n: number) => void
}) {
  const lang = uiLang()
  const items = course.quiz
  const q = useQuiz(items)
  const item = items[q.i]
  if (!item)
    return (
      <Card title={m.course_quiz_score({ n: q.score, total: items.length })}>
        <p className="text-muted-foreground">
          {m.course_quiz_best({ n: Math.max(best, q.score) })}
        </p>
        <Button
          className="mt-4"
          onClick={() => {
            onScore(q.score)
            q.restart()
          }}
        >
          {m.course_retry()}
        </Button>
      </Card>
    )
  return (
    <Card title={m.course_quiz_progress({ n: q.i + 1, total: items.length })}>
      <Line t={item.q} furi trans />
      <ul className="mt-4 space-y-2">
        {item.options.map((o, n) => {
          const right = q.picked !== null && n === item.answer
          const wrong = q.picked === n && n !== item.answer
          return (
            <li key={n}>
              <button
                type="button"
                disabled={q.picked !== null}
                onClick={() => q.answer(n)}
                className={cn(
                  'tile min-h-14 w-full px-4 py-2 text-left',
                  right && 'border-success text-success',
                  wrong && 'border-destructive text-destructive',
                )}
              >
                <Line t={o} furi trans />
              </button>
            </li>
          )
        })}
      </ul>
      {q.picked !== null && (
        <div className="mt-4 space-y-3" aria-live="polite">
          <p className="font-bold">
            {q.picked === item.answer
              ? m.course_quiz_right()
              : m.course_quiz_wrong()}
          </p>
          <p className="text-muted-foreground">{pick(item.why, lang)}</p>
          <Button
            onClick={() => {
              if (q.i + 1 >= items.length) onScore(q.score)
              q.next()
            }}
          >
            {q.i + 1 >= items.length
              ? m.course_quiz_finish()
              : m.course_quiz_next()}
          </Button>
        </div>
      )}
    </Card>
  )
}
