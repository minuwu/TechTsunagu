import { Link } from '@tanstack/react-router'
import { pick } from '@/domain/wakatta'
import { useMachineCheck } from '@/features/machine'
import { useCurrentWorker } from '@/features/session'
import { m } from '@/i18n/paraglide/messages'
import { ScreenSkeleton, Steps } from '@/ui/v4/components/Feedback'
import { Card, PageTitle } from '@/ui/v4/components/Card'
import { Chatbot } from '@/ui/v4/components/Chatbot'
import { Icon, Mark } from '@/ui/v4/components/Icon'
import { LangToggle } from '@/ui/v4/components/LangToggle'
import { Question } from '@/ui/v4/components/Question'
import { StatusChip } from '@/ui/v4/components/StatusChip'
import { StatePlate } from '@/ui/v4/components/StatePlate'
import { Button } from '@/ui/v4/components/ui/button'
import { uiLang } from '@/ui/v4/lib/format'

/** The page behind a Machine's QR sticker: read, pass the Check, unlock the Panel. */
export function MachineScreen() {
  const session = useCurrentWorker()
  const c = useMachineCheck(session.workerId)
  const ui = uiLang()

  if (c.loading) return <ScreenSkeleton />
  if (c.notFound || !c.machine) return <p>{m.machine_not_found()}</p>
  const name = pick(c.machine.name, ui)

  // First scan on this phone: pick who you are (no login)
  if (!c.worker)
    return (
      <>
        <PageTitle sub={m.pick_worker_sub()}>{name}</PageTitle>
        <Card title={m.pick_worker()} id="pick">
          <ul className="space-y-2">
            {c.workers.map((w) => (
              <li key={w.id}>
                <button
                  type="button"
                  className="tile group flex min-h-12 w-full items-center justify-between gap-3 px-4 py-2 text-left font-bold"
                  onClick={() => session.setWorkerId(w.id)}
                >
                  {w.name}
                  <Mark
                    id="arrow"
                    className="size-5 text-muted-foreground transition-transform duration-200 group-hover:translate-x-0.5 group-hover:text-primary"
                  />
                </button>
              </li>
            ))}
          </ul>
        </Card>
      </>
    )

  const lang = c.lang
  return (
    <>
      <PageTitle sub={m.machine_hello({ name: c.worker.name })}>
        {name}
      </PageTitle>
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <LangToggle value={lang} onChange={c.setLang} />
        <Button variant="ghost" size="sm" onClick={session.clear}>
          {m.not_me()}
        </Button>
      </div>
      {c.status && (
        <p className="mb-4 flex flex-wrap items-center gap-2">
          {m.your_status()}{' '}
          <StatusChip status={c.status.status} days={c.status.daysLeft} />
        </p>
      )}

      {c.note && (
        <Steps
          label={m.steps_label()}
          steps={[m.step_read(), m.step_check(), m.step_done()]}
          current={
            c.step === 'read'
              ? 0
              : c.step === 'check' || !c.result?.passed
                ? 1
                : 3
          }
        />
      )}
      {!c.note ? (
        <Card tone="warning">{m.no_note()}</Card>
      ) : c.step === 'read' ? (
        <Card title={m.instruction_title({ v: c.note.versionNo })} id="read">
          <ol className="space-y-3" lang={lang}>
            {c.note.steps.map((s, i) => (
              <li
                key={i}
                className="flex items-start gap-3 inset-card p-3.5 text-lg"
              >
                <span className="tnum grid size-9 shrink-0 place-items-center rounded-full bg-[var(--shell)] text-lg font-bold text-[var(--shell-foreground)]">
                  {i + 1}
                </span>
                {s.icon && (
                  <Icon
                    id={s.icon}
                    tip
                    className="mt-0.5 size-8 text-[var(--indigo)]"
                  />
                )}
                <span className="min-w-0 pt-0.5">{pick(s.text, lang)}</span>
              </li>
            ))}
          </ol>
          <div className="mt-4 flex flex-wrap gap-2">
            <Button className="min-h-14 flex-1 text-lg" onClick={c.startCheck}>
              {m.understood_take_check()}
            </Button>
          </div>
        </Card>
      ) : c.step === 'check' ? (
        <Card title={m.check_title()} id="check">
          <div className="space-y-6">
            {c.note.questions.map((q, i) => {
              const term = c.termOf(q)
              return (
                <div key={q.id} className="space-y-2">
                  <Question
                    q={q}
                    lang={lang}
                    name={q.id}
                    value={c.answers[i]}
                    onChange={(o) => c.answer(i, o)}
                  />
                  {term && (
                    <p className="text-sm text-muted-foreground">
                      <span lang="ja">{term.ja}</span>
                      {` (${term.reading}) = `}
                      <span lang={lang}>
                        {lang === 'ja' ? term.reading : term[lang]}
                      </span>
                    </p>
                  )}
                </div>
              )
            })}
          </div>
          <div className="mt-4 flex flex-wrap gap-2">
            <Button variant="outline" onClick={c.backToRead}>
              {m.back_to_read()}
            </Button>
            <Button
              disabled={!c.canSubmit}
              loading={c.submitting}
              onClick={() => void c.submit()}
            >
              {m.check_submit()}
            </Button>
          </div>
        </Card>
      ) : c.result?.passed ? (
        <Card tone="success" title={m.passed_title()} id="result">
          <StatePlate state="ready" className="mb-4">
            <span className="font-bold">{c.worker.name}</span>
          </StatePlate>
          <p className="mb-4 text-lg">{m.passed_body()}</p>
          <Link
            to="/panel/$machineId"
            params={{ machineId: c.machine.id }}
            className="font-bold text-primary underline"
          >
            {m.see_panel()}
          </Link>
        </Card>
      ) : (
        <Card tone="danger" title={m.failed_title()} id="result">
          <StatePlate state="locked" className="mb-4">
            <span className="font-bold">{c.worker.name}</span>
          </StatePlate>
          <p className="mb-2">{m.failed_body()}</p>
          <ul className="mb-4 list-disc pl-6" lang={lang}>
            {c.result?.wrong.map((q) => (
              <li key={q.id}>{pick(q.prompt, lang)}</li>
            ))}
          </ul>
          <Button onClick={c.backToRead}>{m.read_again()}</Button>
        </Card>
      )}

      <p className="mt-6">
        <Link
          to="/me/$workerId"
          params={{ workerId: c.worker.id }}
          className="text-primary underline"
        >
          {m.my_page()}
        </Link>
      </p>
      <Chatbot workerId={c.worker.id} machineId={c.machine.id} lang={lang} />
    </>
  )
}
