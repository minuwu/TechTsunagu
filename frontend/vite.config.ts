import { defineConfig } from 'vite'
import { paraglideVitePlugin } from '@inlang/paraglide-js'
import { tanstackStart } from '@tanstack/react-start/plugin/vite'
import viteReact from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { nitro } from 'nitro/vite'

// `vite build --mode static` (npm run build:static): the whole demo pre-rendered to plain files in
// .output/public for a static host (Azure Static Web Apps); it runs on the in-browser demo data.
// The demo store's own ids (src/services/mocks/seed.ts)
const MACHINES = ['m-plasma', 'm-press', 'm-crane', 'm-weld']
const WORKERS = ['w-rahim', 'w-sadia', 'w-tanvir', 'w-tanaka']
const DEMO_PAGES = [
  ...MACHINES.flatMap((id) => [
    `/m/${id}`,
    `/panel/${id}`,
    `/foreman/record/${id}`,
    `/foreman/library/${id}`,
  ]),
  ...WORKERS.flatMap((id) => [`/me/${id}`, `/me/${id}/life`]),
  '/senpai/demo-tanaka-7f3a9c',
  '/senpai/demo-mori-2b8e41',
]

export default defineConfig(({ mode }) => ({
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
    tanstackStart(
      mode === 'static'
        ? {
            prerender: { enabled: true, crawlLinks: true, failOnError: true },
            // Pages with ids: the demo hub lists them in the browser only, so the crawler cannot find them
            pages: DEMO_PAGES.map((path) => ({ path })),
          }
        : {},
    ),
    viteReact(),
  ],
}))
