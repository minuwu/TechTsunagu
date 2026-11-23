import { Link } from '@tanstack/react-router'
import { pick } from '@/domain/wakatta'
import {
  useBriefingAdmin,
  useLibrary,
  useNearMissAdmin,
  useQrStickers,
  useSenpaiAdmin,
} from '@/features/foreman'
import { m } from '@/i18n/paraglide/messages'
import { EmptyState, ScreenSkeleton } from '@/ui/v2/components/Feedback'
import { Card, inputClass } from '@/ui/v2/components/Card'
import { Icon } from '@/ui/v2/components/Icon'
import { RecorderButton } from '@/ui/v2/components/RecorderButton'
import { meetupLabel } from '@/ui/v2/components/StatusChip'
import { Button } from '@/ui/v2/components/ui/button'
import { fmtDate, uiLang } from '@/ui/v2/lib/format'
import { ForemanPage } from './ForemanBoardScreen'

/** Morning Briefing (朝礼): record, AI translates and drafts one question, publish, see receipts. */
export function ForemanBriefingScreen() {
  const b = useBriefingAdmin()
  const lang = uiLang()
  const receiptLabel = {
    'not-opened': m.notice_new(),
    opened: m.notice_opened(),
    understood: m.notice_understood(),
  }
  return (
    <ForemanPage title={m.briefing_title()} sub={m.briefing_sub()}>
      <div className="grid gap-4 md:grid-cols-2">
        <Card id="new" title={m.briefing_new()}>
          {b.step === 'record' && (
            <div className="space-y-3">
              <RecorderButton recorder={b.recorder} />
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={!b.recorder.audio}
                  onClick={() => void b.process(false)}
                >
                  {m.make_draft()}
                </Button>
                <Button variant="outline" onClick={() => void b.process(true)}>
                  {m.use_sample()}
                </Button>
              </div>
            </div>
          )}
          {b.step === 'processing' && (
            <p aria-live="polite">{m.ai_working()}</p>
          )}
          {b.step === 'review' && b.draft && (
            <div className="space-y-3">
              <p lang="ja" className="font-bold">
                {b.draft.text.ja}
              </p>
              <p lang="en" className="text-muted-foreground">
                {b.draft.text.en}
              </p>
              <p className="text-sm font-bold">{m.check_preview()}</p>
              <p lang="ja">{b.draft.question.prompt.ja}</p>
              <div className="flex gap-2">
                <Button variant="outline" onClick={b.again}>
                  {m.back()}
                </Button>
                <Button loading={b.publishing} onClick={() => void b.publish()}>
                  {m.publish()}
                </Button>
              </div>
            </div>
          )}
          {b.step === 'done' && (
            <div className="space-y-3" aria-live="polite">
              <p className="font-bold text-success">{m.briefing_published()}</p>
              <Button variant="outline" onClick={b.again}>
                {m.record_another()}
              </Button>
            </div>
          )}
        </Card>
        <Card id="receipts" title={m.receipts()}>
          {b.latest ? (
            <>
              <p className="mb-3" lang={lang}>
                {pick(b.latest.text, lang)}
              </p>
              <ul className="divide-y divide-[var(--rule)]">
                {b.receipts.map((r) => (
                  <li key={r.worker.id} className="flex justify-between py-2">
                    {r.worker.name}
                    <strong
                      className={
                        r.state === 'understood'
                          ? 'text-success'
                          : r.state === 'opened'
                            ? 'text-warning'
                            : 'text-destructive'
                      }
                    >
                      {receiptLabel[r.state]}
                    </strong>
                  </li>
                ))}
              </ul>
            </>
          ) : (
            <EmptyState>{m.no_briefing()}</EmptyState>
          )}
        </Card>
      </div>
    </ForemanPage>
  )
}

/** Near-miss Reports (ヒヤリハット) from Workers, translated to Japanese. */
export function ForemanNearMissesScreen() {
  const n = useNearMissAdmin()
  const lang = uiLang()
  return (
    <ForemanPage title={m.near_misses_title()} sub={m.near_misses_sub()}>
      {n.reports.length === 0 ? (
        <EmptyState mark="check">{m.no_near_misses()}</EmptyState>
      ) : (
        <ul className="space-y-3">
          {n.reports.map((r) => (
            <li key={r.id}>
              <Card tone={r.acknowledgedAt ? undefined : 'warning'} id={r.id}>
                <p className="mb-1 text-sm text-muted-foreground">
                  {r.workerName}
                  {' · '}
                  {r.machine && pick(r.machine.name, lang)}
                  {' · '}
                  {fmtDate(r.createdAt, lang, true)}
                </p>
                <p lang="ja" className="text-lg font-bold">
                  {r.text.ja}
                </p>
                <p lang="en" className="text-sm">
                  {r.text.en}
                </p>
                {r.photo && (
                  <img
                    src={r.photo}
                    alt={m.photo_preview()}
                    className="mt-2 max-h-48 rounded-md"
                  />
                )}
                <div className="mt-3">
                  {r.acknowledgedAt ? (
                    <span className="font-bold text-success">{m.acked()}</span>
                  ) : (
                    <Button size="sm" onClick={() => n.ack(r.id)}>
                      {m.ack()}
                    </Button>
                  )}
                </div>
              </Card>
            </li>
          ))}
        </ul>
      )}
    </ForemanPage>
  )
}

/** Knowledge Library: every approved Note Version with what changed. */
export function ForemanLibraryScreen() {
  const l = useLibrary()
  const lang = uiLang()
  return (
    <ForemanPage
      title={m.library_title({
        name: l.machine ? pick(l.machine.name, lang) : '',
      })}
      sub={m.library_sub()}
    >
      <nav aria-label={m.machine()} className="mb-4 flex flex-wrap gap-2">
        {l.machines.map((mc) => (
          <Link
            key={mc.id}
            to="/foreman/library/$machineId"
            params={{ machineId: mc.id }}
            className="rounded-md border border-border px-3 py-2 font-bold [&.active]:bg-primary [&.active]:text-primary-foreground"
          >
            {pick(mc.name, lang)}
          </Link>
        ))}
      </nav>
      <ol className="space-y-4">
        {l.versions.map(({ version: v, isCurrent, added, removed }) => (
          <li key={v.id}>
            <Card
              id={v.id}
              tone={isCurrent ? 'success' : undefined}
              title={`${m.version({ v: v.versionNo })}${isCurrent ? ` · ${m.current()}` : ''}`}
            >
              <p className="mb-2 text-sm text-muted-foreground">
                {v.recordedBy}
                {' · '}
                {fmtDate(v.approvedAt ?? v.createdAt, lang)}
              </p>
              {(added.length > 0 || removed.length > 0) && (
                <div className="mb-3 space-y-1 text-sm" lang="ja">
                  {added.map((t) => (
                    <p key={`a${t}`} className="text-success">{`+ ${t}`}</p>
                  ))}
                  {removed.map((t) => (
                    <p
                      key={`r${t}`}
                      className="text-destructive line-through"
                    >{`− ${t}`}</p>
                  ))}
                </div>
              )}
              <ol className="list-decimal space-y-1 pl-6" lang="ja">
                {v.steps.map((s, i) => (
                  <li key={i}>
                    {s.icon && <Icon id={s.icon} className="mr-1" />}
                    {s.text.ja}
                  </li>
                ))}
              </ol>
              <details className="mt-2">
                <summary className="cursor-pointer text-sm text-muted-foreground">
                  {m.transcript()}
                </summary>
                <p lang="ja" className="mt-1 text-sm">
                  {v.transcriptJa}
                </p>
              </details>
            </Card>
          </li>
        ))}
      </ol>
    </ForemanPage>
  )
}

/** Printable QR stickers, one per Machine. */
export function ForemanQrScreen() {
  const q = useQrStickers()
  const lang = uiLang()
  return (
    <ForemanPage title={m.qr_title()} sub={m.qr_sub()}>
      <Button
        variant="outline"
        className="mb-4 print:hidden"
        onClick={() => window.print()}
      >
        {m.print()}
      </Button>
      {q.loading ? (
        <ScreenSkeleton />
      ) : (
        <ul className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {q.stickers.map((s) => (
            <li
              key={s.machine.id}
              className="rounded-lg border border-[var(--rule)] bg-white p-3 text-center text-black shadow-[var(--shadow-sm)]"
            >
              <img
                src={s.dataUrl}
                alt={m.qr_alt({ name: pick(s.machine.name, lang) })}
                className="mx-auto w-full"
              />
              <p className="font-black" lang="ja">
                {s.machine.name.ja}
              </p>
              <p className="text-sm" lang="en">
                {s.machine.name.en}
              </p>
              <p className="text-xs break-all">{s.url}</p>
            </li>
          ))}
        </ul>
      )}
    </ForemanPage>
  )
}

/** Senpai pairs and the Support Log (特定技能 義務的支援) export. */
export function ForemanSenpaiScreen() {
  const s = useSenpaiAdmin()
  const lang = uiLang()
  return (
    <ForemanPage title={m.senpai_admin_title()} sub={m.senpai_admin_sub()}>
      <div className="grid gap-4 md:grid-cols-2">
        <Card id="pairs" title={m.pairs()} tone="life">
          <ul className="space-y-3">
            {s.pairs.map((p) => (
              <li key={p.id} className="inset-card p-3">
                <p className="font-bold">
                  {p.workerName}
                  {' ⇄ '}
                  {p.senpaiName}
                </p>
                <p className="text-sm">
                  {p.kind === 'coworker'
                    ? m.senpai_coworker()
                    : m.senpai_jichikai()}
                  {' · '}
                  {m.meetups_count({ count: p.meetups.length })}
                </p>
                <Link
                  to="/senpai/$token"
                  params={{ token: p.token }}
                  className="text-sm break-all text-primary underline"
                >
                  {p.link}
                </Link>
              </li>
            ))}
          </ul>
        </Card>
        <Card id="add-pair" title={m.add_pair()} tone="life">
          <form
            className="space-y-3"
            onSubmit={(e) => {
              e.preventDefault()
              void s.add()
            }}
          >
            <label className="block space-y-1">
              <span>{m.worker()}</span>
              <select
                className={inputClass}
                value={s.form.workerId}
                onChange={(e) => s.form.setWorkerId(e.target.value)}
              >
                <option value="">{m.choose()}</option>
                {s.workers.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="block space-y-1">
              <span>{m.senpai_name()}</span>
              <input
                className={inputClass}
                value={s.form.senpaiName}
                onChange={(e) => s.form.setSenpaiName(e.target.value)}
              />
            </label>
            <label className="block space-y-1">
              <span>{m.senpai_kind()}</span>
              <select
                className={inputClass}
                value={s.form.kind}
                onChange={(e) =>
                  s.form.setKind(e.target.value as 'coworker' | 'jichikai')
                }
              >
                <option value="coworker">{m.senpai_coworker()}</option>
                <option value="jichikai">{m.senpai_jichikai()}</option>
              </select>
            </label>
            <Button type="submit" disabled={!s.canAdd || s.adding}>
              {m.add()}
            </Button>
          </form>
        </Card>
        <Card id="log" title={m.support_log()} className="md:col-span-2">
          <p className="mb-3 text-muted-foreground">{m.support_log_body()}</p>
          <Button
            onClick={() =>
              s.downloadSupportLog({
                coffee: meetupLabel('coffee'),
                event: meetupLabel('event'),
                drill: meetupLabel('drill'),
                other: meetupLabel('other'),
                notice: m.notice_receipt(),
              })
            }
          >
            {m.download_csv()}
          </Button>
          <p className="mt-2 text-sm text-muted-foreground">
            {fmtDate(new Date(), lang)}
          </p>
        </Card>
      </div>
    </ForemanPage>
  )
}
