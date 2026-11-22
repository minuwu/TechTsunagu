import { useState } from 'react'
import { useParams } from '@tanstack/react-router'
import { nextOccurrences } from '@/domain/calendar'
import { tokyoDate, type MeetupKind, type Tri } from '@/domain/wakatta'
import { useAction } from '@/lib/action'
import { useListQuery, useList } from '@/services/queries/wakatta'
import { SENPAI_REPLIES, logMeetup, senpaiReply } from '@/services/wakatta'

/** Senpai's private link: their Worker, forwarded Asks, shared Events, Meet-ups. */
export function useSenpai() {
  const { token = '' } = useParams({ strict: false })
  const pairs = useListQuery('pairs')
  const workers = useList('workers')
  const asks = useList('asks', 5000)
  const notices = useList('notices')
  const meetups = useList('meetups', 5000)
  const pair = pairs.data?.find((p) => p.token === token)
  const worker = workers.find((w) => w.id === pair?.workerId)
  const reply = useAction((a: { askId: string; reply: Tri }) =>
    senpaiReply(token, a.askId, a.reply),
  )
  const [drafts, setDrafts] = useState<Record<string, string>>({})
  const [kind, setKind] = useState<MeetupKind>('coffee')
  const [note, setNote] = useState('')
  const log = useAction(logMeetup)
  const now = new Date()

  return {
    loading: pairs.isPending,
    notFound: pairs.isSuccess && !pair,
    pair,
    worker,
    asks: asks.filter((a) => a.workerId === pair?.workerId && a.forwarded),
    quickReplies: SENPAI_REPLIES,
    drafts,
    setDraft: (askId: string, v: string) =>
      setDrafts((d) => ({ ...d, [askId]: v })),
    sendQuick: (askId: string, r: Tri) => reply.mutate({ askId, reply: r }),
    // shortcut: free text is not translated in the demo; Gemini translate-only prompt does it later
    sendFree: (askId: string) => {
      const ja = drafts[askId]?.trim()
      if (ja) reply.mutate({ askId, reply: { ja, bn: ja, en: ja } })
    },
    events: notices
      .filter((n) => n.status === 'published')
      .flatMap((n) =>
        n.events
          .filter((e) => e.confirmed)
          .flatMap((e) =>
            nextOccurrences(e, now, 1).map((at) => ({
              key: e.id,
              title: e.title,
              at,
            })),
          ),
      )
      .sort((a, b) => a.at.getTime() - b.at.getTime()),
    meetups: meetups.filter((m) => m.pairId === pair?.id),
    kind,
    setKind,
    note,
    setNote,
    logging: log.isPending,
    logMeetup: async () => {
      if (!pair) return
      await log.mutateAsync({
        pairId: pair.id,
        kind,
        date: tokyoDate(now),
        note: note.trim(),
        loggedBy: 'senpai',
      })
      setNote('')
    },
  }
}
