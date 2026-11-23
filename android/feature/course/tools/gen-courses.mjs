// Regenerates the bundled course JSON from frontend/src/domain/courses.
// Run from the repo root: node android/feature/course/tools/gen-courses.mjs  (Node 23.6+, type stripping)
import { readdirSync, writeFileSync, mkdirSync, rmSync } from 'node:fs'
import { pathToFileURL } from 'node:url'
import { join, resolve } from 'node:path'

const src = resolve('frontend/src/domain/courses')
const out = resolve('android/feature/course/src/main/assets/courses')
rmSync(out, { recursive: true, force: true })
mkdirSync(out, { recursive: true })

const order = { core: 0, trade: 1, language: 2 }
const all = []
for (const f of readdirSync(src).filter((n) => n.endsWith('.ts') && !/^(index|types)\.ts$|\.test\.ts$/.test(n))) {
  const mod = await import(pathToFileURL(join(src, f)).href)
  const course = Object.values(mod).find((v) => v && typeof v === 'object' && v.id)
  all.push(course)
}
all.sort((a, b) => order[a.category] - order[b.category] || a.id.localeCompare(b.id))
for (const c of all) writeFileSync(join(out, `${c.id}.json`), JSON.stringify(c))
const index = all.map(({ id, category, level, minutes, title, sub }) => ({ id, category, level, minutes, title, sub }))
writeFileSync(join(out, 'index.json'), JSON.stringify(index))
console.log(`wrote ${all.length} courses`)
