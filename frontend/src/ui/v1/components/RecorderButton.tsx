import { useEffect, useMemo } from 'react'
import type { useRecorder } from '@/lib/recorder'
import { m } from '@/i18n/paraglide/messages'
import { Button } from './ui/button'

/** Big push-to-record button with a timer; plays back the take. */
export function RecorderButton({
  recorder,
}: {
  recorder: ReturnType<typeof useRecorder>
}) {
  const { state, seconds, audio } = recorder
  const src = useMemo(
    () => (audio ? URL.createObjectURL(audio) : null),
    [audio],
  )
  useEffect(() => () => void (src && URL.revokeObjectURL(src)), [src])
  return (
    <div className="space-y-2" aria-live="polite">
      {state === 'recording' ? (
        <Button
          className="min-h-16 w-full bg-destructive text-lg"
          onClick={recorder.stop}
        >
          <span aria-hidden="true">{'■ '}</span>
          {m.rec_stop({ seconds })}
        </Button>
      ) : (
        <Button className="min-h-16 w-full text-lg" onClick={recorder.start}>
          <span aria-hidden="true">{'● '}</span>
          {audio ? m.rec_again() : m.rec_start()}
        </Button>
      )}
      {state === 'denied' && (
        <p className="text-destructive">{m.rec_denied()}</p>
      )}
      {state === 'unsupported' && (
        <p className="text-destructive">{m.rec_unsupported()}</p>
      )}
      {src && state === 'done' && (
        <audio controls src={src} className="w-full">
          <track kind="captions" />
        </audio>
      )}
    </div>
  )
}
