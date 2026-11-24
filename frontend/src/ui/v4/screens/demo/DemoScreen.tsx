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

/** Play the Story: the story demo fills the window under the header. Phones get a note instead, so they
 *  never download the prototype. */
export function DemoScreen() {
  const wide = useWide()
  return (
    <section aria-labelledby="demo-title">
      <h1 id="demo-title" className="sr-only">
        {m.demo_h()}
      </h1>

      {/* top-20: under the h-20 sticky header, out of main's padding and max width */}
      <div className="fixed inset-x-0 top-20 bottom-0 z-10 hidden lg:block">
        {wide && (
          <iframe
            src={PROTOTYPE}
            title={m.demo_frame_title()}
            className="block size-full border-0"
          />
        )}
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
