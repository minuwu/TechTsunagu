import type { ReactNode } from 'react'
import { Navigate, useParams, useSearch } from '@tanstack/react-router'
import type { ScreenId } from '@/contract/ui-kit'
import { useSession, useSignIn, type SignInError } from '@/features/session'
import { m } from '@/i18n/paraglide/messages'
import { USE_MOCK } from '@/services/api'
import type { AccountRole } from '@/services/wakatta'

// Who may open each screen with the real API. The others (home, Senpai link, course) need no account.
const NEEDS: Partial<Record<ScreenId, AccountRole>> = {
  machine: 'worker',
  workerWork: 'worker',
  workerLife: 'worker',
  panel: 'foreman', // shortcut: a Foreman's session; the device Panel token (POST /machines/{id}/panel-token) is not used yet
  foremanBoard: 'foreman',
  foremanRecord: 'foreman',
  foremanAssign: 'foreman',
  foremanSkills: 'foreman',
  foremanHeatmap: 'foreman',
  foremanGlossary: 'foreman',
  foremanBriefing: 'foreman',
  foremanNearMisses: 'foreman',
  foremanLibrary: 'foreman',
  foremanQr: 'foreman',
  foremanSenpai: 'foreman',
  community: 'poster',
}

const field =
  'w-full rounded-md border border-input bg-background px-3 py-2 text-base'
const button =
  'w-full rounded-md bg-primary px-4 py-2 font-bold text-primary-foreground disabled:opacity-50'

const errorText: Record<SignInError, () => string> = {
  unknownCode: m.signin_unknown_code,
  wrongPin: m.pin_wrong,
  locked: m.signin_locked,
  rateLimited: m.signin_rate_limited,
  failed: m.signin_failed,
}

/** Real API: signs the right person in before a screen that needs an account. Mock: renders the screen. */
export function AuthGate({
  screen,
  children,
}: {
  screen: ScreenId
  children: ReactNode
}) {
  const session = useSession()
  const params = useParams({ strict: false }) as { workerId?: string }
  const role = NEEDS[screen]
  if (USE_MOCK || !role) return <>{children}</>
  if (session?.user.role === role && session.mustChangePin)
    return <SignIn role={role} choosePin />
  if (session?.user.role !== role) return <SignIn role={role} />
  // A Worker's pages are their own: another Worker's link opens yours
  if (params.workerId !== undefined && params.workerId !== session.user.id)
    return (
      <Navigate
        to={screen === 'workerLife' ? '/me/$workerId/life' : '/me/$workerId'}
        params={{ workerId: session.user.id }}
        replace
      />
    )
  return <>{children}</>
}

function SignIn({
  role,
  choosePin = false,
}: {
  role: AccountRole
  choosePin?: boolean
}) {
  // A Machine's QR carries the shop code (?s=), so a new phone skips typing it
  const search = useSearch({ strict: false }) as { s?: unknown }
  const s = useSignIn(role, typeof search.s === 'string' ? search.s : '')
  const title =
    role === 'foreman'
      ? m.signin_title_foreman()
      : role === 'worker'
        ? m.signin_title_worker()
        : m.signin_title_poster()
  const error = s.error && (
    <p role="alert" className="text-destructive">
      {errorText[s.error]()}
    </p>
  )

  if (choosePin)
    return (
      <Frame title={m.signin_choose_title()}>
        <p className="text-muted-foreground">{m.signin_choose_hint()}</p>
        <form
          className="space-y-3"
          onSubmit={(e) => {
            e.preventDefault()
            void s.changePin()
          }}
        >
          {s.askCurrentPin && (
            <Pin label={m.signin_current_pin()} value={s.pin} set={s.setPin} />
          )}
          <Pin label={m.signin_new_pin()} value={s.newPin} set={s.setNewPin} />
          {error}
          <button
            type="submit"
            className={button}
            disabled={!s.canChange || s.busy}
          >
            {m.signin_save_pin()}
          </button>
        </form>
      </Frame>
    )

  if (s.step === 'code')
    return (
      <Frame title={title}>
        <form
          className="space-y-3"
          onSubmit={(e) => {
            e.preventDefault()
            void s.find()
          }}
        >
          <label className="block space-y-1">
            <span className="font-bold">
              {role === 'poster'
                ? m.signin_code_district()
                : m.signin_code_shop()}
            </span>
            <input
              className={`${field} tracking-widest uppercase`}
              autoComplete="off"
              autoCapitalize="characters"
              value={s.code}
              onChange={(e) => s.setCode(e.target.value)}
            />
          </label>
          {error}
          <button
            type="submit"
            className={button}
            disabled={!s.canFind || s.busy}
          >
            {m.signin_next()}
          </button>
        </form>
      </Frame>
    )

  return (
    <Frame title={title}>
      <form
        className="space-y-3"
        onSubmit={(e) => {
          e.preventDefault()
          void s.signIn()
        }}
      >
        <fieldset className="space-y-2">
          <legend className="mb-1 font-bold">{m.signin_who()}</legend>
          {s.people.map((p) => (
            <label
              key={p.id}
              className="flex items-center gap-3 rounded-md border border-input px-3 py-2"
            >
              <input
                type="radio"
                name="person"
                checked={s.userId === p.id}
                onChange={() => s.setUserId(p.id)}
              />
              {p.displayName}
            </label>
          ))}
        </fieldset>
        <Pin label={m.signin_pin()} value={s.pin} set={s.setPin} />
        {error}
        <button
          type="submit"
          className={button}
          disabled={!s.canSignIn || s.busy}
        >
          {m.signin_open()}
        </button>
        <button
          type="button"
          className="w-full text-sm underline"
          onClick={s.back}
        >
          {m.signin_back()}
        </button>
      </form>
    </Frame>
  )
}

function Frame({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="mx-auto mt-6 max-w-sm space-y-4 rounded-lg border border-border bg-card p-6 text-card-foreground">
      <h1 className="text-2xl font-bold">{title}</h1>
      {children}
    </section>
  )
}

function Pin({
  label,
  value,
  set,
}: {
  label: string
  value: string
  set: (v: string) => void
}) {
  return (
    <label className="block space-y-1">
      <span className="font-bold">{label}</span>
      <input
        className={`${field} text-center text-2xl tracking-[0.5em]`}
        type="password"
        inputMode="numeric"
        autoComplete="off"
        value={value}
        onChange={(e) => set(e.target.value)}
      />
    </label>
  )
}
