import { Link } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'

// Phone and tablet: text tabs on a hairline, flush with the page edge like the header links; the current
// one is ink with a steel rule under it. Desktop (lg): a flat vertical rail inside a hairline frame.
const tab =
  'relative inline-flex min-h-11 shrink-0 items-center font-bold whitespace-nowrap text-muted-foreground transition-colors duration-150 after:absolute after:inset-x-0 after:-bottom-px after:h-0.5 after:bg-transparent after:transition-colors hover:text-foreground [&.active]:text-foreground [&.active]:after:bg-primary lg:px-4 lg:after:inset-x-auto lg:after:inset-y-0 lg:after:left-0 lg:after:h-auto lg:after:w-1 lg:hover:bg-accent/60 lg:[&.active]:bg-accent'

export function ForemanNav() {
  return (
    <nav
      aria-label={m.foreman_nav()}
      className="-mx-4 mb-8 overflow-x-auto border-b border-[var(--rule)] px-4 [scrollbar-width:thin] sm:mx-0 sm:px-0 lg:mb-0 lg:overflow-visible lg:border lg:border-foreground lg:py-1"
    >
      <div className="flex gap-6 lg:flex-col lg:gap-0">
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
