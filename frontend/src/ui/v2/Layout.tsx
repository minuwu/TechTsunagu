import { useEffect, useState, type ReactNode } from 'react'
import { Link, useLocation } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'
import { BridgeMark } from './components/Brand'
import { Toaster } from './components/Feedback'
import { LocaleSwitcher } from './components/LocaleSwitcher'

// The splash belongs to the first document load only, never to in-app navigation
let splashShown = false

/** Brand splash: the bridge draws itself on harbour navy, then fades (CSS only, F-38 safe). */
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

export function Layout({ children }: { children: ReactNode }) {
  const path = useLocation({ select: (l) => l.pathname })
  return (
    <div className="flex min-h-dvh flex-col">
      <Splash />
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded-md focus:bg-card focus:p-3 focus:shadow-[var(--shadow-md)]"
      >
        {m.skip_to_main()}
      </a>
      <header className="sticky top-0 z-30 bg-[var(--shell)] text-[var(--shell-foreground)]">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-2.5">
          <Link
            to="/"
            className="flex min-w-0 items-center gap-3 rounded-md py-1 focus-visible:outline-[var(--shell-foreground)]"
          >
            <BridgeMark className="w-11" />
            <span className="flex min-w-0 flex-col leading-tight">
              <span className="display text-xl">{m.app_name()}</span>
              <span className="hidden truncate text-sm text-[var(--shell-muted)] sm:block">
                {m.app_tagline()}
              </span>
            </span>
          </Link>
          <LocaleSwitcher />
        </div>
        <div className="harbour-line" />
      </header>
      <main
        id="main"
        className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:py-10"
      >
        {/* Re-keyed per route so each screen settles in (page transition) */}
        <div key={path} className="page-enter">
          {children}
        </div>
      </main>
      <footer className="border-t border-[var(--rule)] bg-card">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-5 text-sm text-muted-foreground">
          <span className="flex items-center gap-2">
            <BridgeMark className="w-8 text-[var(--indigo)]" />
            {m.footer_made_by()}
          </span>
          <span>
            {m.app_name()} · {m.app_tagline()}
          </span>
        </div>
      </footer>
      <Toaster />
    </div>
  )
}
