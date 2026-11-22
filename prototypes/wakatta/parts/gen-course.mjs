// Builds F.js from F.js.tmpl: course content and strings come from the React app so both stay in step.
// usage: node --experimental-strip-types parts/gen-course.mjs   (run from prototypes/wakatta)
import fs from 'node:fs'
import { COURSE, PANELS, HANDBOOK, REEL, DRILLS, INSPECT } from '../../../frontend/src/domain/courseContent.ts'

const i18n = (l) => JSON.parse(fs.readFileSync(`../../frontend/src/i18n/${l}.json`, 'utf8'))
const [en, ja, bn] = [i18n('en'), i18n('ja'), i18n('bn')]
const tx = Object.fromEntries(
  Object.keys(en)
    .filter((k) => k.startsWith('course_'))
    .map((k) => [k, { en: en[k], ja: ja[k], bn: bn[k] }]),
)
const data = { course: COURSE, panels: PANELS, handbook: HANDBOOK, reel: REEL, drills: DRILLS, inspect: INSPECT, tx }
const tmpl = fs.readFileSync('parts/F.js.tmpl', 'utf8')
fs.writeFileSync('parts/F.js', tmpl.replace('__DATA__', () => JSON.stringify(data)))
console.log('wrote parts/F.js')
