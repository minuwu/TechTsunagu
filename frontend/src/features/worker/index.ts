import { useState } from 'react'
import { nextOccurrences, toIcs } from '@/domain/calendar'
import {
  latestSignOff,
  pick,
  skillStatus,
  tokyoDate,
  type Lang,
  type MeetupKind,
  type NearMissReport,
  type Tri,
} from '@/domain/wakatta'
import { useAction } from '@/lib/action'
import { downloadText, imageToDataUrl } from '@/lib/files'
import { useRecorder } from '@/lib/recorder'
import { useListQuery, useList } from '@/services/queries/wakatta'
import {
  askQuestion,
  forwardAsk,
  logMeetup,
  openTarget,
  reportNearMiss,
  submitCheck,
  type CheckResult,
} from '@/services/wakatta'

function useWorker(workerId: string | null) {
  const workers = useListQuery('workers')
  const worker = workers.data?.find((w) => w.id === workerId)
  const [lang, setLang] = useState<Lang | null>(null)
  return {
    loading: workers.isPending,
    worker,
    lang: lang ?? worker?.language ?? 'bn',
    setLang,
  }
}

/** One-question Check for a Briefing or Notice. */
function useOneQuestion(workerId: string | null, kind: 'briefing' | 'notice') {
  const [answers, setAnswers] = useState<Record<string, number>>({})
  const [results, setResults] = useState<Record<string, CheckResult>>({})
  const check = useAction(submitCheck)
  return {
    answers,
    results,
    answer: (id: string, option: number) =>
      setAnswers((a) => ({ ...a, [id]: option })),
    submit: async (id: string, lang: Lang) => {
      if (!workerId || answers[id] === undefined) return
      const r = await check.mutateAsync({
        workerId,
        target: { kind, id },
        language: lang,
        answers: [answers[id]],
      })
      setResults((x) => ({ ...x, [id]: r }))
    },
    retry: (id: string) => {
      const drop = <T>(o: Record<string, T>) =>
        Object.fromEntries(Object.entries(o).filter(([k]) => k !== id))
      setResults(drop)
      setAnswers(drop)
    },
    submitting: check.isPending,
  }
}

/** Work tab: Skill Passport, today's Briefing, Near-miss Report. */
export function useWorkerWork(workerId: string | null) {
  const base = useWorker(workerId)
  const machines = useList('machines')
  const signOffs = useList('signOffs')
  const assignments = useList('assignments')
  const briefings = useList('briefings')
  const receipts = useList('receipts')

  const now = new Date()
  const today = tokyoDate(now)
  const passport = machines.map((m) => {
    const s = latestSignOff(signOffs, workerId ?? '', m.id)
    return {
      machine: m,
      passedAt: s?.passedAt ?? null,
      ...skillStatus(s, m, now),
    }
  })
  const myShift = assignments
    .filter((a) => a.workerId === workerId && a.date === today)
    .map((a) => ({ ...a, machine: machines.find((m) => m.id === a.machineId) }))

  const briefing = briefings[0]
  const receipt = receipts.find(
    (r) => r.targetId === briefing?.id && r.workerId === workerId,
  )
  const open = useAction((id: string) => openTarget(id, workerId ?? ''))
  const briefingCheck = useOneQuestion(workerId, 'briefing')

  // Near-miss form
  const recorder = useRecorder(60)
  const [nmMachine, setNmMachine] = useState('')
  const [nmText, setNmText] = useState('')
  const [nmPhoto, setNmPhoto] = useState<string | null>(null)
  const [nmSent, setNmSent] = useState<NearMissReport | null>(null)
  const report = useAction(reportNearMiss)

  return {
    ...base,
    passport,
    myShift,
    briefing,
    briefingOpened: !!receipt,
    briefingPassed: receipt?.passed === true,
    openBriefing: () => briefing && !receipt && open.mutate(briefing.id),
    briefingCheck,
    nearMiss: {
      machineId: nmMachine || machines[0]?.id || '',
      setMachineId: setNmMachine,
      text: nmText,
      setText: setNmText,
      photo: nmPhoto,
      setPhotoFile: async (file: File | undefined) =>
        setNmPhoto(file ? await imageToDataUrl(file) : null),
      recorder,
      canSend: !!(recorder.audio || nmText.trim()),
      sending: report.isPending,
      sent: nmSent,
      send: async () => {
        if (!workerId) return
        const r = await report.mutateAsync({
          workerId,
          machineId: nmMachine || machines[0]?.id || '',
          text: nmText.trim(),
          audio: recorder.audio,
          photo: nmPhoto,
        })
        setNmSent(r)
        setNmText('')
        setNmPhoto(null)
        recorder.reset()
      },
      again: () => setNmSent(null),
    },
    machines,
  }
}

export interface UpcomingEvent {
  key: string
  noticeId: string
  title: Tri
  at: Date
  day: 'today' | 'tomorrow' | 'later'
}

/** Life tab: Notices, Events, Ask, Senpai. */
export function useWorkerLife(workerId: string | null) {
  const base = useWorker(workerId)
  const notices = useList('notices', 5000)
  const receipts = useList('receipts')
  const posters = useList('posters')
  const asks = useList('asks', 5000)
  const pairs = useList('pairs')
  const meetups = useList('meetups')

  const now = new Date()
  const today = tokyoDate(now)
  const tomorrow = tokyoDate(new Date(now.getTime() + 86_400_000))
  const published = notices.filter((n) => n.status === 'published')

  const events: UpcomingEvent[] = published
    .flatMap((n) =>
      n.events
        .filter((e) => e.confirmed)
        .flatMap((e) =>
          nextOccurrences(e, now, 2).map((at) => ({
            key: `${e.id}-${at.getTime()}`,
            noticeId: n.id,
            title: e.title,
            at,
            day: (tokyoDate(at) === today
              ? 'today'
              : tokyoDate(at) === tomorrow
                ? 'tomorrow'
                : 'later') as UpcomingEvent['day'],
          })),
        ),
    )
    .sort((a, b) => a.at.getTime() - b.at.getTime())

  const [expanded, setExpanded] = useState<string | null>(null)
  const open = useAction((id: string) => openTarget(id, workerId ?? ''))
  const noticeCheck = useOneQuestion(workerId, 'notice')

  const recorder = useRecorder(30)
  const [askText, setAskText] = useState('')
  const ask = useAction(askQuestion)
  const forward = useAction(forwardAsk)

  const pair = pairs.find((p) => p.workerId === workerId)
  const [meetKind, setMeetKind] = useState<MeetupKind>('coffee')
  const [meetNote, setMeetNote] = useState('')
  const log = useAction(logMeetup)

  return {
    ...base,
    events,
    downloadCalendar: () => {
      const all = published.flatMap((n) => n.events.filter((e) => e.confirmed))
      const ics = toIcs(
        all.map((e) => ({ ...e, title: pick(e.title, base.lang) })),
        now,
      )
      downloadText('wakatta-life.ics', ics, 'text/calendar')
    },
    notices: published.map((n) => {
      const r = receipts.find(
        (x) => x.targetId === n.id && x.workerId === workerId,
      )
      return {
        notice: n,
        poster: posters.find((p) => p.id === n.posterId)?.name ?? '',
        opened: !!r,
        understood: r?.passed === true,
      }
    }),
    expanded,
    toggle: (id: string) => {
      setExpanded((cur) => (cur === id ? null : id))
      if (!receipts.some((x) => x.targetId === id && x.workerId === workerId))
        open.mutate(id)
    },
    noticeCheck,
    asks: asks.filter((a) => a.workerId === workerId),
    askText,
    setAskText,
    recorder,
    asking: ask.isPending,
    canAsk: !!(askText.trim() || recorder.audio),
    ask: async () => {
      if (!workerId) return
      await ask.mutateAsync({
        workerId,
        text: askText.trim(),
        audio: recorder.audio,
      })
      setAskText('')
      recorder.reset()
    },
    forward: (id: string) => forward.mutate(id),
    pair,
    meetups: meetups.filter((m) => m.pairId === pair?.id),
    meetKind,
    setMeetKind,
    meetNote,
    setMeetNote,
    logging: log.isPending,
    logMeetup: async () => {
      if (!pair) return
      await log.mutateAsync({
        pairId: pair.id,
        kind: meetKind,
        date: today,
        note: meetNote.trim(),
        loggedBy: 'worker',
      })
      setMeetNote('')
    },
  }
}
