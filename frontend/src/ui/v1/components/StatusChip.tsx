import type { MeetupKind, SkillStatus } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'
import { cn } from '@/ui/v1/lib/utils'

const STYLE: Record<SkillStatus, string> = {
  valid: 'bg-success text-primary-foreground',
  expiring: 'bg-warning text-primary-foreground',
  expired: 'bg-destructive text-primary-foreground',
  none: 'border border-border bg-muted text-muted-foreground',
}
// Shape as well as colour, so status never depends on colour alone (F-35)
const MARK: Record<SkillStatus, string> = {
  valid: '●',
  expiring: '▲',
  expired: '✕',
  none: '–',
}

export function statusLabel(s: SkillStatus) {
  return {
    valid: m.status_valid(),
    expiring: m.status_expiring(),
    expired: m.status_expired(),
    none: m.status_none(),
  }[s]
}

export function StatusChip({
  status,
  days,
}: {
  status: SkillStatus
  days?: number | null
}) {
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-sm font-bold whitespace-nowrap',
        STYLE[status],
      )}
    >
      <span aria-hidden="true">{MARK[status]}</span>
      {statusLabel(status)}
      {status === 'expiring' && days != null && ` · ${m.days_left({ days })}`}
    </span>
  )
}

export const meetupLabel = (k: MeetupKind) =>
  ({
    coffee: m.meet_coffee(),
    event: m.meet_event(),
    drill: m.meet_drill(),
    other: m.meet_other(),
  })[k]
