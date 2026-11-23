import { useCallback, useEffect, useRef, useState } from 'react'
import {
  doorDone,
  parseProgress,
  passportEarned,
  PROGRESS_KEY,
  REFLEX_MS,
  type Door,
  type Progress,
} from '@/domain/course'
import { buzzer } from '@/lib/audio'
import { usePersisted } from '@/lib/persisted'

/** The worker's course progress. Offline-first in localStorage (spec §5.1); the real API comes with the XREQ. */
export function useCourse(id: string) {
  const [raw, set] = usePersisted(`${PROGRESS_KEY}:${id}`, null)
  const progress = parseProgress(raw)
  const save = (p: Progress) => set(JSON.stringify(p))
  return {
    progress,
    done: (d: Door) => doorDone(progress, d),
    earned: passportEarned(progress),
    finish: (d: 'read' | 'watch' | 'inspect') =>
      save({ ...progress, [d]: true }),
    passDrill: (id: string) =>
      progress.drills.includes(id)
        ? undefined
        : save({ ...progress, drills: [...progress.drills, id] }),
    reset: () => set(null),
    saveQuiz: (score: number) =>
      score > progress.quiz ? save({ ...progress, quiz: score }) : undefined,
  }
}

/** Quiz: one question at a time; the answer locks, the score counts first tries. */
export function useQuiz(items: { answer: number }[]) {
  const [i, setI] = useState(0)
  const [picked, setPicked] = useState<number | null>(null)
  const [score, setScore] = useState(0)
  const finished = i >= items.length
  return {
    i,
    picked,
    score,
    finished,
    answer: (n: number) => {
      if (picked !== null || finished) return
      setPicked(n)
      if (n === items[i]!.answer) setScore((s) => s + 1)
    },
    next: () => {
      setPicked(null)
      setI((x) => x + 1)
    },
    restart: () => {
      setI(0)
      setPicked(null)
      setScore(0)
    },
  }
}

const BEAT_MS = 2600

/** Door 2: steps through karaoke lines; the speed toggle slows both the beat and the voice. */
export function useReel(lines: string[], onEnd: () => void) {
  const [i, setI] = useState(0)
  const [playing, setPlaying] = useState(false)
  const [slow, setSlow] = useState(false)
  const end = useRef(onEnd)
  useEffect(() => {
    end.current = onEnd
  })
  const last = lines.length - 1
  const rate = slow ? 0.75 : 1

  useEffect(() => {
    if (!playing) return
    if (typeof speechSynthesis !== 'undefined' && lines[i]) {
      speechSynthesis.cancel()
      speechSynthesis.speak(
        Object.assign(new SpeechSynthesisUtterance(lines[i]), {
          lang: 'ja-JP',
          rate,
        }),
      )
    }
    const timer = setTimeout(() => {
      if (i < last) setI(i + 1)
      else {
        setPlaying(false)
        end.current()
      }
    }, BEAT_MS / rate)
    return () => clearTimeout(timer)
  }, [playing, i, last, rate, lines])

  useEffect(
    () => () => {
      if (typeof speechSynthesis !== 'undefined') speechSynthesis.cancel()
    },
    [],
  )

  return {
    i,
    playing,
    slow,
    toggleSlow: () => setSlow((s) => !s),
    toggle: () => setPlaying((p) => !p),
    replay: () => {
      setI(0)
      setPlaying(true)
    },
  }
}

export type ReflexState = 'idle' | 'wait' | 'go' | 'early' | 'done'

/** E-stop drill: a random wait, an alarm, then the time to hit the button. */
export function useReflex() {
  const [state, setState] = useState<ReflexState>('idle')
  const [ms, setMs] = useState(0)
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined)
  const t0 = useRef(0)
  useEffect(() => () => clearTimeout(timer.current), [])

  return {
    state,
    ms,
    passed: state === 'done' && ms < REFLEX_MS,
    start: useCallback(() => {
      setState('wait')
      timer.current = setTimeout(
        () => {
          t0.current = performance.now()
          buzzer()
          setState('go')
        },
        1000 + Math.random() * 2000,
      )
    }, []),
    hit: () => {
      if (state === 'wait') {
        clearTimeout(timer.current)
        setState('early')
      } else if (state === 'go') {
        setMs(Math.round(performance.now() - t0.current))
        setState('done')
      }
    },
  }
}

/** Hazard-spotting drill: tap hazards before the clock runs out. */
export function useSpot(secs: number, hazardCount: number, need: number) {
  const [running, setRunning] = useState(false)
  const [ended, setEnded] = useState(false)
  const [left, setLeft] = useState(secs)
  const [taps, setTaps] = useState<Record<number, boolean>>({}) // index → was a hazard
  const found = Object.values(taps).filter(Boolean).length

  useEffect(() => {
    if (!running) return
    const timer = setTimeout(() => {
      setLeft((s) => s - 1)
      if (left <= 1) {
        setRunning(false)
        setEnded(true)
      }
    }, 1000)
    return () => clearTimeout(timer)
  }, [running, left])

  return {
    running,
    ended,
    left,
    taps,
    found,
    passed: ended && found >= need,
    start: () => {
      setLeft(secs)
      setTaps({})
      setEnded(false)
      setRunning(true)
    },
    tap: (index: number, hazard: boolean) => {
      if (!running || taps[index] !== undefined) return
      setTaps((t) => ({ ...t, [index]: hazard }))
      if (hazard && found + 1 >= hazardCount) {
        setRunning(false)
        setEnded(true)
      }
    },
  }
}
