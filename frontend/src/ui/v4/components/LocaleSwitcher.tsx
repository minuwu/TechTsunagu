import { m } from '@/i18n/paraglide/messages'
import { getLocale, setLocale, type Locale } from '@/i18n/paraglide/runtime'
import { cn } from '@/ui/v4/lib/utils'

// Each label is written in its own language so anyone can find theirs (F-01)
const LABELS: Record<Locale, string> = { ja: '日本語', en: 'EN', bn: 'বাংলা' }

/** UI language: three square segments, the current one filled black. */
export function LocaleSwitcher() {
  const current = getLocale()
  return (
    <div
      role="group"
      aria-label={m.language_label()}
      className="flex shrink-0 border border-foreground"
    >
      {(Object.keys(LABELS) as Locale[]).map((locale) => (
        <button
          key={locale}
          type="button"
          aria-pressed={locale === current}
          lang={locale}
          onClick={() => setLocale(locale)}
          className={cn(
            'min-h-11 min-w-11 cursor-pointer px-2.5 text-sm font-bold transition-colors duration-300 sm:px-3',
            locale === current
              ? 'bg-foreground text-background'
              : 'text-foreground hover:bg-primary hover:text-primary-foreground',
          )}
        >
          {LABELS[locale]}
        </button>
      ))}
    </div>
  )
}
