import type { ReactNode } from 'react'
import type { QueryClient } from '@tanstack/react-query'
import {
  HeadContent,
  Scripts,
  createRootRouteWithContext,
} from '@tanstack/react-router'
import { getLocale } from '@/i18n/paraglide/runtime'
import { KitScreen } from '@/shell/KitScreen'
import { kits, pickKitId } from '@/shell/ui-registry'
import baseCss from '@/theme/base.css?url'

export const Route = createRootRouteWithContext<{ queryClient: QueryClient }>()(
  {
    // Runs once per document load (on the server first), so ?ui= is read at startup only (A-05)
    loader: ({ location }) => ({
      kitId: pickKitId(location.searchStr, import.meta.env.VITE_UI_VERSION),
    }),
    shouldReload: false,
    head: ({ loaderData }) => ({
      meta: [
        { charSet: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { title: 'Wakatta?' },
      ],
      links: [
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        {
          rel: 'preconnect',
          href: 'https://fonts.gstatic.com',
          crossOrigin: 'anonymous',
        },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Manrope:wght@400;600;700;800&family=Noto+Sans+JP:wght@400;700&family=Hind+Siliguri:wght@400;500;600;700&display=swap',
        },
        { rel: 'stylesheet', href: baseCss },
        ...(loaderData
          ? [{ rel: 'stylesheet', href: kits[loaderData.kitId].css }]
          : []),
      ],
    }),
    shellComponent: RootDocument,
    // The kit renders 404 too, so it is translated and styled like every other screen (F-01)
    notFoundComponent: () => <KitScreen screen="notFound" />,
  },
)

function RootDocument({ children }: { children: ReactNode }) {
  return (
    <html lang={getLocale()}>
      <head>
        <HeadContent />
      </head>
      <body>
        {children}
        <Scripts />
      </body>
    </html>
  )
}
