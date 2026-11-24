import { useSyncExternalStore } from 'react'
import { usePersisted } from '@/lib/persisted'

export type Theme = 'light' | 'dark'

const DARK = '(prefers-color-scheme: dark)'
const subscribe = (onChange: () => void) => {
  const q = window.matchMedia(DARK)
  q.addEventListener('change', onChange)
  return () => q.removeEventListener('change', onChange)
}

/** Light or dark. The device decides (CSS color-scheme, so nothing flashes) until the header button pins
 *  one on this device; the key is the one the v3 kit uses, so the choice carries across kits.
 *  shortcut: a pinned theme applies after hydration, so it can flash when it differs from the device
 *  setting; a theme cookie read on the server would remove that. */
export function useTheme(): {
  theme: Theme
  pinned: Theme | null
  toggle: () => void
} {
  const [stored, setStored] = usePersisted('wakatta.theme', null)
  const deviceDark = useSyncExternalStore(
    subscribe,
    () => window.matchMedia(DARK).matches,
    () => false,
  )
  const pinned = stored === 'light' || stored === 'dark' ? stored : null
  const theme: Theme = pinned ?? (deviceDark ? 'dark' : 'light')
  return {
    theme,
    pinned,
    toggle: () => setStored(theme === 'dark' ? 'light' : 'dark'),
  }
}
