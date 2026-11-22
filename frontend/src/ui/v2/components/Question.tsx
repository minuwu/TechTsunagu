import { pick, type CheckQuestion, type Lang } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'
import { Button } from './ui/button'
import { Mark } from './Icon'

/** One multiple-choice question; reused by Note Checks, Briefings and Notices. */
export function Question({
  q,
  lang,
  value,
  onChange,
  name,
}: {
  q: CheckQuestion
  lang: Lang
  value: number | undefined
  onChange: (i: number) => void
  name: string
}) {
  return (
    <fieldset className="space-y-2" lang={lang}>
      <legend className="mb-2 text-lg font-bold">{pick(q.prompt, lang)}</legend>
      {q.options.map((o, i) => (
        <label
          key={i}
          className="flex min-h-12 cursor-pointer items-center gap-3 rounded-md border border-border bg-card px-3 py-2 transition-colors hover:border-primary has-[:checked]:border-2 has-[:checked]:border-primary has-[:checked]:bg-accent has-[:focus-visible]:ring-[3px] has-[:focus-visible]:ring-ring/50"
        >
          <input
            type="radio"
            name={name}
            checked={value === i}
            onChange={() => onChange(i)}
            className="size-5 shrink-0"
          />
          {pick(o, lang)}
        </label>
      ))}
    </fieldset>
  )
}

interface OneCheck {
  answers: Record<string, number>
  results: Record<string, { passed: boolean }>
  answer: (id: string, option: number) => void
  submit: (id: string, lang: Lang) => Promise<void>
  retry: (id: string) => void
  submitting: boolean
}

/** Question + submit + result, for Briefings and Notices. */
export function OneQuestion({
  id,
  q,
  lang,
  check,
  done,
}: {
  id: string
  q: CheckQuestion
  lang: Lang
  check: OneCheck
  done?: boolean
}) {
  const result = check.results[id]
  if (done || result?.passed)
    return (
      <p
        className="inline-flex items-center gap-2 rounded-sm border-2 border-success px-3 py-1.5 font-bold text-success"
        aria-live="polite"
      >
        <Mark id="check" />
        {m.check_understood()}
      </p>
    )
  return (
    <div className="space-y-3" aria-live="polite">
      <Question
        q={q}
        lang={lang}
        name={`q-${id}`}
        value={check.answers[id]}
        onChange={(i) => check.answer(id, i)}
      />
      {result && !result.passed ? (
        <div className="space-y-2">
          <p className="flex items-center gap-2 font-bold text-destructive">
            <Mark id="x" />
            {m.check_wrong_one()}
          </p>
          <Button variant="outline" onClick={() => check.retry(id)}>
            {m.retry()}
          </Button>
        </div>
      ) : (
        <Button
          disabled={check.answers[id] === undefined || check.submitting}
          onClick={() => void check.submit(id, lang)}
        >
          {m.check_submit()}
        </Button>
      )}
    </div>
  )
}
