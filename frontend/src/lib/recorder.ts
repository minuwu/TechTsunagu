import { useRef, useState } from 'react'

export type RecorderState =
  'idle' | 'recording' | 'done' | 'denied' | 'unsupported'

/** Microphone capture with MediaRecorder; stops itself after `maxSeconds`. */
export function useRecorder(maxSeconds = 60) {
  const [state, setState] = useState<RecorderState>('idle')
  const [audio, setAudio] = useState<Blob | null>(null)
  const [seconds, setSeconds] = useState(0)
  const rec = useRef<MediaRecorder | null>(null)
  const timer = useRef<ReturnType<typeof setInterval> | null>(null)

  const stop = () => {
    if (timer.current) clearInterval(timer.current)
    if (rec.current?.state === 'recording') rec.current.stop()
  }

  const start = async () => {
    if (typeof MediaRecorder === 'undefined' || !navigator.mediaDevices) {
      setState('unsupported')
      return
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      const chunks: Blob[] = []
      const r = new MediaRecorder(stream)
      r.ondataavailable = (e) => chunks.push(e.data)
      r.onstop = () => {
        stream.getTracks().forEach((t) => t.stop())
        setAudio(new Blob(chunks, { type: r.mimeType }))
        setState('done')
      }
      rec.current = r
      r.start()
      setSeconds(0)
      setState('recording')
      timer.current = setInterval(() => {
        setSeconds((s) => {
          if (s + 1 >= maxSeconds) stop()
          return s + 1
        })
      }, 1000)
    } catch {
      setState('denied')
    }
  }

  const reset = () => {
    stop()
    setAudio(null)
    setSeconds(0)
    setState('idle')
  }

  return { state, audio, seconds, start, stop, reset }
}
