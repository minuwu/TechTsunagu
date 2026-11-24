import '@fontsource/aileron/400.css'
import '@fontsource/aileron/700.css'
import '@fontsource/aileron/800.css'
import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Link, useLocation } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'
import { ping } from '@/lib/audio'
import { useDataStatus, useWriteFailure } from '@/features/health'
import { useDemoScenario } from '@/features/hub'
import { usePersisted } from '@/lib/persisted'
import { startSmoothScroll } from '@/ui/v4/lib/motion'
import { toast } from '@/ui/v4/lib/toast'
import { cn } from '@/ui/v4/lib/utils'
import { BridgeMark } from './components/Brand'
import { Toaster } from './components/Feedback'
import { Mark } from './components/Icon'
import { LocaleSwitcher } from './components/LocaleSwitcher'

// The splash belongs to the first document load only, never to in-app navigation
let splashShown = false

/** Brand splash: a cobalt field and the mark, then it clears (CSS only, F-38 safe). */
function Splash() {
  const [show] = useState(() => !splashShown)
  useEffect(() => {
    splashShown = true
  }, [])
  if (!show) return null
  return (
    <div aria-hidden="true" className="splash">
      <div className="flex flex-col items-center gap-4">
        <BridgeMark draw className="w-24" />
        <span className="poster-h2 !text-5xl uppercase">{m.app_name()}</span>
      </div>
    </div>
  )
}

// 24px square-cap line icons
const ICONS = {
  home: 'M4 11.5 12 5l8 6.5V20h-5v-5h-6v5H4z',
  course: 'M5 4h9a3 3 0 0 1 3 3v13H8a3 3 0 0 1-3-3zM9 8h5M9 12h5',
  board: 'M4 5h16v14H4zM10 5v14M15 5v14',
  town: 'M3 20h18M5 20V9l5-3 5 3v11M15 20v-7h4v7M8 12h4',
} as const

const TABS = [
  { to: '/', icon: 'home', label: m.tab_home, match: (p: string) => p === '/' },
  {
    to: '/course',
    icon: 'course',
    label: m.tab_course,
    match: (p: string) => p.startsWith('/course'),
  },
  {
    to: '/foreman',
    icon: 'board',
    label: m.tab_board,
    match: (p: string) => p.startsWith('/foreman'),
  },
  {
    to: '/community',
    icon: 'town',
    label: m.tab_community,
    match: (p: string) => p.startsWith('/community'),
  },
] as const

/** Phones: a flat bar on the bottom edge, the current tab filled cobalt. */
function TabBar({ path, sound }: { path: string; sound: boolean }) {
  const active = TABS.findIndex((t) => t.match(path))
  return (
    <nav aria-label={m.tabs_label()} className="tabbar">
      <ul className="grid grid-cols-4">
        {TABS.map((t, i) => (
          <li
            key={t.to}
            className="border-r border-[var(--rule)] last:border-r-0"
          >
            <Link
              to={t.to}
              aria-current={i === active ? 'page' : undefined}
              onClick={() => sound && ping(i >= active)}
              className={cn(
                'flex min-h-16 flex-col items-center justify-center gap-0.5 text-xs font-bold tracking-wider uppercase',
                i === active
                  ? 'bg-primary text-primary-foreground'
                  : 'text-foreground hover:bg-foreground hover:text-background',
              )}
            >
              <svg
                viewBox="0 0 24 24"
                aria-hidden="true"
                className="size-6"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="square"
                strokeLinejoin="miter"
              >
                <path d={ICONS[t.icon]} />
              </svg>
              {t.label()}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  )
}

/** Desktop: the same four destinations as text links in the header. */
function HeaderLinks({ path, sound }: { path: string; sound: boolean }) {
  const active = TABS.findIndex((t) => t.match(path))
  return (
    <nav aria-label={m.tabs_label()} className="hidden md:block">
      <ul className="flex gap-6">
        {TABS.map((t, i) => (
          <li key={t.to}>
            <Link
              to={t.to}
              aria-current={i === active ? 'page' : undefined}
              onClick={() => sound && ping(i >= active)}
              className={cn(
                'flex min-h-11 items-center border-b-2 text-sm font-semibold transition-colors duration-300',
                i === active
                  ? 'border-primary text-primary'
                  : 'border-transparent text-foreground hover:text-primary',
              )}
            >
              {t.label()}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  )
}

const SCENARIO_LABEL = {
  live: m.demo_live,
  loading: m.demo_loading,
  empty: m.demo_empty,
  error: m.demo_error,
} as const

/** Demo-state switcher for presenting (mock data only; see useDemoScenario). */
function DemoSwitcher() {
  const demo = useDemoScenario()
  if (!demo.available) return null
  return (
    <aside
      aria-label={m.demo_state()}
      // Phones: above the "Ask Wakatta" row (5rem + safe area), which would otherwise cover Loading / Empty / Error
      className="fixed bottom-[calc(9rem+env(safe-area-inset-bottom))] left-3 z-40 flex items-center border border-foreground bg-background text-sm md:bottom-4"
    >
      <span className="px-3 text-xs font-bold tracking-wider uppercase">
        {m.demo_state()}
      </span>
      {demo.options.map((s) => (
        <button
          key={s}
          type="button"
          aria-pressed={demo.scenario === s}
          onClick={() => demo.choose(s)}
          className={cn(
            'min-h-11 cursor-pointer border-l border-foreground px-3 font-bold transition-colors duration-300',
            demo.scenario === s
              ? 'bg-primary text-primary-foreground'
              : 'hover:bg-foreground hover:text-background',
          )}
        >
          {SCENARIO_LABEL[s]()}
        </button>
      ))}
    </aside>
  )
}

const WRITE_MESSAGE = {
  busy: m.write_busy,
  limit: m.write_limit,
  offline: m.write_offline,
  failed: m.write_failed,
} as const

/** A failed write (save, send, ask) becomes an error toast, so nothing fails silently. */
function WriteFailureToast() {
  const failure = useWriteFailure()
  // Highest id already shown: a remount or the cache dropping old failures never repeats a toast
  const shown = useRef(failure?.id ?? 0)
  useEffect(() => {
    if (!failure || failure.id <= shown.current) return
    shown.current = failure.id
    toast(WRITE_MESSAGE[failure.kind](), 'error')
  }, [failure])
  return null
}

/** Offline or a failed read: a caution band under the header, never silent stale data. */
function ConnectionBanner() {
  const { status, retry } = useDataStatus()
  if (status === 'ok') return null
  return (
    <div
      role="alert"
      className="border-b border-warning bg-[color-mix(in_oklch,var(--warning)_10%,var(--background))]"
    >
      <div className="mx-auto flex max-w-[90rem] flex-wrap items-center gap-x-4 gap-y-2 px-4 py-2.5 sm:px-6">
        <Mark id="alert" className="size-5 shrink-0 text-warning" />
        <p className="flex-1 font-bold">
          {status === 'offline' ? m.data_offline() : m.data_error()}
        </p>
        {status === 'error' && (
          <button
            type="button"
            onClick={retry}
            className="min-h-11 cursor-pointer border border-foreground px-4 font-bold transition-colors duration-300 hover:bg-foreground hover:text-background"
          >
            {m.retry()}
          </button>
        )}
      </div>
    </div>
  )
}

const toggle =
  'grid size-11 shrink-0 cursor-pointer place-items-center border border-foreground text-foreground transition-colors duration-300 hover:bg-foreground hover:text-background'

export function Layout({ children }: { children: ReactNode }) {
  const path = useLocation({ select: (l) => l.pathname })
  const [soundPref, setSound] = usePersisted('wakatta.sound', 'on')
  const sound = soundPref === 'on'
  // Lenis smooth wheel scroll for the whole kit (touch stays native; no smoothing under reduced motion)
  useEffect(() => startSmoothScroll(), [])
  return (
    <div className="v4 v4-ground flex min-h-dvh flex-col">
      <Splash />
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:bg-foreground focus:p-3 focus:text-background"
      >
        {m.skip_to_main()}
      </a>
      <header className="sticky top-0 z-30 h-20 border-b border-[var(--rule)] bg-background/95 backdrop-blur-md">
        <div className="mx-auto flex h-full max-w-[90rem] items-center justify-between gap-4 px-4 sm:px-6">
          <Link to="/" className="flex min-w-0 items-center gap-3 py-1">
            <span
              aria-hidden="true"
              className="size-4 shrink-0 bg-foreground"
            />
            <span className="display truncate text-lg font-extrabold tracking-tight uppercase">
              {m.app_name()}
            </span>
          </Link>
          <div className="flex items-center gap-4 sm:gap-6">
            <HeaderLinks path={path} sound={sound} />
            <button
              type="button"
              className={toggle}
              aria-pressed={sound}
              onClick={() => setSound(sound ? 'off' : 'on')}
            >
              <svg
                viewBox="0 0 24 24"
                aria-hidden="true"
                className="size-5"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="square"
              >
                <path d="M4 9.5v5h3.5L12 18.5v-13L7.5 9.5zM16 9c1.2 1.6 1.2 4.4 0 6" />
                {!sound && <path d="M18.5 7l3 10" />}
              </svg>
              <span className="sr-only">
                {sound ? m.sound_on() : m.sound_off()}
              </span>
            </button>
            <LocaleSwitcher />
          </div>
        </div>
        <ConnectionBanner />
      </header>
      <main
        id="main"
        className="mx-auto w-full max-w-[90rem] flex-1 px-4 pt-8 pb-28 sm:px-6 md:pt-12 md:pb-16"
      >
        <div key={path} className="page-enter">
          {children}
        </div>
      </main>
      <TabBar path={path} sound={sound} />
      <DemoSwitcher />
      <WriteFailureToast />
      <Toaster />
    </div>
  )
}
