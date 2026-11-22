import type { ReactNode } from 'react'
import { Link } from '@tanstack/react-router'
import { pick } from '@/domain/wakatta'
import { useBoard } from '@/features/foreman'
import { m } from '@/i18n/paraglide/messages'
import { Card, PageTitle } from '@/ui/v1/components/Card'
import { ForemanNav } from '@/ui/v1/components/Nav'
import { PinGate } from '@/ui/v1/components/PinGate'
import { StatusChip } from '@/ui/v1/components/StatusChip'
import { Button } from '@/ui/v1/components/ui/button'
import { fmtDate, uiLang } from '@/ui/v1/lib/format'
import { cn } from '@/ui/v1/lib/utils'

/** Every Foreman screen: PIN, nav, title. */
export function ForemanPage({
  title,
  sub,
  children,
}: {
  title: string
  sub?: string
  children: ReactNode
}) {
  return (
    <PinGate gate="foreman">
      <ForemanNav />
      <PageTitle sub={sub}>{title}</PageTitle>
      {children}
    </PinGate>
  )
}

const PANEL = {
  ready: 'bg-success text-primary-foreground',
  locked: 'bg-destructive text-primary-foreground',
  idle: 'bg-muted text-muted-foreground',
}

/** Board: who is on which Machine, what is locked and why, open Confusions. */
export function ForemanBoardScreen() {
  const b = useBoard()
  const lang = uiLang()
  return (
    <ForemanPage title={m.board_title()} sub={m.board_sub()}>
      {b.loading ? (
        <p>{m.loading()}</p>
      ) : (
        <div className="space-y-4">
          {b.flags.length > 0 && (
            <Card
              tone="danger"
              title={m.flags_title({ count: b.flags.length })}
              id="flags"
            >
              <ul className="space-y-2">
                {b.flags.map((f) => (
                  <li key={f.id} className="flex flex-wrap items-center gap-2">
                    <strong>{f.workerName}</strong>
                    {pick(f.machine.name, lang)}
                    <StatusChip status={f.status} />
                    <Link
                      to="/foreman/assign"
                      className="text-primary underline"
                    >
                      {m.reassign()}
                    </Link>
                  </li>
                ))}
              </ul>
            </Card>
          )}

          <div className="grid gap-4 sm:grid-cols-2">
            {b.rows.map((r) => (
              <Card
                key={r.machine.id}
                id={`mc-${r.machine.id}`}
                title={pick(r.machine.name, lang)}
              >
                <p
                  className={cn(
                    'mb-3 rounded-md px-3 py-2 text-lg font-black',
                    PANEL[r.panel as keyof typeof PANEL],
                  )}
                >
                  {r.panel === 'ready'
                    ? m.panel_ready()
                    : r.panel === 'locked'
                      ? m.panel_locked()
                      : m.panel_idle()}
                  {r.activeWorker && ` · ${r.activeWorker}`}
                </p>
                <ul className="mb-3 space-y-1">
                  {r.assigned.map((a) => (
                    <li
                      key={a.id}
                      className="flex items-center justify-between gap-2"
                    >
                      <span>
                        {a.shift === 'day' ? m.shift_day() : m.shift_night()}
                        {': '}
                        {a.workerName}
                      </span>
                      <StatusChip status={a.status} />
                    </li>
                  ))}
                  {r.assigned.length === 0 && (
                    <li className="text-muted-foreground">
                      {m.nobody_assigned()}
                    </li>
                  )}
                </ul>
                <div className="flex flex-wrap gap-3 text-sm font-bold">
                  <Link
                    to="/foreman/record/$machineId"
                    params={{ machineId: r.machine.id }}
                    className="text-primary underline"
                  >
                    {m.record_note()}
                  </Link>
                  <Link
                    to="/foreman/library/$machineId"
                    params={{ machineId: r.machine.id }}
                    className="text-primary underline"
                  >
                    {m.library()}
                  </Link>
                  <Link
                    to="/panel/$machineId"
                    params={{ machineId: r.machine.id }}
                    className="text-primary underline"
                  >
                    {m.hub_panel()}
                  </Link>
                </div>
              </Card>
            ))}
          </div>

          <Card title={m.confusions_title()} id="confusions" tone="warning">
            {b.openNearMisses > 0 && (
              <p className="mb-3">
                <Link
                  to="/foreman/near-misses"
                  className="font-bold text-primary underline"
                >
                  {m.open_near_misses({ count: b.openNearMisses })}
                </Link>
              </p>
            )}
            {b.confusions.length === 0 ? (
              <p className="text-muted-foreground">{m.no_confusions()}</p>
            ) : (
              <ul className="divide-y divide-border">
                {b.confusions.map((c) => (
                  <li
                    key={c.id}
                    className="flex flex-wrap items-center justify-between gap-2 py-2"
                  >
                    <span>
                      <strong>{c.workerName}</strong>
                      {' · '}
                      {c.machine && pick(c.machine.name, lang)}
                      {' · '}
                      <span lang="ja">{c.term?.ja ?? m.unknown_term()}</span>
                      <span className="ml-2 text-sm text-muted-foreground">
                        {fmtDate(c.createdAt, lang, true)}
                      </span>
                    </span>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => b.ack(c.id)}
                    >
                      {m.ack()}
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
      )}
    </ForemanPage>
  )
}
