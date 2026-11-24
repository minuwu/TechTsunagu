import type { ComponentType } from 'react'
import { electricalArt } from './electrical'
import { evacuationArt } from './evacuation'
import { fallPreventionArt } from './fall-prevention'
import { fiveSArt } from './five-s'
import { forkliftArt } from './forklift'
import { genbaWordsArt } from './genba-words'
import { heatstrokeArt } from './heatstroke'
import { hiyariHattoArt } from './hiyari-hatto'
import { hourenSouArt } from './houren-sou'
import { kyKatsudoArt } from './ky-katsudo'
import { lowBackArt } from './low-back'
import { machineHazardsArt } from './machine-hazards'
import { onboardingSafetyArt } from './onboarding-safety'
import { ppeArt } from './ppe'
import { safetySignsArt } from './safety-signs'
import { shisaKankoArt } from './shisa-kanko'
import { tamagakeArt } from './tamagake'
import { weldingArt } from './welding'

type Art = ComponentType<{ className?: string }>

// A course without a cover gets the plain cobalt block below.
const ART: Record<string, Art> = {
  electrical: electricalArt,
  evacuation: evacuationArt,
  'fall-prevention': fallPreventionArt,
  'five-s': fiveSArt,
  forklift: forkliftArt,
  'genba-words': genbaWordsArt,
  heatstroke: heatstrokeArt,
  'hiyari-hatto': hiyariHattoArt,
  'houren-sou': hourenSouArt,
  'ky-katsudo': kyKatsudoArt,
  'low-back': lowBackArt,
  'machine-hazards': machineHazardsArt,
  'onboarding-safety': onboardingSafetyArt,
  ppe: ppeArt,
  'safety-signs': safetySignsArt,
  'shisa-kanko': shisaKankoArt,
  tamagake: tamagakeArt,
  welding: weldingArt,
}

/** The cover illustration of a course (400 x 300 poster art). */
export function CourseArt({
  id,
  className,
}: {
  id: string
  className?: string
}) {
  const Cover = ART[id]
  return Cover ? (
    <Cover {...(className ? { className } : {})} />
  ) : (
    <svg
      viewBox="0 0 400 300"
      aria-hidden="true"
      className={className}
      preserveAspectRatio="xMidYMid slice"
    >
      <rect width="400" height="300" fill="#E4ECF2" />
      <rect x="40" y="40" width="160" height="220" fill="#214C69" />
      <rect x="200" y="140" width="160" height="120" fill="#0A1C26" />
    </svg>
  )
}
