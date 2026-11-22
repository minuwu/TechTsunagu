import { Link } from '@tanstack/react-router'
import { pick } from '@/domain/wakatta'
import { useHub } from '@/features/hub'
import { m } from '@/i18n/paraglide/messages'
import { Card, PageTitle } from '@/ui/v1/components/Card'
import { Button } from '@/ui/v1/components/ui/button'
import { uiLang } from '@/ui/v1/lib/format'

const tile =
  'flex min-h-11 items-center justify-between gap-2 rounded-md border border-border px-3 py-2 font-bold hover:bg-accent'

/** Demo hub: every role one tap away, so judges can follow the story. */
export function HomeScreen() {
  const hub = useHub()
  const lang = uiLang()
  return (
    <>
      <PageTitle sub={m.hub_sub()}>{m.hub_title()}</PageTitle>
      {hub.isMock && (
        <p className="mb-4 inline-block rounded-md bg-muted px-2 text-sm text-muted-foreground">
          {m.mock_badge()}
        </p>
      )}
      <div className="grid gap-4 md:grid-cols-2">
        <Card title={m.hub_worker()} id="hub-worker">
          <p className="mb-3 text-muted-foreground">{m.hub_worker_body()}</p>
          <ul className="space-y-2">
            {hub.workers.map((w) => (
              <li key={w.id}>
                <Link
                  to="/me/$workerId"
                  params={{ workerId: w.id }}
                  className={tile}
                >
                  {w.name}
                  <span className="text-sm text-muted-foreground">
                    {w.language.toUpperCase()}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </Card>

        <Card title={m.hub_machines()} id="hub-machines">
          <p className="mb-3 text-muted-foreground">{m.hub_machines_body()}</p>
          <ul className="space-y-2">
            {hub.machines.map((mc) => (
              <li key={mc.id} className="flex gap-2">
                <Link
                  to="/m/$machineId"
                  params={{ machineId: mc.id }}
                  className={`${tile} flex-1`}
                >
                  {pick(mc.name, lang)}
                  <span className="text-sm text-muted-foreground">
                    {m.hub_scan()}
                  </span>
                </Link>
                <Link
                  to="/panel/$machineId"
                  params={{ machineId: mc.id }}
                  className={tile}
                >
                  {m.hub_panel()}
                </Link>
              </li>
            ))}
          </ul>
        </Card>

        <Card title={m.hub_foreman()} id="hub-foreman">
          <p className="mb-3 text-muted-foreground">
            {m.hub_foreman_body({ pin: hub.pins.foreman })}
          </p>
          <Link to="/foreman" className={tile}>
            {m.hub_open_board()}
          </Link>
        </Card>

        <Card title={m.hub_community()} id="hub-community" tone="life">
          <p className="mb-3 text-muted-foreground">
            {m.hub_community_body({ pin: hub.pins.community })}
          </p>
          <Link to="/community" className={tile}>
            {m.hub_open_community()}
          </Link>
        </Card>

        <Card title={m.course_nav()} id="hub-course">
          <p className="mb-3 text-muted-foreground">{m.course_sub()}</p>
          <Link to="/course" className={tile}>
            {m.course_open()}
          </Link>
        </Card>

        <Card title={m.hub_senpai()} id="hub-senpai" tone="life">
          <p className="mb-3 text-muted-foreground">{m.hub_senpai_body()}</p>
          <ul className="space-y-2">
            {hub.pairs.map((p) => (
              <li key={p.id}>
                <Link
                  to="/senpai/$token"
                  params={{ token: p.token }}
                  className={tile}
                >
                  {p.senpaiName}
                </Link>
              </li>
            ))}
          </ul>
        </Card>

        <Card title={m.hub_reset()} id="hub-reset">
          <p className="mb-3 text-muted-foreground">{m.hub_reset_body()}</p>
          <Button
            variant="outline"
            disabled={hub.resetting}
            onClick={hub.reset}
          >
            {m.hub_reset()}
          </Button>
          {hub.resetDone && (
            <p className="mt-2 text-success" aria-live="polite">
              {m.hub_reset_done()}
            </p>
          )}
        </Card>
      </div>
    </>
  )
}
