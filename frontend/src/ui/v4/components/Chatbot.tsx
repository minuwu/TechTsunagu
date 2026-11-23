import { useEffect, useId, useRef } from 'react'
import { pick, type Lang } from '@/domain/wakatta'
import { useChatbot, type ChatTurn } from '@/features/chatbot'
import { m } from '@/i18n/paraglide/messages'
import { inputClass } from './Card'
import { Mark } from './Icon'
import { RecorderButton } from './RecorderButton'
import { Button } from './ui/button'

/** The floating "Ask Wakatta" chatbot (XREQ-016), bottom right on the Worker's screens and the Machine screen. */
export function Chatbot({
  workerId,
  machineId = null,
  lang,
}: {
  workerId: string | null
  machineId?: string | null
  lang: Lang
}) {
  const c = useChatbot(workerId, machineId)
  const titleId = useId()
  const input = useRef<HTMLInputElement>(null)
  const end = useRef<HTMLDivElement>(null)
  const { open, setOpen } = c

  useEffect(() => {
    if (!open) return
    input.current?.focus()
    const close = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false)
    window.addEventListener('keydown', close)
    return () => window.removeEventListener('keydown', close)
  }, [open, setOpen])
  useEffect(() => {
    end.current?.scrollIntoView({ block: 'end' })
  }, [c.turns.length, c.asking])

  return (
    // Above the phone tab bar (.tabbar: 4rem + safe area, hidden from 48rem up)
    <div className="fixed right-4 bottom-[calc(5rem+env(safe-area-inset-bottom))] z-50 flex flex-col items-end gap-3 md:bottom-6">
      {open && (
        <section
          role="dialog"
          aria-labelledby={titleId}
          className="flex max-h-[min(70vh,40rem)] w-[min(26rem,calc(100vw-2rem))] flex-col border border-foreground bg-background shadow-xl"
        >
          <header className="flex items-start justify-between gap-3 border-b border-border p-4">
            <div>
              <h2 id={titleId} className="text-lg font-bold">
                {m.chat_title()}
              </h2>
              <p className="text-sm text-muted-foreground">{m.chat_body()}</p>
              {machineId && (
                <p className="mt-1 text-sm font-bold">
                  {m.chat_machine_hint()}
                </p>
              )}
            </div>
            <button
              type="button"
              aria-label={m.chat_close()}
              className="grid size-11 shrink-0 place-items-center hover:bg-accent focus-visible:ring-[3px] focus-visible:ring-ring/40 focus-visible:outline-none"
              onClick={() => setOpen(false)}
            >
              <Mark id="x" className="size-5" />
            </button>
          </header>

          <div
            className="flex-1 space-y-4 overflow-y-auto p-4"
            aria-live="polite"
          >
            {c.turns.length === 0 && !c.asking && (
              <p className="text-muted-foreground">{m.chat_empty()}</p>
            )}
            {c.turns.map((t) => (
              <Turn key={t.id} turn={t} lang={lang} chat={c} />
            ))}
            {c.asking && (
              <p className="flex items-center gap-2 text-muted-foreground">
                <Mark id="wait" className="size-5 animate-spin" />
                {m.chat_thinking()}
              </p>
            )}
            {c.failed && <p className="text-destructive">{m.chat_failed()}</p>}
            <div ref={end} />
          </div>

          <form
            className="space-y-2 border-t border-border p-4"
            onSubmit={(e) => {
              e.preventDefault()
              if (c.canAsk) void c.send()
            }}
          >
            <RecorderButton recorder={c.recorder} />
            <div className="flex gap-2">
              <label className="sr-only" htmlFor={`${titleId}-q`}>
                {m.chat_input()}
              </label>
              <input
                ref={input}
                id={`${titleId}-q`}
                className={inputClass}
                value={c.text}
                placeholder={m.chat_input()}
                onChange={(e) => c.setText(e.target.value)}
              />
              <Button
                type="submit"
                size="sm"
                disabled={!c.canAsk}
                loading={c.asking}
              >
                {m.chat_send()}
              </Button>
            </div>
          </form>
        </section>
      )}

      <Button
        size="lg"
        aria-expanded={open}
        className="shadow-lg"
        onClick={() => setOpen(!open)}
      >
        <Mark id={open ? 'x' : 'search'} />
        {m.chat_open()}
      </Button>
    </div>
  )
}

function Turn({
  turn,
  lang,
  chat,
}: {
  turn: ChatTurn
  lang: Lang
  chat: ReturnType<typeof useChatbot>
}) {
  const r = turn.reply
  const attribution = [
    ...new Set(r.sources.map((s) => s.attribution).filter((a) => !!a)),
  ]
  return (
    <article className="space-y-2">
      <p className="ml-8 bg-accent px-3 py-2 font-bold">{r.question}</p>

      {r.emergency && r.answer ? (
        <div
          role="alert"
          className="border-2 border-destructive bg-destructive/10 p-3 text-destructive"
        >
          <p className="flex items-center gap-2 font-bold uppercase">
            <Mark id="alert" />
            {m.chat_emergency()}
          </p>
          <p className="mt-1 text-lg font-bold" lang={lang}>
            {pick(r.answer, lang)}
          </p>
        </div>
      ) : r.covered && r.answer ? (
        <div className="space-y-2 border border-border p-3">
          <p lang={lang}>{pick(r.answer, lang)}</p>
          <ul className="flex flex-wrap gap-2" aria-label={m.chat_source()}>
            {r.sources.map((s) => (
              <li key={s.id}>
                {s.url ? (
                  <a
                    href={s.url}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-block border border-foreground px-2 py-1 text-sm underline hover:bg-accent"
                  >
                    {s.title}
                    {s.page !== null &&
                      `, ${m.chat_page({ page: String(s.page) })}`}
                  </a>
                ) : (
                  <span className="inline-block border border-foreground px-2 py-1 text-sm">
                    {s.title}
                  </span>
                )}
              </li>
            ))}
          </ul>
          {attribution.map((a) => (
            <p key={a} className="text-xs text-muted-foreground">
              {a}
            </p>
          ))}
          {r.check && <Practice turn={turn} lang={lang} chat={chat} />}
        </div>
      ) : (
        <div className="space-y-2 border border-border p-3">
          <p className="text-muted-foreground">{m.chat_not_covered()}</p>
          {turn.forwarded ? (
            <p className="font-bold">{m.chat_forwarded()}</p>
          ) : (
            <Button
              size="sm"
              variant="outline"
              loading={chat.forwarding}
              onClick={() => void chat.forward(turn)}
            >
              {m.chat_forward()}
            </Button>
          )}
        </div>
      )}
    </article>
  )
}

/** The practice question: graded on the phone with correctIndex; it is not a Check and changes no Sign-off. */
function Practice({
  turn,
  lang,
  chat,
}: {
  turn: ChatTurn
  lang: Lang
  chat: ReturnType<typeof useChatbot>
}) {
  const q = turn.reply.check!
  const answered = turn.picked !== null
  return (
    <fieldset className="space-y-2 border-t border-border pt-3" lang={lang}>
      <legend className="text-sm font-bold uppercase">
        {m.chat_practice()}
      </legend>
      <p className="font-bold">{pick(q.prompt, lang)}</p>
      {q.options.map((o, i) => {
        const right = answered && i === q.correctIndex
        const wrong = answered && i === turn.picked && i !== q.correctIndex
        return (
          <button
            key={i}
            type="button"
            disabled={answered}
            aria-pressed={turn.picked === i}
            className={`flex min-h-11 w-full items-center gap-2 border px-3 py-2 text-left disabled:cursor-default ${
              right
                ? 'border-2 border-success text-success'
                : wrong
                  ? 'border-2 border-destructive text-destructive'
                  : 'border-border hover:border-primary'
            }`}
            onClick={() => chat.pick(turn.id, i)}
          >
            {right && <Mark id="check" />}
            {wrong && <Mark id="x" />}
            {pick(o, lang)}
          </button>
        )
      })}
      {answered && (
        <p
          className={
            turn.picked === q.correctIndex
              ? 'font-bold text-success'
              : 'font-bold text-destructive'
          }
        >
          {turn.picked === q.correctIndex ? m.chat_right() : m.chat_wrong()}
        </p>
      )}
    </fieldset>
  )
}
