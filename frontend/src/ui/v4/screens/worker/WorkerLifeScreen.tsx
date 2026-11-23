import { useParams } from '@tanstack/react-router'
import { pick, type MeetupKind } from '@/domain/wakatta'
import { useWorkerLife } from '@/features/worker'
import { m } from '@/i18n/paraglide/messages'
import { ScreenSkeleton } from '@/ui/v4/components/Feedback'
import { Card, PageTitle, inputClass } from '@/ui/v4/components/Card'
import { Chatbot } from '@/ui/v4/components/Chatbot'
import { LangToggle } from '@/ui/v4/components/LangToggle'
import { WorkerTabs } from '@/ui/v4/components/Nav'
import { OneQuestion } from '@/ui/v4/components/Question'
import { RecorderButton } from '@/ui/v4/components/RecorderButton'
import { meetupLabel } from '@/ui/v4/components/StatusChip'
import { Button } from '@/ui/v4/components/ui/button'
import { fmtDate } from '@/ui/v4/lib/format'

/** Worker's Life tab: upcoming Events, Notices, Ask, Senpai. */
export function WorkerLifeScreen() {
  const { workerId = '' } = useParams({ strict: false })
  const l = useWorkerLife(workerId)
  if (l.loading) return <ScreenSkeleton />
  if (!l.worker) return <p>{m.worker_not_found()}</p>
  const lang = l.lang
  const dayLabel = { today: m.today(), tomorrow: m.tomorrow(), later: '' }

  return (
    <>
      <PageTitle>{l.worker.name}</PageTitle>
      <WorkerTabs workerId={workerId} />
      <div className="mb-4">
        <LangToggle value={lang} onChange={l.setLang} />
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <Card title={m.upcoming()} id="events" tone="life">
          {l.events.length === 0 ? (
            <p className="text-muted-foreground">{m.no_events()}</p>
          ) : (
            <ul className="mb-3 space-y-2">
              {l.events.slice(0, 6).map((e) => (
                <li
                  key={e.key}
                  className="flex flex-wrap justify-between gap-2"
                >
                  <span className="font-bold" lang={lang}>
                    {pick(e.title, lang)}
                  </span>
                  <span>
                    {dayLabel[e.day] && (
                      <strong className="mr-2 text-life">
                        {dayLabel[e.day]}
                      </strong>
                    )}
                    {fmtDate(e.at, lang, true)}
                  </span>
                </li>
              ))}
            </ul>
          )}
          <Button variant="outline" onClick={l.downloadCalendar}>
            {m.add_to_calendar()}
          </Button>
        </Card>

        <Card title={m.ask_title()} id="ask" tone="life">
          <p className="mb-3 text-sm text-muted-foreground">{m.ask_body()}</p>
          <div className="space-y-3">
            <RecorderButton recorder={l.recorder} />
            <label className="block space-y-1">
              <span>{m.or_type()}</span>
              <input
                className={inputClass}
                value={l.askText}
                onChange={(e) => l.setAskText(e.target.value)}
              />
            </label>
            <Button
              disabled={!l.canAsk}
              loading={l.asking}
              onClick={() => void l.ask()}
            >
              {m.ask_send()}
            </Button>
          </div>
          <ul className="mt-4 space-y-3" aria-live="polite">
            {l.asks.map((a) => (
              <li key={a.id} className="inset-card p-3">
                <p className="font-bold">{a.text}</p>
                {a.covered && a.answer ? (
                  <>
                    <p lang={lang}>{pick(a.answer, lang)}</p>
                    <p className="text-sm text-muted-foreground">
                      {m.ask_source()}{' '}
                      {a.sourceNoticeIds
                        .map(
                          (id) =>
                            l.notices.find((n) => n.notice.id === id)?.notice
                              .title,
                        )
                        .filter((t) => !!t)
                        .map((t) => pick(t!, lang))
                        .join(', ')}
                    </p>
                  </>
                ) : a.senpaiReply ? (
                  <p lang={lang}>
                    <strong>{l.pair?.senpaiName}</strong>
                    {': '}
                    {pick(a.senpaiReply, lang)}
                  </p>
                ) : a.forwarded ? (
                  <p className="text-muted-foreground">{m.ask_forwarded()}</p>
                ) : (
                  <div className="space-y-2">
                    <p className="text-muted-foreground">
                      {m.ask_not_covered()}
                    </p>
                    {l.pair && (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => l.forward(a.id)}
                      >
                        {m.ask_forward({ name: l.pair.senpaiName })}
                      </Button>
                    )}
                  </div>
                )}
              </li>
            ))}
          </ul>
        </Card>

        <Card
          title={m.notices()}
          id="notices"
          tone="life"
          className="md:col-span-2"
        >
          <ul className="space-y-3">
            {l.notices.map(({ notice: n, poster, opened, understood }) => (
              <li key={n.id} className="rounded-md border border-border">
                <button
                  type="button"
                  aria-expanded={l.expanded === n.id}
                  className="flex min-h-11 w-full items-center justify-between gap-2 p-3 text-left"
                  onClick={() => l.toggle(n.id)}
                >
                  <span>
                    <span className="block font-bold" lang={lang}>
                      {pick(n.title, lang)}
                    </span>
                    <span className="text-sm text-muted-foreground">
                      {poster}
                    </span>
                  </span>
                  <span className="text-sm font-bold">
                    {understood
                      ? m.notice_understood()
                      : opened
                        ? m.notice_opened()
                        : m.notice_new()}
                  </span>
                </button>
                {l.expanded === n.id && (
                  <div className="space-y-4 border-t border-border p-3">
                    <ol
                      className="list-decimal space-y-1 pl-6 text-lg"
                      lang={lang}
                    >
                      {n.steps.map((s, i) => (
                        <li key={i}>{pick(s, lang)}</li>
                      ))}
                    </ol>
                    <details>
                      <summary className="cursor-pointer text-sm text-muted-foreground">
                        {m.original_ja()}
                      </summary>
                      <p lang="ja" className="mt-2 text-sm whitespace-pre-line">
                        {n.sourceJa}
                      </p>
                    </details>
                    <OneQuestion
                      id={n.id}
                      q={n.question}
                      lang={lang}
                      check={l.noticeCheck}
                      done={understood}
                    />
                  </div>
                )}
              </li>
            ))}
          </ul>
        </Card>

        <Card
          title={m.my_senpai()}
          id="senpai"
          tone="life"
          className="md:col-span-2"
        >
          {!l.pair ? (
            <p className="text-muted-foreground">{m.no_senpai()}</p>
          ) : (
            <div className="space-y-3">
              <p className="text-lg">
                <strong>{l.pair.senpaiName}</strong>{' '}
                <span className="text-muted-foreground">
                  {l.pair.kind === 'coworker'
                    ? m.senpai_coworker()
                    : m.senpai_jichikai()}
                </span>
              </p>
              <ul className="space-y-1">
                {l.meetups.map((mt) => (
                  <li key={mt.id}>
                    {fmtDate(mt.date, lang)}
                    {' · '}
                    {meetupLabel(mt.kind)}
                    {mt.note && ` · ${mt.note}`}
                  </li>
                ))}
              </ul>
              <div className="flex flex-wrap items-end gap-2">
                <label className="space-y-1">
                  <span className="block">{m.meetup_kind()}</span>
                  <select
                    className={inputClass}
                    value={l.meetKind}
                    onChange={(e) =>
                      l.setMeetKind(e.target.value as MeetupKind)
                    }
                  >
                    {(['coffee', 'event', 'drill', 'other'] as const).map(
                      (k) => (
                        <option key={k} value={k}>
                          {meetupLabel(k)}
                        </option>
                      ),
                    )}
                  </select>
                </label>
                <label className="flex-1 space-y-1">
                  <span className="block">{m.note_optional()}</span>
                  <input
                    className={inputClass}
                    value={l.meetNote}
                    onChange={(e) => l.setMeetNote(e.target.value)}
                  />
                </label>
                <Button disabled={l.logging} onClick={() => void l.logMeetup()}>
                  {m.log_meetup()}
                </Button>
              </div>
            </div>
          )}
        </Card>
      </div>
      <Chatbot workerId={workerId} lang={l.lang} />
    </>
  )
}
