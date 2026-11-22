import { m } from '@/i18n/paraglide/messages'
import { getLocale, setLocale, type Locale } from '@/i18n/paraglide/runtime'
import { cn } from '@/ui/v2/lib/utils'

// Each label is written in its own language so anyone can find theirs (F-01)
const LABELS: Record<Locale, string> = { ja: '日本語', en: 'EN', bn: 'বাংলা' }

/** UI language on the navy header: a three-way segmented switch. */
export function LocaleSwitcher() {
  const current = getLocale()
  return (
    <div
      role="group"
      aria-label={m.language_label()}
      className="flex shrink-0 rounded-lg bg-[var(--shell-raised)] p-1"
    >
      {(Object.keys(LABELS) as Locale[]).map((locale) => (
        <button
          key={locale}
          type="button"
          aria-pressed={locale === current}
          lang={locale}
          onClick={() => setLocale(locale)}
          className={cn(
            'min-h-11 min-w-11 cursor-pointer rounded-md px-2.5 text-sm font-bold transition-colors duration-150 focus-visible:outline-[var(--shell-foreground)] sm:px-3',
            locale === current
              ? 'bg-[var(--shell-foreground)] text-[var(--shell)] shadow-[var(--shadow-sm)]'
              : 'text-[var(--shell-muted)] hover:text-[var(--shell-foreground)]',
          )}
        >
          {LABELS[locale]}
        </button>
      ))}
    </div>
  )
}
