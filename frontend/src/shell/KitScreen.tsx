import { lazy, Suspense, type ComponentType } from 'react'
import { useLoaderData } from '@tanstack/react-router'
import type { ScreenId } from '@/contract/ui-kit'
import { AuthGate } from './AuthGate'
import { kits, type KitId } from './ui-registry'

type KitView = ComponentType<{ screen: ScreenId }>

// One lazy component per kit, built once at module load so React keeps its state between renders
const views: Partial<Record<KitId, KitView>> = Object.fromEntries(
  Object.entries(kits).map(([id, kit]) => [
    id,
    lazy(async () => {
      const { Layout, screens } = (await kit.load()).default
      return {
        default: ({ screen }: { screen: ScreenId }) => {
          const Screen = screens[screen]
          return (
            <Layout>
              <AuthGate screen={screen}>
                <Screen />
              </AuthGate>
            </Layout>
          )
        },
      }
    }),
  ]),
)

// Renders the active kit's screen for a route (S-08). The kit id comes from the root loader (S-35).
export function KitScreen({ screen }: { screen: ScreenId }) {
  const { kitId } = useLoaderData({ from: '__root__' })
  const Kit = views[kitId]
  if (!Kit) throw new Error(`UI kit "${kitId}" is not registered`) // unreachable: pickKitId only returns registered ids
  return (
    <Suspense fallback={null}>
      <Kit screen={screen} />
    </Suspense>
  )
}
