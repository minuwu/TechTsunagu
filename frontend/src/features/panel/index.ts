import { useParams } from '@tanstack/react-router'
import {
  isCleared,
  latestSignOff,
  skillStatus,
  tokyoDate,
  type SkillStatus,
} from '@/domain/wakatta'
import { useQuery } from '@tanstack/react-query'
import { USE_MOCK } from '@/services/api'
import { useListQuery } from '@/services/queries/wakatta'
import { machinePanel } from '@/services/wakatta'

export type PanelState = 'idle' | 'locked' | 'ready'

// Backend reasons (app/domain/skills.py panel_state) as the Skill Statuses the screens label
const LIVE_REASON: Record<string, SkillStatus> = {
  no_note: 'none',
  no_sign_off: 'none',
  expired: 'expired',
  expiring: 'expiring',
  override: 'valid',
}

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

  // Real API: the backend decides (it also knows Foreman overrides); polls like the lists
  const live = useQuery({
    queryKey: ['wakatta', 'panel', machineId],
    queryFn: () => machinePanel(machineId),
    enabled: !USE_MOCK && !!machineId,
    refetchInterval: 2000,
  })
  if (!USE_MOCK) {
    const p = live.data
    return {
      loading: machines.isPending || live.isPending,
      unknown: machines.isError || live.isError,
      notFound: machines.isSuccess && !machine,
      machine,
      worker: p?.worker
        ? {
            id: p.worker.id,
            name: p.worker.displayName,
            language: 'ja' as const,
          }
        : undefined,
      // An override lets the Machine run, like a valid Sign-off
      state: (p?.state === 'override'
        ? 'ready'
        : (p?.state ?? 'idle')) as PanelState,
      reason: p?.reason ? (LIVE_REASON[p.reason] ?? null) : null,
      assigned,
    }
  }

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
    // No data is not "no Machine" and not "nobody here": the Panel must say it cannot tell
    unknown: [machines, workers, signOffs].some((q) => q.isError),
    notFound: machines.isSuccess && !machine,
    machine,
    worker,
    state,
    reason: status?.status ?? null,
    assigned,
  }
}
