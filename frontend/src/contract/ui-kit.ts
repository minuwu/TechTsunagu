import type { ComponentType, ReactNode } from 'react'

export type ScreenId = 'home' | 'notFound' // one per route; extend when a screen is added (A-03)

export interface UiKit {
  Layout: ComponentType<{ children: ReactNode }>
  screens: Record<ScreenId, ComponentType>
}
