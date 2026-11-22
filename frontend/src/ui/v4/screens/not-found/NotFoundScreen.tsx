import { Link } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'

export function NotFoundScreen() {
  return (
    <section
      aria-labelledby="not-found-title"
      className="max-w-[40em] space-y-4"
    >
      <h1 id="not-found-title" className="display text-4xl">
        {m.not_found_title()}
      </h1>
      <p>{m.not_found_body()}</p>
      <Link
        to="/"
        className="inline-block py-2 text-primary underline underline-offset-4"
      >
        {m.back_home()}
      </Link>
    </section>
  )
}
