import type { NoticeEvent } from './wakatta'

// Japan has no daylight saving, so a fixed +9h offset is exact.
const JST_MS = 9 * 3_600_000
const DAY_MS = 86_400_000
const DAYS = ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA']

function byDay(rrule: string): number[] | null {
  const parts = Object.fromEntries(
    rrule.split(';').map((p) => p.split('=') as [string, string]),
  )
  if (parts.FREQ !== 'WEEKLY' || !parts.BYDAY) return null
  const days = parts.BYDAY.split(',').map((d: string) => DAYS.indexOf(d))
  return days.includes(-1) ? null : days
}

/** True when the rule is one we can expand. Unsupported rules must not publish. */
export const isValidRrule = (rrule: string) => byDay(rrule) !== null

/**
 * Next `count` occurrences at or after `from`.
 * shortcut: only one-off dates and FREQ=WEEKLY;BYDAY=… rules; add the `rrule` package when Notices need more.
 */
export function nextOccurrences(
  event: NoticeEvent,
  from: Date,
  count: number,
): Date[] {
  const start = new Date(event.startsAt)
  if (!event.rrule) return start >= from ? [start] : []
  const days = byDay(event.rrule)
  if (!days) return []
  // Time of day in Japan, kept from the first occurrence
  const timeOfDay = (start.getTime() + JST_MS) % DAY_MS
  const base = Math.max(from.getTime(), start.getTime())
  const firstDay = Math.floor((base + JST_MS) / DAY_MS) * DAY_MS - JST_MS
  const out: Date[] = []
  for (let d = 0; out.length < count && d < 7 * (count + 1); d++) {
    const dayStart = firstDay + d * DAY_MS
    const weekday = new Date(dayStart + JST_MS).getUTCDay()
    const at = new Date(dayStart + timeOfDay)
    if (days.includes(weekday) && at.getTime() >= base) out.push(at)
  }
  return out
}

const icsDate = (iso: string) => {
  // Local Japan time, written with TZID so every calendar app shows 8:00 as 8:00
  const local = new Date(Date.parse(iso) + JST_MS).toISOString()
  return local.slice(0, 19).replace(/[-:]/g, '')
}

const icsText = (s: string) =>
  s
    .replace(/\\/g, '\\\\')
    .replace(/;/g, '\\;')
    .replace(/,/g, '\\,')
    .replace(/\n/g, '\\n')

const utf8 = new TextEncoder()

/** RFC 5545 §3.1: lines longer than 75 octets continue on a line starting with a space, never splitting a character. */
export function foldIcsLine(line: string): string {
  const out: string[] = []
  let current = ''
  let octets = 0
  for (const ch of line) {
    const size = utf8.encode(ch).length
    // A continuation line's leading space counts toward its 75 octets
    if (octets + size > (out.length === 0 ? 75 : 74)) {
      out.push(current)
      current = ''
      octets = 0
    }
    current += ch
    octets += size
  }
  out.push(current)
  return out.join('\r\n ')
}

/** One .ics file for all events; titles in the Worker's language. */
export function toIcs(
  events: {
    id: string
    title: string
    startsAt: string
    rrule: string | null
  }[],
  stamp: Date,
): string {
  const dtstamp = stamp.toISOString().replace(/[-:]/g, '').slice(0, 15) + 'Z'
  const lines = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Wakatta//Life//EN',
    'CALSCALE:GREGORIAN',
    // Japan has no daylight saving: one STANDARD block at +0900
    'BEGIN:VTIMEZONE',
    'TZID:Asia/Tokyo',
    'BEGIN:STANDARD',
    'DTSTART:19700101T000000',
    'TZOFFSETFROM:+0900',
    'TZOFFSETTO:+0900',
    'TZNAME:JST',
    'END:STANDARD',
    'END:VTIMEZONE',
    ...events.flatMap((e) => [
      'BEGIN:VEVENT',
      `UID:${e.id}@wakatta`,
      `DTSTAMP:${dtstamp}`,
      `DTSTART;TZID=Asia/Tokyo:${icsDate(e.startsAt)}`,
      'DURATION:PT30M',
      ...(e.rrule ? [`RRULE:${e.rrule}`] : []),
      `SUMMARY:${icsText(e.title)}`,
      'BEGIN:VALARM',
      'TRIGGER:-PT12H',
      'ACTION:DISPLAY',
      `DESCRIPTION:${icsText(e.title)}`,
      'END:VALARM',
      'END:VEVENT',
    ]),
    'END:VCALENDAR',
  ]
  return lines.map(foldIcsLine).join('\r\n') + '\r\n'
}
