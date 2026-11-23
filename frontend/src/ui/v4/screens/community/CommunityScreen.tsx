import { pick } from '@/domain/wakatta'
import { useCommunity, type SourceKind } from '@/features/community'
import { m } from '@/i18n/paraglide/messages'
import { Card, PageTitle, inputClass } from '@/ui/v4/components/Card'
import { PinGate } from '@/ui/v4/components/PinGate'
import { RecorderButton } from '@/ui/v4/components/RecorderButton'
import { Button } from '@/ui/v4/components/ui/button'
import { fmtDate, uiLang } from '@/ui/v4/lib/format'

/** Community Poster (自治会 / city / employer): post a Notice, confirm dates, see who understood. */
export function CommunityScreen() {
  const c = useCommunity()
  const lang = uiLang()
  const kinds: [SourceKind, string][] = [
    ['photo', m.src_photo()],
    ['text', m.src_text()],
    ['voice', m.src_voice()],
  ]
  return (
    <PinGate gate="community">
      <PageTitle sub={m.community_sub()}>{m.community_title()}</PageTitle>
      <div className="grid gap-4 lg:grid-cols-2">
        <Card id="post" title={m.post_notice()} tone="life">
          {c.step === 'source' && (
            <div className="space-y-3">
              <label className="block space-y-1">
                <span>{m.poster()}</span>
                <select
                  className={inputClass}
                  value={c.posterId}
                  onChange={(e) => c.setPosterId(e.target.value)}
                >
                  {c.posters.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </label>
              <div
                role="group"
                aria-label={m.source_kind()}
                className="flex flex-wrap gap-1"
              >
                {kinds.map(([k, label]) => (
                  <Button
                    key={k}
                    size="sm"
                    variant={c.kind === k ? 'default' : 'outline'}
                    aria-pressed={c.kind === k}
                    onClick={() => c.setKind(k)}
                  >
                    {label}
                  </Button>
                ))}
              </div>
              {c.kind === 'photo' && (
                <>
                  <label className="block space-y-1">
                    <span>{m.flyer_photo()}</span>
                    <input
                      type="file"
                      accept="image/*"
                      capture="environment"
                      onChange={(e) => void c.setPhotoFile(e.target.files?.[0])}
                    />
                  </label>
                  {c.photo && (
                    <img
                      src={c.photo}
                      alt={m.photo_preview()}
                      className="max-h-56 rounded-md"
                    />
                  )}
                </>
              )}
              {c.kind === 'text' && (
                <label className="block space-y-1">
                  <span>{m.notice_text_ja()}</span>
                  <textarea
                    className={inputClass}
                    rows={5}
                    lang="ja"
                    value={c.text}
                    onChange={(e) => c.setText(e.target.value)}
                  />
                </label>
              )}
              {c.kind === 'voice' && <RecorderButton recorder={c.recorder} />}
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={!c.canProcess}
                  onClick={() => void c.process()}
                >
                  {m.make_draft()}
                </Button>
                <Button variant="outline" onClick={() => void c.useSample()}>
                  {m.use_sample_flyer()}
                </Button>
              </div>
            </div>
          )}
          {c.step === 'processing' && (
            <p aria-live="polite">{m.ai_working()}</p>
          )}
          {c.step === 'review' && c.draft && (
            <div className="space-y-4">
              <div>
                <p className="text-lg font-bold" lang="ja">
                  {c.draft.title.ja}
                </p>
                <p lang="en" className="text-muted-foreground">
                  {c.draft.title.en}
                </p>
              </div>
              <ol className="list-decimal space-y-2 pl-6">
                {c.draft.steps.map((s, i) => (
                  <li key={i}>
                    <span lang="ja">{s.ja}</span>
                  </li>
                ))}
              </ol>
              <fieldset className="space-y-2 rounded-md border-2 border-warning p-3">
                <legend className="px-1 font-bold">{m.confirm_dates()}</legend>
                <p className="text-sm text-muted-foreground">
                  {m.confirm_dates_body()}
                </p>
                {c.draftEvents.map(({ event: e, valid, next }) => (
                  <label
                    key={e.id}
                    className="flex items-start gap-3 rounded-md bg-muted p-2"
                  >
                    <input
                      type="checkbox"
                      className="mt-1 size-5"
                      checked={e.confirmed}
                      disabled={!valid}
                      onChange={(x) => c.setConfirmed(e.id, x.target.checked)}
                    />
                    <span>
                      <span className="block font-bold" lang={lang}>
                        {pick(e.title, lang)}
                      </span>
                      <span className="block text-sm">
                        {m.next_dates()}{' '}
                        {next.map((d) => fmtDate(d, lang, true)).join(' / ')}
                      </span>
                      {e.rrule && (
                        <code className="text-xs text-muted-foreground">
                          {e.rrule}
                        </code>
                      )}
                      {!valid && (
                        <span className="block text-sm text-destructive">
                          {m.rrule_invalid()}
                        </span>
                      )}
                    </span>
                  </label>
                ))}
              </fieldset>
              <details>
                <summary className="cursor-pointer text-sm text-muted-foreground">
                  {m.original_ja()}
                </summary>
                <p lang="ja" className="mt-1 text-sm whitespace-pre-line">
                  {c.draft.sourceJa}
                </p>
              </details>
              <div className="flex flex-wrap gap-2">
                <Button variant="outline" onClick={c.again}>
                  {m.back()}
                </Button>
                <Button
                  disabled={!c.canPublish}
                  loading={c.publishing}
                  onClick={() => void c.publish()}
                >
                  {m.publish()}
                </Button>
              </div>
              {!c.canPublish && (
                <p className="text-sm text-warning">
                  {m.publish_needs_confirm()}
                </p>
              )}
            </div>
          )}
          {c.step === 'done' && (
            <div className="space-y-3" aria-live="polite">
              <p className="font-bold text-success">{m.notice_published()}</p>
              <Button variant="outline" onClick={c.again}>
                {m.post_another()}
              </Button>
            </div>
          )}
        </Card>

        <Card id="reach" title={m.reach()} tone="life">
          <ul className="space-y-4">
            {c.reach.map((r) => (
              <li key={r.notice.id}>
                <p className="font-bold" lang="ja">
                  {r.notice.title.ja}
                </p>
                <p className="text-sm text-muted-foreground">{r.poster}</p>
                <div
                  className="mt-1 flex h-4 overflow-hidden rounded-full bg-muted"
                  role="img"
                  aria-label={m.reach_line({
                    understood: r.understood,
                    opened: r.opened,
                    notOpened: r.notOpened,
                  })}
                >
                  <div
                    className="bg-success"
                    style={{
                      width: `${(r.understood / Math.max(1, r.total)) * 100}%`,
                    }}
                  />
                  <div
                    className="bg-warning"
                    style={{
                      width: `${(r.opened / Math.max(1, r.total)) * 100}%`,
                    }}
                  />
                </div>
                <p className="text-sm">
                  {m.reach_line({
                    understood: r.understood,
                    opened: r.opened,
                    notOpened: r.notOpened,
                  })}
                </p>
              </li>
            ))}
          </ul>
        </Card>
      </div>
    </PinGate>
  )
}
