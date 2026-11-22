import { useEffect, useRef, type ReactNode } from 'react'
import { m } from '@/i18n/paraglide/messages'
import { dismissToast, useToasts, type ToastTone } from '@/ui/v2/lib/toast'
import { cn } from '@/ui/v2/lib/utils'
import { Mark, type MarkId } from './Icon'
import { Button } from './ui/button'

const TONE: Record<ToastTone, { mark: MarkId; className: string }> = {
  success: { mark: 'check', className: 'text-success' },
  info: { mark: 'info', className: 'text-primary' },
  error: { mark: 'alert', className: 'text-destructive' },
}

/** Toast stack, bottom centre on phones and bottom right on desktop; read out politely (F-37). */
export function Toaster() {
  const items = useToasts()
  return (
    <div
      aria-live="polite"
      className="pointer-events-none fixed inset-x-0 bottom-4 z-40 flex flex-col items-center gap-2 px-4 sm:items-end"
    >
      {items.map((t) => (
        <div
          key={t.id}
          role="status"
          className="toast-enter pointer-events-auto flex w-full max-w-sm items-center gap-3 rounded-lg border border-[var(--rule)] bg-card py-2 pr-2 pl-4 shadow-[var(--shadow-lg)]"
        >
          <Mark
            id={TONE[t.tone].mark}
            className={cn('size-5', TONE[t.tone].className)}
          />
          <p className="flex-1 font-bold">{t.message}</p>
          <button
            type="button"
            onClick={() => dismissToast(t.id)}
            className="grid size-11 shrink-0 cursor-pointer place-items-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground"
          >
            <Mark id="x" className="size-4" />
            <span className="sr-only">{m.close()}</span>
          </button>
        </div>
      ))}
    </div>
  )
}

/** Skeleton lines standing in for content while it loads (F-31). */
export function Skeleton({ className }: { className?: string }) {
  return <div aria-hidden="true" className={cn('skeleton h-4', className)} />
}

/** A loading screen shaped like the screen it stands in for. */
export function ScreenSkeleton({ panels = 2 }: { panels?: number }) {
  return (
    <div aria-busy="true" className="space-y-4">
      <span className="sr-only" role="status">
        {m.loading()}
      </span>
      <Skeleton className="h-9 w-2/3 max-w-md" />
      <Skeleton className="w-1/2 max-w-sm" />
      <div className="grid gap-4 pt-2 sm:grid-cols-2">
        {Array.from({ length: panels }, (_, i) => (
          <div
            key={i}
            className="space-y-3 rounded-lg border border-[var(--rule)] bg-card p-5"
          >
            <Skeleton className="h-5 w-1/3" />
            <Skeleton />
            <Skeleton className="w-5/6" />
            <Skeleton className="h-11 w-full" />
          </div>
        ))}
      </div>
    </div>
  )
}

/** An empty list: says what is (not) here and, when there is one, the next action (F-31). */
export function EmptyState({
  children,
  action,
  mark = 'info',
}: {
  children: ReactNode
  action?: ReactNode
  mark?: MarkId
}) {
  return (
    <div className="flex flex-col items-start gap-3 rounded-md border border-dashed border-[var(--rule)] bg-background px-4 py-5 sm:flex-row sm:items-center">
      <span className="grid size-10 shrink-0 place-items-center rounded-full bg-muted text-muted-foreground">
        <Mark id={mark} className="size-5" />
      </span>
      <div className="flex-1 text-muted-foreground">{children}</div>
      {action}
    </div>
  )
}

/** Step indicator for a flow: 1 Read → 2 Check → 3 Done (F-25). */
export function Steps({
  steps,
  current,
  label,
}: {
  steps: string[]
  current: number
  label: string
}) {
  return (
    <nav aria-label={label} className="mb-5">
      <ol className="flex items-center gap-2">
        {steps.map((s, i) => {
          const done = i < current
          const here = i === current
          return (
            <li
              key={s}
              aria-current={here ? 'step' : undefined}
              className="flex flex-1 items-center gap-2"
            >
              <span
                className={cn(
                  'tnum grid size-8 shrink-0 place-items-center rounded-full border-2 text-sm font-bold transition-colors duration-300',
                  done && 'border-primary bg-primary text-primary-foreground',
                  here && 'border-primary bg-card text-primary',
                  !done &&
                    !here &&
                    'border-[var(--rule)] text-muted-foreground',
                )}
              >
                {done ? <Mark id="check" className="size-4" /> : i + 1}
              </span>
              <span
                className={cn(
                  'text-sm font-bold whitespace-nowrap',
                  here ? 'text-foreground' : 'text-muted-foreground',
                )}
              >
                {s}
              </span>
              {i < steps.length - 1 && (
                <span
                  aria-hidden="true"
                  className={cn(
                    'h-0.5 min-w-3 flex-1 rounded-full transition-colors duration-300',
                    done ? 'bg-primary' : 'bg-[var(--rule)]',
                  )}
                />
              )}
            </li>
          )
        })}
      </ol>
    </nav>
  )
}

/** In-page confirmation for a destructive action (F-30), on the native <dialog>. */
export function ConfirmDialog({
  open,
  title,
  body,
  confirmLabel,
  onConfirm,
  onCancel,
}: {
  open: boolean
  title: string
  body: string
  confirmLabel: string
  onConfirm: () => void
  onCancel: () => void
}) {
  const ref = useRef<HTMLDialogElement>(null)
  useEffect(() => {
    const d = ref.current
    if (!d) return
    if (open && !d.open) d.showModal()
    if (!open && d.open) d.close()
  }, [open])
  return (
    <dialog
      ref={ref}
      className="sheet"
      aria-labelledby="confirm-title"
      onCancel={(e) => {
        e.preventDefault()
        onCancel()
      }}
    >
      <div className="space-y-3 p-6">
        <h2 id="confirm-title" className="display text-xl">
          {title}
        </h2>
        <p className="text-muted-foreground">{body}</p>
      </div>
      <div className="flex flex-wrap justify-end gap-2 border-t border-[var(--rule)] bg-background px-6 py-4">
        <Button variant="outline" onClick={onCancel}>
          {m.cancel()}
        </Button>
        <Button variant="danger" onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </div>
    </dialog>
  )
}
