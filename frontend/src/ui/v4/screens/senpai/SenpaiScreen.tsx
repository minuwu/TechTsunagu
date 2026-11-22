import { pick, type MeetupKind } from '@/domain/wakatta'
import { useSenpai } from '@/features/senpai'
import { m } from '@/i18n/paraglide/messages'
import { ScreenSkeleton } from '@/ui/v4/components/Feedback'
import { Card, PageTitle, inputClass } from '@/ui/v4/components/Card'
import { meetupLabel } from '@/ui/v4/components/StatusChip'
import { Mark } from '@/ui/v4/components/Icon'
import { Button } from '@/ui/v4/components/ui/button'
import { fmtDate } from '@/ui/v4/lib/format'

/** Senpai's private link (no login): forwarded Asks, shared Events, Meet-ups. Japanese first. */
export function SenpaiScreen() {
  const s = useSenpai()
  if (s.loading) return <ScreenSkeleton />
  if (s.notFound || !s.pair) return <p>{m.senpai_link_invalid()}</p>
  return (
    <>
      <PageTitle sub={m.senpai_sub({ worker: s.worker?.name ?? '' })}>
        {m.senpai_hello({ name: s.pair.senpaiName })}
      </PageTitle>
      <div className="grid gap-4 md:grid-cols-2">
        <Card
          id="asks"
          title={m.forwarded_asks()}
          tone="life"
          className="md:col-span-2"
        >
          {s.asks.length === 0 ? (
            <p className="text-muted-foreground">{m.no_asks()}</p>
          ) : (
            <ul className="space-y-4">
              {s.asks.map((a) => (
                <li key={a.id} className="inset-card p-3">
                  <p className="mb-2 text-sm text-muted-foreground">
                    {fmtDate(a.createdAt, 'ja', true)}
                  </p>
                  <p className="font-bold">{a.text}</p>
                  {a.senpaiReply ? (
                    <p
                      className="mt-2 flex items-start gap-2 text-success"
                      lang="ja"
                    >
                      <Mark id="check" className="mt-1" />
                      {a.senpaiReply.ja}
                    </p>
                  ) : (
                    <div className="mt-3 space-y-2">
                      <div className="flex flex-wrap gap-2">
                        {s.quickReplies.map((r) => (
                          <Button
                            key={r.ja}
                            size="sm"
                            variant="outline"
                            lang="ja"
                            onClick={() => s.sendQuick(a.id, r)}
                          >
                            {r.ja}
                          </Button>
                        ))}
                      </div>
                      <div className="flex gap-2">
                        <input
                          aria-label={m.free_reply()}
                          className={inputClass}
                          lang="ja"
                          value={s.drafts[a.id] ?? ''}
                          onChange={(e) => s.setDraft(a.id, e.target.value)}
                        />
                        <Button onClick={() => s.sendFree(a.id)}>
                          {m.send()}
                        </Button>
                      </div>
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </Card>
        <Card id="events" title={m.upcoming()} tone="life">
          <ul className="space-y-2">
            {s.events.map((e) => (
              <li key={e.key} className="flex flex-wrap justify-between gap-2">
                <span lang="ja">{pick(e.title, 'ja')}</span>
                <span>{fmtDate(e.at, 'ja', true)}</span>
              </li>
            ))}
          </ul>
        </Card>
        <Card id="meetups" title={m.meetups()} tone="life">
          <ul className="mb-3 space-y-1">
            {s.meetups.map((mt) => (
              <li key={mt.id}>
                {fmtDate(mt.date, 'ja')}
                {' · '}
                {meetupLabel(mt.kind)}
                {mt.note && ` · ${mt.note}`}
              </li>
            ))}
          </ul>
          <div className="space-y-2">
            <select
              className={inputClass}
              aria-label={m.meetup_kind()}
              value={s.kind}
              onChange={(e) => s.setKind(e.target.value as MeetupKind)}
            >
              {(['coffee', 'event', 'drill', 'other'] as const).map((k) => (
                <option key={k} value={k}>
                  {meetupLabel(k)}
                </option>
              ))}
            </select>
            <input
              className={inputClass}
              aria-label={m.note_optional()}
              value={s.note}
              onChange={(e) => s.setNote(e.target.value)}
            />
            <Button disabled={s.logging} onClick={() => void s.logMeetup()}>
              {m.log_meetup()}
            </Button>
          </div>
        </Card>
      </div>
    </>
  )
}
