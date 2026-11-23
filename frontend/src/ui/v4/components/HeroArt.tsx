import { cn } from '@/ui/v4/lib/utils'

/** Hero poster: Megane Bridge as flat geometry. Two arches, their reflection, a black deck, a cobalt sun. */
export function HeroArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 600 360"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
      className={cn('block w-full', className)}
    >
      <rect width="600" height="360" fill="#E3E2DE" />
      <rect x="0" y="180" width="600" height="180" fill="#C7C7C7" />
      <circle cx="450" cy="92" r="52" fill="#1351AA" />
      <g fill="none" stroke="#141414" strokeWidth="14">
        <path d="M60 180a110 110 0 0 1 220 0" />
        <path d="M280 180a110 110 0 0 1 220 0" />
      </g>
      <g fill="none" stroke="#141414" strokeWidth="14" opacity="0.28">
        <path d="M60 180a110 110 0 0 0 220 0" />
        <path d="M280 180a110 110 0 0 0 220 0" />
      </g>
      <rect x="0" y="172" width="600" height="16" fill="#141414" />
      <g fill="#141414">
        <rect x="52" y="180" width="16" height="48" />
        <rect x="272" y="180" width="16" height="48" />
        <rect x="492" y="180" width="16" height="48" />
      </g>
      <g stroke="#141414" strokeWidth="3">
        <path d="M0 252h600M0 288h600M0 324h600" opacity="0.18" />
      </g>
    </svg>
  )
}
