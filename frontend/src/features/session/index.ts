import { useState } from 'react'
import { usePersisted } from '@/lib/persisted'
import { verifyPin } from '@/services/wakatta'

/** The Worker using this phone (picked on first scan; no login, plan: Global constraints). */
export function useCurrentWorker() {
  const [workerId, setWorkerId] = usePersisted('wakatta.worker', null)
  return { workerId, setWorkerId, clear: () => setWorkerId(null) }
}

export type PinRole = 'foreman' | 'community'

/** Foreman and Community Poster screens sit behind a shop PIN. */
export function usePinGate(role: PinRole) {
  const [unlocked, setUnlocked] = usePersisted(`wakatta.pin.${role}`, null)
  const [pin, setPin] = useState('')
  const [error, setError] = useState(false)
  const [busy, setBusy] = useState(false)
  const submit = async () => {
    setBusy(true)
    const ok = await verifyPin(role, pin)
    setBusy(false)
    setError(!ok)
    if (ok) setUnlocked('1')
    setPin('')
  }
  return {
    unlocked: unlocked === '1',
    pin,
    setPin: (v: string) => setPin(v.replace(/\D/g, '').slice(0, 4)),
    error,
    busy,
    submit,
    lock: () => setUnlocked(null),
  }
}
