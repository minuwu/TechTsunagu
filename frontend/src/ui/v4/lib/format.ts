import type { Lang } from '@/domain/wakatta'
import { getLocale } from '@/i18n/paraglide/runtime'

/** The UI locale as a content language. */
export const uiLang = (): Lang => getLocale() as Lang

const intl: Record<Lang, string> = { ja: 'ja-JP', en: 'en-GB', bn: 'bn-BD' }

/** Date (and optional time) in Japan time, in the given language. */
export function fmtDate(
  value: string | Date,
  lang: Lang = uiLang(),
  withTime = false,
) {
  return new Intl.DateTimeFormat(intl[lang], {
    timeZone: 'Asia/Tokyo',
    month: 'short',
    day: 'numeric',
    weekday: 'short',
    ...(withTime ? { hour: '2-digit', minute: '2-digit' } : {}),
  }).format(typeof value === 'string' ? new Date(value) : value)
}
