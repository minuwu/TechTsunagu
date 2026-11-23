import { useEffect, useSyncExternalStore } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useSearch } from '@tanstack/react-router'
import { useAction } from '@/lib/action'
import { USE_MOCK } from '@/services/api'
import { useList, useListQuery } from '@/services/queries/wakatta'
import {
  DEMO_PINS,
  SCENARIOS,
  controlsShown,
  getScenario,
  isScenario,
  resetDemo,
  setScenario,
  subscribeScenario,
  type Scenario,
} from '@/services/wakatta'

/** Demo hub: every role and screen one tap away. */
export function useHub() {
  const workersQ = useListQuery('workers')
  const machinesQ = useListQuery('machines')
  const workers = workersQ.data ?? []
  const machines = machinesQ.data ?? []
  const pairs = useList('pairs')
  const reset = useAction(resetDemo)
  return {
    isMock: USE_MOCK,
    loading: workersQ.isPending || machinesQ.isPending,
    pins: DEMO_PINS,
    workers,
    machines,
    pairs,
    reset: () => reset.mutate(undefined),
    resetting: reset.isPending,
    resetDone: reset.isSuccess,
  }
}

/** Demo states for presenting (mock store only): `?demo=loading|empty|error|live` picks one for the tab,
 *  and the switcher stays visible there; in development it is always visible. */
export function useDemoScenario() {
  const client = useQueryClient()
  const scenario = useSyncExternalStore(
    subscribeScenario,
    getScenario,
    () => 'live' as Scenario,
  )
  const shown = useSyncExternalStore(
    subscribeScenario,
    controlsShown,
    () => false,
  )
  const choose = (next: Scenario) => {
    setScenario(next)
    void client.resetQueries({ queryKey: ['wakatta'] })
  }
  // ?demo= is read loosely: no route declares it, so it never changes any route's own search params
  const asked = (useSearch({ strict: false }) as Record<string, unknown>).demo
  useEffect(() => {
    if (USE_MOCK && isScenario(asked) && asked !== getScenario()) {
      setScenario(asked)
      void client.resetQueries({ queryKey: ['wakatta'] })
    }
  }, [asked, client])
  return {
    available: USE_MOCK && (import.meta.env.DEV || shown),
    scenario,
    options: SCENARIOS,
    choose,
  }
}
