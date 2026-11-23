import { Link } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'

// Phone: tabs on a hairline, the current one has an indigo rule under it.
// Desktop (lg): a vertical rail; the current one is a filled row with the rule on its left edge.
const tab =
  'relative inline-flex min-h-11 shrink-0 items-center rounded-t-md px-3 font-bold whitespace-nowrap text-muted-foreground transition-colors duration-150 after:absolute after:inset-x-2 after:-bottom-px after:h-0.5 after:rounded-full after:bg-transparent after:transition-colors hover:bg-accent/60 hover:text-foreground [&.active]:text-foreground [&.active]:after:bg-primary lg:rounded-lg lg:px-3.5 lg:after:inset-x-auto lg:after:inset-y-2.5 lg:after:left-0 lg:after:h-auto lg:after:w-0.5 lg:[&.active]:bg-accent'

export function ForemanNav() {
  return (
    <nav
      aria-label={m.foreman_nav()}
      className="-mx-4 mb-8 overflow-x-auto border-b border-[var(--rule)] px-4 [scrollbar-width:thin] sm:mx-0 sm:px-0 lg:mb-0 lg:overflow-visible lg:rounded-2xl lg:border lg:border-[var(--tile-border)] lg:bg-[var(--tile)] lg:p-2 lg:shadow-[var(--shadow-sm)]"
    >
      <div className="flex gap-1 lg:flex-col lg:gap-0.5">
        <Link to="/foreman" activeOptions={{ exact: true }} className={tab}>
          {m.nav_board()}
        </Link>
        <Link to="/foreman/assign" className={tab}>
          {m.nav_assign()}
        </Link>
        <Link to="/foreman/skills" className={tab}>
          {m.nav_skills()}
        </Link>
        <Link to="/foreman/heatmap" className={tab}>
          {m.nav_heatmap()}
        </Link>
        <Link to="/foreman/briefing" className={tab}>
          {m.nav_briefing()}
        </Link>
        <Link to="/foreman/near-misses" className={tab}>
          {m.nav_near_misses()}
        </Link>
        <Link to="/foreman/glossary" className={tab}>
          {m.nav_glossary()}
        </Link>
        <Link to="/foreman/senpai" className={tab}>
          {m.nav_senpai()}
        </Link>
        <Link to="/foreman/qr" className={tab}>
          {m.nav_qr()}
        </Link>
      </div>
    </nav>
  )
}

const seg =
  'flex min-h-11 items-center justify-center rounded-md px-3 font-bold text-muted-foreground transition-[background-color,color,box-shadow] duration-150 hover:text-foreground [&.active]:bg-card [&.active]:text-foreground [&.active]:shadow-[var(--shadow-sm)]'

export function WorkerTabs({ workerId }: { workerId: string }) {
  return (
    <nav
      aria-label={m.worker_nav()}
      className="mb-6 grid grid-cols-2 gap-1 rounded-lg bg-muted p-1"
    >
      <Link
        to="/me/$workerId"
        params={{ workerId }}
        activeOptions={{ exact: true }}
        className={seg}
      >
        {m.tab_work()}
      </Link>
      <Link to="/me/$workerId/life" params={{ workerId }} className={seg}>
        {m.tab_life()}
      </Link>
    </nav>
  )
}
