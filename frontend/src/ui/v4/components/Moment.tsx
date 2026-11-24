import { useEffect, useRef } from 'react'
import type { AnimationItem } from 'lottie-web'
import { reducedMotion } from '@/ui/v4/lib/motion'
import { cn } from '@/ui/v4/lib/utils'

// Brand one-shots (frontend/public/brand/lottie/, docs/brand/manifests): shape layers only, hold the last pose
const PATH = {
  'machine-unlock': '/brand/lottie/flow/machine-unlock.json',
  'understood-stamp': '/brand/lottie/flow/understood-stamp.json',
  'check-wrong': '/brand/lottie/flow/check-wrong.json',
} as const
export type MomentId = keyof typeof PATH

/** A brand Lottie moment, played once where something just happened (the Machine unlocking, a Check
 *  passed). The light SVG player loads lazily; under reduced motion it shows the final frame, still (F-38).
 *  Decorative: the words beside it say what happened, so it is hidden from assistive tech. */
export function Moment({
  id,
  className,
}: {
  id: MomentId
  className?: string
}) {
  const ref = useRef<HTMLDivElement>(null)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    let anim: AnimationItem | null = null
    let cancelled = false
    void import('lottie-web/build/player/lottie_light').then(
      ({ default: lottie }) => {
        if (cancelled) return
        const still = reducedMotion()
        anim = lottie.loadAnimation({
          container: el,
          renderer: 'svg',
          loop: false,
          autoplay: !still,
          path: PATH[id],
        })
        if (still) {
          const a = anim
          a.addEventListener('DOMLoaded', () =>
            a.goToAndStop(a.totalFrames - 1, true),
          )
        }
      },
    )
    return () => {
      cancelled = true
      anim?.destroy()
    }
  }, [id])
  return (
    <div
      ref={ref}
      aria-hidden="true"
      className={cn('aspect-square', className)}
    />
  )
}
