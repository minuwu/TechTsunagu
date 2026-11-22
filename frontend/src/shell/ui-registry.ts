import type { UiKit } from '@/contract/ui-kit'
import v1Css from '@/ui/v1/tokens.css?url'
import v2Css from '@/ui/v2/tokens.css?url'
import v3Css from '@/ui/v3/tokens.css?url'

// The only file that references ui/* (A-05). Dynamic imports keep unused kits out of the bundle.
export const kits = {
  v1: { load: () => import('@/ui/v1'), css: v1Css },
  v2: { load: () => import('@/ui/v2'), css: v2Css },
  v3: { load: () => import('@/ui/v3'), css: v3Css },
} satisfies Record<
  string,
  { load: () => Promise<{ default: UiKit }>; css: string }
>

export type KitId = keyof typeof kits
const DEFAULT_KIT: KitId = 'v3'

const isKitId = (id: string | null | undefined): id is KitId =>
  id != null && Object.hasOwn(kits, id)

// ?ui= wins over the build default; unknown values fall back, never a blank page (F-42)
export function pickKitId(search: string, buildDefault?: string): KitId {
  const fromUrl = new URLSearchParams(search).get('ui')
  if (isKitId(fromUrl)) return fromUrl
  if (isKitId(buildDefault)) return buildDefault
  return DEFAULT_KIT
}
