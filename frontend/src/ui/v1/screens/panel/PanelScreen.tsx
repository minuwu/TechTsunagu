import { pick } from '@/domain/wakatta'
import { usePanel } from '@/features/panel'
import { m } from '@/i18n/paraglide/messages'
import { statusLabel } from '@/ui/v1/components/StatusChip'
import { cn } from '@/ui/v1/lib/utils'
import { uiLang } from '@/ui/v1/lib/format'

/** Machine Panel: the tablet beside the Machine. Software lock only (ADR 0003). */
export function PanelScreen() {
  const p = usePanel()
  if (p.loading) return <p>{m.loading()}</p>
  if (p.notFound || !p.machine) return <p>{m.machine_not_found()}</p>
  return (
    <div
      role="status"
      aria-live="polite"
      className={cn(
        'flex min-h-[70vh] flex-col items-center justify-center gap-6 rounded-xl p-8 text-center text-primary-foreground',
        p.state === 'ready' && 'bg-success',
        p.state === 'locked' && 'bg-destructive',
        p.state === 'idle' && 'bg-muted text-foreground',
      )}
    >
      <h1 className="text-2xl">{pick(p.machine.name, uiLang())}</h1>
      <p className="text-6xl font-black sm:text-8xl">
        <span aria-hidden="true">
          {p.state === 'ready' ? '✓ ' : p.state === 'locked' ? '✕ ' : '… '}
        </span>
        {p.state === 'ready'
          ? m.panel_ready()
          : p.state === 'locked'
            ? m.panel_locked()
            : m.panel_idle()}
      </p>
      {p.worker && (
        <p className="text-2xl">
          {p.worker.name}
          {p.reason && ` · ${statusLabel(p.reason)}`}
        </p>
      )}
      {p.state === 'locked' && (
        <p className="text-xl">{m.panel_locked_hint()}</p>
      )}
      {p.state === 'idle' && <p className="text-xl">{m.panel_idle_hint()}</p>}
      <p className="text-lg opacity-90">
        {m.panel_assigned()}{' '}
        {p.assigned.length ? p.assigned.join('・') : m.none()}
      </p>
      <p className="text-sm opacity-80">{m.panel_software_note()}</p>
    </div>
  )
}
