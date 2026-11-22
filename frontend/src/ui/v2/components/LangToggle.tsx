import type { Lang } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'
import { cn } from '@/ui/v2/lib/utils'

// Content language (instructions, notices), separate from the UI language. Labels in their own script.
const LABELS: Record<Lang, string> = {
  bn: 'বাংলা',
  en: 'English',
  ja: 'やさしい日本語',
}

export function LangToggle({
  value,
  onChange,
  options = ['bn', 'en', 'ja'],
}: {
  value: Lang
  onChange: (l: Lang) => void
  options?: Lang[]
}) {
  return (
    <div
      role="group"
      aria-label={m.content_language()}
      className="inline-flex max-w-full flex-wrap gap-1 rounded-lg bg-muted p-1"
    >
      {options.map((l) => (
        <button
          key={l}
          type="button"
          aria-pressed={l === value}
          lang={l}
          onClick={() => onChange(l)}
          className={cn(
            'min-h-11 cursor-pointer rounded-md px-3 font-bold transition-[background-color,color,box-shadow] duration-150',
            l === value
              ? 'bg-card text-primary shadow-[var(--shadow-sm)]'
              : 'text-muted-foreground hover:text-foreground',
          )}
        >
          {LABELS[l]}
        </button>
      ))}
    </div>
  )
}
