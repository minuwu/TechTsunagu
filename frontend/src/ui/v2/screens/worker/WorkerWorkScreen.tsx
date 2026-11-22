import { Link, useParams } from '@tanstack/react-router'
import { pick } from '@/domain/wakatta'
import { useWorkerWork } from '@/features/worker'
import { m } from '@/i18n/paraglide/messages'
import { useToastWhen } from '@/ui/v2/lib/toast'
import { ScreenSkeleton } from '@/ui/v2/components/Feedback'
import { Card, PageTitle, inputClass } from '@/ui/v2/components/Card'
import { LangToggle } from '@/ui/v2/components/LangToggle'
import { WorkerTabs } from '@/ui/v2/components/Nav'
import { OneQuestion } from '@/ui/v2/components/Question'
import { RecorderButton } from '@/ui/v2/components/RecorderButton'
import { StatusChip } from '@/ui/v2/components/StatusChip'
import { Button } from '@/ui/v2/components/ui/button'
import { fmtDate } from '@/ui/v2/lib/format'

/** Worker's Work tab: today's shift, Skill Passport, Morning Briefing, Near-miss Report. */
export function WorkerWorkScreen() {
  const { workerId = '' } = useParams({ strict: false })
  const w = useWorkerWork(workerId)
  useToastWhen(w.nearMiss.sent != null, m.near_miss_sent)
  if (w.loading) return <ScreenSkeleton />
  if (!w.worker) return <p>{m.worker_not_found()}</p>
  const lang = w.lang
  const nm = w.nearMiss

  return (
    <>
      <PageTitle>{w.worker.name}</PageTitle>
      <WorkerTabs workerId={workerId} />
      <div className="mb-4">
        <LangToggle value={lang} onChange={w.setLang} />
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <Card title={m.my_shift()} id="shift">
          {w.myShift.length === 0 ? (
            <p className="text-muted-foreground">{m.no_shift()}</p>
          ) : (
            <ul className="space-y-2">
              {w.myShift.map((s) => (
                <li
                  key={s.id}
                  className="flex items-center justify-between gap-2"
                >
                  <span lang={lang}>
                    {s.machine && pick(s.machine.name, lang)}
                    {' · '}
                    {s.shift === 'day' ? m.shift_day() : m.shift_night()}
                  </span>
                  <Link
                    to="/m/$machineId"
                    params={{ machineId: s.machineId }}
                    className="font-bold text-primary underline"
                  >
                    {m.open_machine()}
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>

        <Card title={m.briefing_today()} id="briefing">
          {!w.briefing ? (
            <p className="text-muted-foreground">{m.no_briefing()}</p>
          ) : !w.briefingOpened ? (
            <Button onClick={() => w.openBriefing()}>
              {m.open_briefing()}
            </Button>
          ) : (
            <div className="space-y-4">
              <p className="text-lg" lang={lang}>
                {pick(w.briefing.text, lang)}
              </p>
              <OneQuestion
                id={w.briefing.id}
                q={w.briefing.question}
                lang={lang}
                check={w.briefingCheck}
                done={w.briefingPassed}
              />
            </div>
          )}
        </Card>

        <Card
          title={m.skill_passport()}
          id="passport"
          className="md:col-span-2"
        >
          <p className="mb-3 text-sm text-muted-foreground">
            {m.skill_passport_body()}
          </p>
          <ul className="divide-y divide-[var(--rule)]">
            {w.passport.map((p) => (
              <li
                key={p.machine.id}
                className="flex flex-wrap items-center justify-between gap-2 py-2"
              >
                <span lang={lang}>{pick(p.machine.name, lang)}</span>
                <span className="flex items-center gap-3">
                  {p.passedAt && (
                    <span className="text-sm text-muted-foreground">
                      {m.passed_on({ date: fmtDate(p.passedAt) })}
                    </span>
                  )}
                  <StatusChip status={p.status} days={p.daysLeft} />
                </span>
              </li>
            ))}
          </ul>
        </Card>

        <Card
          title={m.near_miss_title()}
          id="nearmiss"
          tone="warning"
          className="md:col-span-2"
        >
          {nm.sent ? (
            <div className="space-y-3" aria-live="polite">
              <p className="font-bold text-success">{m.near_miss_sent()}</p>
              <p lang="ja">{nm.sent.text.ja}</p>
              <Button variant="outline" onClick={nm.again}>
                {m.near_miss_again()}
              </Button>
            </div>
          ) : (
            <div className="space-y-3">
              <p className="text-sm text-muted-foreground">
                {m.near_miss_body()}
              </p>
              <label className="block space-y-1">
                <span>{m.machine()}</span>
                <select
                  className={inputClass}
                  value={nm.machineId}
                  onChange={(e) => nm.setMachineId(e.target.value)}
                >
                  {w.machines.map((mc) => (
                    <option key={mc.id} value={mc.id}>
                      {pick(mc.name, lang)}
                    </option>
                  ))}
                </select>
              </label>
              <RecorderButton recorder={nm.recorder} />
              <label className="block space-y-1">
                <span>{m.or_type()}</span>
                <textarea
                  className={inputClass}
                  rows={2}
                  value={nm.text}
                  onChange={(e) => nm.setText(e.target.value)}
                />
              </label>
              <label className="block space-y-1">
                <span>{m.photo_optional()}</span>
                <input
                  type="file"
                  accept="image/*"
                  capture="environment"
                  onChange={(e) => void nm.setPhotoFile(e.target.files?.[0])}
                />
              </label>
              {nm.photo && (
                <img
                  src={nm.photo}
                  alt={m.photo_preview()}
                  className="max-h-40 rounded-md"
                />
              )}
              <Button
                disabled={!nm.canSend}
                loading={nm.sending}
                onClick={() => void nm.send()}
              >
                {m.near_miss_send()}
              </Button>
            </div>
          )}
        </Card>
      </div>
    </>
  )
}
