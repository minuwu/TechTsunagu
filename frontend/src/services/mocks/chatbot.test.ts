import { describe, expect, it } from 'vitest'
import { EMERGENCY_CARD, answerKnowledge } from './ai'
import { seed } from './seed'

describe('mock chatbot (same shape as POST /api/knowledge/asks)', () => {
  const db = seed(new Date())

  it('shows the fixed card for an emergency, with no sources or check', () => {
    const a = answerKnowledge(
      'My friend is bleeding',
      null,
      db.notes,
      db.notices,
    )
    expect(a).toMatchObject({
      emergency: true,
      covered: true,
      sources: [],
      check: null,
    })
    expect(a.answer).toEqual(EMERGENCY_CARD)
  })

  it('answers a safety question from the cited guide page, with a practice question', () => {
    const a = answerKnowledge(
      'Why must I stop the machine before cleaning it?',
      null,
      db.notes,
      db.notices,
    )
    expect(a.covered).toBe(true)
    expect(a.sources[0]).toMatchObject({ kind: 'safety', page: 15 })
    expect(a.sources[0]?.attribution).toContain('厚生労働省')
    expect(a.check?.options[a.check.correctIndex]?.en).toBe(
      'Stop the machine completely',
    )
  })

  it("uses the Machine's own instruction on a Machine screen", () => {
    const a = answerKnowledge(
      'What must I wear?',
      'm-plasma',
      db.notes,
      db.notices,
    )
    expect(a.sources[0]?.kind).toBe('note')
  })

  it('says not covered instead of guessing', () => {
    const a = answerKnowledge(
      'Can you recommend a ramen shop?',
      null,
      db.notes,
      db.notices,
    )
    expect(a).toMatchObject({
      covered: false,
      answer: null,
      sources: [],
      check: null,
    })
  })
})
