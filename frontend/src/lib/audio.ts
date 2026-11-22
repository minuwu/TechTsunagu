// Procedural sound, 0 KB of audio files (spec §6.4). Browser only; never call during SSR.
let ctx: AudioContext | null = null

/** A decaying sine: a clear chime at 1200 Hz, a dull thud at 320 Hz. */
export function tone(hz: number, secs: number, gain = 0.25) {
  ctx ??= new AudioContext()
  void ctx.resume() // browsers start suspended until a user gesture
  const osc = ctx.createOscillator()
  const g = ctx.createGain()
  osc.frequency.value = hz
  g.gain.setValueAtTime(gain, ctx.currentTime)
  g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + secs)
  osc.connect(g).connect(ctx.destination)
  osc.start()
  osc.stop(ctx.currentTime + secs)
}

export const buzzer = () => tone(110, 0.5, 0.3)

/** A soft two-note ping for taps (procedural, no files). */
export const ping = (up = true) => {
  tone(up ? 523 : 392, 0.09, 0.06)
  setTimeout(() => tone(up ? 784 : 330, 0.12, 0.05), 60)
}
