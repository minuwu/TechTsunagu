import { defineConfig } from 'vite'
import { paraglideVitePlugin } from '@inlang/paraglide-js'
import { tanstackStart } from '@tanstack/react-start/plugin/vite'
import viteReact from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { nitro } from 'nitro/vite'

export default defineConfig({
  resolve: { tsconfigPaths: true },
  plugins: [
    paraglideVitePlugin({
      project: './project.inlang',
      outdir: './src/i18n/paraglide',
      emitTsDeclarations: true,
      // Saved choice, then the browser language, then English (F-01)
      strategy: ['cookie', 'preferredLanguage', 'baseLocale'],
    }),
    nitro(),
    tailwindcss(),
    tanstackStart(),
    viteReact(),
  ],
})
