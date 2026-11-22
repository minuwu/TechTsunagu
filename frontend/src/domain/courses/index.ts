import type { CourseData } from './types'
import { electrical } from './electrical'
import { evacuation } from './evacuation'
import { fallPrevention } from './fall-prevention'
import { fiveS } from './five-s'
import { forklift } from './forklift'
import { genbaWords } from './genba-words'
import { heatstroke } from './heatstroke'
import { hiyariHatto } from './hiyari-hatto'
import { hourenSou } from './houren-sou'
import { kyKatsudo } from './ky-katsudo'
import { lowBack } from './low-back'
import { machineHazards } from './machine-hazards'
import { onboardingSafety } from './onboarding-safety'
import { ppe } from './ppe'
import { safetySigns } from './safety-signs'
import { shisaKanko } from './shisa-kanko'
import { tamagake } from './tamagake'
import { welding } from './welding'

export const COURSES: CourseData[] = [
  electrical,
  evacuation,
  fallPrevention,
  fiveS,
  forklift,
  genbaWords,
  heatstroke,
  hiyariHatto,
  hourenSou,
  kyKatsudo,
  lowBack,
  machineHazards,
  onboardingSafety,
  ppe,
  safetySigns,
  shisaKanko,
  tamagake,
  welding,
]

export const courseById = (id: string | null) =>
  COURSES.find((c) => c.id === id)
