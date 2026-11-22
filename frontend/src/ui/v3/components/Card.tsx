import type { CSSProperties, ReactNode } from 'react'
import { cn } from '@/ui/v3/lib/utils'

type Tone = 'danger' | 'success' | 'warning' | 'life'

// A tone tints the title band and edge; the title text and icons still say what it is (F-35)
const TONE: Record<Tone, { edge: string; band: string; dot: string }> = {
  danger: {
    edge: 'border-destructive/40',
    band: 'bg-destructive/[0.06] text-destructive',
    dot: 'bg-destructive',
  },
  success: {
    edge: 'border-success/40',
    band: 'bg-success/[0.06] text-success',
    dot: 'bg-success',
  },
  warning: {
    edge: 'border-warning/40',
    band: 'bg-warning/[0.07] text-warning',
    dot: 'bg-warning',
  },
  life: {
    edge: 'border-life/35',
    band: 'bg-life/[0.06] text-life',
    dot: 'bg-life',
  },
}

/** A panel: hairline edge, soft depth, optional title band with a tone. */
export function Card({
  title,
  children,
  className,
  tone,
  id,
  aside,
}: {
  title?: ReactNode
  children: ReactNode
  className?: string
  tone?: Tone | undefined
  id?: string
  aside?: ReactNode
}) {
  const t = tone ? TONE[tone] : undefined
  return (
    <section
      aria-labelledby={title && id ? `${id}-title` : undefined}
      className={cn(
        // card-surface: light-blue panel, blue hairline, hover lift (tokens.css); a tone keeps its edge
        'card-surface door min-w-0 text-card-foreground',
        t && `${t.edge}!`,
        className,
      )}
      style={
        tone
          ? ({
              '--door-glow': `color-mix(in oklch, var(--${tone === 'danger' ? 'destructive' : tone}) 30%, transparent)`,
            } as CSSProperties)
          : undefined
      }
    >
      {title && (
        <div
          className={cn(
            'flex items-center justify-between gap-3 border-b px-4 py-3 sm:px-5',
            t ? cn(t.band, t.edge) : 'border-[var(--tile-border)]',
          )}
        >
          <h2
            id={id ? `${id}-title` : undefined}
            className="display flex items-center gap-2.5 text-lg leading-snug"
          >
            {t && (
              <span
                aria-hidden="true"
                className={cn('size-2 shrink-0 rounded-full', t.dot)}
              />
            )}
            {title}
          </h2>
          {aside}
        </div>
      )}
      <div className="p-4 sm:p-5">{children}</div>
    </section>
  )
}

export function PageTitle({
  children,
  sub,
}: {
  children: ReactNode
  sub?: ReactNode
}) {
  return (
    <div className="mb-8 space-y-2">
      <h1 className="display text-3xl leading-tight break-words text-balance sm:text-4xl">
        {children}
      </h1>
      {sub && (
        <p className="max-w-[60ch] text-pretty text-muted-foreground">{sub}</p>
      )}
    </div>
  )
}

export const inputClass =
  'min-h-11 w-full min-w-0 rounded-md border border-input bg-card px-3 py-2 text-base shadow-[inset_0_1px_2px_oklch(0.26_0.07_262/0.05)] outline-none transition-[border-color,box-shadow] duration-150 placeholder:text-muted-foreground hover:border-primary/60 focus-visible:border-primary focus-visible:ring-[3px] focus-visible:ring-ring/25'
