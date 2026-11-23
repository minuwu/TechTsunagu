import { useState } from 'react'
import { Link } from '@tanstack/react-router'
import { pick } from '@/domain/wakatta'
import { useHub } from '@/features/hub'
import { m } from '@/i18n/paraglide/messages'
import { Landing } from './Landing'
import { Card } from '@/ui/v4/components/Card'
import { ConfirmDialog, Skeleton } from '@/ui/v4/components/Feedback'
import { Mark } from '@/ui/v4/components/Icon'
import { Button } from '@/ui/v4/components/ui/button'
import { uiLang } from '@/ui/v4/lib/format'
import { useToastWhen } from '@/ui/v4/lib/toast'

// One row = one thing to open: a clickable blue tile (tokens.css) with an arrow that nudges
const row =
  'tile group flex min-h-12 items-center justify-between gap-3 px-3.5 py-2 font-bold'
const arrow =
  'size-5 text-muted-foreground transition-transform duration-200 group-hover:translate-x-0.5 group-hover:text-primary'

function RowsSkeleton() {
  return (
    <div className="space-y-2" aria-hidden="true">
      <Skeleton className="h-12" />
      <Skeleton className="h-12" />
      <Skeleton className="h-12" />
    </div>
  )
}

/** Demo hub: every role one tap away, so judges can follow the story. */
export function HomeScreen() {
  const hub = useHub()
  const lang = uiLang()
  const [confirming, setConfirming] = useState(false)
  useToastWhen(hub.resetDone, m.hub_reset_done)
  return (
    <>
      <Landing />

      <section
        id="demo"
        aria-labelledby="demo-h"
        className="poster-grid scroll-mt-24"
      >
        <div className="poster-side">
          <h2 id="demo-h" className="poster-label">
            {m.lp_label_demo()}
          </h2>
        </div>
        <div className="poster-main">
          <p className="mb-6 flex flex-wrap items-center gap-3 text-muted-foreground">
            {hub.isMock && (
              <span className="inline-flex items-center gap-2 border border-[var(--rule)] px-3 py-1 text-sm font-bold">
                <span
                  aria-hidden="true"
                  className="size-2 rounded-full bg-[var(--life)]"
                />
                {m.mock_badge()}
              </span>
            )}
          </p>
          <div className="grid gap-5 lg:grid-cols-12">
            <Card
              title={m.hub_worker()}
              id="hub-worker"
              className="lg:col-span-5"
            >
              <p className="mb-4 text-muted-foreground">
                {m.hub_worker_body()}
              </p>
              {hub.workers.length === 0 ? (
                <RowsSkeleton />
              ) : (
                <ul className="space-y-2">
                  {hub.workers.map((w) => (
                    <li key={w.id}>
                      <Link
                        to="/me/$workerId"
                        params={{ workerId: w.id }}
                        className={row}
                      >
                        <span className="min-w-0 truncate">{w.name}</span>
                        <span className="flex items-center gap-2">
                          <span className="bg-muted px-2 text-sm text-muted-foreground">
                            {w.language.toUpperCase()}
                          </span>
                          <Mark id="arrow" className={arrow} />
                        </span>
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </Card>

            <Card
              title={m.hub_machines()}
              id="hub-machines"
              className="lg:col-span-7"
            >
              <p className="mb-4 text-muted-foreground">
                {m.hub_machines_body()}
              </p>
              {hub.machines.length === 0 ? (
                <RowsSkeleton />
              ) : (
                <ul className="space-y-2">
                  {hub.machines.map((mc) => (
                    <li key={mc.id} className="flex gap-2">
                      <Link
                        to="/m/$machineId"
                        params={{ machineId: mc.id }}
                        className={`${row} min-w-0 flex-1`}
                      >
                        <span className="min-w-0">{pick(mc.name, lang)}</span>
                        <span className="flex shrink-0 items-center gap-1.5 text-sm text-muted-foreground group-hover:text-primary">
                          <Mark id="qr" className="size-5" />
                          <span className="hidden sm:inline">
                            {m.hub_scan()}
                          </span>
                        </span>
                      </Link>
                      <Link
                        to="/panel/$machineId"
                        params={{ machineId: mc.id }}
                        className={`${row} shrink-0`}
                      >
                        {m.hub_panel()}
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </Card>

            <Card
              title={m.hub_foreman()}
              id="hub-foreman"
              className="lg:col-span-4"
            >
              <p className="mb-4 text-muted-foreground">
                {m.hub_foreman_body({ pin: hub.pins.foreman })}
              </p>
              <Link to="/foreman" className={row}>
                {m.hub_open_board()}
                <Mark id="arrow" className={arrow} />
              </Link>
            </Card>

            <Card
              title={m.hub_community()}
              id="hub-community"
              tone="life"
              className="lg:col-span-4"
            >
              <p className="mb-4 text-muted-foreground">
                {m.hub_community_body({ pin: hub.pins.community })}
              </p>
              <Link to="/community" className={row}>
                {m.hub_open_community()}
                <Mark id="arrow" className={arrow} />
              </Link>
            </Card>

            <Card
              title={m.hub_senpai()}
              id="hub-senpai"
              tone="life"
              className="lg:col-span-4"
            >
              <p className="mb-4 text-muted-foreground">
                {m.hub_senpai_body()}
              </p>
              <ul className="space-y-2">
                {hub.pairs.map((p) => (
                  <li key={p.id}>
                    <Link
                      to="/senpai/$token"
                      params={{ token: p.token }}
                      className={row}
                    >
                      {p.senpaiName}
                      <Mark id="arrow" className={arrow} />
                    </Link>
                  </li>
                ))}
              </ul>
            </Card>
          </div>

          <div className="card-surface mt-8 flex flex-wrap items-center gap-x-4 gap-y-3 px-5 py-4">
            <div className="min-w-0 flex-1">
              <h2 className="font-bold">{m.hub_reset()}</h2>
              <p className="text-sm text-muted-foreground">
                {m.hub_reset_body()}
              </p>
            </div>
            <Button
              variant="outline"
              loading={hub.resetting}
              onClick={() => setConfirming(true)}
            >
              {!hub.resetting && <Mark id="reset" />}
              {m.hub_reset()}
            </Button>
          </div>
        </div>
      </section>
      <ConfirmDialog
        open={confirming}
        title={m.reset_confirm_title()}
        body={m.reset_confirm_body()}
        confirmLabel={m.hub_reset()}
        onCancel={() => setConfirming(false)}
        onConfirm={() => {
          setConfirming(false)
          hub.reset()
        }}
      />
    </>
  )
}
