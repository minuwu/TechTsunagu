import type { ReactNode } from 'react'
import { m } from '@/i18n/paraglide/messages'
import { cn } from '@/ui/v3/lib/utils'
import { BridgeMark } from './Brand'
import { Mark } from './Icon'

export type PlateState = 'ready' | 'locked' | 'idle'

const LOOK: Record<PlateState, string> = {
  locked: 'bg-destructive text-primary-foreground',
  ready: 'bg-success text-primary-foreground',
  idle: 'border border-dashed border-border bg-muted text-muted-foreground',
}

const MARK = { locked: 'lock', ready: 'unlock', idle: 'wait' } as const

export const plateWord = (s: PlateState) =>
  s === 'ready'
    ? m.panel_ready()
    : s === 'locked'
      ? m.panel_locked()
      : m.panel_idle()

/**
 * The state plate: Lock in red, Ready in green, waiting in mist. Word + mark + colour, so it reads
 * without colour (F-35). The large plate carries the bridge arc, which draws itself on Ready.
 * Re-keyed on change so it settles in.
 */
export function StatePlate({
  state,
  size = 'md',
  art,
  children,
  className,
}: {
  state: PlateState
  size?: 'sm' | 'md' | 'lg'
  /** Shown instead of the mark, e.g. the unlock moment right after the plate turns Ready */
  art?: ReactNode
  children?: ReactNode
  className?: string
}) {
  const lg = size === 'lg'
  const sm = size === 'sm'
  return (
    <div
      key={state}
      className={cn(
        'plate-swing relative flex flex-col overflow-hidden rounded-lg',
        LOOK[state],
        lg && 'shadow-[var(--shadow-md)]',
        className,
      )}
    >
      {lg && state !== 'idle' && (
        <BridgeMark
          draw={state === 'ready'}
          className="pointer-events-none absolute -bottom-8 left-1/2 w-[min(36rem,90%)] -translate-x-1/2 opacity-15"
        />
      )}
      <div
        className={cn(
          'relative flex flex-1 items-center gap-3',
          lg
            ? 'flex-col justify-center gap-5 px-6 py-12 text-center sm:py-16'
            : sm
              ? 'flex-wrap px-3 py-2'
              : 'px-4 py-3',
        )}
      >
        {art ?? (
          <span
            aria-hidden="true"
            className={cn(
              'grid shrink-0 place-items-center rounded-full',
              state === 'idle' ? 'bg-card' : 'bg-white/15',
              lg ? 'size-24 sm:size-28' : sm ? 'size-7' : 'size-10',
            )}
          >
            <Mark
              id={MARK[state]}
              className={lg ? 'size-12 sm:size-14' : sm ? 'size-4' : 'size-5'}
            />
          </span>
        )}
        <p
          className={cn(
            'display leading-none',
            lg ? 'text-5xl sm:text-7xl' : sm ? 'text-base' : 'text-xl',
          )}
        >
          {plateWord(state)}
        </p>
        {children && (
          <div className={cn('min-w-0', lg ? 'space-y-3' : 'text-base')}>
            {children}
          </div>
        )}
      </div>
    </div>
  )
}
