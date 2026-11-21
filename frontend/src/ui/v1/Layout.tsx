import type { ReactNode } from 'react'
import { Link } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'
import { LocaleSwitcher } from './components/LocaleSwitcher'

export function Layout({ children }: { children: ReactNode }) {
  return (
    <>
      <a
        href="#main"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:rounded-md focus:bg-background focus:p-3"
      >
        {m.skip_to_main()}
      </a>
      <header className="border-b border-border">
        <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-2">
          <Link to="/" className="py-2 text-lg font-bold">
            {m.app_name()}
          </Link>
          <LocaleSwitcher />
        </div>
      </header>
      <main id="main" className="mx-auto max-w-5xl px-4 py-8">
        {children}
      </main>
    </>
  )
}
