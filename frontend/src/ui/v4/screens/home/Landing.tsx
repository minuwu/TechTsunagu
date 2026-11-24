import { useRef, type MouseEvent } from 'react'
import { Link } from '@tanstack/react-router'
import { useGSAP } from '@gsap/react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { DOORS, type Door } from '@/domain/course'
import { m } from '@/i18n/paraglide/messages'
import { HeroArt } from '@/ui/v4/components/HeroArt'
import { Journey } from '@/ui/v4/components/journey/Journey'
import {
  PosterIndex,
  PosterRow,
  PosterSection,
} from '@/ui/v4/components/Poster'
import { buttonVariants } from '@/ui/v4/components/ui/button'
import { MOTION_OK, scrollToSection } from '@/ui/v4/lib/motion'

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
  const steps = [
    [m.lp_how1_t(), m.lp_how1()],
    [m.lp_how2_t(), m.lp_how2()],
    [m.lp_how3_t(), m.lp_how3()],
    [m.lp_how4_t(), m.lp_how4()],
  ] as const
  const [lead, accent] = splitHeadline(m.lp_h1())
  return (
    <div ref={scope}>
      <section className="poster-grid min-h-[85vh] md:min-h-[85vh]">
        <div className="poster-side flex flex-col justify-between gap-8">
          <div className="space-y-4">
            <p className="poster-label">{m.lp_manifesto()}</p>
            <span aria-hidden="true" className="block size-4 bg-foreground" />
          </div>
          <p className="poster-label hidden md:block">{m.lp_scroll()}</p>
        </div>
        <div className="poster-main flex flex-col justify-between gap-10">
          <div data-hero className="space-y-6">
            <p className="poster-label !text-primary">{m.lp_eyebrow()}</p>
            <h1 className="poster-h1 max-w-[14ch] md:max-w-none">
              {lead}
              <span className="text-primary">{accent}</span>
            </h1>
          </div>
          <div data-hero>
            <HeroArt className="max-h-72 border border-[var(--rule)] md:max-h-80" />
          </div>
          <div data-hero className="grid items-end gap-8 md:grid-cols-2">
            <p className="max-w-[26rem] text-lg leading-relaxed text-pretty text-muted-foreground [word-break:auto-phrase]">
              {m.lp_lead()}
            </p>
            <div className="flex flex-wrap items-center gap-6">
              <a
                href="#demo"
                onClick={glide('demo')}
                className={buttonVariants({ variant: 'default', size: 'lg' })}
              >
                {m.lp_cta_demo()}
              </a>
              <a
                href="#how"
                onClick={glide('how')}
                className="min-h-11 content-center font-bold underline hover:text-primary"
              >
                {m.lp_cta_how()}
              </a>
            </div>
          </div>
        </div>
      </section>

      <Journey />

      <PosterSection id="how" label={m.lp_label_system()}>
        <h2 data-reveal className="poster-h2 mb-10 max-w-[12ch]">
          {m.lp_how_h()}
        </h2>
        <ol
          data-reveal
          className="grid border-t border-l border-[var(--rule)] sm:grid-cols-2 xl:grid-cols-4"
        >
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
        <div data-reveal>
          <h2 className="poster-h2 mb-4 max-w-[14ch]">{m.lp_problem_h()}</h2>
          <p className="mb-10 max-w-[40em] text-lg text-pretty text-muted-foreground">
            {m.lp_problem_lead()}
          </p>
        </div>
        <div data-reveal>
          <PosterRow
            n={1}
            title={
              <span className="tnum" data-count="46.2" data-suffix="%">
                {STAT1}
              </span>
            }
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
        <div
          data-reveal
          className="mt-10 grid border-t border-l border-[var(--rule)] lg:grid-cols-2"
        >
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
        <div
          data-reveal
          className="grid border-t border-l border-[var(--rule)] lg:grid-cols-2"
        >
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
        <div data-reveal>
          <h2 className="poster-h2 mb-4 max-w-[12ch]">{m.lp_course_h()}</h2>
          <p className="mb-10 max-w-[40em] text-lg text-pretty text-muted-foreground">
            {m.lp_course()}
          </p>
        </div>
        <div data-reveal>
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
        <div
          data-reveal
          className="poster-main flex flex-col justify-between gap-10"
        >
          <h2 className="poster-h1">{m.lp_demo_h()}</h2>
          <div className="flex flex-wrap items-end justify-between gap-6">
            <p className="max-w-[32rem] text-lg text-muted-foreground">
              {m.hub_sub()}
            </p>
            <a
              href="#demo"
              onClick={glide('demo')}
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
    </div>
  )
}
