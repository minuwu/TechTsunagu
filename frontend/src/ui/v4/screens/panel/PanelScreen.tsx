import { pick } from '@/domain/wakatta'
import { usePanel } from '@/features/panel'
import { m } from '@/i18n/paraglide/messages'
import { StatePlate } from '@/ui/v4/components/StatePlate'
import { statusLabel } from '@/ui/v4/components/StatusChip'
import { uiLang } from '@/ui/v4/lib/format'

/** Machine Panel: the tablet beside the Machine. Software lock only (ADR 0003). */
export function PanelScreen() {
  const p = usePanel()
  if (p.loading)
    return (
      <div aria-busy="true" className="skeleton min-h-[60vh] rounded-lg">
        <span className="sr-only">{m.loading()}</span>
      </div>
    )
  if (p.notFound || !p.machine) return <p>{m.machine_not_found()}</p>
  return (
    <div className="space-y-4">
      <h1 className="display text-3xl sm:text-4xl">
        {pick(p.machine.name, uiLang())}
      </h1>
      <div role="status" aria-live="polite">
        <StatePlate state={p.state} size="lg" className="min-h-[55vh]">
          {p.worker && (
            <p className="text-2xl font-bold sm:text-3xl">
              {p.worker.name}
              {p.reason && (
                <span className="font-normal"> · {statusLabel(p.reason)}</span>
              )}
            </p>
          )}
          {p.state === 'locked' && (
            <p className="text-xl">{m.panel_locked_hint()}</p>
          )}
          {p.state === 'idle' && (
            <p className="text-xl">{m.panel_idle_hint()}</p>
          )}
        </StatePlate>
      </div>
      <div className="flex flex-wrap items-baseline justify-between gap-x-6 gap-y-1 rounded-lg border border-[var(--rule)] bg-card px-4 py-3">
        <dl className="flex flex-wrap gap-x-2">
          <dt className="text-muted-foreground">{m.panel_assigned()}</dt>
          <dd className="font-bold">
            {p.assigned.length ? p.assigned.join('・') : m.none()}
          </dd>
        </dl>
        <p className="text-sm text-muted-foreground">
          {m.panel_software_note()}
        </p>
      </div>
    </div>
  )
}
