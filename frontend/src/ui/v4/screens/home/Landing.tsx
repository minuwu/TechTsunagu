import { useRef } from 'react'
import { Link } from '@tanstack/react-router'
import { useGSAP } from '@gsap/react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { m } from '@/i18n/paraglide/messages'
import { HeroFuji } from '@/ui/v4/components/HeroFuji'
import { Journey } from '@/ui/v4/components/journey/Journey'
import {
  PosterIndex,
  PosterRow,
  PosterSection,
} from '@/ui/v4/components/Poster'
import { buttonVariants } from '@/ui/v4/components/ui/button'
import { uiLang } from '@/ui/v4/lib/format'
import { MOTION_OK, splashDelay } from '@/ui/v4/lib/motion'

/** The headline splits at its first sentence break; the second half is the cobalt word group. */
function splitHeadline(h: string): [string, string] {
  // the space after the break stays with the first half: English keeps it, Japanese has none
  const hit = /^(.+?[.。、，]\s*)(.+)$/.exec(h)
  return hit ? [hit[1]!, hit[2]!] : [h, '']
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
        const afterSplash = splashDelay()
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

/** A bulleted list on a poster block; `dot` is the square's colour class. */
function Bullets({ items, dot }: { items: string[]; dot: string }) {
  return (
    <ul className="space-y-3">
      {items.map((t) => (
        <li key={t} className="flex gap-3">
          <span
            aria-hidden="true"
            className={`mt-2.5 size-2 shrink-0 ${dot}`}
          />
          {t}
        </li>
      ))}
    </ul>
  )
}

/** The pitch, from the hackathon proposal (submission/TechTsunagu_Proposal.md): the problem, the evidence,
 *  how the loop closes, what is different, what is built and by whom. */
export function Landing() {
  const scope = useLandingMotion()
  const num = new Intl.NumberFormat(uiLang())
  const dec = new Intl.NumberFormat(uiLang(), { minimumFractionDigits: 1 })
  const steps = [
    [m.home_how1_t(), m.home_how1()],
    [m.home_how2_t(), m.home_how2()],
    [m.home_how3_t(), m.home_how3()],
    [m.home_how4_t(), m.home_how4()],
  ] as const
  const features = [
    [m.home_f1_h(), m.home_f1()],
    [m.home_f2_h(), m.home_f2()],
    [m.home_f3_h(), m.home_f3()],
    [m.home_f4_h(), m.home_f4()],
  ] as const
  const different = [
    [m.home_diff1_t(), m.home_diff1()],
    [m.home_diff2_t(), m.home_diff2()],
    [m.home_diff3_t(), m.home_diff3()],
    [m.home_diff4_t(), m.home_diff4()],
  ] as const
  const [lead, accent] = splitHeadline(m.home_h1())
  return (
    <div ref={scope}>
      <section className="poster-grid fuji-scope relative isolate min-h-[85vh] md:min-h-[85vh]">
        <HeroFuji />
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
        </div>
      </section>

      <Journey />

      <PosterSection id="why" label={m.home_label_problem()}>
        <div data-reveal className="max-w-[40em] space-y-6">
          <h2 className="poster-h2 max-w-[16ch]">{m.home_problem_h()}</h2>
          <p className="poster-label">{m.home_problem_tag()}</p>
          <p className="text-lg text-pretty">{m.home_problem_story()}</p>
          <p className="text-lg text-pretty text-muted-foreground">
            {m.home_problem_why()}
          </p>
          <p className="display border-l-4 border-primary pl-4 text-2xl text-pretty">
            {m.home_problem_fix()}
          </p>
        </div>
      </PosterSection>

      <PosterSection label={m.home_label_evidence()}>
        <h2 data-reveal className="poster-h2 mb-10 max-w-[14ch]">
          {m.home_evidence_h()}
        </h2>
        <div data-reveal>
          <PosterRow
            n={1}
            title={<span className="tnum">{num.format(2571037)}</span>}
            sub={m.home_stat1()}
          />
          <PosterRow
            n={2}
            title={
              <span className="tnum" data-count="15.4" data-suffix="%">
                {`${dec.format(15.4)}%`}
              </span>
            }
            sub={m.home_stat2()}
          />
          <PosterRow
            n={3}
            title={<span className="tnum">{num.format(6244)}</span>}
            sub={m.home_stat3()}
          />
          <PosterRow
            n={4}
            title={
              <span className="tnum">{`${dec.format(6.5)} · ${dec.format(2.6)}`}</span>
            }
            sub={m.home_stat4()}
          />
        </div>
        <p data-reveal className="mt-8 max-w-[40em] text-lg text-pretty">
          {m.home_target()}
        </p>
        <p className="mt-4 max-w-[60em] text-sm text-muted-foreground">
          {m.home_sources()}
        </p>
        <div
          data-reveal
          className="mt-10 grid border-t border-l border-[var(--rule)] lg:grid-cols-2"
        >
          <div className="border-r border-b border-[var(--rule)] p-6 text-muted-foreground">
            <h3 className="display mb-4 text-xl">{m.home_today_h()}</h3>
            <Bullets
              items={[m.home_today1(), m.home_today2(), m.home_today3()]}
              dot="bg-[var(--rule)]"
            />
          </div>
          <div className="border-r border-b border-[var(--rule)] bg-foreground p-6 text-background">
            <h3 className="display mb-4 text-xl">{m.home_answer_h()}</h3>
            <Bullets
              items={[m.home_answer1(), m.home_answer2(), m.home_answer3()]}
              dot="bg-primary"
            />
          </div>
        </div>
      </PosterSection>

      <PosterSection id="how" label={m.lp_label_system()}>
        <h2 data-reveal className="poster-h2 mb-10 max-w-[14ch]">
          {m.home_how_h()}
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

      <PosterSection label={m.home_label_features()}>
        <div
          data-reveal
          className="grid border-t border-l border-[var(--rule)] lg:grid-cols-2"
        >
          {features.map(([h, body], i) => (
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

      <PosterSection label={m.lp_label_why()}>
        <h2 data-reveal className="poster-h2 mb-10 max-w-[14ch]">
          {m.home_diff_h()}
        </h2>
        <div data-reveal>
          {different.map(([title, sub], i) => (
            <PosterRow key={title} n={i + 1} title={title} sub={sub} />
          ))}
        </div>
      </PosterSection>

      <PosterSection label={m.home_label_built()}>
        <h2 data-reveal className="poster-h2 mb-10 max-w-[14ch]">
          {m.home_built_h()}
        </h2>
        <div
          data-reveal
          className="grid border-t border-l border-[var(--rule)] lg:grid-cols-2"
        >
          <div className="border-r border-b border-[var(--rule)] p-6">
            <Bullets
              items={[
                m.home_built1(),
                m.home_built2(),
                m.home_built3(),
                m.home_built4(),
                m.home_built5(),
              ]}
              dot="bg-primary"
            />
          </div>
          <div className="border-r border-b border-[var(--rule)] bg-foreground p-6 text-background">
            <h3 className="display mb-4 text-xl">{m.home_quality_h()}</h3>
            <Bullets
              items={[m.home_quality1(), m.home_quality2()]}
              dot="bg-primary"
            />
          </div>
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
              {m.lp_demo_sub()}
            </p>
            <Link
              to="/demo"
              className={buttonVariants({
                variant: 'secondary',
                size: 'lg',
                className: '!px-10 !py-5',
              })}
            >
              {m.lp_cta_demo()}
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}
