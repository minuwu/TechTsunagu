import handler from '@tanstack/react-start/server-entry'
import { paraglideMiddleware } from './i18n/paraglide/server'

// Resolves the locale per request (cookie, then Accept-Language, then en) so SSR renders the right language
export default {
  fetch(request: Request): Promise<Response> {
    return paraglideMiddleware(request, () => handler.fetch(request))
  },
}
