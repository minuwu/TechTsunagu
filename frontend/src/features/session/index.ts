import { useMemo, useState } from 'react'
import { usePersisted } from '@/lib/persisted'
import { ApiError, SESSION_KEY, USE_MOCK, type Session } from '@/services/api'
import {
  changePin,
  roster,
  signIn,
  signOut,
  verifyPin,
  type AccountRole,
  type RosterEntry,
} from '@/services/wakatta'

/** The signed-in account (real API only; the mock has no accounts). */
export function useSession(): Session | null {
  const [raw] = usePersisted(SESSION_KEY, null)
  return useMemo(() => {
    if (USE_MOCK || !raw) return null
    try {
      // An expired token gets a 401, which clears it (services/api.ts)
      return JSON.parse(raw) as Session
    } catch {
      return null
    }
  }, [raw])
}

/** The Worker using this phone. Mock: picked on first scan. Real API: the signed-in Worker. */
export function useCurrentWorker() {
  const [picked, setWorkerId] = usePersisted('wakatta.worker', null)
  const session = useSession()
  if (!USE_MOCK)
    return {
      workerId: session?.user.role === 'worker' ? session.user.id : null,
      setWorkerId: () => undefined, // the Worker signs in instead (AuthGate)
      clear: () => void signOut(),
    }
  return {
    workerId: picked,
    setWorkerId,
    clear: () => setWorkerId(null),
  }
}

export type PinRole = 'foreman' | 'community'

const accountRole = (role: PinRole): AccountRole =>
  role === 'community' ? 'poster' : role

/** Foreman and Community Poster screens. Mock: a shared demo PIN. Real API: AuthGate signed the person in
 *  before the screen renders, so the gate is open for that role and "lock" signs out. */
export function usePinGate(role: PinRole) {
  const [unlocked, setUnlocked] = usePersisted(`wakatta.pin.${role}`, null)
  const session = useSession()
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
    unlocked: USE_MOCK
      ? unlocked === '1'
      : session?.user.role === accountRole(role) && !session.mustChangePin,
    pin,
    setPin: (v: string) => setPin(v.replace(/\D/g, '').slice(0, 4)),
    error,
    busy,
    submit,
    lock: () => (USE_MOCK ? setUnlocked(null) : void signOut()),
  }
}

export type SignInError =
  'unknownCode' | 'wrongPin' | 'locked' | 'rateLimited' | 'failed'

const errorOf = (e: unknown): SignInError => {
  if (!(e instanceof ApiError)) return 'failed'
  if (e.status === 404) return 'unknownCode'
  if (e.status === 401) return 'wrongPin'
  if (e.status === 423) return 'locked'
  if (e.status === 429) return 'rateLimited'
  return 'failed'
}

const digits = (v: string) => v.replace(/\D/g, '').slice(0, 6)

/** Real API sign-in: shop (or district) code, then your name, then your PIN; a first PIN must be changed. */
export function useSignIn(role: AccountRole, initialCode = '') {
  const [lastCode, setLastCode] = usePersisted(`wakatta.code.${role}`, null)
  const [code, setCode] = useState(initialCode || lastCode || '')
  const [people, setPeople] = useState<RosterEntry[] | null>(null)
  const [userId, setUserId] = useState('')
  const [pin, setPin] = useState('')
  const [newPin, setNewPin] = useState('')
  const [error, setError] = useState<SignInError | null>(null)
  const [busy, setBusy] = useState(false)
  // After a reload on the "choose your PIN" step the current PIN is no longer known: ask for it
  const [signedInHere, setSignedInHere] = useState(false)
  const run = async (fn: () => Promise<void>) => {
    setBusy(true)
    setError(null)
    try {
      await fn()
    } catch (e) {
      setError(errorOf(e))
    } finally {
      setBusy(false)
    }
  }
  return {
    step: people === null ? ('code' as const) : ('person' as const),
    code,
    setCode: (v: string) => setCode(v.toUpperCase().replace(/\s/g, '')),
    people: people ?? [],
    userId,
    setUserId,
    pin,
    setPin: (v: string) => setPin(digits(v)),
    newPin,
    setNewPin: (v: string) => setNewPin(digits(v)),
    error,
    busy,
    canFind: code.length >= 4,
    canSignIn: !!userId && pin.length >= 4,
    canChange: pin.length >= 4 && newPin.length >= 4 && newPin !== pin,
    find: () =>
      run(async () => {
        const list = await roster(code, role)
        setPeople(list)
        setUserId(list.length === 1 ? list[0]!.id : '')
        setLastCode(code)
      }),
    back: () => {
      setPeople(null)
      setPin('')
      setError(null)
    },
    signIn: () =>
      run(async () => {
        await signIn(userId, pin)
        setSignedInHere(true)
      }),
    askCurrentPin: !signedInHere,
    changePin: () =>
      run(async () => {
        await changePin(pin, newPin)
        setPin('')
        setNewPin('')
      }),
  }
}
