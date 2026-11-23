import { cn } from '@/ui/v3/lib/utils'

// Spot illustrations from the brand kit (frontend/public/brand/images/spot/, track R, cobalt only)
export type SpotName =
  | 'role-worker'
  | 'role-foreman'
  | 'role-poster'
  | 'role-senpai'
  | 'state-404'
  | 'state-empty'
  | 'state-error'
  | 'state-no-results'
  | 'state-offline'
  | 'state-success'

/** A decorative brand illustration (alt=""): the words around it carry the meaning (F-35).
 *  `.spot` gives it a porcelain ground at night, where the cobalt line art would sink into navy. */
export function Spot({
  name,
  className,
}: {
  name: SpotName
  className?: string
}) {
  return (
    <img
      src={`/brand/images/spot/${name}.svg`}
      alt=""
      width={800}
      height={800}
      loading="lazy"
      decoding="async"
      className={cn('spot shrink-0', className)}
    />
  )
}
