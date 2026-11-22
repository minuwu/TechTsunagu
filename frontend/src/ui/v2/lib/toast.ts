import { useEffect, useRef, useSyncExternalStore } from 'react'

// Presentation-only feedback store for this kit (F-30). Features return flags; the kit turns a flag
// turning true into a toast. No business state lives here.

export type ToastTone = 'success' | 'info' | 'error'
export interface Toast {
  id: number
  message: string
  tone: ToastTone
}

let toasts: Toast[] = []
let next = 1
const listeners = new Set<() => void>()
const emit = () => listeners.forEach((l) => l())

export function dismissToast(id: number) {
  toasts = toasts.filter((t) => t.id !== id)
  emit()
}

export function toast(message: string, tone: ToastTone = 'success') {
  const id = next++
  toasts = [...toasts.slice(-2), { id, message, tone }]
  emit()
  setTimeout(() => dismissToast(id), 4000)
}

export function useToasts() {
  return useSyncExternalStore(
    (l) => {
      listeners.add(l)
      return () => listeners.delete(l)
    },
    () => toasts,
    () => toasts,
  )
}

/** Shows `message` once each time `when` turns from false to true. */
export function useToastWhen(
  when: boolean | undefined,
  message: () => string,
  tone: ToastTone = 'success',
) {
  const was = useRef(Boolean(when))
  useEffect(() => {
    if (when && !was.current) toast(message(), tone)
    was.current = Boolean(when)
  }, [when, message, tone])
}
