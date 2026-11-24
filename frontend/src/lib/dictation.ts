import { useEffect, useRef, useState } from 'react'

// The Web Speech API is not in TypeScript's DOM types; only the parts used here.
interface SpeechResult {
  isFinal: boolean
  0: { transcript: string }
}
interface Recognition {
  lang: string
  continuous: boolean
  interimResults: boolean
  onresult:
    | ((e: { resultIndex: number; results: ArrayLike<SpeechResult> }) => void)
    | null
  onend: (() => void) | null
  onerror: (() => void) | null
  start(): void
  stop(): void
}
type RecognitionCtor = new () => Recognition

function recognitionCtor(): RecognitionCtor | undefined {
  if (typeof window === 'undefined') return undefined
  const w = window as unknown as Record<string, RecognitionCtor | undefined>
  return w.SpeechRecognition ?? w.webkitSpeechRecognition
}

/** "Dictate instead" (XREQ-004): the browser's own speech recognition fills a text box when the AI could not
 *  transcribe a recording or the phone is offline. `supported` is false where the API is missing (Firefox). */
// `onText` must not depend on render state (use a functional setState): it is captured when dictation starts.
export function useDictation(onText: (text: string) => void, lang = 'ja-JP') {
  const Ctor = recognitionCtor()
  const [listening, setListening] = useState(false)
  const rec = useRef<Recognition | null>(null)

  useEffect(() => () => rec.current?.stop(), [])

  return {
    supported: !!Ctor,
    listening,
    start: () => {
      if (!Ctor || rec.current) return
      const r = new Ctor()
      r.lang = lang
      r.continuous = true
      r.interimResults = false
      r.onresult = (e) => {
        for (let i = e.resultIndex; i < e.results.length; i++) {
          const res = e.results[i]
          if (res?.isFinal) onText(res[0].transcript)
        }
      }
      r.onend = r.onerror = () => {
        rec.current = null
        setListening(false)
      }
      rec.current = r
      r.start()
      setListening(true)
    },
    stop: () => rec.current?.stop(),
  }
}
