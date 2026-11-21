import { useHealth } from '@/features/health'
import { m } from '@/i18n/paraglide/messages'
import { Button } from '@/ui/v1/components/ui/button'

export function HomeScreen() {
  const health = useHealth()
  return (
    <section aria-labelledby="health-title" className="max-w-[40em] space-y-4">
      <h1 id="health-title" className="text-2xl">
        {m.health_title()}
      </h1>
      {health.isMock && (
        <p className="inline-block rounded-md bg-muted px-2 text-sm text-muted-foreground">
          {m.mock_badge()}
        </p>
      )}
      <div aria-live="polite">
        {health.state === 'loading' && <p>{m.health_loading()}</p>}
        {health.state === 'up' && (
          <p className="text-success">
            <span aria-hidden="true">{'✓ '}</span>
            {m.health_up()}
          </p>
        )}
        {health.state === 'down' && (
          <p className="text-destructive">
            <span aria-hidden="true">{'⚠ '}</span>
            {m.health_down({ status: health.status })}
          </p>
        )}
        {health.state === 'error' && (
          <div role="alert" className="space-y-3">
            <p className="text-destructive">
              <span aria-hidden="true">{'⚠ '}</span>
              {m.health_error()}
            </p>
            <Button variant="outline" onClick={health.retry}>
              {m.retry()}
            </Button>
          </div>
        )}
      </div>
    </section>
  )
}
