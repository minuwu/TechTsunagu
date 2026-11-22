// Wakatta? domain types and pure rules. Terms follow GLOSSARY.md.

export type Lang = 'ja' | 'bn' | 'en'
export type Tri = Record<Lang, string>
export const LANGS: Lang[] = ['ja', 'bn', 'en']

export const pick = (text: Tri, lang: Lang) => text[lang] || text.ja

export type IconId =
  | 'ppe-goggles'
  | 'ppe-gloves'
  | 'hot-surface'
  | 'lockout'
  | 'measure'
  | 'crane-sling'
  | 'emergency-stop'
  | 'hands-clear'
  | 'ventilation'
  | 'check-list'
  | 'garbage'
  | 'calendar'

export interface Worker {
  id: string
  name: string
  language: Lang
}

export interface Machine {
  id: string
  name: Tri
  currentVersionId: string | null
  activeWorkerId: string | null // last Worker who opened this Machine's QR page
}

export interface GlossaryTerm {
  id: string
  ja: string
  reading: string
  bn: string
  en: string
}

export interface CheckQuestion {
  id: string
  termId: string | null
  prompt: Tri
  options: Tri[]
  correctIndex: number
}

export interface Step {
  text: Tri
  icon: IconId | null
}

export interface NoteVersion {
  id: string
  machineId: string
  versionNo: number
  status: 'draft' | 'approved'
  recordedBy: string
  transcriptJa: string
  steps: Step[]
  backTranslation: { bn: string; en: string } // Bengali / English text translated back to Japanese
  questions: CheckQuestion[]
  createdAt: string
  approvedAt: string | null
}

export type CheckTarget =
  | { kind: 'note'; id: string }
  | { kind: 'briefing'; id: string }
  | { kind: 'notice'; id: string }

export interface CheckRecord {
  id: string
  workerId: string
  target: CheckTarget
  language: Lang
  answers: number[]
  passed: boolean
  createdAt: string
}

export interface Confusion {
  id: string
  checkId: string
  questionId: string
  termId: string | null
  machineId: string
  workerId: string
  createdAt: string
  acknowledgedAt: string | null
}

export interface SignOff {
  id: string
  workerId: string
  machineId: string
  noteVersionId: string
  passedAt: string
}

export type Shift = 'day' | 'night'

export interface ShiftAssignment {
  id: string
  workerId: string
  machineId: string
  date: string // YYYY-MM-DD, Asia/Tokyo
  shift: Shift
}

export interface Briefing {
  id: string
  text: Tri
  question: CheckQuestion
  createdAt: string
}

export interface Receipt {
  targetId: string // Briefing or Notice id
  workerId: string
  openedAt: string
  passed: boolean | null
}

export interface NearMissReport {
  id: string
  workerId: string
  machineId: string
  text: Tri
  photo: string | null // data URL
  createdAt: string
  acknowledgedAt: string | null
}

export type PosterKind = 'jichikai' | 'city' | 'employer'

export interface Poster {
  id: string
  name: string
  kind: PosterKind
}

export type NoticeCategory = 'garbage' | 'disaster' | 'event' | 'duty' | 'other'

export interface NoticeEvent {
  id: string
  title: Tri
  startsAt: string // ISO with +09:00
  rrule: string | null // only FREQ=WEEKLY;BYDAY=… is supported
  confirmed: boolean
}

export interface Notice {
  id: string
  posterId: string
  category: NoticeCategory
  sourceKind: 'text' | 'voice' | 'photo'
  sourceJa: string
  photo: string | null
  title: Tri
  steps: Tri[]
  events: NoticeEvent[]
  question: CheckQuestion
  status: 'draft' | 'published'
  createdAt: string
}

export type SenpaiKind = 'coworker' | 'jichikai'

export interface SenpaiPair {
  id: string
  workerId: string
  senpaiName: string
  kind: SenpaiKind
  token: string
}

export interface Ask {
  id: string
  workerId: string
  text: string
  covered: boolean
  answer: Tri | null
  sourceNoticeIds: string[]
  forwarded: boolean
  senpaiReply: Tri | null
  createdAt: string
}

export type MeetupKind = 'coffee' | 'event' | 'drill' | 'other'

export interface Meetup {
  id: string
  pairId: string
  kind: MeetupKind
  date: string
  note: string
  loggedBy: 'worker' | 'senpai'
}

// ---------- Rules ----------

export const SIGN_OFF_DAYS = 90
export const EXPIRING_DAYS = 14
const DAY_MS = 86_400_000

export type SkillStatus = 'valid' | 'expiring' | 'expired' | 'none'

/** A Sign-off is valid for the Machine's current Note Version and for 90 days. */
export function skillStatus(
  signOff: SignOff | undefined,
  machine: Machine,
  now: Date,
): { status: SkillStatus; daysLeft: number | null } {
  if (!signOff) return { status: 'none', daysLeft: null }
  const daysLeft = Math.floor(
    (Date.parse(signOff.passedAt) + SIGN_OFF_DAYS * DAY_MS - now.getTime()) /
      DAY_MS,
  )
  if (signOff.noteVersionId !== machine.currentVersionId || daysLeft < 0)
    return { status: 'expired', daysLeft: Math.max(daysLeft, 0) }
  return { status: daysLeft < EXPIRING_DAYS ? 'expiring' : 'valid', daysLeft }
}

export const isCleared = (s: SkillStatus) => s === 'valid' || s === 'expiring'

/** Latest Sign-off of one Worker on one Machine. */
export function latestSignOff(
  signOffs: SignOff[],
  workerId: string,
  machineId: string,
): SignOff | undefined {
  return signOffs
    .filter((s) => s.workerId === workerId && s.machineId === machineId)
    .sort((a, b) => b.passedAt.localeCompare(a.passedAt))[0]
}

/** All questions must be right to pass; wrong ones become Confusions. */
export function gradeCheck(questions: CheckQuestion[], answers: number[]) {
  const wrong = questions.filter((q, i) => answers[i] !== q.correctIndex)
  return { passed: wrong.length === 0, wrong }
}

/** Today's date in Japan as YYYY-MM-DD. */
export function tokyoDate(now: Date): string {
  return new Date(now.getTime() + 9 * 3_600_000).toISOString().slice(0, 10)
}
