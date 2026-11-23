import type { ReactNode } from 'react'
import type { IconId } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'
import { cn } from '@/ui/v4/lib/utils'

// Shop pictograms and UI marks in Poster Modernist style: 24px grid, bold 2-unit stroke.
const PICTO: Record<IconId, ReactNode> = {
  'ppe-goggles': (
    <>
      <path d="M3 8h7v8H5l-2-3zM14 8h7v5l-2 3h-5z" />
      <path d="M10 11h4M1 11h2M21 11h2M6 11h2M16 11h2" />
    </>
  ),
  'ppe-gloves': (
    <>
      <path d="M8 18v-4l-4-3v-2l4 2V5h8v13M12 5v6" />
      <path d="M7 18h10M7 21h10" />
    </>
  ),
  'hot-surface': (
    <>
      <path d="M6 9l2-2-2-2 2-2M11 9l2-2-2-2 2-2M16 9l2-2-2-2 2-2" />
      <path d="M2 13h20M5 13v7h14v-7" />
    </>
  ),
  lockout: (
    <>
      <rect x="5" y="11" width="14" height="10" />
      <path d="M8 11V5h8v6M5 16h14M12 13v3" />
    </>
  ),
  measure: (
    <>
      <path d="M3 15 15 3l6 6L9 21z" />
      <path d="M5 13l2 2M7 11l3 3M9 9l2 2M11 7l3 3M13 5l2 2" />
    </>
  ),
  'crane-sling': (
    <>
      <path d="M12 2v4M12 6v4h4v3h-4v-1" />
      <path d="M12 13 4 18M12 13l8 5" />
      <rect x="4" y="18" width="16" height="4" />
    </>
  ),
  'emergency-stop': (
    <>
      <path d="M8 3h8l5 5v8l-5 5H8l-5-5V8z" />
      <path d="M7 12h10" />
    </>
  ),
  'hands-clear': (
    <>
      <path d="M9 13V5h2v-2h2v2h2v3h2v6l-3 7H9v-4" />
      <path d="M9 15l-5-2v-2l5 2M11 5v6M13 3v8M15 5v6" />
    </>
  ),
  ventilation: (
    <>
      <circle cx="12" cy="12" r="9" />
      <circle cx="12" cy="12" r="2" />
      <path d="M12 10V3h4l-4 7M14 12h7v4l-7-4M12 14v7H8l4-7M10 12H3V8l7 4" />
    </>
  ),
  'check-list': (
    <>
      <rect x="4" y="5" width="16" height="16" />
      <path d="M8 5V3h8v2M7 10l2 2 3-3M14 10h3M7 16l2 2 3-3M14 16h3" />
    </>
  ),
  garbage: (
    <path d="M9 6V3h6v3M3 6h18M5 6l1 15h12l1-15M9 10v7M12 10v7M15 10v7" />
  ),
  calendar: (
    <>
      <rect x="3" y="5" width="18" height="16" />
      <path d="M3 10h18M7 2v4M17 2v4M7 13h2M11 13h2M15 13h2M7 17h2M11 17h2M15 17h2" />
    </>
  ),
}

const LABEL: Record<IconId, () => string> = {
  'ppe-goggles': m.icon_goggles,
  'ppe-gloves': m.icon_gloves,
  'hot-surface': m.icon_hot,
  lockout: m.icon_lockout,
  measure: m.icon_measure,
  'crane-sling': m.icon_sling,
  'emergency-stop': m.icon_estop,
  'hands-clear': m.icon_hands,
  ventilation: m.icon_vent,
  'check-list': m.icon_check,
  garbage: m.icon_garbage,
  calendar: m.icon_calendar,
}

function Svg({
  children,
  className,
  label,
}: {
  children: ReactNode
  className?: string | undefined
  label?: string | undefined
}) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="square"
      strokeLinejoin="miter"
      className={cn('inline-block size-[1.25em] shrink-0', className)}
      role={label ? 'img' : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
    >
      {children}
    </svg>
  )
}

/** Shop pictogram announced by name. */
export function Icon({
  id,
  className,
  tip = false,
}: {
  id: IconId
  className?: string
  tip?: boolean
}) {
  const svg = (
    <Svg className={className} label={LABEL[id]()}>
      {PICTO[id]}
    </Svg>
  )
  return tip ? (
    <span data-tip={LABEL[id]()} className="inline-flex rounded-sm">
      {svg}
    </span>
  ) : (
    svg
  )
}

const MARKS = {
  check: <path d="M4 12l5 5L20 6" />,
  x: <path d="M5 5l14 14M19 5L5 19" />,
  alert: <path d="M12 3l9 17H3zM12 8v5M12 17.5v.01" />,
  dash: <path d="M5 12h14" />,
  info: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v6M12 7.5v.01" />
    </>
  ),
  search: (
    <>
      <circle cx="10" cy="10" r="6" />
      <path d="m14.5 14.5 5.5 5.5" />
    </>
  ),
  lock: (
    <>
      <rect x="5" y="11" width="14" height="10" />
      <path d="M8 11V5h8v6M12 14v4" />
    </>
  ),
  unlock: (
    <>
      <rect x="5" y="11" width="14" height="10" />
      <path d="M8 11V4h8v3M12 14v4" />
    </>
  ),
  arrow: <path d="M4 12h15M13 6l6 6-6 6" />,
  mic: (
    <>
      <rect x="9" y="3" width="6" height="9" />
      <path d="M9 6h6M6 8v5h12V8M12 16v5M8 21h8" />
    </>
  ),
  stop: <rect x="6" y="6" width="12" height="12" />,
  wait: (
    <path d="M6 3h12M6 21h12M7 3l5 9-5 9M17 3l-5 9 5 9M9 17h6M12 10v2" />
  ),
  qr: (
    <path d="M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM7 7h.01M17 7h.01M7 17h.01M14 14h2v2h-2zM18 18h2v2h-2zM14 19h3M19 14v3" />
  ),
  reset: <path d="M4 12a8 8 0 1 0 2.5-5.8M4 3v5h5" />,
} as const

export type MarkId = keyof typeof MARKS

/** Decorative UI mark. */
export function Mark({ id, className }: { id: MarkId; className?: string }) {
  return <Svg className={className}>{MARKS[id]}</Svg>
}
