import { defineConfig } from 'vitest/config'
import { paraglideVitePlugin } from '@inlang/paraglide-js'
import viteReact from '@vitejs/plugin-react'

// Separate from vite.config.ts: the Start and Nitro plugins build a server, which tests do not need
export default defineConfig({
  resolve: { tsconfigPaths: true },
  plugins: [
    paraglideVitePlugin({
      project: './project.inlang',
      outdir: './src/i18n/paraglide',
      emitTsDeclarations: true,
      strategy: ['cookie', 'preferredLanguage', 'baseLocale'],
    }),
    viteReact(),
  ],
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.{ts,tsx}'],
  },
})
