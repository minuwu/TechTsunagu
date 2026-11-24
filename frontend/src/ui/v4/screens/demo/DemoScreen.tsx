import { useSyncExternalStore } from 'react'
import { Link } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'

// The two-phone prototype, copied from prototypes/wakatta into public/
const PROTOTYPE = '/prototype/index.html'
// Same edge as Tailwind's lg: the 16:9 stage shows from here, the "wider screen" note below it
const WIDE = '(min-width: 64rem)'

function useWide() {
  return useSyncExternalStore(
    (onChange) => {
      const q = window.matchMedia(WIDE)
      q.addEventListener('change', onChange)
      return () => q.removeEventListener('change', onChange)
    },
    () => window.matchMedia(WIDE).matches,
    () => false, // server: no frame; the stage keeps its space and the frame loads after hydrating
  )
}

/** Live demo: the Foreman's and the Worker's phones side by side in a 16:9 stage. Phones get a note
 *  instead, so they never download the prototype. */
export function DemoScreen() {
  const wide = useWide()
  return (
    <section aria-labelledby="demo-title" className="space-y-6">
      <div className="max-w-[48em] space-y-3">
        <p className="poster-label !text-primary">{m.tab_demo()}</p>
        <h1 id="demo-title" className="display text-4xl">
          {m.demo_h()}
        </h1>
        <p className="text-lg text-pretty text-muted-foreground">
          {m.demo_lead()}
        </p>
      </div>

      <div className="hidden space-y-3 lg:block">
        <div className="demo-stage">
          {wide && (
            <iframe
              src={PROTOTYPE}
              title={m.demo_frame_title()}
              className="block size-full border-0"
            />
          )}
        </div>
        <p className="text-right">
          <a
            href={PROTOTYPE}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-block min-h-11 content-center font-bold underline hover:text-primary"
          >
            {m.demo_full_screen()}
            <span className="sr-only"> {m.demo_new_tab()}</span>
          </a>
        </p>
      </div>

      <div className="card-surface space-y-3 p-5 lg:hidden">
        <h2 className="font-bold">{m.demo_narrow_h()}</h2>
        <p className="text-muted-foreground">{m.demo_narrow()}</p>
        <Link
          to="/"
          hash="demo"
          className="inline-block min-h-11 content-center font-bold text-primary underline"
        >
          {m.demo_narrow_link()}
        </Link>
      </div>
    </section>
  )
}
