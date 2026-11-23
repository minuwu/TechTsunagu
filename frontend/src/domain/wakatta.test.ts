// @vitest-environment node
import { describe, expect, it } from 'vitest'
import { isValidRrule, nextOccurrences, toIcs } from './calendar'
import {
  gradeCheck,
  skillStatus,
  type CheckQuestion,
  type Machine,
  type NoticeEvent,
} from './wakatta'

const machine: Machine = {
  id: 'm1',
  name: { ja: '機械', en: 'Machine' },
  currentVersionId: 'v2',
  activeWorkerId: null,
}
const now = new Date('2026-11-23T00:00:00Z')
const daysAgo = (d: number) =>
  new Date(now.getTime() - d * 86_400_000).toISOString()
const signOff = (version: string, passedAt: string) => ({
  id: 's',
  workerId: 'w',
  machineId: 'm1',
  noteVersionId: version,
  passedAt,
})

describe('skillStatus', () => {
  it('is valid for the current version within 90 days', () => {
    expect(skillStatus(signOff('v2', daysAgo(10)), machine, now).status).toBe(
      'valid',
    )
  })
  it('is expiring in the last 14 days', () => {
    expect(skillStatus(signOff('v2', daysAgo(80)), machine, now).status).toBe(
      'expiring',
    )
  })
  it('expires after 90 days', () => {
    expect(skillStatus(signOff('v2', daysAgo(91)), machine, now).status).toBe(
      'expired',
    )
  })
  it('expires when a new Note Version is approved', () => {
    expect(skillStatus(signOff('v1', daysAgo(1)), machine, now).status).toBe(
      'expired',
    )
  })
  it('is none without a Sign-off', () => {
    expect(skillStatus(undefined, machine, now).status).toBe('none')
  })
})

describe('gradeCheck', () => {
  const q = (id: string, correctIndex: number): CheckQuestion => ({
    id,
    termId: null,
    prompt: { ja: '', en: '' },
    options: [],
    correctIndex,
  })
  it('passes only when every answer is right', () => {
    expect(gradeCheck([q('a', 0), q('b', 2)], [0, 2]).passed).toBe(true)
  })
  it('returns the wrong questions', () => {
    const r = gradeCheck([q('a', 0), q('b', 2)], [0, 1])
    expect(r.passed).toBe(false)
    expect(r.wrong.map((w) => w.id)).toEqual(['b'])
  })
})

describe('calendar', () => {
  const burnable: NoticeEvent = {
    id: 'e1',
    title: { ja: '可燃ごみ', en: '' },
    startsAt: '2026-11-02T08:00:00+09:00',
    rrule: 'FREQ=WEEKLY;BYDAY=MO,TH',
    confirmed: true,
  }
  it('expands Mon/Thu 8:00 in Japan time', () => {
    // 2026-11-23 is a Monday
    const dates = nextOccurrences(
      burnable,
      new Date('2026-11-22T12:00:00+09:00'),
      3,
    )
    expect(dates.map((d) => d.toISOString())).toEqual([
      '2026-11-22T23:00:00.000Z', // Mon 23 Nov 08:00 JST
      '2026-11-25T23:00:00.000Z', // Thu 26 Nov
      '2026-11-29T23:00:00.000Z', // Mon 30 Nov
    ])
  })
  it('rejects rules it cannot expand', () => {
    expect(isValidRrule('FREQ=WEEKLY;BYDAY=MO,TH')).toBe(true)
    expect(isValidRrule('FREQ=MONTHLY;BYMONTHDAY=1')).toBe(false)
    expect(isValidRrule('FREQ=WEEKLY;BYDAY=XX')).toBe(false)
  })
  it('keeps a one-off date only if it is still ahead', () => {
    const drill = {
      ...burnable,
      rrule: null,
      startsAt: '2026-11-01T09:00:00+09:00',
    }
    expect(nextOccurrences(drill, now, 3)).toEqual([])
  })
  it('writes an .ics with TZID and RRULE', () => {
    const ics = toIcs(
      [
        {
          id: 'e1',
          title: 'Burnable, bag',
          startsAt: burnable.startsAt,
          rrule: burnable.rrule,
        },
      ],
      now,
    )
    expect(ics).toContain('DTSTART;TZID=Asia/Tokyo:20261102T080000')
    expect(ics).toContain('RRULE:FREQ=WEEKLY;BYDAY=MO,TH')
    expect(ics).toContain('SUMMARY:Burnable\\, bag')
  })
})
