import type { Lang } from '@/domain/wakatta'
import { m } from '@/i18n/paraglide/messages'
import { Button } from './ui/button'

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
      className="flex flex-wrap gap-1"
    >
      {options.map((l) => (
        <Button
          key={l}
          size="sm"
          variant={l === value ? 'default' : 'outline'}
          aria-pressed={l === value}
          lang={l}
          onClick={() => onChange(l)}
        >
          {LABELS[l]}
        </Button>
      ))}
    </div>
  )
}
