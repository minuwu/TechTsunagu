import type { Tri } from './wakatta'

/** One Safety Skill, four Doors (docs/ROVERVILLA_COURSE_PLATFORM_SPEC.md §3). */
export const DOORS = ['read', 'watch', 'play', 'inspect'] as const
export type Door = (typeof DOORS)[number]

// shortcut: 3 of the 9 drills open the Play Door, raise to 9 when the Foreman roster can require it
export const PLAY_NEEDED = 3
export const REFLEX_MS = 400
export const RING_HZ = { intact: 1200, cracked: 320 } as const

export interface Progress {
  read: boolean
  watch: boolean
  inspect: boolean
  drills: string[] // ids of passed drills
}
export const emptyProgress: Progress = {
  read: false,
  watch: false,
  inspect: false,
  drills: [],
}

export const PROGRESS_KEY = 'rv:course_progress:v1'

/** Stored value is untrusted (dev tools, older build): anything odd becomes empty. */
export function parseProgress(raw: string | null): Progress {
  try {
    const p = JSON.parse(raw ?? '') as Partial<Progress>
    return {
      read: p.read === true,
      watch: p.watch === true,
      inspect: p.inspect === true,
      drills: Array.isArray(p.drills)
        ? p.drills.filter((d) => typeof d === 'string')
        : [],
    }
  } catch {
    return emptyProgress
  }
}

export const doorDone = (p: Progress, d: Door) =>
  d === 'play' ? p.drills.length >= PLAY_NEEDED : p[d]

export const passportEarned = (p: Progress) =>
  DOORS.every((d) => doorDone(p, d))

/** Force on each leg of a two-leg sling: W/2 = T·cos(θ/2). Doubles from 0° to 120°. */
export const slingTension = (loadKg: number, angleDeg: number) =>
  loadKg / 2 / Math.cos((angleDeg * Math.PI) / 360)

/** `玉掛{たまが}け` → ruby parts: [text, reading?] */
export const rubyParts = (s: string): [string, string?][] =>
  s
    .split(/([一-鿿々]+\{[^}]+\})/)
    .filter(Boolean)
    .map((p) => {
      const r = /^(.+)\{(.+)\}$/.exec(p)
      return r ? [r[1]!, r[2]!] : [p]
    })

export const plain = (s: string) => s.replace(/\{[^}]*\}/g, '')

// --- content (one demo course: Crane slinging, 玉掛け) ---

export interface Panel {
  who: 'foreman' | 'worker'
  text: Tri
}

export type Drill = { id: string; name: Tri; goal: Tri } & (
  | {
      kind: 'spot'
      secs: number
      need: number
      items: { label: Tri; hazard: boolean }[]
    }
  | { kind: 'sling'; loadKg: number; wllKg: number }
  | { kind: 'reflex' }
  | { kind: 'ring'; wheels: boolean[] } // true = intact
  | { kind: 'seq'; steps: Tri[] } // listed in the right order
)

export interface InspectItem {
  id: string
  name: Tri
  defective: boolean
  spots: { x: number; y: number; check: Tri }[]
}
