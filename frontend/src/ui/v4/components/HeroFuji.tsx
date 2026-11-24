import { useRef } from 'react'
import { useGSAP } from '@gsap/react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { MOTION_OK, splashDelay } from '@/ui/v4/lib/motion'

/** Fuji as three flat silhouettes, back to front: the farthest is the largest and most abstract, the
 *  middle one carries the Hōei shoulder on its right slope, the nearest is the sharpest. Concave slopes,
 *  a notched crater. All share one 640×260 viewBox with the base at y 260; each polygon runs on to y 460
 *  so a layer lifted by the parallax never opens a gap (the .fuji box clips the rest).
 *  `rise` is the parallax: px the layer lifts per 100 px of scroll, so the farthest layer moves most. */
const LAYERS = [
  {
    depth: 'back',
    rise: 12,
    points:
      '0,460 0,240 120,184 236,78 272,64 300,68 328,62 362,76 486,180 640,244 640,460',
  },
  {
    depth: 'mid',
    rise: 8,
    points:
      '110,460 110,260 228,204 330,110 356,98 378,103 400,96 428,112 470,160 488,156 522,192 600,260 600,460',
  },
  {
    depth: 'front',
    rise: 5,
    points:
      '330,460 330,260 420,216 488,146 500,138 514,143 528,138 590,206 640,236 640,460',
  },
] as const

/** Landing hero backdrop: decorative, behind the hero text, never focusable or clickable. Colours, sizes,
 *  the float and the hover glow live in tokens.css (.fuji); this file adds the GSAP motion. */
export function HeroFuji() {
  const scope = useRef<HTMLDivElement>(null)
  useGSAP(
    () => {
      gsap.registerPlugin(ScrollTrigger)
      const root = scope.current
      const hero = root?.parentElement
      if (!root || !hero) return
      const layers = root.querySelectorAll<SVGSVGElement>('[data-rise]')
      const mm = gsap.matchMedia()
      // Front layer first, then 150 ms apart, as the splash clears. power1.out is the brief's
      // cubic-bezier(0.25, 0.46, 0.45, 0.94). clearProps hands opacity back to the CSS value.
      mm.add(MOTION_OK, () => {
        gsap.from(layers, {
          opacity: 0,
          duration: 0.8,
          ease: 'power1.out',
          stagger: { each: 0.15, from: 'end' },
          delay: splashDelay(),
          clearProps: 'opacity',
        })
      })
      // Parallax on desktop only and only across the hero; tablets and phones keep the layers still
      mm.add(`${MOTION_OK} and (min-width: 64rem)`, () => {
        for (const layer of layers) {
          const rise = Number(layer.dataset.rise)
          gsap.to(layer, {
            y: () => (-rise * hero.offsetHeight) / 100,
            ease: 'none',
            scrollTrigger: {
              trigger: hero,
              start: 'top top',
              end: 'bottom top',
              scrub: true,
              invalidateOnRefresh: true,
            },
          })
        }
      })
      return () => mm.revert()
    },
    { scope },
  )
  return (
    <div ref={scope} aria-hidden="true" className="fuji">
      <div className="fuji-float">
        {LAYERS.map((l) => (
          <svg
            key={l.depth}
            data-rise={l.rise}
            viewBox="0 0 640 260"
            preserveAspectRatio="xMaxYMax meet"
            focusable="false"
            className={`fuji-layer fuji-${l.depth}`}
          >
            <polygon points={l.points} />
          </svg>
        ))}
      </div>
    </div>
  )
}
