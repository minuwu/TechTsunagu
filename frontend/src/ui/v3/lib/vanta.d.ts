// Vanta 0.5.24 ships no types. Only what the kit calls is declared (vanta README, "More options").
declare module 'vanta/dist/vanta.waves.min' {
  export interface VantaEffect {
    setOptions(options: Record<string, unknown>): void
    resize(): void
    destroy(): void
  }
  type Waves = (
    options: { el: HTMLElement; THREE: unknown } & Record<string, unknown>,
  ) => VantaEffect
  // The UMD build's export is webpack's `{ default: WAVES }`, so a bundler may hand over either shape
  const WAVES: Waves | { default: Waves }
  export default WAVES
}

// three r134 (the release Vanta targets) has no bundled types; the kit only hands the namespace to Vanta.
declare module 'three' {
  export const REVISION: string
}
