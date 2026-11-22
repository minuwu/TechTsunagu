import type { ReactNode } from 'react'
import { cn } from '@/ui/v1/lib/utils'

export function Card({
  title,
  children,
  className,
  tone,
  id,
}: {
  title?: ReactNode
  children: ReactNode
  className?: string
  tone?: 'danger' | 'success' | 'warning' | 'life' | undefined
  id?: string
}) {
  return (
    <section
      aria-labelledby={title && id ? `${id}-title` : undefined}
      className={cn(
        'rounded-lg border border-border bg-card p-4 text-card-foreground sm:p-5',
        tone === 'danger' && 'border-l-8 border-l-destructive',
        tone === 'success' && 'border-l-8 border-l-success',
        tone === 'warning' && 'border-l-8 border-l-warning',
        tone === 'life' && 'border-l-8 border-l-life',
        className,
      )}
    >
      {title && (
        <h2 id={id ? `${id}-title` : undefined} className="mb-3 text-lg">
          {title}
        </h2>
      )}
      {children}
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
    <div className="mb-6 space-y-1">
      <h1 className="text-2xl sm:text-3xl">{children}</h1>
      {sub && <p className="text-muted-foreground">{sub}</p>}
    </div>
  )
}

export const inputClass =
  'min-h-11 w-full rounded-md border border-input bg-background px-3 py-2 text-base outline-none focus-visible:ring-[3px] focus-visible:ring-ring/60'
