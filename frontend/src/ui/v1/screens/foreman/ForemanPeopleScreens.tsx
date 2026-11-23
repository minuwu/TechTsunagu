import { pick } from '@/domain/wakatta'
import {
  useAssign,
  useGlossaryAdmin,
  useHeatmap,
  useSkillMatrix,
} from '@/features/foreman'
import { m } from '@/i18n/paraglide/messages'
import { Card, inputClass } from '@/ui/v1/components/Card'
import { StatusChip } from '@/ui/v1/components/StatusChip'
import { Button } from '@/ui/v1/components/ui/button'
import { fmtDate, uiLang } from '@/ui/v1/lib/format'
import { ForemanPage } from './ForemanBoardScreen'

const SHIFTS = ['day', 'night'] as const
const shiftLabel = (s: (typeof SHIFTS)[number]) =>
  s === 'day' ? m.shift_day() : m.shift_night()

/** Shift Assignments for today; warns when the Worker has no valid Sign-off. */
export function ForemanAssignScreen() {
  const a = useAssign()
  const lang = uiLang()
  return (
    <ForemanPage
      title={m.assign_title()}
      sub={m.assign_sub({ date: fmtDate(a.today, lang) })}
    >
      <div className="space-y-4">
        {a.machines.map((mc) => (
          <Card key={mc.id} id={`as-${mc.id}`} title={pick(mc.name, lang)}>
            <div className="grid gap-3 sm:grid-cols-2">
              {SHIFTS.map((s) => {
                const v = a.value(mc.id, s)
                const st = a.statusOf(mc.id, v)
                return (
                  <label key={s} className="block space-y-1">
                    <span className="font-bold">{shiftLabel(s)}</span>
                    <select
                      className={inputClass}
                      value={v}
                      onChange={(e) => a.set(mc.id, s, e.target.value)}
                    >
                      <option value="">{m.nobody()}</option>
                      {a.workers.map((w) => (
                        <option key={w.id} value={w.id}>
                          {w.name}
                        </option>
                      ))}
                    </select>
                    {v && (
                      <span className="flex items-center gap-2 text-sm">
                        <StatusChip status={st} />
                        {(st === 'expired' || st === 'none') && (
                          <span className="font-bold text-destructive">
                            {m.will_be_locked()}
                          </span>
                        )}
                      </span>
                    )}
                  </label>
                )
              })}
            </div>
          </Card>
        ))}
        {a.saved && (
          <p className="font-bold text-success" aria-live="polite">
            {m.saved()}
          </p>
        )}
        {a.confirming ? (
          <Card tone="warning" id="confirm" title={m.confirm_changes()}>
            <ul className="mb-3 list-disc pl-6">
              {a.changes.map((c) => (
                <li key={c.key}>
                  {c.machine && pick(c.machine.name, lang)}
                  {' · '}
                  {shiftLabel(c.shift)}
                  {' → '}
                  {c.workerName ?? m.nobody()}
                </li>
              ))}
            </ul>
            <div className="flex gap-2">
              <Button variant="outline" onClick={a.cancel}>
                {m.back()}
              </Button>
              <Button disabled={a.saving} onClick={() => void a.save()}>
                {m.save()}
              </Button>
            </div>
          </Card>
        ) : (
          <Button disabled={!a.dirty} onClick={a.toConfirm}>
            {m.review_changes()}
          </Button>
        )}
      </div>
    </ForemanPage>
  )
}

/** Skill Matrix (力量表). */
export function ForemanSkillsScreen() {
  const s = useSkillMatrix()
  const lang = uiLang()
  return (
    <ForemanPage title={m.skills_title()} sub={m.skills_sub()}>
      <div className="overflow-x-auto rounded-lg border border-border bg-card">
        <table className="w-full text-left">
          <caption className="sr-only">{m.skills_title()}</caption>
          <thead>
            <tr className="border-b border-border">
              <th scope="col" className="p-3">
                {m.worker()}
              </th>
              {s.machines.map((mc) => (
                <th key={mc.id} scope="col" className="p-3">
                  {pick(mc.name, lang)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {s.rows.map((r) => (
              <tr
                key={r.worker.id}
                className="border-b border-border last:border-0"
              >
                <th scope="row" className="p-3">
                  {r.worker.name}
                </th>
                {r.cells.map((c) => (
                  <td key={c.machineId} className="p-3">
                    <StatusChip status={c.status} days={c.daysLeft} />
                    {c.passedAt && (
                      <span className="mt-1 block text-xs text-muted-foreground">
                        {fmtDate(c.passedAt, lang)}
                      </span>
                    )}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </ForemanPage>
  )
}

/** Confusion Heatmap: which Glossary Terms trip Workers up, on which Machine. */
export function ForemanHeatmapScreen() {
  const h = useHeatmap()
  const lang = uiLang()
  return (
    <ForemanPage title={m.heatmap_title()} sub={m.heatmap_sub()}>
      {h.top && (
        <Card tone="warning" id="top" className="mb-4">
          <p className="text-lg">
            {m.heatmap_top({ term: h.top.term.ja, count: h.top.total })}{' '}
            <span lang={lang}>
              {lang === 'ja' ? h.top.term.reading : h.top.term[lang]}
            </span>
          </p>
          <p className="text-sm text-muted-foreground">{m.heatmap_tip()}</p>
        </Card>
      )}
      {h.rows.length === 0 ? (
        <p className="text-muted-foreground">{m.no_confusions()}</p>
      ) : (
        <div className="overflow-x-auto rounded-lg border border-border bg-card">
          <table className="w-full text-left">
            <caption className="sr-only">{m.heatmap_title()}</caption>
            <thead>
              <tr className="border-b border-border">
                <th scope="col" className="p-3">
                  {m.term()}
                </th>
                {h.machines.map((mc) => (
                  <th key={mc.id} scope="col" className="p-3">
                    {pick(mc.name, lang)}
                  </th>
                ))}
                <th scope="col" className="p-3">
                  {m.total()}
                </th>
              </tr>
            </thead>
            <tbody>
              {h.rows.map((r) => (
                <tr
                  key={r.term.id}
                  className="border-b border-border last:border-0"
                >
                  <th scope="row" className="p-3" lang="ja">
                    {r.term.ja}
                    <span className="block text-xs font-normal text-muted-foreground">
                      {r.term.reading}
                    </span>
                  </th>
                  {r.cells.map((c) => (
                    <td
                      key={c.machineId}
                      className="p-3 text-center font-bold"
                      style={{
                        background: c.count
                          ? `color-mix(in oklch, var(--color-warning) ${Math.round((c.count / h.max) * 70) + 10}%, transparent)`
                          : undefined,
                      }}
                    >
                      {c.count || ''}
                    </td>
                  ))}
                  <td className="p-3 font-bold">{r.total}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </ForemanPage>
  )
}

/** Glossary Terms: locked translations the AI must use. */
export function ForemanGlossaryScreen() {
  const g = useGlossaryAdmin()
  const fields = [
    ['ja', m.g_ja()],
    ['reading', m.g_reading()],
    ['en', m.g_en()],
  ] as const
  return (
    <ForemanPage title={m.glossary_title()} sub={m.glossary_sub()}>
      <Card id="add" title={m.glossary_add()} className="mb-4">
        <form
          className="grid gap-3 sm:grid-cols-4"
          onSubmit={(e) => {
            e.preventDefault()
            void g.add()
          }}
        >
          {fields.map(([k, label]) => (
            <label key={k} className="block space-y-1">
              <span>{label}</span>
              <input
                className={inputClass}
                lang={k === 'reading' ? 'ja' : k}
                value={g.form[k]}
                onChange={(e) => g.setField(k, e.target.value)}
              />
            </label>
          ))}
          {g.duplicate && (
            <p className="text-destructive sm:col-span-4">{m.glossary_dup()}</p>
          )}
          <Button
            type="submit"
            disabled={!g.canAdd || g.duplicate || g.adding}
            className="sm:col-span-4 sm:justify-self-start"
          >
            {m.add()}
          </Button>
        </form>
      </Card>
      <div className="overflow-x-auto rounded-lg border border-border bg-card">
        <table className="w-full text-left">
          <caption className="sr-only">{m.glossary_title()}</caption>
          <thead>
            <tr className="border-b border-border">
              {fields.map(([k, label]) => (
                <th key={k} scope="col" className="p-3">
                  {label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {g.glossary.map((t) => (
              <tr key={t.id} className="border-b border-border last:border-0">
                <td className="p-3 font-bold" lang="ja">
                  {t.ja}
                </td>
                <td className="p-3" lang="ja">
                  {t.reading}
                </td>
                <td className="p-3" lang="en">
                  {t.en}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </ForemanPage>
  )
}
