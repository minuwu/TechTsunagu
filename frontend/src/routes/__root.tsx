import type { ReactNode } from 'react'
import type { QueryClient } from '@tanstack/react-query'
import {
  HeadContent,
  Scripts,
  createRootRouteWithContext,
} from '@tanstack/react-router'
import { m } from '@/i18n/paraglide/messages'
import { getLocale } from '@/i18n/paraglide/runtime'
import { KitScreen } from '@/shell/KitScreen'
import { kits, pickKitId } from '@/shell/ui-registry'
import baseCss from '@/theme/base.css?url'

// Share cards need an absolute image URL; the same public base the QR stickers use
const SITE = import.meta.env.VITE_PUBLIC_URL || 'http://localhost:3000'

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
        { name: 'description', content: m.lp_lead({}, { locale: 'ja' }) },
        // The browser bar matches the kit's harbour-navy header
        { name: 'theme-color', content: '#16223b' },
        { property: 'og:type', content: 'website' },
        { property: 'og:site_name', content: 'Wakatta?' },
        {
          property: 'og:title',
          content: `Wakatta? ${m.lp_h1({}, { locale: 'ja' })}`,
        },
        { property: 'og:description', content: m.lp_h1({}, { locale: 'en' }) },
        {
          property: 'og:image',
          content: `${SITE}/brand/images/social/og-default-1200.png`,
        },
        { property: 'og:image:width', content: '1200' },
        { property: 'og:image:height', content: '630' },
        { name: 'twitter:card', content: 'summary_large_image' },
      ],
      links: [
        { rel: 'icon', href: '/brand/favicon/favicon.ico', sizes: '32x32' },
        {
          rel: 'icon',
          href: '/brand/favicon/favicon.svg',
          type: 'image/svg+xml',
        },
        {
          rel: 'apple-touch-icon',
          href: '/brand/favicon/apple-touch-icon-180.png',
        },
        { rel: 'manifest', href: '/brand/favicon/site.webmanifest' },
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        {
          rel: 'preconnect',
          href: 'https://fonts.gstatic.com',
          crossOrigin: 'anonymous',
        },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Manrope:wght@400;600;700;800&family=Noto+Sans+JP:wght@400;700&display=swap',
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
