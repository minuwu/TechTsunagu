import { m } from '@/i18n/paraglide/messages'
import { getLocale, setLocale, type Locale } from '@/i18n/paraglide/runtime'
import { Button } from './ui/button'

// Each label is written in its own language so anyone can find theirs (F-01)
const LABELS: Record<Locale, string> = { ja: '日本語', en: 'EN' }

export function LocaleSwitcher() {
  const current = getLocale()
  return (
    <div role="group" aria-label={m.language_label()} className="flex gap-1">
      {(Object.keys(LABELS) as Locale[]).map((locale) => (
        <Button
          key={locale}
          size="sm"
          variant={locale === current ? 'default' : 'ghost'}
          aria-pressed={locale === current}
          lang={locale}
          onClick={() => setLocale(locale)}
        >
          {LABELS[locale]}
        </Button>
      ))}
    </div>
  )
}
