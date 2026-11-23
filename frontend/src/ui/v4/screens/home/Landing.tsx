import { Link } from '@tanstack/react-router'
import { DOORS, type Door } from '@/domain/course'
import { m } from '@/i18n/paraglide/messages'
import { GlobeMap } from '@/ui/v4/components/GlobeMap'
import {
  PosterIndex,
  PosterRow,
  PosterSection,
} from '@/ui/v4/components/Poster'
import { buttonVariants } from '@/ui/v4/components/ui/button'

const DOOR_NAME: Record<Door, () => string> = {
  read: m.course_door_read,
  watch: m.course_door_watch,
  play: m.course_door_play,
  inspect: m.course_door_inspect,
}
const DOOR_SUB: Record<Door, () => string> = {
  read: m.course_door_read_sub,
  watch: m.course_door_watch_sub,
  play: m.course_door_play_sub,
  inspect: m.course_door_inspect_sub,
}

const STAT1 = '46.2%'
const STAT2 = '3.98 · 3.91'
const MHLW = 'https://www.mhlw.go.jp/stf/newpage_75722.html'

/** The headline splits at its first sentence break; the second half is the cobalt word group. */
function splitHeadline(h: string): [string, string] {
  // the space after the break stays with the first half: English keeps it, Japanese has none
  const hit = /^(.+?[.。、，]\s*)(.+)$/.exec(h)
  return hit ? [hit[1]!, hit[2]!] : [h, '']
}

/** The pitch: why this project, the gap, how it is covered (docs/superpowers/plans/2026-11-22-wakatta.md). */
export function Landing() {
  const steps = [
    [m.lp_how1_t(), m.lp_how1()],
    [m.lp_how2_t(), m.lp_how2()],
    [m.lp_how3_t(), m.lp_how3()],
    [m.lp_how4_t(), m.lp_how4()],
  ] as const
  const [lead, accent] = splitHeadline(m.lp_h1())
  return (
    <>
      <section className="poster-grid min-h-[85vh] md:min-h-[85vh]">
        <div className="poster-side flex flex-col justify-between gap-8">
          <div className="space-y-4">
            <p className="poster-label">{m.lp_manifesto()}</p>
            <span aria-hidden="true" className="block size-4 bg-foreground" />
          </div>
          <p className="poster-label hidden md:block">{m.lp_scroll()}</p>
        </div>
        <div className="poster-main flex flex-col justify-between gap-10">
          <div className="space-y-6">
            <p className="poster-label !text-primary">{m.lp_eyebrow()}</p>
            <h1 className="poster-h1 max-w-[14ch] md:max-w-none">
              {lead}
              <span className="text-primary">{accent}</span>
            </h1>
          </div>
          <GlobeMap className="h-[26rem] border border-[var(--foreground)] sm:h-[30rem] lg:h-[34rem]" />
          <div className="grid items-end gap-8 md:grid-cols-2">
            <p className="max-w-[26rem] text-lg leading-relaxed text-pretty text-muted-foreground [word-break:auto-phrase]">
              {m.lp_lead()}
            </p>
            <div className="flex flex-wrap items-center gap-6">
              <a
                href="#demo"
                className={buttonVariants({ variant: 'default', size: 'lg' })}
              >
                {m.lp_cta_demo()}
              </a>
              <a
                href="#how"
                className="min-h-11 content-center font-bold underline hover:text-primary"
              >
                {m.lp_cta_how()}
              </a>
            </div>
          </div>
        </div>
      </section>

      <PosterSection id="how" label={m.lp_label_system()}>
        <h2 className="poster-h2 mb-10 max-w-[12ch]">{m.lp_how_h()}</h2>
        <ol className="grid border-t border-l border-[var(--rule)] sm:grid-cols-2 xl:grid-cols-4">
          {steps.map(([title, body], i) => (
            <li
              key={title}
              className="border-r border-b border-[var(--rule)] p-5 transition-colors duration-300 hover:bg-white/20"
            >
              <PosterIndex n={i + 1} className="mb-10 block" />
              <h3 className="display mb-2 text-2xl">{title}</h3>
              <p className="text-pretty text-muted-foreground">{body}</p>
            </li>
          ))}
        </ol>
      </PosterSection>

      <PosterSection id="why" label={m.lp_label_why()}>
        <h2 className="poster-h2 mb-4 max-w-[14ch]">{m.lp_problem_h()}</h2>
        <p className="mb-10 max-w-[40em] text-lg text-pretty text-muted-foreground">
          {m.lp_problem_lead()}
        </p>
        <div>
          <PosterRow
            n={1}
            title={<span className="tnum">{STAT1}</span>}
            sub={m.lp_stat1()}
          />
          <PosterRow
            n={2}
            title={<span className="tnum">{STAT2}</span>}
            sub={m.lp_stat2()}
          />
          <PosterRow n={3} title={m.lp_gap_h()} sub={m.lp_gap_lead()} />
        </div>
        <p className="mt-4 text-sm text-muted-foreground">
          <a href={MHLW} className="underline" rel="noreferrer" target="_blank">
            {m.lp_source()}
          </a>
        </p>
        <div className="mt-10 grid border-t border-l border-[var(--rule)] lg:grid-cols-2">
          <div className="border-r border-b border-[var(--rule)] p-6">
            <h3 className="display mb-4 text-xl text-muted-foreground">
              {m.lp_done_h()}
            </h3>
            <ul className="space-y-3 text-muted-foreground">
              {[m.lp_done1(), m.lp_done2(), m.lp_done3()].map((t) => (
                <li key={t} className="flex gap-3">
                  <span
                    aria-hidden="true"
                    className="mt-2.5 size-2 shrink-0 bg-[var(--rule)]"
                  />
                  {t}
                </li>
              ))}
            </ul>
          </div>
          <div className="border-r border-b border-[var(--rule)] bg-foreground p-6 text-background">
            <h3 className="display mb-4 text-xl">{m.lp_only_h()}</h3>
            <ul className="space-y-3">
              {[m.lp_only1(), m.lp_only2()].map((t) => (
                <li key={t} className="flex gap-3">
                  <span
                    aria-hidden="true"
                    className="mt-2.5 size-2 shrink-0 bg-primary"
                  />
                  {t}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </PosterSection>

      <PosterSection label={m.lp_label_beyond()}>
        <div className="grid border-t border-l border-[var(--rule)] lg:grid-cols-2">
          {[
            [m.lp_life_h(), m.lp_life()],
            [m.lp_biz_h(), m.lp_biz()],
          ].map(([h, body], i) => (
            <section
              key={h}
              className="border-r border-b border-[var(--rule)] p-6 transition-colors duration-300 hover:bg-white/20"
            >
              <PosterIndex n={i + 1} className="mb-8 block" />
              <h2 className="display mb-3 text-2xl">{h}</h2>
              <p className="text-pretty">{body}</p>
            </section>
          ))}
        </div>
      </PosterSection>

      <PosterSection id="course" label={m.lp_label_courses()}>
        <h2 className="poster-h2 mb-4 max-w-[12ch]">{m.lp_course_h()}</h2>
        <p className="mb-10 max-w-[40em] text-lg text-pretty text-muted-foreground">
          {m.lp_course()}
        </p>
        <div>
          {DOORS.map((d, i) => (
            <PosterRow
              key={d}
              n={i + 1}
              to="/course"
              title={DOOR_NAME[d]()}
              sub={DOOR_SUB[d]()}
            />
          ))}
          <Link
            to="/course"
            className="poster-row !min-h-0 justify-end border-b border-[var(--rule)] font-bold underline hover:text-primary"
          >
            {m.lp_all_courses()}
          </Link>
        </div>
      </PosterSection>

      <section className="poster-grid min-h-[50vh]">
        <div className="poster-side">
          <p className="poster-label">{m.lp_label_access()}</p>
        </div>
        <div className="poster-main flex flex-col justify-between gap-10">
          <h2 className="poster-h1">{m.lp_demo_h()}</h2>
          <div className="flex flex-wrap items-end justify-between gap-6">
            <p className="max-w-[32rem] text-lg text-muted-foreground">
              {m.hub_sub()}
            </p>
            <a
              href="#demo"
              className={buttonVariants({
                variant: 'secondary',
                size: 'lg',
                className: '!px-10 !py-5',
              })}
            >
              {m.lp_cta_demo()}
            </a>
          </div>
        </div>
      </section>
    </>
  )
}
