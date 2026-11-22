import { useEffect, useState } from 'react'
import { REFLEX_MS, RING_HZ, slingTension, type Drill } from '@/domain/course'
import { DRILLS } from '@/domain/courseContent'
import { pick } from '@/domain/wakatta'
import { useReflex, useSpot } from '@/features/course'
import { m } from '@/i18n/paraglide/messages'
import { tone } from '@/lib/audio'
import { Card } from '@/ui/v2/components/Card'
import { Button } from '@/ui/v2/components/ui/button'
import { uiLang } from '@/ui/v2/lib/format'
import { cn } from '@/ui/v2/lib/utils'

type Props<K extends Drill['kind']> = {
  drill: Extract<Drill, { kind: K }>
  onPass: () => void
}

function Result({ ok, onRetry }: { ok: boolean; onRetry: () => void }) {
  return (
    <div role="status" className="mt-4 flex flex-wrap items-center gap-3">
      <span
        className={cn(
          'text-lg font-bold',
          ok ? 'text-success' : 'text-destructive',
        )}
      >
        {ok ? m.course_drill_pass() : m.course_drill_fail()}
      </span>
      <Button variant="outline" onClick={onRetry}>
        {m.course_retry()}
      </Button>
    </div>
  )
}

const shuffled = (n: number) =>
  [...Array(n).keys()].sort(() => Math.random() - 0.5)

function SpotDrill({ drill, onPass }: Props<'spot'>) {
  const lang = uiLang()
  const hazards = drill.items.filter((x) => x.hazard).length
  const s = useSpot(drill.secs, hazards, drill.need)
  useEffect(() => {
    if (s.passed) onPass()
  }, [s.passed, onPass])
  return (
    <div>
      {!s.running && !s.ended && (
        <Button onClick={s.start}>{m.course_start()}</Button>
      )}
      {(s.running || s.ended) && (
        <>
          <p className="font-bold">
            {m.course_time_left({ n: s.left })} ·{' '}
            {m.course_spot_found({ n: s.found })}
          </p>
          <ul className="mt-3 grid gap-2 sm:grid-cols-2">
            {drill.items.map((it, i) => (
              <li key={i}>
                <button
                  type="button"
                  disabled={!s.running}
                  onClick={() => s.tap(i, it.hazard)}
                  className={cn(
                    'tile min-h-12 w-full px-4 py-2 text-left font-bold',
                    s.taps[i] === true && 'border-success bg-success/10',
                    s.taps[i] === false &&
                      'border-destructive bg-destructive/10',
                  )}
                >
                  {pick(it.label, lang)}
                </button>
              </li>
            ))}
          </ul>
        </>
      )}
      {s.ended && <Result ok={s.passed} onRetry={s.start} />}
    </div>
  )
}

function SlingDrill({ drill, onPass }: Props<'sling'>) {
  const [deg, setDeg] = useState(100)
  const [lifted, setLifted] = useState(false)
  const kg = Math.round(slingTension(drill.loadKg, deg))
  const ok = kg <= drill.wllKg
  return (
    <div className="space-y-3">
      <label className="block font-bold">
        {m.course_sling_angle({ deg })}
        <input
          type="range"
          min={0}
          max={120}
          step={5}
          value={deg}
          className="mt-2 block w-full"
          onChange={(e) => {
            setDeg(+e.target.value)
            setLifted(false)
          }}
        />
      </label>
      <p>
        {m.course_sling_tension({ kg })} ·{' '}
        {m.course_sling_limit({ kg: drill.wllKg })}
      </p>
      <meter
        className="h-4 w-full"
        min={0}
        max={drill.loadKg}
        high={drill.wllKg}
        value={kg}
      />
      <Button
        onClick={() => {
          setLifted(true)
          if (ok) onPass()
        }}
      >
        {m.course_sling_lift()}
      </Button>
      {lifted && (
        <p
          role="status"
          className={cn('font-bold', ok ? 'text-success' : 'text-destructive')}
        >
          {ok ? m.course_sling_ok() : m.course_sling_bad()}
        </p>
      )}
    </div>
  )
}

function ReflexDrill({ onPass }: Props<'reflex'>) {
  const r = useReflex()
  useEffect(() => {
    if (r.passed) onPass()
  }, [r.passed, onPass])
  return (
    <div className="space-y-3">
      <p>{m.course_reflex_idle()}</p>
      {(r.state === 'idle' || r.state === 'early' || r.state === 'done') && (
        <Button onClick={r.start}>
          {r.state === 'idle' ? m.course_start() : m.course_retry()}
        </Button>
      )}
      {(r.state === 'wait' || r.state === 'go') && (
        <button
          type="button"
          onClick={r.hit}
          className={cn(
            'min-h-32 w-full rounded-xl text-3xl font-black text-white transition-colors',
            r.state === 'go' ? 'bg-destructive' : 'bg-muted-foreground',
          )}
        >
          {r.state === 'go' ? m.course_reflex_go() : m.course_reflex_wait()}
          <span className="block text-lg">{m.course_estop()}</span>
        </button>
      )}
      {r.state === 'early' && (
        <p role="alert" className="font-bold text-destructive">
          {m.course_reflex_early()}
        </p>
      )}
      {r.state === 'done' && (
        <p
          role="status"
          className={cn(
            'font-bold',
            r.passed ? 'text-success' : 'text-destructive',
          )}
        >
          {m.course_reflex_result({ ms: r.ms, limit: REFLEX_MS })} ·{' '}
          {r.passed ? m.course_drill_pass() : m.course_drill_fail()}
        </p>
      )}
    </div>
  )
}

function RingDrill({ drill, onPass }: Props<'ring'>) {
  const [order, setOrder] = useState(() => shuffled(drill.wheels.length))
  const [said, setSaid] = useState<Record<number, boolean>>({})
  const [checked, setChecked] = useState(false)
  const ok = order.every((w) => said[w] === drill.wheels[w])
  const retry = () => {
    setOrder(shuffled(drill.wheels.length))
    setSaid({})
    setChecked(false)
  }
  return (
    <div className="space-y-3">
      <p className="text-muted-foreground">{m.course_sound_note()}</p>
      <ul className="space-y-2">
        {order.map((w, n) => (
          <li
            key={w}
            className="card-surface flex flex-wrap items-center gap-2 p-3"
          >
            <Button
              variant="outline"
              onClick={() =>
                tone(
                  drill.wheels[w] ? RING_HZ.intact : RING_HZ.cracked,
                  drill.wheels[w] ? 1.2 : 0.15,
                )
              }
            >
              {m.course_ring_tap({ n: n + 1 })}
            </Button>
            <Button
              size="sm"
              variant={said[w] === true ? 'default' : 'outline'}
              aria-pressed={said[w] === true}
              onClick={() => setSaid({ ...said, [w]: true })}
            >
              {m.course_ring_ok()}
            </Button>
            <Button
              size="sm"
              variant={said[w] === false ? 'danger' : 'outline'}
              aria-pressed={said[w] === false}
              onClick={() => setSaid({ ...said, [w]: false })}
            >
              {m.course_ring_reject()}
            </Button>
          </li>
        ))}
      </ul>
      <Button
        disabled={Object.keys(said).length < order.length}
        onClick={() => {
          setChecked(true)
          if (ok) onPass()
        }}
      >
        {m.course_check()}
      </Button>
      {checked && <Result ok={ok} onRetry={retry} />}
    </div>
  )
}

function SeqDrill({ drill, onPass }: Props<'seq'>) {
  const lang = uiLang()
  const n = drill.steps.length
  const [order, setOrder] = useState(() => shuffled(n))
  const [picked, setPicked] = useState<number[]>([])
  const [mistakes, setMistakes] = useState(0)
  const finished = picked.length === n
  const ok = finished && mistakes <= 1
  useEffect(() => {
    if (ok) onPass()
  }, [ok, onPass])
  const tap = (i: number) =>
    i === picked.length ? setPicked([...picked, i]) : setMistakes(mistakes + 1)
  return (
    <div className="space-y-3">
      <p className="text-muted-foreground">{m.course_seq_hint()}</p>
      <ul className="space-y-2">
        {order.map((i) => {
          const at = picked.indexOf(i)
          return (
            <li key={i}>
              <button
                type="button"
                disabled={at >= 0 || finished}
                onClick={() => tap(i)}
                className={cn(
                  'tile flex min-h-12 w-full items-center gap-3 px-4 py-2 text-left font-bold',
                  at >= 0 && 'border-success bg-success/10',
                )}
              >
                <span className="grid size-7 shrink-0 place-items-center rounded-full bg-muted text-sm">
                  {at >= 0 ? at + 1 : ''}
                </span>
                {pick(drill.steps[i]!, lang)}
              </button>
            </li>
          )
        })}
      </ul>
      <p aria-live="polite">{m.course_mistakes({ n: mistakes })}</p>
      {finished && (
        <Result
          ok={ok}
          onRetry={() => {
            setOrder(shuffled(n))
            setPicked([])
            setMistakes(0)
          }}
        />
      )}
    </div>
  )
}

function Engine({ drill, onPass }: { drill: Drill; onPass: () => void }) {
  switch (drill.kind) {
    case 'spot':
      return <SpotDrill drill={drill} onPass={onPass} />
    case 'sling':
      return <SlingDrill drill={drill} onPass={onPass} />
    case 'reflex':
      return <ReflexDrill drill={drill} onPass={onPass} />
    case 'ring':
      return <RingDrill drill={drill} onPass={onPass} />
    case 'seq':
      return <SeqDrill drill={drill} onPass={onPass} />
  }
}

/** Door 3: the nine drills. */
export function PlayDoor({
  passed,
  need,
  onPass,
}: {
  passed: string[]
  need: number
  onPass: (id: string) => void
}) {
  const lang = uiLang()
  const [open, setOpen] = useState<string | null>(null)
  const drill = DRILLS.find((d) => d.id === open)
  if (drill)
    return (
      <Card title={pick(drill.name, lang)}>
        <p className="mb-4 text-muted-foreground">{pick(drill.goal, lang)}</p>
        <Engine key={drill.id} drill={drill} onPass={() => onPass(drill.id)} />
        <Button className="mt-6" variant="ghost" onClick={() => setOpen(null)}>
          {m.course_back()}
        </Button>
      </Card>
    )
  return (
    <div className="space-y-3">
      <p className="font-bold">
        {m.course_drills_passed({
          n: passed.length,
          total: DRILLS.length,
          need,
        })}
      </p>
      <ul className="grid gap-2 sm:grid-cols-2">
        {DRILLS.map((d) => (
          <li key={d.id}>
            <button
              type="button"
              onClick={() => setOpen(d.id)}
              className="tile flex min-h-14 w-full items-center justify-between gap-3 px-4 py-2 text-left font-bold"
            >
              {pick(d.name, lang)}
              {passed.includes(d.id) && (
                <span className="text-success">{m.course_drill_pass()}</span>
              )}
            </button>
          </li>
        ))}
      </ul>
    </div>
  )
}
