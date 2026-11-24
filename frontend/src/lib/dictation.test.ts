import { afterEach, describe, expect, it } from 'vitest'
import { act, renderHook } from '@testing-library/react'
import { useDictation } from './dictation'

type Handler = ((e: unknown) => void) | null

/** A stand-in for the browser's SpeechRecognition: the test says what it "heard". */
class FakeRecognition {
  static last: FakeRecognition | null = null
  lang = ''
  continuous = false
  interimResults = true
  onresult: Handler = null
  onend: Handler = null
  onerror: Handler = null
  start() {
    FakeRecognition.last = this
  }
  stop() {
    this.onend?.(null)
  }
  hear(transcript: string, isFinal = true) {
    const result = Object.assign([{ transcript }], { isFinal })
    this.onresult?.({ resultIndex: 0, results: [result] })
  }
}

const w = window as unknown as Record<string, unknown>

afterEach(() => {
  delete w.webkitSpeechRecognition
})

describe('useDictation', () => {
  it('is not offered where the browser has no speech recognition', () => {
    const { result } = renderHook(() => useDictation(() => {}))
    expect(result.current.supported).toBe(false)
  })

  it('listens in Japanese and passes on only final results', () => {
    w.webkitSpeechRecognition = FakeRecognition
    const heard: string[] = []
    const { result } = renderHook(() => useDictation((t) => heard.push(t)))

    act(() => result.current.start())
    expect(result.current.listening).toBe(true)
    expect(FakeRecognition.last?.lang).toBe('ja-JP')

    FakeRecognition.last?.hear('電源を', false)
    FakeRecognition.last?.hear('電源を切ってください')
    expect(heard).toEqual(['電源を切ってください'])

    act(() => result.current.stop())
    expect(result.current.listening).toBe(false)
  })
})
