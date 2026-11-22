import { useCallback, useSyncExternalStore } from 'react'

// A localStorage value shared by every component and tab. Server render sees `initial`.
const EVENT = 'persisted-change'

function subscribe(onChange: () => void) {
  window.addEventListener('storage', onChange)
  window.addEventListener(EVENT, onChange)
  return () => {
    window.removeEventListener('storage', onChange)
    window.removeEventListener(EVENT, onChange)
  }
}

export function usePersisted(key: string, initial: string | null) {
  const value = useSyncExternalStore(
    subscribe,
    () => localStorage.getItem(key) ?? initial,
    () => initial,
  )
  const set = useCallback(
    (next: string | null) => {
      if (next === null) localStorage.removeItem(key)
      else localStorage.setItem(key, next)
      window.dispatchEvent(new Event(EVENT))
    },
    [key],
  )
  return [value, set] as const
}
