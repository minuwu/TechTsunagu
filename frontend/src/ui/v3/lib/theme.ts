import { usePersisted } from '@/lib/persisted'

export type Theme = 'light' | 'dark'

/** The kit's theme: Porcelain ('light') by default, Harbour night ('dark') on request; kept per device. */
export function useTheme(): [Theme, (next: Theme) => void] {
  const [value, set] = usePersisted('wakatta.theme', 'light')
  return [value === 'dark' ? 'dark' : 'light', set]
}
