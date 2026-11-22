import { useAction } from '@/lib/action'
import { USE_MOCK } from '@/services/api'
import { useList } from '@/services/queries/wakatta'
import { DEMO_PINS, resetDemo } from '@/services/wakatta'

/** Demo hub: every role and screen one tap away. */
export function useHub() {
  const workers = useList('workers')
  const machines = useList('machines')
  const pairs = useList('pairs')
  const reset = useAction(resetDemo)
  return {
    isMock: USE_MOCK,
    pins: DEMO_PINS,
    workers,
    machines,
    pairs,
    reset: () => reset.mutate(undefined),
    resetting: reset.isPending,
    resetDone: reset.isSuccess,
  }
}
