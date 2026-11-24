import { useState } from 'react'
import { useQueries } from '@tanstack/react-query'
import { isValidRrule, upcomingDates } from '@/domain/calendar'
import type { Notice } from '@/domain/wakatta'
import { useAction } from '@/lib/action'
import { useDictation } from '@/lib/dictation'
import { imageToDataUrl } from '@/lib/files'
import { useRecorder } from '@/lib/recorder'
import { USE_MOCK } from '@/services/api'
import { useList } from '@/services/queries/wakatta'
import {
  confirmEvent,
  createNoticeDraft,
  noticeReach,
  publishNotice,
} from '@/services/wakatta'

export type PostStep = 'source' | 'processing' | 'review' | 'done'
export type SourceKind = Notice['sourceKind']

/** Community Poster: post a Notice from text, voice or a flyer photo; confirm dates; see reach. */
export function useCommunity() {
  const posters = useList('posters')
  const notices = useList('notices', 5000)
  const receipts = useList('receipts', 5000)
  const workers = useList('workers')
  const [posterId, setPosterId] = useState('')
  const [kind, setKind] = useState<SourceKind>('photo')
  const [text, setText] = useState('')
  const [photo, setPhoto] = useState<string | null>(null)
  const [step, setStep] = useState<PostStep>('source')
  const [draftId, setDraftId] = useState<string | null>(null)
  const recorder = useRecorder(60)
  // "Dictate instead" (XREQ-004): after a voice draft the AI could not make, or offline
  const [offerDictation, setOfferDictation] = useState(false)
  const dictation = useDictation((heard) =>
    setText((t) => (t ? `${t}${heard}` : heard)),
  )
  const create = useAction(createNoticeDraft)
  const confirm = useAction((a: { eventId: string; confirmed: boolean }) =>
    confirmEvent(draftId ?? '', a.eventId, a.confirmed),
  )
  const publish = useAction(publishNotice)

  // Ticks show at once; the store catches up on the next refetch
  const [ticks, setTicks] = useState<Record<string, boolean>>({})
  const poster = posterId || posters[0]?.id || ''
  const stored = notices.find((n) => n.id === draftId) ?? null
  const draft = stored && {
    ...stored,
    events: stored.events.map((e) => ({
      ...e,
      confirmed: ticks[e.id] ?? e.confirmed,
    })),
  }
  const now = new Date()
  const published = notices.filter((n) => n.status === 'published')
  // Real API: the Poster sees counts per Notice, never who (GET /api/notices/{id}/reach)
  const liveReach = useQueries({
    queries: published.map((n) => ({
      queryKey: ['wakatta', 'reach', n.id],
      queryFn: () => noticeReach(n.id),
      enabled: !USE_MOCK,
      refetchInterval: 5000,
    })),
  })

  return {
    posters,
    posterId: poster,
    setPosterId,
    kind,
    setKind,
    text,
    setText,
    photo,
    setPhotoFile: async (file: File | undefined) =>
      setPhoto(file ? await imageToDataUrl(file) : null),
    recorder,
    dictation,
    showDictation:
      kind !== 'photo' &&
      dictation.supported &&
      (offerDictation ||
        (typeof navigator !== 'undefined' && !navigator.onLine)),
    /** Back to the source step as text, with what the AI heard, the browser listening */
    dictateInstead: () => {
      recorder.reset()
      setKind('text')
      setText(draft?.sourceJa ?? '')
      setDraftId(null)
      setOfferDictation(true)
      setStep('source')
      dictation.start()
    },
    canProcess:
      (kind === 'text' && !!text.trim()) ||
      (kind === 'photo' && !!photo) ||
      (kind === 'voice' && !!recorder.audio),
    step,
    process: async () => {
      setStep('processing')
      try {
        const d = await create.mutateAsync({
          posterId: poster,
          sourceKind: kind,
          text: text.trim(),
          audio: recorder.audio,
          photo,
        })
        setDraftId(d.id)
        setStep('review')
      } catch {
        if (!navigator.onLine) setOfferDictation(true)
        setStep('source')
      }
    },
    /** Demo path: the cached garbage-flyer take. */
    useSample: async () => {
      setStep('processing')
      const d = await create.mutateAsync({
        posterId: poster,
        sourceKind: 'photo',
        text: '',
        audio: null,
        photo: null,
      })
      setDraftId(d.id)
      setStep('review')
    },
    draft,
    draftEvents: (draft?.events ?? []).map((e) => ({
      event: e,
      valid: !e.rrule || isValidRrule(e.rrule),
      next: upcomingDates(e, now, 3),
    })),
    setConfirmed: (eventId: string, confirmed: boolean) => {
      setTicks((t) => ({ ...t, [eventId]: confirmed }))
      confirm.mutate({ eventId, confirmed })
    },
    canPublish:
      !confirm.isPending &&
      !!draft &&
      draft.events.every(
        (e) => e.confirmed && (!e.rrule || isValidRrule(e.rrule)),
      ),
    publish: async () => {
      if (!draftId) return
      await publish.mutateAsync(draftId)
      setStep('done')
    },
    publishing: publish.isPending,
    again: () => {
      setText('')
      setPhoto(null)
      recorder.reset()
      setDraftId(null)
      setTicks({})
      setStep('source')
    },
    reach: published.map((n, i) => {
      const live = liveReach[i]?.data
      if (!USE_MOCK)
        return {
          notice: n,
          poster: posters.find((p) => p.id === n.posterId)?.name ?? '',
          understood: live?.understood ?? 0,
          opened: Math.max(0, (live?.opened ?? 0) - (live?.understood ?? 0)),
          notOpened: live?.notOpened ?? 0,
          total: live?.workers ?? 0,
        }
      const rs = receipts.filter((r) => r.targetId === n.id)
      return {
        notice: n,
        poster: posters.find((p) => p.id === n.posterId)?.name ?? '',
        understood: rs.filter((r) => r.passed).length,
        opened: rs.filter((r) => !r.passed).length,
        notOpened: Math.max(0, workers.length - rs.length),
        total: workers.length,
      }
    }),
  }
}
