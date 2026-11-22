import type { ComponentType, ReactNode } from 'react'

export type ScreenId =
  | 'home'
  | 'notFound'
  | 'machine'
  | 'panel'
  | 'workerWork'
  | 'workerLife'
  | 'foremanBoard'
  | 'foremanRecord'
  | 'foremanAssign'
  | 'foremanSkills'
  | 'foremanHeatmap'
  | 'foremanGlossary'
  | 'foremanBriefing'
  | 'foremanNearMisses'
  | 'foremanLibrary'
  | 'foremanQr'
  | 'foremanSenpai'
  | 'community'
  | 'senpai'
// one per route; extend when a screen is added (A-03)

export interface UiKit {
  Layout: ComponentType<{ children: ReactNode }>
  screens: Record<ScreenId, ComponentType>
}
