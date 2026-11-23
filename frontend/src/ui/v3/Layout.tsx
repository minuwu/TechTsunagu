import { useEffect, useState, type ReactNode } from 'react'
import { Link, useLocation } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'
import { ping } from '@/lib/audio'
import { useDataStatus } from '@/features/health'
import { useDemoScenario } from '@/features/hub'
import { usePersisted } from '@/lib/persisted'
import { startSmoothScroll } from './lib/motion'
import { useTheme } from './lib/theme'
import { BridgeMark } from './components/Brand'
import { Toaster } from './components/Feedback'
import { Mark } from './components/Icon'
import { LocaleSwitcher } from './components/LocaleSwitcher'

// The splash belongs to the first document load only, never to in-app navigation
let splashShown = false

/** Brand splash: the bridge draws itself on night blue, then fades (CSS only, F-38 safe). */
function Splash() {
  const [show] = useState(() => !splashShown)
  useEffect(() => {
    splashShown = true
  }, [])
  if (!show) return null
  return (
    <div aria-hidden="true" className="splash">
      <div className="flex flex-col items-center gap-4">
        <BridgeMark draw className="w-24 text-[var(--shell-foreground)]" />
        <span className="display text-2xl">{m.app_name()}</span>
      </div>
    </div>
  )
}

// 24px line icons, same stroke as the kit's marks
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

/** Frosted floating tab bar in thumb reach; the pill springs to the current tab (spec §2.5). */
function TabBar({ path, sound }: { path: string; sound: boolean }) {
  const active = TABS.findIndex((t) => t.match(path))
  return (
    <nav aria-label={m.tabs_label()} className="tabbar glass lg:hidden">
      {active >= 0 && (
        <span
          aria-hidden="true"
          className="tabbar-pill"
          style={{
            width: `calc((100% - 0.75rem) / ${TABS.length})`,
            transform: `translateX(${active * 100}%)`,
          }}
        />
      )}
      <ul className="relative grid grid-cols-4">
        {TABS.map((t, i) => (
          <li key={t.to}>
            <Link
              to={t.to}
              aria-current={i === active ? 'page' : undefined}
              onClick={() => sound && ping(i >= active)}
              className={`flex min-h-14 flex-col items-center justify-center gap-0.5 rounded-xl text-sm font-bold ${i === active ? 'text-primary-foreground' : 'text-muted-foreground hover:text-foreground'}`}
            >
              <svg
                viewBox="0 0 24 24"
                aria-hidden="true"
                className="size-6"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
                strokeLinejoin="round"
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

/** Desktop (lg and up): the same four tabs sit in the header instead of floating at the bottom. */
function HeaderTabs({ path, sound }: { path: string; sound: boolean }) {
  const active = TABS.findIndex((t) => t.match(path))
  return (
    <nav aria-label={m.tabs_label()} className="hidden lg:block">
      <ul className="flex items-center gap-1">
        {TABS.map((t, i) => (
          <li key={t.to}>
            <Link
              to={t.to}
              aria-current={i === active ? 'page' : undefined}
              onClick={() => sound && ping(i >= active)}
              className={`flex min-h-11 items-center gap-2 rounded-lg px-3.5 font-bold transition-colors duration-150 ${i === active ? 'bg-[var(--shell-raised)] text-[var(--shell-foreground)] shadow-[inset_0_-2px_0_var(--primary)]' : 'text-[var(--shell-muted)] hover:bg-[var(--shell-raised)]/60 hover:text-[var(--shell-foreground)]'}`}
            >
              <svg
                viewBox="0 0 24 24"
                aria-hidden="true"
                className="size-5"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
                strokeLinejoin="round"
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
    <div
      role="group"
      aria-label={m.demo_state()}
      className="glass fixed bottom-28 left-3 z-40 flex items-center gap-1 rounded-xl p-1 text-sm lg:bottom-4"
    >
      <span className="px-2 font-bold text-muted-foreground">
        {m.demo_state()}
      </span>
      {demo.options.map((s) => (
        <button
          key={s}
          type="button"
          aria-pressed={demo.scenario === s}
          onClick={() => demo.choose(s)}
          className={`min-h-9 cursor-pointer rounded-lg px-2.5 font-bold transition-colors ${demo.scenario === s ? 'bg-primary text-primary-foreground' : 'hover:bg-accent'}`}
        >
          {SCENARIO_LABEL[s]()}
        </button>
      ))}
    </div>
  )
}

/** Offline or a failed read: a caution band under the header, never silent stale data. */
function ConnectionBanner() {
  const { status, retry } = useDataStatus()
  if (status === 'ok') return null
  return (
    <div
      role="alert"
      className="border-b border-warning/40 bg-[color-mix(in_oklch,var(--warning)_12%,var(--background))]"
    >
      <div
        className={`${frame} flex flex-wrap items-center gap-x-4 gap-y-2 py-2.5`}
      >
        <Mark id="alert" className="size-5 shrink-0 text-warning" />
        <p className="flex-1 font-bold">
          {status === 'offline' ? m.data_offline() : m.data_error()}
        </p>
        {status === 'error' && (
          <button
            type="button"
            onClick={retry}
            className="min-h-11 cursor-pointer rounded-md border border-border bg-card px-4 font-bold hover:border-primary"
          >
            {m.retry()}
          </button>
        )}
      </div>
    </div>
  )
}

// One width for header and content: the Foreman console uses the extra room on wide screens
const frame = 'mx-auto w-full max-w-6xl px-4 lg:px-8 xl:max-w-7xl'

const toggle =
  'grid size-11 shrink-0 cursor-pointer place-items-center rounded-lg bg-[var(--shell-raised)] text-[var(--shell-foreground)] transition-transform duration-150 hover:scale-105 active:scale-95'

export function Layout({ children }: { children: ReactNode }) {
  const path = useLocation({ select: (l) => l.pathname })
  const [theme, setTheme] = useTheme()
  const [soundPref, setSound] = usePersisted('wakatta.sound', 'on')
  const sound = soundPref === 'on'
  const light = theme === 'light'
  // Lenis smooth wheel scroll for the whole kit (touch stays native; no smoothing under reduced motion)
  useEffect(() => startSmoothScroll(), [])
  return (
    <div
      data-theme={light ? 'light' : 'dark'}
      className="v3 v3-ground flex min-h-dvh flex-col"
    >
      <Splash />
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded-md focus:bg-card focus:p-3 focus:shadow-[var(--shadow-md)]"
      >
        {m.skip_to_main()}
      </a>
      <header className="sticky top-0 z-30 bg-[var(--shell)]/95 text-[var(--shell-foreground)] backdrop-blur-md">
        <div
          className={`${frame} flex items-center justify-between gap-3 py-2`}
        >
          <Link
            to="/"
            className="flex min-w-0 items-center gap-3 rounded-md py-1 focus-visible:outline-[var(--shell-foreground)]"
          >
            <BridgeMark className="w-10 text-[var(--shell-accent)]" />
            <span className="display sr-only text-xl sm:not-sr-only sm:truncate">
              {m.app_name()}
            </span>
          </Link>
          <HeaderTabs path={path} sound={sound} />
          <div className="flex items-center gap-2">
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
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M4 9.5v5h3.5L12 18.5v-13L7.5 9.5zM16 9c1.2 1.6 1.2 4.4 0 6" />
                {!sound && <path d="M18.5 7l3 10" />}
              </svg>
              <span className="sr-only">
                {sound ? m.sound_on() : m.sound_off()}
              </span>
            </button>
            <button
              type="button"
              className={toggle}
              onClick={() => setTheme(light ? 'dark' : 'light')}
            >
              <svg
                viewBox="0 0 24 24"
                aria-hidden="true"
                className="size-5"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                {light ? (
                  <path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z" />
                ) : (
                  <path d="M12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6 7 7M17 17l1.4 1.4M5.6 18.4 7 17M17 7l1.4-1.4M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8z" />
                )}
              </svg>
              <span className="sr-only">
                {light ? m.theme_to_dark() : m.theme_to_light()}
              </span>
            </button>
            <LocaleSwitcher />
          </div>
        </div>
        <div className="harbour-line" />
      </header>
      <ConnectionBanner />
      <main id="main" className={`${frame} flex-1 pt-6 pb-16 sm:pt-10`}>
        {/* Re-keyed per route so each screen settles in (page transition) */}
        <div key={path} className="page-enter">
          {children}
        </div>
      </main>
      <footer className="border-t border-[var(--rule)] pb-28 lg:pb-0">
        <div
          className={`${frame} flex flex-wrap items-center justify-between gap-x-6 gap-y-2 py-6 text-sm text-muted-foreground`}
        >
          <span className="flex items-center gap-2.5">
            <BridgeMark className="w-7 text-[var(--indigo)]" />
            {m.footer_made_by()}
          </span>
          <span>
            {m.app_name()} · {m.app_tagline()}
          </span>
        </div>
      </footer>
      <TabBar path={path} sound={sound} />
      <DemoSwitcher />
      <Toaster />
    </div>
  )
}
