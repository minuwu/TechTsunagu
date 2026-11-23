import type { Drill, InspectItem, Panel } from '../course'
import type { Tri } from '../wakatta'

export type CourseCategory = 'core' | 'trade' | 'language'

export interface QuizItem {
  q: Tri
  options: [Tri, Tri, Tri, Tri]
  answer: 0 | 1 | 2 | 3
  why: Tri
}

export interface Term {
  ja: string // base form, no furigana braces
  reading: string // hiragana
  en: string
  note?: Tri
}

/** One course = the four Doors (read, watch, play, inspect) plus quiz and glossary. Ja text carries `漢字{よみ}` furigana. */
export interface CourseData {
  id: string // kebab-case, equals the file name
  category: CourseCategory
  level: 1 | 2 | 3
  minutes: number // honest total learning time
  title: Tri
  sub: Tri
  /** Plain-language legal or guideline basis, e.g. 労働安全衛生法 第59条. Not statute text. */
  refs: string[]
  panels: Panel[] // Door 1 comic
  handbook: Tri[] // Door 1 plain-language rules
  reel: Tri[] // Door 2 shisa-kanko / key-phrase lines
  drills: Drill[] // Door 3
  inspect: InspectItem[] // Door 4
  quiz: QuizItem[]
  glossary: Term[]
}
