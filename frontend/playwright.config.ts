import { defineConfig, devices } from '@playwright/test'

// Runs against the production build, so the smoke test sees what judges see (S-30)
export default defineConfig({
  testDir: './tests/e2e',
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? 'github' : 'list',
  // Reduced motion by default: axe and screenshots see settled UI, never a half-faded reveal.
  // Tests that check the motion itself opt back in with test.use({ reducedMotion: 'no-preference' }).
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    reducedMotion: 'reduce',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run build && npm start',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 180_000,
    env: { PORT: '3000', VITE_USE_MOCK: 'true' },
  },
})
