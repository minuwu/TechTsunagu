import type { MeetupKind, SkillStatus } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'
import { cn } from '@/ui/v2/lib/utils'
import { Mark, type MarkId } from './Icon'

const STYLE: Record<SkillStatus, string> = {
  valid: 'border-success/30 bg-success/10 text-success',
  expiring: 'border-warning/35 bg-warning/10 text-warning',
  expired: 'border-destructive/30 bg-destructive/10 text-destructive',
  none: 'border-[var(--rule)] bg-muted text-muted-foreground',
}
// Shape as well as colour, so status never depends on colour alone (F-35)
const MARK: Record<SkillStatus, MarkId> = {
  valid: 'check',
  expiring: 'alert',
  expired: 'x',
  none: 'dash',
}

export function statusLabel(s: SkillStatus) {
  return {
    valid: m.status_valid(),
    expiring: m.status_expiring(),
    expired: m.status_expired(),
    none: m.status_none(),
  }[s]
}

/** A status pill: tint, mark and word, so it never relies on colour alone. */
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
        'inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-sm font-bold whitespace-nowrap',
        STYLE[status],
      )}
    >
      <Mark id={MARK[status]} className="size-4" />
      {statusLabel(status)}
      {status === 'expiring' && days != null && (
        <span className="tnum">· {m.days_left({ days })}</span>
      )}
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
