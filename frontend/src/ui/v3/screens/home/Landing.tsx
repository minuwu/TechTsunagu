import {
  useRef,
  type CSSProperties,
  type MouseEvent,
  type ReactNode,
} from 'react'
import { Link } from '@tanstack/react-router'
import { useGSAP } from '@gsap/react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { DOORS, type Door } from '@/domain/course'
import { m } from '@/i18n/paraglide/messages'
import { usePersisted } from '@/lib/persisted'
import { HarbourWaves } from '@/ui/v3/components/HarbourWaves'
import { MOTION_OK, scrollToSection } from '@/ui/v3/lib/motion'
import { cn } from '@/ui/v3/lib/utils'

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
const GLOW = ['primary', 'life', 'warning', 'success']
const glow = (token: string) =>
  ({
    '--door-glow': `color-mix(in oklch, var(--${token}) 35%, transparent)`,
  }) as CSSProperties

const MHLW = 'https://www.mhlw.go.jp/stf/newpage_75722.html'

function Section({
  id,
  title,
  lead,
  children,
}: {
  id: string
  title: string
  lead?: string
  children: ReactNode
}) {
  return (
    <section id={id} aria-labelledby={`${id}-h`} className="mb-14 scroll-mt-24">
      <div data-reveal>
        <h2 id={`${id}-h`} className="display mb-3 text-3xl sm:text-4xl">
          {title}
        </h2>
        {lead && (
          <p className="mb-6 max-w-[40em] text-lg text-pretty text-muted-foreground">
            {lead}
          </p>
        )}
      </div>
      {children}
    </section>
  )
}

// An in-page link that glides with Lenis and hands focus to the section (href kept for no-JS)
const glide = (id: string) => (e: MouseEvent<HTMLAnchorElement>) => {
  e.preventDefault()
  scrollToSection(id)
}

/** Landing motion (GSAP, only when motion is welcome, F-38): the hero settles in after the splash, blocks
 *  rise as they scroll into view, the sourced statistic counts up. Content is never hidden from assistive
 *  tech: reveals change opacity, not visibility, and everything ends at full opacity. */
function useLandingMotion() {
  const scope = useRef<HTMLDivElement>(null)
  useGSAP(
    () => {
      gsap.registerPlugin(ScrollTrigger)
      const root = scope.current
      if (!root) return
      const mm = gsap.matchMedia()
      mm.add(MOTION_OK, () => {
        const q = gsap.utils.selector(root)
        const afterSplash = document.querySelector('.splash') ? 1.05 : 0.1
        gsap.from(q('[data-hero]'), {
          y: 24,
          opacity: 0,
          duration: 0.8,
          ease: 'power3.out',
          stagger: 0.09,
          delay: afterSplash,
        })
        // Blocks already on screen follow the hero in; the rest rise as they scroll into view
        const fold = window.innerHeight * 0.92
        const all = q('[data-reveal]')
        const inView = all.filter((el) => el.getBoundingClientRect().top < fold)
        const blocks = all.filter((el) => !inView.includes(el))
        gsap.from(inView, {
          y: 28,
          opacity: 0,
          duration: 0.7,
          ease: 'power3.out',
          stagger: 0.08,
          delay: afterSplash + 0.35,
        })
        gsap.set(blocks, { opacity: 0, y: 28 })
        // Web fonts change line breaks and so every block's position: measure again once they are in
        void document.fonts.ready.then(() => ScrollTrigger.refresh())
        ScrollTrigger.batch(blocks, {
          start: 'top 88%',
          once: true,
          onEnter: (els) =>
            gsap.to(els, {
              opacity: 1,
              y: 0,
              duration: 0.7,
              ease: 'power3.out',
              stagger: 0.08,
              overwrite: true,
            }),
        })
        for (const el of q('[data-count]') as HTMLElement[]) {
          const end = Number(el.dataset.count)
          const suffix = el.dataset.suffix ?? ''
          const n = { v: 0 }
          gsap.to(n, {
            v: end,
            duration: 1.4,
            ease: 'power2.out',
            scrollTrigger: { trigger: el, start: 'top 85%', once: true },
            onUpdate: () => {
              el.textContent = `${n.v.toFixed(1)}${suffix}`
            },
          })
        }
      })
      return () => mm.revert()
    },
    { scope },
  )
  return scope
}

/** The pitch: why this project, the gap, how it is covered (docs/superpowers/plans/2026-11-22-wakatta.md). */
export function Landing() {
  const scope = useLandingMotion()
  const [theme] = usePersisted('wakatta.theme', 'dark')
  const steps = [
    [m.lp_how1_t(), m.lp_how1()],
    [m.lp_how2_t(), m.lp_how2()],
    [m.lp_how3_t(), m.lp_how3()],
    [m.lp_how4_t(), m.lp_how4()],
  ] as const
  return (
    <div ref={scope}>
      <section className="card-surface door mb-14 bg-transparent! p-6 sm:min-h-[28rem] sm:p-10 lg:min-h-[32rem]">
        <HarbourWaves theme={theme === 'light' ? 'light' : 'dark'} />
        <p data-hero className="mb-3 font-bold text-[var(--indigo)]">
          {m.lp_eyebrow()}
        </p>
        <h1
          data-hero
          className="display mb-5 max-w-[16em] text-4xl leading-tight text-balance sm:text-6xl"
        >
          {m.lp_h1()}
        </h1>
        <p
          data-hero
          className="mb-8 max-w-[38em] text-lg leading-relaxed text-pretty [word-break:auto-phrase] sm:text-xl"
        >
          {m.lp_lead()}
        </p>
        <div data-hero className="flex flex-wrap gap-3">
          <a
            href="#demo"
            onClick={glide('demo')}
            className="inline-flex min-h-12 items-center rounded-xl bg-primary px-6 font-bold text-primary-foreground shadow-[var(--shadow-md)] transition-transform duration-150 hover:scale-[1.03] active:scale-95"
          >
            {m.lp_cta_demo()}
          </a>
          <a
            href="#how"
            onClick={glide('how')}
            className="tile inline-flex min-h-12 items-center px-6 font-bold"
          >
            {m.lp_cta_how()}
          </a>
        </div>
      </section>

      <Section id="why" title={m.lp_problem_h()} lead={m.lp_problem_lead()}>
        <div className="grid gap-4 sm:grid-cols-2">
          {[
            ['46.2%', m.lp_stat1(), 'destructive'],
            ['3.98 · 3.91', m.lp_stat2(), 'warning'],
          ].map(([n, text, tone]) => (
            <div
              key={n}
              data-reveal
              style={glow(tone!)}
              className="card-surface door p-6"
            >
              <p
                className="display tnum mb-2 text-5xl sm:text-6xl"
                {...(/^[\d.]+%$/.test(n!)
                  ? { 'data-count': n!.slice(0, -1), 'data-suffix': '%' }
                  : {})}
              >
                {n}
              </p>
              <p className="text-lg text-pretty">{text}</p>
            </div>
          ))}
        </div>
        <p className="mt-4 text-sm text-muted-foreground">
          <a href={MHLW} className="underline" rel="noreferrer" target="_blank">
            {m.lp_source()}
          </a>
        </p>
      </Section>

      <Section id="gap" title={m.lp_gap_h()} lead={m.lp_gap_lead()}>
        <div className="grid gap-4 lg:grid-cols-2">
          <div data-reveal className="card-surface p-6">
            <h3 className="display mb-4 text-xl text-muted-foreground">
              {m.lp_done_h()}
            </h3>
            <ul className="space-y-3 text-muted-foreground">
              {[m.lp_done1(), m.lp_done2(), m.lp_done3()].map((t) => (
                <li key={t} className="flex gap-3">
                  <span
                    aria-hidden="true"
                    className="mt-2.5 size-2 shrink-0 rounded-full bg-border"
                  />
                  {t}
                </li>
              ))}
            </ul>
          </div>
          <div
            data-reveal
            style={glow('success')}
            className="card-surface door border-success/50! p-6"
          >
            <h3 className="display mb-4 text-xl text-success">
              {m.lp_only_h()}
            </h3>
            <ul className="space-y-3">
              {[m.lp_only1(), m.lp_only2()].map((t) => (
                <li key={t} className="flex gap-3">
                  <span
                    aria-hidden="true"
                    className="mt-2.5 size-2 shrink-0 rounded-full bg-success"
                  />
                  {t}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </Section>

      <Section id="how" title={m.lp_how_h()}>
        <ol className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {steps.map(([title, body], i) => (
            <li
              key={title}
              data-reveal
              style={glow(GLOW[i]!)}
              className="card-surface door p-5"
            >
              <span
                aria-hidden="true"
                className="display mb-3 grid size-10 place-items-center rounded-full bg-primary text-primary-foreground"
              >
                {i + 1}
              </span>
              <h3 className="display mb-2 text-xl">{title}</h3>
              <p className="text-pretty text-muted-foreground">{body}</p>
            </li>
          ))}
        </ol>
      </Section>

      <div className="mb-14 grid gap-4 lg:grid-cols-2">
        <section
          aria-labelledby="life-h"
          data-reveal
          style={glow('life')}
          className="card-surface door p-6"
        >
          <h2 id="life-h" className="display mb-3 text-2xl">
            {m.lp_life_h()}
          </h2>
          <p className="text-pretty">{m.lp_life()}</p>
        </section>
        <section
          aria-labelledby="biz-h"
          data-reveal
          className="card-surface door p-6"
        >
          <h2 id="biz-h" className="display mb-3 text-2xl">
            {m.lp_biz_h()}
          </h2>
          <p className="text-pretty">{m.lp_biz()}</p>
        </section>
      </div>

      <Section id="course" title={m.lp_course_h()} lead={m.lp_course()}>
        <ul className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {DOORS.map((d, i) => (
            <li key={d} data-reveal>
              <Link
                to="/course"
                style={glow(GLOW[i]!)}
                className={cn(
                  'tile door flex min-h-28 flex-col justify-end gap-0.5 p-4',
                )}
              >
                <span className="display text-xl">{DOOR_NAME[d]()}</span>
                <span className="text-sm text-muted-foreground">
                  {DOOR_SUB[d]()}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </Section>
    </div>
  )
}
