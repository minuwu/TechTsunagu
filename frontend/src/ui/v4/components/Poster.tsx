import type { ReactNode } from 'react'
import { Link } from '@tanstack/react-router'
import { cn } from '@/ui/v4/lib/utils'

/** A grid-locked section: columns 1-3 carry a sticky label, 4-12 the content (one column on phones). */
export function PosterSection({
  label,
  id,
  children,
  className,
}: {
  label: ReactNode
  id?: string
  children: ReactNode
  className?: string
}) {
  return (
    <section
      id={id}
      aria-label={typeof label === 'string' ? label : undefined}
      className="poster-grid"
    >
      <div className="poster-side">
        <p className="poster-label">{label}</p>
      </div>
      <div className={cn('poster-main', className)}>{children}</div>
    </section>
  )
}

/** Mono index number: 01, 02, 03. */
export function PosterIndex({
  n,
  className,
}: {
  n: number
  className?: string
}) {
  return (
    <span
      aria-hidden="true"
      className={cn('poster-index tnum shrink-0 pt-2', className)}
    >
      {String(n).padStart(2, '0')}
    </span>
  )
}

/** Typographic list item: hairline above, index left, big title that turns cobalt on hover. */
export function PosterRow({
  n,
  title,
  sub,
  aside,
  onClick,
  to,
  className,
}: {
  n: number
  title: ReactNode
  sub?: ReactNode
  aside?: ReactNode
  onClick?: () => void
  to?: string
  className?: string
}) {
  const body = (
    <>
      <PosterIndex n={n} />
      <span className="min-w-0 flex-1 space-y-2">
        <span className="poster-row-title block">{title}</span>
        {sub && (
          <span className="block max-w-[60ch] text-muted-foreground">
            {sub}
          </span>
        )}
      </span>
      {aside && (
        <span className="shrink-0 pt-2 text-sm font-bold">{aside}</span>
      )}
    </>
  )
  if (to)
    return (
      <Link to={to} className={cn('poster-row', className)}>
        {body}
      </Link>
    )
  return onClick ? (
    <button
      type="button"
      onClick={onClick}
      className={cn('poster-row cursor-pointer', className)}
    >
      {body}
    </button>
  ) : (
    <div className={cn('poster-row', className)}>{body}</div>
  )
}
