import type { ReactNode } from 'react'
import { usePinGate, type PinRole } from '@/features/session'
import { m } from '@/i18n/paraglide/messages'
import { Card, inputClass } from './Card'
import { Button } from './ui/button'
import { Mark } from './Icon'

/** Foreman and Community Poster screens open with the shop PIN. */
export function PinGate({
  gate: role,
  children,
}: {
  gate: PinRole
  children: ReactNode
}) {
  const gate = usePinGate(role)
  if (gate.unlocked)
    return (
      <>
        <div className="mb-2 flex justify-end">
          <Button variant="ghost" size="sm" onClick={gate.lock}>
            <Mark id="lock" />
            {m.pin_lock()}
          </Button>
        </div>
        {children}
      </>
    )
  return (
    <Card className="mx-auto mt-6 max-w-sm">
      <span
        aria-hidden="true"
        className="mb-4 grid size-12 place-items-center rounded-full bg-accent text-primary"
      >
        <Mark id="lock" className="size-6" />
      </span>
      <h1 className="display mb-4 text-2xl">
        {role === 'foreman' ? m.pin_title_foreman() : m.pin_title_community()}
      </h1>
      <form
        className="space-y-3"
        onSubmit={(e) => {
          e.preventDefault()
          void gate.submit()
        }}
      >
        <label className="block space-y-1">
          <span className="font-bold">{m.pin_label()}</span>
          <input
            className={`${inputClass} display tnum text-center text-3xl tracking-[0.5em]`}
            inputMode="numeric"
            autoComplete="off"
            value={gate.pin}
            onChange={(e) => gate.setPin(e.target.value)}
          />
        </label>
        {gate.error && (
          <p role="alert" className="text-destructive">
            {m.pin_wrong()}
          </p>
        )}
        <Button
          type="submit"
          disabled={gate.pin.length !== 4}
          loading={gate.busy}
          className="w-full"
        >
          {m.pin_open()}
        </Button>
        <p className="text-sm text-muted-foreground">{m.pin_hint()}</p>
      </form>
    </Card>
  )
}
