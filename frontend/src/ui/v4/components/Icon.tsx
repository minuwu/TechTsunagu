import type { ReactNode } from 'react'
import type { IconId } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'
import { cn } from '@/ui/v4/lib/utils'

// One drawn set, one stroke: the shop's fixed pictograms plus the UI marks. 24px grid, 1.75 stroke.
const PICTO: Record<IconId, ReactNode> = {
  'ppe-goggles': (
    <>
      <path d="M2.5 11.5c0-2 1.3-3.5 3.2-3.5h12.6c1.9 0 3.2 1.5 3.2 3.5v1c0 2-1.4 3.5-3.2 3.5h-3.1l-1.8-2h-2.8l-1.8 2H5.7c-1.8 0-3.2-1.5-3.2-3.5z" />
      <path d="M6 11h3M15 11h3" />
    </>
  ),
  'ppe-gloves': (
    <path d="M8 21v-4.5l-3.2-3.8a1.6 1.6 0 0 1 2.3-2.2L8 11.4V5a1.5 1.5 0 0 1 3 0v5.5V3.8a1.5 1.5 0 0 1 3 0v6.7V5.2a1.5 1.5 0 0 1 3 0V14c0 2.6-1.2 4.2-3 5v2" />
  ),
  'hot-surface': (
    <>
      <path d="M7.5 3.5c-1.4 1.7 1.4 2.8 0 4.5M12 3.5c-1.4 1.7 1.4 2.8 0 4.5M16.5 3.5c-1.4 1.7 1.4 2.8 0 4.5" />
      <path d="M3 12h18M5 12v8h14v-8" />
    </>
  ),
  lockout: (
    <>
      <rect x="5" y="10.5" width="14" height="10.5" rx="1.5" />
      <path d="M8 10.5V7a4 4 0 0 1 8 0v3.5M12 14.5v3" />
    </>
  ),
  measure: (
    <>
      <path d="M3 15.5 15.5 3 21 8.5 8.5 21z" />
      <path d="m7 11.5 2 2M10 8.5l2 2M13 5.5l2 2M5.5 14.5l1.2 1.2" />
    </>
  ),
  'crane-sling': (
    <>
      <path d="M12 2v5M12 7c1.7 0 2.8 1 2.8 2.4S13.6 12 12 12" />
      <path d="m12 12-6.5 5h13z" />
      <rect x="4" y="17" width="16" height="4" rx="0.5" />
    </>
  ),
  'emergency-stop': (
    <>
      <path d="M8.3 2.5h7.4l5.3 5.3v7.4l-5.3 5.3H8.3L3 15.2V7.8z" />
      <path d="M8 12h8" />
    </>
  ),
  'hands-clear': (
    <path d="M7 13.5V6.2a1.5 1.5 0 0 1 3 0V11V4.5a1.5 1.5 0 0 1 3 0V11V5.6a1.5 1.5 0 0 1 3 0V12V8.7a1.5 1.5 0 0 1 3 0V15a6 6 0 0 1-6 6h-.8a6 6 0 0 1-5-2.6l-2.4-3.6a1.5 1.5 0 0 1 2.5-1.7z" />
  ),
  ventilation: (
    <>
      <circle cx="12" cy="12" r="9.5" />
      <circle cx="12" cy="12" r="1.4" />
      <path d="M12 10.6c-.4-3.4 1.2-5.6 3.4-5.2 2 .4 1.4 3.6-2.4 5.4M13.3 12.6c3 1.5 4 4 2.4 5.5-1.5 1.4-3.9-.8-3.6-4.8M10.7 12.7c-2.8 2-5.5 1.7-6-.4-.5-2 2.8-2.8 5.9-.6" />
    </>
  ),
  'check-list': (
    <>
      <rect x="5" y="4.5" width="14" height="16.5" rx="1.5" />
      <path d="M9 3h6v3H9zM8.5 11l1.6 1.6 3-3M8.5 16.5l1.6 1.6 3-3M15.5 11.5h.5M15.5 17h.5" />
    </>
  ),
  garbage: <path d="M4 7h16M9.5 7V4h5v3M6 7l1 14h10l1-14M10 11v6M14 11v6" />,
  calendar: (
    <>
      <rect x="3" y="5" width="18" height="16" rx="1.5" />
      <path d="M3 10h18M8 3v4M16 3v4M7.5 14h2M11 14h2M14.5 14h2M7.5 17.5h2M11 17.5h2" />
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
      strokeWidth={1.75}
      strokeLinecap="round"
      strokeLinejoin="round"
      className={cn('inline-block size-[1.25em] shrink-0', className)}
      role={label ? 'img' : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
    >
      {children}
    </svg>
  )
}

/** A shop pictogram (PPE, hot surface…), announced by its name. */
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
  // With tip, the name also shows on hover for readers who do not know the pictogram
  return tip ? (
    <span data-tip={LABEL[id]()} className="inline-flex rounded-sm">
      {svg}
    </span>
  ) : (
    svg
  )
}

const MARKS = {
  check: <path d="m5 12.5 4.5 4.5L19 7" />,
  x: <path d="M6 6l12 12M18 6 6 18" />,
  alert: <path d="M12 3.5 21.5 20h-19zM12 10v4.5M12 17.2v.01" />,
  dash: <path d="M6 12h12" />,
  info: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v5.5M12 7.8v.01" />
    </>
  ),
  search: (
    <path d="M10.5 17a6.5 6.5 0 1 0 0-13 6.5 6.5 0 0 0 0 13zM15.5 15.5 20 20" />
  ),
  lock: (
    <>
      <rect x="5" y="10.5" width="14" height="10.5" rx="1.5" />
      <path d="M8 10.5V7a4 4 0 0 1 8 0v3.5" />
    </>
  ),
  unlock: (
    <>
      <rect x="5" y="10.5" width="14" height="10.5" rx="1.5" />
      <path d="M8 10.5V7a4 4 0 0 1 7.6-1.8" />
    </>
  ),
  arrow: <path d="M5 12h14M13 6l6 6-6 6" />,
  mic: (
    <>
      <rect x="9" y="3" width="6" height="11" rx="3" />
      <path d="M5.5 11a6.5 6.5 0 0 0 13 0M12 17.5V21" />
    </>
  ),
  stop: <rect x="6.5" y="6.5" width="11" height="11" rx="1" />,
  wait: (
    <path d="M7 3h10M7 21h10M8 3c0 5 8 5 8 9s-8 4-8 9M16 3c0 5-8 5-8 9s8 4 8 9" />
  ),
  qr: (
    <path d="M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h2v2h-2zM18 18h2v2h-2zM14 18h2M18 14h2" />
  ),
  reset: <path d="M4 12a8 8 0 1 0 2.4-5.7M4 4v4h4" />,
} as const

export type MarkId = keyof typeof MARKS

/** A UI mark (check, lock, arrow…). Decorative: the text next to it carries the meaning. */
export function Mark({ id, className }: { id: MarkId; className?: string }) {
  return <Svg className={className}>{MARKS[id]}</Svg>
}
