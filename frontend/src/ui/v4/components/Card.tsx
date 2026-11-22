import type { ReactNode } from 'react'
import { cn } from '@/ui/v4/lib/utils'

type Tone = 'danger' | 'success' | 'warning' | 'life'

// A tone is a 4px bar on the left edge and a coloured title; the title text still says what it is (F-35)
const TONE: Record<Tone, { bar: string; text: string }> = {
  danger: { bar: 'border-l-destructive', text: 'text-destructive' },
  success: { bar: 'border-l-success', text: 'text-success' },
  warning: { bar: 'border-l-warning', text: 'text-warning' },
  life: { bar: 'border-l-life', text: 'text-life' },
}

/** A panel: hairline box, flat, optional title row with a tone. */
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
        'card-surface min-w-0 text-card-foreground',
        t && cn('border-l-4', t.bar),
        className,
      )}
    >
      {title && (
        <div className="flex items-center justify-between gap-3 border-b border-[var(--tile-border)] px-4 py-3 sm:px-5">
          <h2
            id={id ? `${id}-title` : undefined}
            className={cn('display text-lg leading-snug', t?.text)}
          >
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
    <div className="mb-10 space-y-4 border-b border-[var(--rule)] pb-8">
      <h1 className="poster-h2 break-words">{children}</h1>
      {sub && (
        <p className="max-w-[60ch] text-lg text-pretty text-muted-foreground">
          {sub}
        </p>
      )}
    </div>
  )
}

export const inputClass =
  'min-h-11 w-full min-w-0 border border-input bg-card px-3 py-2 text-base outline-none transition-colors duration-300 placeholder:text-muted-foreground hover:bg-white/20 focus-visible:border-primary focus-visible:ring-[3px] focus-visible:ring-ring/25'
