import { useParams } from '@tanstack/react-router'
import {
  isCleared,
  latestSignOff,
  skillStatus,
  tokyoDate,
} from '@/domain/wakatta'
import { useListQuery } from '@/services/queries/wakatta'

export type PanelState = 'idle' | 'locked' | 'ready'

/** Machine Panel: simulated interlock beside the Machine (ADR 0003). Polls every 2 s. */
export function usePanel() {
  const { machineId = '' } = useParams({ strict: false })
  const machines = useListQuery('machines', 2000)
  const workers = useListQuery('workers', 2000)
  const signOffs = useListQuery('signOffs', 2000)
  const assignments = useListQuery('assignments', 2000)

  const machine = machines.data?.find((m) => m.id === machineId)
  const worker = workers.data?.find((w) => w.id === machine?.activeWorkerId)
  const today = tokyoDate(new Date())
  const assigned = (assignments.data ?? [])
    .filter((a) => a.machineId === machineId && a.date === today)
    .map((a) => workers.data?.find((w) => w.id === a.workerId)?.name)
    .filter(Boolean) as string[]

  const status =
    machine && worker
      ? skillStatus(
          latestSignOff(signOffs.data ?? [], worker.id, machine.id),
          machine,
          new Date(),
        )
      : null
  const state: PanelState = !status
    ? 'idle'
    : isCleared(status.status)
      ? 'ready'
      : 'locked'

  return {
    loading: machines.isPending,
    notFound: machines.isSuccess && !machine,
    machine,
    worker,
    state,
    reason: status?.status ?? null,
    assigned,
  }
}
