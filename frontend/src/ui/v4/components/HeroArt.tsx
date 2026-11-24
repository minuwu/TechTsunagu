import { cn } from '@/ui/v4/lib/utils'

/** Hero poster: Megane Bridge as flat geometry. Two arches, their reflection, a deck and a sun, drawn in the
 *  theme colours: a navy bridge under a steel sun by day, a snow-white bridge under a pale moon by night. */
export function HeroArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 600 360"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
      className={cn('block w-full', className)}
    >
      <rect width="600" height="360" className="fill-[var(--background)]" />
      <rect
        x="0"
        y="180"
        width="600"
        height="180"
        className="fill-[var(--border)]"
      />
      <circle cx="450" cy="92" r="52" className="fill-[var(--primary)]" />
      <g fill="none" className="stroke-[var(--foreground)]" strokeWidth="14">
        <path d="M60 180a110 110 0 0 1 220 0" />
        <path d="M280 180a110 110 0 0 1 220 0" />
      </g>
      <g
        fill="none"
        className="stroke-[var(--foreground)]"
        strokeWidth="14"
        opacity="0.28"
      >
        <path d="M60 180a110 110 0 0 0 220 0" />
        <path d="M280 180a110 110 0 0 0 220 0" />
      </g>
      <rect
        x="0"
        y="172"
        width="600"
        height="16"
        className="fill-[var(--foreground)]"
      />
      <g className="fill-[var(--foreground)]">
        <rect x="52" y="180" width="16" height="48" />
        <rect x="272" y="180" width="16" height="48" />
        <rect x="492" y="180" width="16" height="48" />
      </g>
      <g className="stroke-[var(--foreground)]" strokeWidth="3">
        <path d="M0 252h600M0 288h600M0 324h600" opacity="0.18" />
      </g>
    </svg>
  )
}
