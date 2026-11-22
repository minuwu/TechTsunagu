import type { IconId } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'

// shortcut: Unicode glyphs stand in for the fixed pictogram set; swap for JIS/ISO 7010 SVGs before the pitch
const GLYPH: Record<IconId, string> = {
  'ppe-goggles': '🥽',
  'ppe-gloves': '🧤',
  'hot-surface': '♨',
  lockout: '🔒',
  measure: '📏',
  'crane-sling': '🏗',
  'emergency-stop': '🛑',
  'hands-clear': '✋',
  ventilation: '🌀',
  'check-list': '☑',
  garbage: '🗑',
  calendar: '📅',
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

export function Icon({ id, className }: { id: IconId; className?: string }) {
  return (
    <span role="img" aria-label={LABEL[id]()} className={className}>
      {GLYPH[id]}
    </span>
  )
}
