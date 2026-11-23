import { cn } from '@/ui/v4/lib/utils'

/** Nagasaki Megane Bridge: geometric stepped arches with reflection forming circles. */
export function BridgeMark({
  className,
  draw = false,
}: {
  className?: string | undefined
  draw?: boolean
}) {
  return (
    <svg
      viewBox="0 0 48 28"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="square"
      strokeLinejoin="miter"
      aria-hidden="true"
      className={cn('shrink-0', className)}
    >
      <path d="M2 14V8h4V4h16v4h4V4h16v4h4v6" />
      <path
        d="M6 14a8 8 0 0 1 16 0M26 14a8 8 0 0 1 16 0"
        pathLength={1}
        className={draw ? 'arc-draw' : undefined}
      />
      <path
        d="M6 14a8 8 0 0 0 16 0M26 14a8 8 0 0 0 16 0"
        pathLength={1}
        className={draw ? 'arc-draw-late' : undefined}
      />
      <path d="M1 14h46" />
    </svg>
  )
}
