import type { InspectArt } from '../types'
import { GROUP1 } from './group1'
import { GROUP2 } from './group2'
import { GROUP3 } from './group3'

// Keyed `${courseId}/${itemId}`. Group 4 (safety-signs, shisa-kanko, tamagake, welding) is not drawn yet.
export const INSPECT_ART: Record<string, InspectArt> = { ...GROUP1, ...GROUP2, ...GROUP3 }
