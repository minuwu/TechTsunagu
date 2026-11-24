// Wakatta? API. Mock path: in-browser store (services/mocks). Real path: endpoints from the plan
// (docs/superpowers/plans/2026-11-22-wakatta.md, "API"); add them to docs/openapi.yaml as the backend lands.

import type {
  Ask,
  Briefing,
  CheckQuestion,
  CheckTarget,
  GlossaryTerm,
  KnowledgeAnswer,
  Lang,
  MeetupKind,
  NearMissReport,
  Notice,
  NoteVersion,
  SenpaiKind,
  SenpaiPair,
  Shift,
  Tri,
} from '@/domain/wakatta'
import { gradeCheck, tokyoDate } from '@/domain/wakatta'
import { isValidRrule } from '@/domain/calendar'
import {
  ApiError,
  USE_MOCK,
  authHeaders,
  getSession,
  request,
  requestAll,
  requestBlob,
  setSession,
  type Session,
} from './api'
import { briefing as liveBriefing, liveList } from './live'
import { applyScenario } from './mocks/scenario'
import type { Db } from './mocks/seed'
import { readDb, resetDb, update, wait } from './mocks/store'
import * as ai from './mocks/ai'

export type { Db }
export type Collection = keyof Db
export { SENPAI_REPLIES, ASK_VOICE_TRANSCRIPT } from './mocks/ai'
export { SHOP_NAME, FOREMAN_NAME } from './mocks/seed'
export {
  SCENARIOS,
  controlsShown,
  getScenario,
  isScenario,
  setScenario,
  subscribeScenario,
  type Scenario,
} from './mocks/scenario'

const json = (
  method: string,
  body: unknown,
  idempotent = false,
): RequestInit => ({
  method,
  headers: { 'Content-Type': 'application/json', ...authHeaders(idempotent) },
  body: JSON.stringify(body),
})

/** A Senpai link route. The token comes from a URL anyone can craft, so encode it: `../` must not reach another route. */
const senpaiPath = (token: string) => `/api/senpai/${encodeURIComponent(token)}`

/** Multipart POST with an Idempotency-Key (every multipart route takes one). Empty fields are left out. */
const multipart = (
  fields: Record<string, string | Blob | null | undefined>,
): RequestInit => {
  const form = new FormData()
  for (const [k, v] of Object.entries(fields)) if (v) form.append(k, v)
  return { method: 'POST', headers: authHeaders(true), body: form }
}

/** Photos travel as data URLs on screen; the API takes them as files. */
const photoFile = async (dataUrl: string | null) =>
  dataUrl ? (await fetch(dataUrl)).blob() : null

const uid = (prefix: string) =>
  `${prefix}-${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`
const nowIso = () => new Date().toISOString()

export async function list<K extends Collection>(key: K): Promise<Db[K]> {
  if (USE_MOCK) return applyScenario(readDb()[key])
  return liveList(key)
}

export async function resetDemo(): Promise<void> {
  if (USE_MOCK) return resetDb()
  throw new ApiError(400, 'NOT_AVAILABLE', 'Reset only exists in demo mode')
}

export async function scanMachine(machineId: string, workerId: string) {
  if (!USE_MOCK)
    return request<void>(`/api/machines/${machineId}/scan`, json('POST', {}))
  update((db) => {
    const m = db.machines.find((x) => x.id === machineId)
    if (m) m.activeWorkerId = workerId
  })
}

export async function createNoteDraft(
  machineId: string,
  input: { audio: Blob | null; text: string },
): Promise<NoteVersion> {
  if (!USE_MOCK)
    return request<NoteVersion>(
      `/api/machines/${machineId}/notes`,
      multipart({ text: input.text, audio: input.audio }),
    )
  await wait(2200) // the AI step
  return update((db) => {
    const versions = db.notes.filter((n) => n.machineId === machineId)
    const currentId = db.machines.find(
      (m) => m.id === machineId,
    )?.currentVersionId
    const current = db.notes.find((n) => n.id === currentId)
    const draft = ai.noteDraft(
      machineId,
      current,
      versions.length + 1,
      nowIso(),
    )
    db.notes.push(draft)
    return draft
  })
}

export async function editDraftStep(noteId: string, index: number, ja: string) {
  if (!USE_MOCK)
    return request<void>(
      `/api/note-versions/${noteId}`,
      json('PATCH', { index, ja }),
    )
  update((db) => {
    const n = db.notes.find((x) => x.id === noteId)
    if (n?.status === 'draft' && n.steps[index]) n.steps[index].text.ja = ja
  })
}

export async function approveNote(noteId: string) {
  if (!USE_MOCK)
    return request<void>(
      `/api/note-versions/${noteId}/approve`,
      json('POST', {}),
    )
  update((db) => {
    const n = db.notes.find((x) => x.id === noteId)
    const m = db.machines.find((x) => x.id === n?.machineId)
    if (!n || !m) return
    n.status = 'approved'
    n.approvedAt = nowIso()
    m.currentVersionId = n.id // older Sign-offs now point at an old version, so they expire
  })
}

export interface CheckResult {
  passed: boolean
  wrong: CheckQuestion[]
}

/** Graded by the "server" (here the mock), never by the screen. */
export async function submitCheck(input: {
  workerId: string
  target: CheckTarget
  language: Lang
  answers: number[]
}): Promise<CheckResult> {
  if (!USE_MOCK) {
    const { target, language, answers } = input // the Worker comes from the token
    const r = await request<{ passed: boolean; wrong: CheckQuestion[] }>(
      '/api/checks',
      json('POST', { target, language, answers }, true),
    )
    return { passed: r.passed, wrong: r.wrong }
  }
  await wait(400)
  return update((db) => {
    const { target } = input
    const note =
      target.kind === 'note'
        ? db.notes.find((n) => n.id === target.id)
        : undefined
    const other =
      target.kind === 'briefing'
        ? db.briefings.find((b) => b.id === target.id)?.question
        : db.notices.find((n) => n.id === target.id)?.question
    const questions = note?.questions ?? (other ? [other] : [])
    if (questions.length === 0)
      throw new ApiError(404, 'NOT_FOUND', 'Check target not found')
    const { passed, wrong } = gradeCheck(questions, input.answers)
    const check = { id: uid('c'), ...input, passed, createdAt: nowIso() }
    db.checks.push(check)
    if (note) {
      for (const q of wrong)
        db.confusions.push({
          id: uid('cf'),
          checkId: check.id,
          questionId: q.id,
          termId: q.termId,
          machineId: note.machineId,
          workerId: input.workerId,
          createdAt: check.createdAt,
          acknowledgedAt: null,
        })
      if (passed)
        db.signOffs.push({
          id: uid('s'),
          workerId: input.workerId,
          machineId: note.machineId,
          noteVersionId: note.id,
          passedAt: check.createdAt,
        })
    } else {
      const r = db.receipts.find(
        (x) => x.targetId === target.id && x.workerId === input.workerId,
      )
      if (r) r.passed = passed || r.passed === true
      else
        db.receipts.push({
          targetId: target.id,
          workerId: input.workerId,
          openedAt: check.createdAt,
          passed,
        })
    }
    return { passed, wrong }
  })
}

export async function ackConfusion(id: string) {
  if (!USE_MOCK)
    return request<void>(`/api/confusions/${id}/ack`, json('POST', {}))
  update((db) => {
    const c = db.confusions.find((x) => x.id === id)
    if (c) c.acknowledgedAt = nowIso()
  })
}

export async function setAssignment(
  machineId: string,
  shift: Shift,
  workerId: string | null,
) {
  if (!USE_MOCK)
    return request<void>(
      '/api/shift-assignments',
      json('PUT', { machineId, shift, workerId }),
    )
  update((db) => {
    const date = tokyoDate(new Date())
    db.assignments = db.assignments.filter(
      (a) =>
        !(a.machineId === machineId && a.shift === shift && a.date === date),
    )
    if (workerId)
      db.assignments.push({ id: uid('a'), workerId, machineId, date, shift })
  })
}

export async function addGlossaryTerm(term: Omit<GlossaryTerm, 'id'>) {
  if (!USE_MOCK)
    // shortcut: the API requires Bengali and a reading, the web form has no Bengali field; ask Anim to make bn optional
    return request<GlossaryTerm>(
      '/api/glossary',
      json('POST', { ...term, reading: term.reading || term.ja, bn: term.en }),
    )
  update((db) => {
    db.glossary.push({ id: uid('g'), ...term })
  })
}

/** A recording, or the text the browser's dictation heard (XREQ-004 "Dictate instead"). */
export async function createBriefingDraft(
  input: Blob | { text: string } | null,
): Promise<Briefing> {
  if (!USE_MOCK)
    return liveBriefing(
      await request(
        '/api/briefings/draft',
        multipart(input instanceof Blob || !input ? { audio: input } : input),
      ),
    )
  await wait(2000)
  return ai.briefingDraft(nowIso())
}

export async function publishBriefing(briefing: Briefing) {
  if (!USE_MOCK) {
    // Only these fields: the API refuses unknown ones (422)
    const { id, text, question } = briefing
    return request<void>('/api/briefings', json('POST', { id, text, question }))
  }
  update((db) => {
    db.briefings.unshift(briefing)
  })
}

/** Receipt: the Worker opened a Briefing or Notice. */
export async function openTarget(
  target: { kind: 'briefing' | 'notice'; id: string },
  workerId: string,
) {
  const targetId = target.id
  if (!USE_MOCK) return request<void>('/api/receipts', json('POST', { target }))
  update((db) => {
    if (
      !db.receipts.some(
        (r) => r.targetId === targetId && r.workerId === workerId,
      )
    )
      db.receipts.push({ targetId, workerId, openedAt: nowIso(), passed: null })
  })
}

export async function reportNearMiss(input: {
  workerId: string
  machineId: string
  text: string
  audio: Blob | null
  photo: string | null
}): Promise<NearMissReport> {
  if (!USE_MOCK)
    return request<NearMissReport>(
      '/api/near-misses',
      multipart({
        machineId: input.machineId,
        text: input.text,
        audio: input.audio,
        photo: await photoFile(input.photo),
      }),
    )
  await wait(1500)
  const text = input.audio ? ai.NEAR_MISS_VOICE : ai.typedNearMiss(input.text)
  return update((db) => {
    const report: NearMissReport = {
      id: uid('nm'),
      workerId: input.workerId,
      machineId: input.machineId,
      text,
      photo: input.photo,
      createdAt: nowIso(),
      acknowledgedAt: null,
    }
    db.nearMisses.unshift(report)
    return report
  })
}

export async function ackNearMiss(id: string) {
  if (!USE_MOCK)
    return request<void>(`/api/near-misses/${id}/ack`, json('POST', {}))
  update((db) => {
    const n = db.nearMisses.find((x) => x.id === id)
    if (n) n.acknowledgedAt = nowIso()
  })
}

export async function createNoticeDraft(input: {
  posterId: string
  sourceKind: Notice['sourceKind']
  text: string
  audio: Blob | null
  photo: string | null
}): Promise<Notice> {
  if (!USE_MOCK) {
    // Exactly the one source sourceKind names; the Poster comes from the token
    const source =
      input.sourceKind === 'text'
        ? { text: input.text }
        : input.sourceKind === 'voice'
          ? { audio: input.audio }
          : { photo: await photoFile(input.photo) }
    return request<Notice>(
      '/api/notices',
      multipart({ sourceKind: input.sourceKind, ...source }),
    )
  }
  await wait(2500)
  return update((db) => {
    const draft = ai.noticeDraft(
      input.posterId,
      input.sourceKind,
      input.photo,
      nowIso(),
    )
    db.notices.unshift(draft)
    return draft
  })
}

export async function confirmEvent(
  noticeId: string,
  eventId: string,
  confirmed: boolean,
) {
  if (!USE_MOCK)
    return request<void>(
      `/api/notices/${noticeId}/events/${eventId}`,
      json('PATCH', { confirmed }),
    )
  update((db) => {
    const e = db.notices
      .find((n) => n.id === noticeId)
      ?.events.find((x) => x.id === eventId)
    if (e) e.confirmed = confirmed
  })
}

export async function publishNotice(noticeId: string) {
  if (!USE_MOCK)
    return request<void>(`/api/notices/${noticeId}/publish`, json('POST', {}))
  update((db) => {
    const n = db.notices.find((x) => x.id === noticeId)
    if (!n) return
    // Same rule the backend enforces: a wrong garbage day is worse than none
    if (
      n.events.some((e) => !e.confirmed || (e.rrule && !isValidRrule(e.rrule)))
    )
      throw new ApiError(
        409,
        'EVENTS_NOT_CONFIRMED',
        'Confirm every date first',
      )
    n.status = 'published'
  })
}

export async function askQuestion(input: {
  workerId: string
  text: string
  audio: Blob | null
}): Promise<Ask> {
  if (!USE_MOCK)
    return request<Ask>(
      '/api/asks',
      multipart({ text: input.text, audio: input.audio }),
    )
  await wait(1600)
  const text = input.audio ? ai.ASK_VOICE_TRANSCRIPT : input.text
  return update((db) => {
    const ask: Ask = {
      id: uid('ask'),
      workerId: input.workerId,
      text,
      ...ai.answerAsk(text, db.notices),
      forwarded: false,
      senpaiReply: null,
      createdAt: nowIso(),
    }
    db.asks.unshift(ask)
    return ask
  })
}

/** The chatbot (XREQ-016): answers only from cited guides, the shop's instructions and Notices. Nothing is stored. */
export async function askChatbot(input: {
  text: string
  audio: Blob | null
  machineId: string | null
}): Promise<KnowledgeAnswer> {
  if (!USE_MOCK)
    return request<KnowledgeAnswer>('/api/knowledge/asks', multipart(input))
  await wait(1200)
  const text = input.audio ? 'How do I throw away garbage?' : input.text
  const db = readDb()
  return {
    question: text,
    ...ai.answerKnowledge(text, input.machineId, db.notes, db.notices),
  }
}

export async function forwardAsk(askId: string) {
  if (!USE_MOCK)
    return request<void>(`/api/asks/${askId}/forward`, json('POST', {}))
  update((db) => {
    const a = db.asks.find((x) => x.id === askId)
    if (a) a.forwarded = true
  })
}

export async function senpaiReply(token: string, askId: string, reply: Tri) {
  if (!USE_MOCK)
    // The backend translates the Japanese for the Worker
    return request<void>(
      `${senpaiPath(token)}/asks/${encodeURIComponent(askId)}/reply`,
      json('POST', { replyJa: reply.ja }),
      false,
    )
  update((db) => {
    const pair = db.pairs.find((p) => p.token === token)
    const a = db.asks.find(
      (x) => x.id === askId && x.workerId === pair?.workerId,
    )
    if (a) a.senpaiReply = reply
  })
}

export async function logMeetup(input: {
  pairId: string
  kind: MeetupKind
  date: string
  note: string
  loggedBy: 'worker' | 'senpai'
  /** The Senpai's link token: a Senpai has no account */
  token?: string
}) {
  const { token, ...meetup } = input
  if (!USE_MOCK) {
    const body = { kind: meetup.kind, date: meetup.date, note: meetup.note }
    return request<void>(
      token ? `${senpaiPath(token)}/meetups` : '/api/meetups',
      json('POST', body, !token),
      !token,
    )
  }
  update((db) => {
    db.meetups.unshift({ id: uid('mu'), ...meetup })
  })
}

export async function addPair(input: {
  workerId: string
  senpaiName: string
  kind: SenpaiKind
}): Promise<SenpaiPair> {
  if (!USE_MOCK)
    return request<SenpaiPair>('/api/senpai-pairs', json('POST', input))
  return update((db) => {
    const pair = {
      id: uid('sp'),
      token: crypto.randomUUID().replace(/-/g, ''),
      ...input,
    }
    db.pairs = db.pairs.filter((p) => p.workerId !== input.workerId)
    db.pairs.push(pair)
    return pair
  })
}

/** Demo PINs, shown on the hub. shortcut: real check is server-side (SHOP_PIN, COMMUNITY_PIN). */
export const DEMO_PINS = { foreman: '1234', community: '5678' } as const

/** Mock only: the real API signs in a person (signIn), not a role. */
export async function verifyPin(
  role: 'foreman' | 'community',
  pin: string,
): Promise<boolean> {
  await wait(300)
  return pin === DEMO_PINS[role]
}

// --- Accounts (real API, XREQ-001; ADR 0010) ---

export type AccountRole = Session['user']['role']
export interface RosterEntry {
  id: string
  displayName: string
}

/** Names to pick from: a shop code for Foremen and Workers, a district code for Posters. */
export const roster = (code: string, role: AccountRole) =>
  request<RosterEntry[]>(
    `/api/auth/roster?code=${encodeURIComponent(code)}&role=${role}`,
  )

export async function signIn(userId: string, pin: string): Promise<Session> {
  const session = await request<Session>(
    '/api/auth/login',
    json('POST', { userId, pin }),
  )
  setSession(session)
  return session
}

/** First sign-in with a PIN the Foreman set: the person chooses their own before anything else. */
export async function changePin(currentPin: string, newPin: string) {
  await request<void>('/api/auth/me/pin', json('PUT', { currentPin, newPin }))
  const session = getSession()
  if (session) setSession({ ...session, mustChangePin: false })
}

export async function signOut() {
  // Best effort: the token is dropped here either way
  if (!USE_MOCK)
    await request<void>('/api/auth/logout', json('POST', {})).catch(
      () => undefined,
    )
  setSession(null)
}

export interface Account {
  id: string
  displayName: string
  language: string
  active: boolean
  mustChangePin: boolean
}
const accountsPath = (role: 'worker' | 'foreman') =>
  role === 'worker' ? '/api/workers' : '/api/foremen'

/** Foreman: the shop's Workers or Foremen, deactivated ones included. */
export const listAccounts = (role: 'worker' | 'foreman') =>
  requestAll<Account>(accountsPath(role))

export const createAccount = (
  role: 'worker' | 'foreman',
  input: { displayName: string; language: string; pin: string },
) => request<Account>(accountsPath(role), json('POST', input))

/** Reset a PIN (the person must choose a new one at next sign-in) or deactivate. */
export const updateAccount = (
  role: 'worker' | 'foreman',
  id: string,
  patch: { pin?: string; active?: boolean },
) => request<Account>(`${accountsPath(role)}/${id}`, json('PATCH', patch))

// --- Real-API-only reads (XREQ-002) ---

/** Poster: how many of the district's Workers opened and understood a Notice. */
export const noticeReach = (noticeId: string) =>
  request<{
    workers: number
    opened: number
    understood: number
    notOpened: number
  }>(`/api/notices/${noticeId}/reach`)

/** Foreman: the Support Log CSV the backend builds (grouped by support item, formula-safe cells). */
export const supportLogCsv = (from: string, to: string) =>
  requestBlob(`/api/support-log.csv?from=${from}&to=${to}`)

/** The Machine Panel as the backend decides it (Sign-off, Note Version, Foreman override). */
export const machinePanel = (machineId: string) =>
  request<{
    state: 'idle' | 'locked' | 'ready' | 'override'
    worker: { id: string; displayName: string } | null
    reason: string | null
  }>(`/api/machines/${machineId}/panel`)

export interface SenpaiView {
  senpaiName: string
  worker: { displayName: string; language: string }
  asks: {
    id: string
    text: string
    textJa: string | null
    createdAt: string
    replyJa: string | null
  }[]
  events: { eventId: string; noticeId: string; title: Tri; at: string }[]
  meetups: {
    id: string
    kind: MeetupKind
    date: string
    note: string
    loggedBy: 'worker' | 'senpai'
    pairId: string
  }[]
}

/** The Senpai's private link: no account, the token is the key (404 when wrong or revoked). */
export const senpaiView = (token: string) =>
  request<SenpaiView>(senpaiPath(token), undefined, false)
