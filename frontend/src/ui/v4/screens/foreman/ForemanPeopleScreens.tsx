import { pick } from '@/domain/wakatta'
import {
  useAccountsAdmin,
  useAssign,
  useGlossaryAdmin,
  useHeatmap,
  useSkillMatrix,
} from '@/features/foreman'
import { m } from '@/i18n/paraglide/messages'
import { EmptyState } from '@/ui/v4/components/Feedback'
import { Card, inputClass } from '@/ui/v4/components/Card'
import { StatusChip } from '@/ui/v4/components/StatusChip'
import { Button } from '@/ui/v4/components/ui/button'
import { fmtDate, uiLang } from '@/ui/v4/lib/format'
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
        <div className="grid gap-4 lg:grid-cols-2">
          {a.machines.map((mc) => (
            <Card key={mc.id} id={`as-${mc.id}`} title={pick(mc.name, lang)}>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
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
        </div>
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
              <Button loading={a.saving} onClick={() => void a.save()}>
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
      <Accounts />
    </ForemanPage>
  )
}

/** Real API only: who can sign in to this shop (XREQ-001). */
function Accounts() {
  const a = useAccountsAdmin()
  const lang = uiLang()
  if (!a.enabled) return null
  const langName = (code: string) =>
    new Intl.DisplayNames([lang], { type: 'language' }).of(code) ?? code
  const roleName = (k: 'worker' | 'foreman') =>
    k === 'worker' ? m.role_worker() : m.role_foreman()
  return (
    <Card id="accounts" title={m.accounts_title()} className="mt-4">
      <form
        className="grid gap-3 sm:grid-cols-5 sm:items-end"
        onSubmit={(e) => {
          e.preventDefault()
          void a.create()
        }}
      >
        <label className="block space-y-1">
          <span>{m.accounts_role()}</span>
          <select
            className={inputClass}
            value={a.form.kind}
            onChange={(e) => a.setKind(e.target.value as 'worker' | 'foreman')}
          >
            <option value="worker">{m.role_worker()}</option>
            <option value="foreman">{m.role_foreman()}</option>
          </select>
        </label>
        <label className="block space-y-1">
          <span>{m.accounts_name()}</span>
          <input
            className={inputClass}
            value={a.form.displayName}
            onChange={(e) => a.setName(e.target.value)}
          />
        </label>
        <label className="block space-y-1">
          <span>{m.accounts_language()}</span>
          <select
            className={inputClass}
            value={a.form.language}
            onChange={(e) => a.setLanguage(e.target.value)}
          >
            {a.languages.map((l) => (
              <option key={l} value={l}>
                {langName(l)}
              </option>
            ))}
          </select>
        </label>
        <label className="block space-y-1">
          <span>{m.accounts_first_pin()}</span>
          <input
            className={inputClass}
            inputMode="numeric"
            autoComplete="off"
            value={a.form.pin}
            onChange={(e) => a.setPin(e.target.value)}
          />
        </label>
        <Button type="submit" disabled={!a.canCreate || a.creating}>
          {m.accounts_add()}
        </Button>
      </form>
      {a.failed && (
        <p role="alert" className="mt-3 text-destructive">
          {m.accounts_failed()}
        </p>
      )}
      <ul className="mt-4 divide-y divide-border">
        {a.accounts.map((p) => (
          <li
            key={`${p.kind}-${p.id}`}
            className="flex flex-wrap items-center gap-3 py-3"
          >
            <span className="font-bold">{p.displayName}</span>
            <span className="text-sm text-muted-foreground">
              {roleName(p.kind)} · {langName(p.language)}
            </span>
            {!p.active && (
              <span className="text-sm text-destructive">
                {m.accounts_inactive()}
              </span>
            )}
            {p.mustChangePin && (
              <span className="text-sm text-muted-foreground">
                {m.accounts_must_change()}
              </span>
            )}
            <span className="ms-auto flex flex-wrap items-center gap-2">
              <input
                className={`${inputClass} w-28`}
                inputMode="numeric"
                autoComplete="off"
                aria-label={`${m.accounts_reset_pin()}: ${p.displayName}`}
                value={a.resetPin(p.id)}
                onChange={(e) => a.setResetPin(p.id, e.target.value)}
              />
              <Button
                size="sm"
                variant="outline"
                disabled={a.resetPin(p.id).length < 4 || a.saving}
                onClick={() => void a.saveResetPin(p)}
              >
                {m.accounts_reset_pin()}
              </Button>
              <Button
                size="sm"
                variant="outline"
                disabled={a.saving}
                onClick={() => a.setActive(p, !p.active)}
              >
                {p.active ? m.accounts_deactivate() : m.accounts_activate()}
              </Button>
            </span>
          </li>
        ))}
      </ul>
    </Card>
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
        <EmptyState mark="check">{m.no_confusions()}</EmptyState>
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
      <div className="grid gap-4 xl:grid-cols-[18rem_minmax(0,1fr)] xl:items-start">
        <div className="xl:sticky xl:top-24">
          <Card id="add" title={m.glossary_add()}>
            <form
              className="grid gap-3 sm:grid-cols-4 xl:grid-cols-1"
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
                <p className="text-destructive sm:col-span-4 xl:col-span-1">
                  {m.glossary_dup()}
                </p>
              )}
              <Button
                type="submit"
                disabled={!g.canAdd || g.duplicate || g.adding}
                className="sm:col-span-4 sm:justify-self-start xl:col-span-1 xl:justify-self-stretch"
              >
                {m.add()}
              </Button>
            </form>
          </Card>
        </div>
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
                  <td className="p-3 font-bold whitespace-nowrap" lang="ja">
                    {t.ja}
                  </td>
                  <td className="p-3 whitespace-nowrap" lang="ja">
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
      </div>
    </ForemanPage>
  )
}
