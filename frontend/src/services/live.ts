// Real API reads, shaped like the mock store (services/mocks/seed.ts) so every screen reads one shape.
// Each role sees only what the backend lets it read (docs/FRONTEND_API_HANDOVER.md §3-4).

import type {
  Ask,
  Briefing,
  CheckQuestion,
  Machine,
  NearMissReport,
  Notice,
  NoteVersion,
  Receipt,
  SenpaiPair,
  Tri,
  Worker,
} from '@/domain/wakatta'
import type {
  AccountOut,
  AskOut,
  BriefingForForeman,
  BriefingForWorker,
  BriefingReceipts,
  CheckQuestionForWorker,
  EventOut,
  MachineDetail,
  MachineOut,
  NearMissOut,
  NoteVersionOut,
  NoticeForPoster,
  NoticeForWorker,
  SenpaiPairOut,
} from './generated'
import { getSession, request, requestAll, requestBlob } from './api'
import type { Db } from './mocks/seed'

type Translated = Record<string, string>

/** Backend text objects carry the shop's languages; screens read ja and en. */
export const tri = (o: Translated | null | undefined): Tri => ({
  ...o,
  ja: o?.ja ?? '',
  en: o?.en ?? o?.ja ?? '',
})

const question = (
  q: CheckQuestionForWorker & { correctIndex?: number },
): CheckQuestion => ({
  id: q.id,
  termId: q.termId ?? null,
  prompt: tri(q.prompt as Translated),
  options: q.options.map((o) => tri(o as Translated)),
  // Workers never get the answer: the server grades (POST /api/checks)
  correctIndex: q.correctIndex ?? -1,
})

// One object URL per blob path, kept for the page's life, so a 5 s poll does not refetch photos
const blobUrls = new Map<string, Promise<string | null>>()
function photoUrl(path: string | null | undefined): Promise<string | null> {
  if (!path) return Promise.resolve(null)
  let url = blobUrls.get(path)
  if (!url) {
    url = requestBlob(path)
      .then((b) => URL.createObjectURL(b))
      .catch(() => {
        blobUrls.delete(path) // try again on the next read
        return null
      })
    blobUrls.set(path, url)
  }
  return url
}

const worker = (a: AccountOut): Worker => ({
  id: a.id,
  name: a.displayName,
  language: a.language as Worker['language'],
})

const machine = (m: MachineOut): Machine => ({
  id: m.id,
  name: tri(m.name as Translated),
  currentVersionId: m.currentVersionId ?? null,
  activeWorkerId: m.activeWorkerId ?? null,
})

const note = (n: NoteVersionOut): NoteVersion => ({
  id: n.id,
  machineId: n.machineId,
  versionNo: n.versionNo,
  status: n.status as NoteVersion['status'],
  recordedBy: n.recordedBy,
  transcriptJa: n.transcriptJa,
  steps: n.steps.map((s) => ({
    text: tri(s.text as Translated),
    icon: (s.icon ?? null) as NoteVersion['steps'][number]['icon'],
  })),
  backTranslation: { en: (n.backTranslation as Translated).en ?? '' },
  questions: n.questions.map(question),
  createdAt: n.createdAt,
  approvedAt: n.approvedAt ?? null,
  needsEdit: n.needsEdit,
  approvedBy: n.approvedBy ?? null,
})

/** A Worker cannot read Note Versions; the Machine carries its current one. */
const currentNote = (m: MachineDetail): NoteVersion | null =>
  m.current
    ? {
        id: m.current.id,
        machineId: m.id,
        versionNo: m.current.versionNo,
        status: 'approved',
        recordedBy: '',
        transcriptJa: '',
        steps: m.current.steps.map((s) => ({
          text: tri(s.text as Translated),
          icon: (s.icon ?? null) as NoteVersion['steps'][number]['icon'],
        })),
        backTranslation: { en: '' },
        questions: m.current.questions.map(question),
        createdAt: '',
        approvedAt: null,
      }
    : null

export const briefing = (
  b: BriefingForWorker | BriefingForForeman,
): Briefing => ({
  id: b.id,
  text: tri(b.text as Translated),
  question: question(b.question),
  createdAt: b.publishedAt ?? b.createdAt,
  ...('needsEdit' in b ? { needsEdit: b.needsEdit } : {}),
})

export const notice = async (
  n: NoticeForWorker | NoticeForPoster,
): Promise<Notice> => ({
  id: n.id,
  posterId: n.posterId,
  category: n.category as Notice['category'],
  sourceKind: n.sourceKind as Notice['sourceKind'],
  sourceJa: n.sourceJa,
  photo: await photoUrl(n.photo),
  title: tri(n.title as Translated),
  steps: n.steps.map((s) => tri(s as Translated)),
  events: n.events.map((e: EventOut) => ({
    id: e.id,
    title: tri(e.title as Translated),
    startsAt: e.startsAt,
    rrule: e.rrule ?? null,
    confirmed: e.confirmed,
    nextDates: e.nextDates,
  })),
  question: question(n.question),
  status: n.status as Notice['status'],
  createdAt: n.publishedAt ?? n.createdAt,
  needsEdit: n.needsEdit,
})

const ask = (a: AskOut): Ask => ({
  id: a.id,
  workerId: a.workerId,
  text: a.text,
  covered: a.covered,
  answer: a.answer ? tri(a.answer as Translated) : null,
  sourceNoticeIds: a.sourceNoticeIds,
  forwarded: a.forwarded,
  senpaiReply: a.senpaiReply ? tri(a.senpaiReply as Translated) : null,
  createdAt: a.createdAt,
})

const nearMiss = async (r: NearMissOut): Promise<NearMissReport> => ({
  id: r.id,
  workerId: r.workerId,
  machineId: r.machineId,
  text: tri(r.text as Translated),
  photo: await photoUrl(r.photo),
  createdAt: r.createdAt,
  acknowledgedAt: r.acknowledgedAt ?? null,
})

const pair = (p: SenpaiPairOut): SenpaiPair => ({
  id: p.id,
  workerId: p.workerId,
  senpaiName: p.senpaiName,
  kind: p.kind as SenpaiPair['kind'],
  token: '', // shown once, when the pair is made; the backend keeps only its hash
})

async function machines() {
  const all = await requestAll<MachineOut>('/api/machines')
  return all.filter((m) => !m.archived)
}

/** The signed-in Worker's own Briefing and Notice receipts, from the `opened`/`passed` on each item. */
async function ownReceipts(workerId: string): Promise<Receipt[]> {
  const [briefings, notices] = await Promise.all([
    requestAll<BriefingForWorker>('/api/briefings'),
    requestAll<NoticeForWorker>('/api/notices'),
  ])
  return [...briefings, ...notices]
    .filter((x) => x.opened)
    .map((x) => ({
      targetId: x.id,
      workerId,
      openedAt: '',
      passed: x.passed ?? null,
    }))
}

/** The Foreman sees receipts of the latest Briefing (the Briefing screen shows only that one). */
async function briefingReceipts(): Promise<Receipt[]> {
  const [latest] = await requestAll<BriefingForForeman>('/api/briefings')
  if (!latest) return []
  const r = await request<BriefingReceipts>(
    `/api/briefings/${latest.id}/receipts`,
  )
  const row = (passed: boolean | null) => (w: { id: string }) => ({
    targetId: latest.id,
    workerId: w.id,
    openedAt: '',
    passed,
  })
  return [...r.understood.map(row(true)), ...r.openedOnly.map(row(false))]
}

type Reader = { [K in keyof Db]: () => Promise<Db[K]> }

/** What each collection is for the signed-in role; empty where that role may not read it. */
export async function liveList<K extends keyof Db>(key: K): Promise<Db[K]> {
  const user = getSession()?.user
  const role = user?.role
  const shop = role === 'foreman' || role === 'worker'
  const none = async () => []
  const readers: Reader = {
    workers: async () =>
      role === 'foreman'
        ? (await requestAll<AccountOut>('/api/workers'))
            .filter((a) => a.active)
            .map(worker)
        : role === 'worker' && user
          ? [
              {
                id: user.id,
                name: user.displayName,
                language: user.language as Worker['language'],
              },
            ]
          : [],
    machines: async () => (shop ? (await machines()).map(machine) : []),
    glossary: async () =>
      shop
        ? (await request<Db['glossary']>('/api/glossary')).map(
            ({ id, ja, reading, en }) => ({
              id,
              ja,
              reading,
              en,
            }),
          )
        : [],
    notes: async () => {
      if (role === 'foreman') {
        const ms = await machines()
        const pages = await Promise.all(
          ms.map((m) =>
            requestAll<NoteVersionOut>(`/api/machines/${m.id}/versions`),
          ),
        )
        return pages.flat().map(note)
      }
      if (role !== 'worker') return []
      const ms = await machines()
      const details = await Promise.all(
        ms.map((m) => request<MachineDetail>(`/api/machines/${m.id}`)),
      )
      return details
        .map(currentNote)
        .filter((n): n is NoteVersion => n !== null)
    },
    checks: none, // no screen reads it; a Check's result comes back from POST /api/checks
    confusions: async () =>
      role === 'foreman'
        ? (
            await requestAll<Db['confusions'][number]>('/api/confusions')
          ).filter(
            (c) => c.machineId !== null, // Chat Confusions have no Machine; the web shows Machine ones
          )
        : [],
    signOffs: async () => (shop ? requestAll('/api/sign-offs') : []),
    assignments: async () => (shop ? request('/api/shift-assignments') : []),
    briefings: async () =>
      shop
        ? (
            await requestAll<BriefingForWorker | BriefingForForeman>(
              '/api/briefings',
            )
          ).map(briefing)
        : [],
    receipts: async () =>
      role === 'worker' && user
        ? ownReceipts(user.id)
        : role === 'foreman'
          ? briefingReceipts()
          : [], // the Poster reads counts per Notice: noticeReach()
    nearMisses: async () =>
      shop
        ? Promise.all(
            (await requestAll<NearMissOut>('/api/near-misses')).map(nearMiss),
          )
        : [],
    posters: async () => (role === 'poster' ? requestAll('/api/posters') : []),
    notices: async () =>
      role === 'poster' || role === 'worker'
        ? Promise.all(
            (
              await requestAll<NoticeForWorker | NoticeForPoster>(
                '/api/notices',
              )
            ).map(notice),
          )
        : [],
    pairs: async () => {
      if (role === 'foreman')
        return (await requestAll<SenpaiPairOut>('/api/senpai-pairs')).map(pair)
      if (role !== 'worker') return []
      const mine = await request<SenpaiPairOut | null>('/api/senpai-pairs/me')
      return mine ? [pair(mine)] : []
    },
    asks: async () =>
      shop ? (await requestAll<AskOut>('/api/asks')).map(ask) : [],
    meetups: async () => (shop ? requestAll('/api/meetups') : []),
  }
  return readers[key]() as Promise<Db[K]>
}
