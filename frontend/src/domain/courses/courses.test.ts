// @vitest-environment node
import { describe, expect, it } from 'vitest'
import { plain } from '../course'
import { COURSES } from './index'

const ids = new Set<string>()
describe.each(COURSES)('course $id', (c) => {
  const tris = [
    c.title, c.sub, ...c.panels.map((p) => p.text), ...c.handbook, ...c.reel,
    ...c.quiz.flatMap((q) => [q.q, q.why, ...q.options]),
    ...c.drills.flatMap((d) => [d.name, d.goal]),
    ...c.inspect.flatMap((i) => [i.name, ...i.spots.map((s) => s.check)]),
  ]
  it('is unique and sized like a real course', () => {
    expect(ids.has(c.id)).toBe(false)
    ids.add(c.id)
    expect(c.panels.length).toBeGreaterThanOrEqual(8)
    expect(c.handbook.length).toBeGreaterThanOrEqual(8)
    expect(c.reel.length).toBeGreaterThanOrEqual(6)
    expect(c.drills.length).toBeGreaterThanOrEqual(3)
    expect(c.inspect.length).toBeGreaterThanOrEqual(3)
    expect(c.quiz.length).toBeGreaterThanOrEqual(10)
    expect(c.glossary.length).toBeGreaterThanOrEqual(10)
    expect(c.refs.length).toBeGreaterThan(0)
  })
  it('has all three languages everywhere, balanced furigana braces', () => {
    for (const t of tris) {
      for (const l of ['ja', 'en', 'bn'] as const) expect(t[l].trim()).not.toBe('')
      expect(plain(t.ja)).not.toMatch(/[{}]/)
    }
  })
  it('quiz answers are in range and spread', () => {
    for (const q of c.quiz) expect([0, 1, 2, 3]).toContain(q.answer)
    expect(new Set(c.quiz.map((q) => q.answer)).size).toBeGreaterThanOrEqual(3)
  })
  it('seq drills have 4+ steps, spot drills have enough hazards', () => {
    for (const d of c.drills) {
      if (d.kind === 'seq') expect(d.steps.length).toBeGreaterThanOrEqual(4)
      if (d.kind === 'spot')
        expect(d.items.filter((i) => i.hazard).length).toBeGreaterThanOrEqual(d.need)
    }
  })
})
