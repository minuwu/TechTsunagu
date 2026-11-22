// @vitest-environment node
import { describe, expect, it } from 'vitest'
import {
  doorDone,
  emptyProgress,
  parseProgress,
  passportEarned,
  plain,
  rubyParts,
  slingTension,
} from './course'
import { DRILLS } from './courseContent'

describe('slingTension', () => {
  it('doubles from 0° to 120° and is 577 kgf at 60°', () => {
    expect(slingTension(1000, 120) / slingTension(1000, 0)).toBeCloseTo(2)
    expect(slingTension(1000, 60)).toBeCloseTo(577.35, 1)
  })
})

describe('rubyParts / plain', () => {
  it('splits furigana and strips it', () => {
    expect(rubyParts('吊{つ}り荷{に}')).toEqual([
      ['吊', 'つ'],
      ['り'],
      ['荷', 'に'],
    ])
    expect(plain('吊{つ}り荷{に}')).toBe('吊り荷')
  })
})

describe('progress', () => {
  it('turns bad storage into empty and earns the passport on all four Doors', () => {
    expect(parseProgress('{oops')).toEqual(emptyProgress)
    expect(parseProgress(null)).toEqual(emptyProgress)
    const done = {
      read: true,
      watch: true,
      inspect: true,
      drills: ['a', 'b', 'c'],
    }
    expect(doorDone(done, 'play')).toBe(true)
    expect(passportEarned(done)).toBe(true)
    expect(passportEarned({ ...done, drills: ['a'] })).toBe(false)
  })
})

describe('content', () => {
  it('has nine drills with unique ids', () => {
    expect(new Set(DRILLS.map((d) => d.id)).size).toBe(9)
  })
})
