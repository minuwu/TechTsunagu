import { cn } from '@/ui/v2/lib/utils'

/**
 * The mark: Nagasaki's Megane bridge. Two arches and their reflection close into a pair of circles.
 * Decorative; the app name always sits beside it.
 */
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
      strokeWidth={2.25}
      strokeLinecap="round"
      aria-hidden="true"
      className={cn('shrink-0', className)}
    >
      <path
        d="M5 14a9 9 0 0 1 18 0M25 14a9 9 0 0 1 18 0"
        pathLength={1}
        className={draw ? 'arc-draw' : undefined}
      />
      <path
        d="M5 14a9 9 0 0 0 18 0M25 14a9 9 0 0 0 18 0"
        pathLength={1}
        opacity={0.45}
        className={draw ? 'arc-draw-late' : undefined}
      />
      <path d="M1.5 14h45" strokeWidth={1.5} opacity={0.7} />
    </svg>
  )
}
