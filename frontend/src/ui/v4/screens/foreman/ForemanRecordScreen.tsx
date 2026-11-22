import { Link } from '@tanstack/react-router'
import { pick } from '@/domain/wakatta'
import { useRecordNote } from '@/features/foreman'
import { m } from '@/i18n/paraglide/messages'
import { ScreenSkeleton } from '@/ui/v4/components/Feedback'
import { Card, inputClass } from '@/ui/v4/components/Card'
import { Icon, Mark } from '@/ui/v4/components/Icon'
import { RecorderButton } from '@/ui/v4/components/RecorderButton'
import { Button } from '@/ui/v4/components/ui/button'
import { uiLang } from '@/ui/v4/lib/format'
import { ForemanPage } from './ForemanBoardScreen'

/** Record a voice note at the Machine; AI drafts steps, Bengali/English, back-translation and Check. */
export function ForemanRecordScreen() {
  const r = useRecordNote()
  const lang = uiLang()
  const name = r.machine ? pick(r.machine.name, lang) : ''
  return (
    <ForemanPage title={m.record_title({ name })} sub={m.record_sub()}>
      {r.loading ? (
        <ScreenSkeleton />
      ) : !r.machine ? (
        <p>{m.machine_not_found()}</p>
      ) : r.step === 'record' ? (
        <Card id="rec" title={m.record_step1()}>
          <div className="space-y-3">
            <RecorderButton recorder={r.recorder} />
            <label className="block space-y-1">
              <span>{m.or_type_ja()}</span>
              <textarea
                className={inputClass}
                rows={3}
                lang="ja"
                value={r.text}
                onChange={(e) => r.setText(e.target.value)}
              />
            </label>
            <div className="flex flex-wrap gap-2">
              <Button disabled={!r.canProcess} onClick={() => void r.process()}>
                {m.make_draft()}
              </Button>
              <Button variant="outline" onClick={() => void r.useSample()}>
                {m.use_sample()}
              </Button>
            </div>
          </div>
        </Card>
      ) : r.step === 'processing' ? (
        <Card id="proc">
          <p aria-live="polite" className="text-lg">
            {m.ai_working()}
          </p>
        </Card>
      ) : r.step === 'done' ? (
        <Card tone="success" id="done" title={m.note_published()}>
          <p className="mb-3">{m.note_published_body()}</p>
          <div className="flex flex-wrap gap-3">
            <Link
              to="/foreman/library/$machineId"
              params={{ machineId: r.machine.id }}
              className="font-bold text-primary underline"
            >
              {m.library()}
            </Link>
            <Link to="/foreman" className="font-bold text-primary underline">
              {m.nav_board()}
            </Link>
            <Button variant="outline" onClick={r.again}>
              {m.record_another()}
            </Button>
          </div>
        </Card>
      ) : r.draft ? (
        <div className="space-y-4">
          <Card id="steps" title={m.review_steps({ v: r.draft.versionNo })}>
            <p className="mb-3 text-sm text-muted-foreground">
              {m.review_steps_body()}
            </p>
            <ol className="space-y-4">
              {r.draft.steps.map((s, i) => (
                <li key={i} className="space-y-1 inset-card p-3">
                  <div className="flex items-start gap-2">
                    <span className="font-bold">{i + 1}</span>
                    {s.icon && <Icon id={s.icon} className="text-xl" />}
                    {r.step === 'review' ? (
                      <textarea
                        aria-label={m.step_n({ n: i + 1 })}
                        className={inputClass}
                        rows={2}
                        lang="ja"
                        value={s.text.ja}
                        onChange={(e) => r.editStep(i, e.target.value)}
                      />
                    ) : (
                      <span lang="ja">{s.text.ja}</span>
                    )}
                  </div>
                  <p lang="bn" className="text-sm">
                    {s.text.bn}
                  </p>
                  <p lang="en" className="text-sm text-muted-foreground">
                    {s.text.en}
                  </p>
                </li>
              ))}
            </ol>
          </Card>
          <Card id="back" title={m.back_translation()} tone="warning">
            <p className="mb-2 text-sm text-muted-foreground">
              {m.back_translation_body()}
            </p>
            <p className="mb-1 text-sm font-bold">{m.from_bn()}</p>
            <p lang="ja" className="mb-3 whitespace-pre-line">
              {r.draft.backTranslation.bn}
            </p>
            <p className="mb-1 text-sm font-bold">{m.from_en()}</p>
            <p lang="ja" className="whitespace-pre-line">
              {r.draft.backTranslation.en}
            </p>
          </Card>
          <Card id="qs" title={m.check_preview()}>
            <ul className="space-y-3">
              {r.draft.questions.map((q) => (
                <li key={q.id}>
                  <p className="font-bold" lang="ja">
                    {q.prompt.ja}
                  </p>
                  <ul className="pl-4">
                    {q.options.map((o, i) => (
                      <li
                        key={i}
                        lang="ja"
                        className={
                          i === q.correctIndex
                            ? 'flex items-center gap-1.5 font-bold text-success'
                            : 'flex items-center gap-1.5'
                        }
                      >
                        <Mark
                          id={i === q.correctIndex ? 'check' : 'dash'}
                          className="size-4"
                        />
                        {o.ja}
                      </li>
                    ))}
                  </ul>
                </li>
              ))}
            </ul>
          </Card>
          {r.step === 'review' ? (
            <Button className="min-h-14 w-full text-lg" onClick={r.toConfirm}>
              {m.next_confirm()}
            </Button>
          ) : (
            <Card tone="danger" id="confirm" title={m.confirm_publish()}>
              <p className="mb-3">{m.confirm_publish_body()}</p>
              <div className="flex flex-wrap gap-2">
                <Button variant="outline" onClick={r.backToReview}>
                  {m.back()}
                </Button>
                <Button disabled={r.approving} onClick={() => void r.approve()}>
                  {m.approve_publish()}
                </Button>
              </div>
            </Card>
          )}
        </div>
      ) : null}
    </ForemanPage>
  )
}
