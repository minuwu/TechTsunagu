import { Link } from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'

const link =
  'rounded-md px-3 py-2 font-bold hover:bg-accent [&.active]:bg-primary [&.active]:text-primary-foreground'

export function ForemanNav() {
  return (
    <nav
      aria-label={m.foreman_nav()}
      className="mb-6 flex flex-wrap gap-1 border-b border-border pb-3"
    >
      <Link to="/foreman" activeOptions={{ exact: true }} className={link}>
        {m.nav_board()}
      </Link>
      <Link to="/foreman/assign" className={link}>
        {m.nav_assign()}
      </Link>
      <Link to="/foreman/skills" className={link}>
        {m.nav_skills()}
      </Link>
      <Link to="/foreman/heatmap" className={link}>
        {m.nav_heatmap()}
      </Link>
      <Link to="/foreman/briefing" className={link}>
        {m.nav_briefing()}
      </Link>
      <Link to="/foreman/near-misses" className={link}>
        {m.nav_near_misses()}
      </Link>
      <Link to="/foreman/glossary" className={link}>
        {m.nav_glossary()}
      </Link>
      <Link to="/foreman/senpai" className={link}>
        {m.nav_senpai()}
      </Link>
      <Link to="/foreman/qr" className={link}>
        {m.nav_qr()}
      </Link>
    </nav>
  )
}

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
        className={`${link} text-center`}
      >
        {m.tab_work()}
      </Link>
      <Link
        to="/me/$workerId/life"
        params={{ workerId }}
        className={`${link} text-center`}
      >
        {m.tab_life()}
      </Link>
    </nav>
  )
}
