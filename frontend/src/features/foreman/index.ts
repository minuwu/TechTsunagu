import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useParams } from '@tanstack/react-router'
import QRCode from 'qrcode'
import {
  isCleared,
  latestSignOff,
  skillStatus,
  tokyoDate,
  type Briefing,
  type MeetupKind,
  type NoteVersion,
  type SenpaiKind,
  type Shift,
  type SkillStatus,
} from '@/domain/wakatta'
import { useAction } from '@/lib/action'
import { downloadBlob, downloadText } from '@/lib/files'
import { useDictation } from '@/lib/dictation'
import { useRecorder } from '@/lib/recorder'
import { USE_MOCK, getSession } from '@/services/api'
import { useListQuery, useList } from '@/services/queries/wakatta'
import {
  ackConfusion,
  ackNearMiss,
  addGlossaryTerm,
  addPair,
  approveNote,
  createBriefingDraft,
  createNoteDraft,
  editDraftStep,
  publishBriefing,
  setAssignment,
  createAccount,
  listAccounts,
  supportLogCsv,
  updateAccount,
  type Account,
} from '@/services/wakatta'

const now = () => new Date()

/** Board: every Machine's Lock, today's Shift Assignments, flags and open Confusions. Polls 5 s. */
export function useBoard() {
  const machines = useListQuery('machines', 5000)
  const workers = useList('workers', 5000)
  const signOffs = useList('signOffs', 5000)
  const assignments = useList('assignments', 5000)
  const confusions = useList('confusions', 5000)
  const glossary = useList('glossary')
  const nearMisses = useList('nearMisses', 5000)
  const ack = useAction(ackConfusion)
  const today = tokyoDate(now())
  const name = (id: string | null) =>
    workers.find((w) => w.id === id)?.name ?? ''

  const rows = (machines.data ?? []).map((m) => {
    const statusOf = (workerId: string) =>
      skillStatus(latestSignOff(signOffs, workerId, m.id), m, now()).status
    const active = m.activeWorkerId ? statusOf(m.activeWorkerId) : null
    return {
      machine: m,
      activeWorker: name(m.activeWorkerId),
      panel: active === null ? 'idle' : isCleared(active) ? 'ready' : 'locked',
      assigned: assignments
        .filter((a) => a.machineId === m.id && a.date === today)
        .map((a) => {
          const status = statusOf(a.workerId)
          return {
            ...a,
            workerName: name(a.workerId),
            status,
            flagged: !isCleared(status),
          }
        }),
    }
  })

  const open = confusions
    .filter((c) => !c.acknowledgedAt)
    .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    .map((c) => ({
      ...c,
      workerName: name(c.workerId),
      term: glossary.find((g) => g.id === c.termId),
      machine: machines.data?.find((m) => m.id === c.machineId),
    }))

  return {
    loading: machines.isPending,
    rows,
    flags: rows.flatMap((r) =>
      r.assigned
        .filter((a) => a.flagged)
        .map((a) => ({ ...a, machine: r.machine })),
    ),
    confusions: open,
    openNearMisses: nearMisses.filter((n) => !n.acknowledgedAt).length,
    ack: (id: string) => ack.mutate(id),
  }
}

export type RecordStep = 'record' | 'processing' | 'review' | 'confirm' | 'done'

/** Record a voice note, review the AI draft, edit, confirm, approve. */
export function useRecordNote() {
  const { machineId = '' } = useParams({ strict: false })
  const machines = useListQuery('machines')
  const glossary = useList('glossary')
  const machine = machines.data?.find((m) => m.id === machineId)
  const recorder = useRecorder(60)
  const [text, setText] = useState('')
  const [step, setStep] = useState<RecordStep>('record')
  const [draft, setDraft] = useState<NoteVersion | null>(null)
  // After a draft the AI could not make, or offline, the record step offers the browser's dictation
  const [offerDictation, setOfferDictation] = useState(false)
  const dictation = useDictation((heard) =>
    setText((t) => (t ? `${t}${heard}` : heard)),
  )
  const create = useAction((input: { audio: Blob | null; text: string }) =>
    createNoteDraft(machineId, input),
  )
  const edit = useAction((a: { index: number; ja: string }) =>
    editDraftStep(draft?.id ?? '', a.index, a.ja),
  )
  const approve = useAction(approveNote)

  return {
    loading: machines.isPending,
    machine,
    machines: machines.data ?? [],
    recorder,
    text,
    setText,
    step,
    draft,
    glossary,
    canProcess: !!(recorder.audio || text.trim()),
    dictation,
    showDictation:
      dictation.supported &&
      (offerDictation ||
        (typeof navigator !== 'undefined' && !navigator.onLine)),
    /** "Dictate instead": back to the record step with what the AI heard, the browser listening */
    dictateInstead: () => {
      recorder.reset()
      setText(draft?.transcriptJa ?? '')
      setDraft(null)
      setOfferDictation(true)
      setStep('record')
      dictation.start()
    },
    process: async () => {
      setStep('processing')
      try {
        setDraft(
          await create.mutateAsync({
            audio: recorder.audio,
            text: text.trim(),
          }),
        )
        setStep('review')
      } catch {
        if (!navigator.onLine) setOfferDictation(true)
        setStep('record')
      }
    },
    /** Demo path: skip the mic and use the cached take. */
    useSample: async () => {
      setStep('processing')
      setDraft(await create.mutateAsync({ audio: null, text: 'sample' }))
      setStep('review')
    },
    editStep: (index: number, ja: string) => {
      setDraft((d) => {
        if (!d) return d
        const steps = d.steps.map((s, i) =>
          i === index ? { ...s, text: { ...s.text, ja } } : s,
        )
        return { ...d, steps }
      })
      edit.mutate({ index, ja })
    },
    toConfirm: () => setStep('confirm'),
    backToReview: () => setStep('review'),
    approve: async () => {
      if (!draft) return
      await approve.mutateAsync(draft.id)
      setStep('done')
    },
    approving: approve.isPending,
    again: () => {
      recorder.reset()
      setText('')
      setDraft(null)
      setStep('record')
    },
  }
}

/** Shift Assignments for today, with a confirm step before saving. */
export function useAssign() {
  const machines = useList('machines')
  const workers = useList('workers')
  const assignments = useList('assignments')
  const signOffs = useList('signOffs')
  const today = tokyoDate(now())
  const current = (machineId: string, shift: Shift) =>
    assignments.find(
      (a) => a.machineId === machineId && a.shift === shift && a.date === today,
    )?.workerId ?? ''
  const [changes, setChanges] = useState<Record<string, string>>({})
  const [confirming, setConfirming] = useState(false)
  const save = useAction(async (all: Record<string, string>) => {
    for (const [key, workerId] of Object.entries(all)) {
      const [machineId, shift] = key.split('|') as [string, Shift]
      await setAssignment(machineId, shift, workerId || null)
    }
  })
  const value = (machineId: string, shift: Shift) =>
    changes[`${machineId}|${shift}`] ?? current(machineId, shift)
  const statusOf = (machineId: string, workerId: string): SkillStatus => {
    const m = machines.find((x) => x.id === machineId)
    return m && workerId
      ? skillStatus(latestSignOff(signOffs, workerId, machineId), m, now())
          .status
      : 'none'
  }
  return {
    today,
    machines,
    workers,
    value,
    statusOf,
    set: (machineId: string, shift: Shift, workerId: string) =>
      setChanges((c) => ({ ...c, [`${machineId}|${shift}`]: workerId })),
    dirty: Object.keys(changes).length > 0,
    changes: Object.entries(changes).map(([key, workerId]) => {
      const [machineId, shift] = key.split('|') as [string, Shift]
      return {
        key,
        machine: machines.find((m) => m.id === machineId),
        shift,
        workerName: workers.find((w) => w.id === workerId)?.name ?? null,
      }
    }),
    confirming,
    toConfirm: () => setConfirming(true),
    cancel: () => setConfirming(false),
    save: async () => {
      await save.mutateAsync(changes)
      setChanges({})
      setConfirming(false)
    },
    saving: save.isPending,
    saved: save.isSuccess && !confirming && Object.keys(changes).length === 0,
  }
}

/** Skill Matrix (力量表): Worker × Machine. */
export function useSkillMatrix() {
  const machines = useList('machines')
  const workers = useList('workers')
  const signOffs = useList('signOffs')
  return {
    machines,
    rows: workers.map((w) => ({
      worker: w,
      cells: machines.map((m) => {
        const s = latestSignOff(signOffs, w.id, m.id)
        return {
          machineId: m.id,
          passedAt: s?.passedAt ?? null,
          ...skillStatus(s, m, now()),
        }
      }),
    })),
  }
}

/** Confusion Heatmap: count by Glossary Term × Machine. */
export function useHeatmap() {
  const machines = useList('machines')
  const glossary = useList('glossary')
  const confusions = useList('confusions', 5000)
  const counts = glossary
    .map((g) => ({
      term: g,
      cells: machines.map((m) => ({
        machineId: m.id,
        count: confusions.filter(
          (c) => c.termId === g.id && c.machineId === m.id,
        ).length,
      })),
    }))
    .map((r) => ({ ...r, total: r.cells.reduce((s, c) => s + c.count, 0) }))
    .filter((r) => r.total > 0)
    .sort((a, b) => b.total - a.total)
  return {
    machines,
    rows: counts,
    max: Math.max(1, ...counts.flatMap((r) => r.cells.map((c) => c.count))),
    top: counts[0] ?? null,
  }
}

/** Glossary Terms: list and add. */
export function useGlossaryAdmin() {
  const glossary = useList('glossary')
  const empty = { ja: '', reading: '', en: '' }
  const [form, setForm] = useState(empty)
  const add = useAction(addGlossaryTerm)
  return {
    glossary,
    form,
    setField: (k: keyof typeof empty, v: string) =>
      setForm((f) => ({ ...f, [k]: v })),
    canAdd: !!(form.ja.trim() && form.en.trim()),
    duplicate: glossary.some((g) => g.ja === form.ja.trim()),
    add: async () => {
      await add.mutateAsync({
        ja: form.ja.trim(),
        reading: form.reading.trim(),
        en: form.en.trim(),
      })
      setForm(empty)
    },
    adding: add.isPending,
  }
}

export type BriefingStep = 'record' | 'processing' | 'review' | 'done'

/** Morning Briefing (朝礼): record, review, publish; receipts per Worker. */
export function useBriefingAdmin() {
  const briefings = useList('briefings', 5000)
  const receipts = useList('receipts', 5000)
  const workers = useList('workers')
  const recorder = useRecorder(60)
  const [step, setStep] = useState<BriefingStep>('record')
  const [draft, setDraft] = useState<Briefing | null>(null)
  // "Dictate instead" (XREQ-004): after a draft the AI could not make, or offline
  const [text, setText] = useState('')
  const [offerDictation, setOfferDictation] = useState(false)
  const dictation = useDictation((heard) =>
    setText((t) => (t ? `${t}${heard}` : heard)),
  )
  const create = useAction(createBriefingDraft)
  const publish = useAction(publishBriefing)
  const latest = briefings[0]
  return {
    recorder,
    step,
    draft,
    text,
    setText,
    dictation,
    showDictation:
      dictation.supported &&
      (offerDictation ||
        (typeof navigator !== 'undefined' && !navigator.onLine)),
    canProcess: !!(recorder.audio || text.trim()),
    process: async (useSample: boolean) => {
      setStep('processing')
      const typed = text.trim()
      const input = useSample ? null : typed ? { text: typed } : recorder.audio
      try {
        setDraft(await create.mutateAsync(input))
        setStep('review')
      } catch {
        if (!navigator.onLine) setOfferDictation(true)
        setStep('record')
      }
    },
    dictateInstead: () => {
      recorder.reset()
      setText(draft?.text.ja ?? '')
      setDraft(null)
      setOfferDictation(true)
      setStep('record')
      dictation.start()
    },
    publish: async () => {
      if (!draft) return
      await publish.mutateAsync(draft)
      setStep('done')
    },
    publishing: publish.isPending,
    again: () => {
      recorder.reset()
      setText('')
      setDraft(null)
      setStep('record')
    },
    latest,
    history: briefings,
    receipts: latest
      ? workers.map((w) => {
          const r = receipts.find(
            (x) => x.targetId === latest.id && x.workerId === w.id,
          )
          return {
            worker: w,
            state: !r ? 'not-opened' : r.passed ? 'understood' : 'opened',
          } as const
        })
      : [],
  }
}

/** Near-miss Reports (ヒヤリハット). */
export function useNearMissAdmin() {
  const reports = useList('nearMisses', 5000)
  const workers = useList('workers')
  const machines = useList('machines')
  const ack = useAction(ackNearMiss)
  return {
    reports: reports.map((r) => ({
      ...r,
      workerName: workers.find((w) => w.id === r.workerId)?.name ?? '',
      machine: machines.find((m) => m.id === r.machineId),
    })),
    ack: (id: string) => ack.mutate(id),
  }
}

/** Knowledge Library: every Note Version of a Machine, with a step diff against the previous one. */
export function useLibrary() {
  const { machineId = '' } = useParams({ strict: false })
  const machines = useList('machines')
  const notes = useList('notes')
  const machine = machines.find((m) => m.id === machineId)
  const versions = notes
    .filter((n) => n.machineId === machineId && n.status === 'approved')
    .sort((a, b) => b.versionNo - a.versionNo)
  return {
    machines,
    machine,
    versions: versions.map((v, i) => {
      const prev = versions[i + 1]
      const before = new Set(prev?.steps.map((s) => s.text.ja) ?? [])
      const after = new Set(v.steps.map((s) => s.text.ja))
      return {
        version: v,
        isCurrent: v.id === machine?.currentVersionId,
        added: prev
          ? v.steps.filter((s) => !before.has(s.text.ja)).map((s) => s.text.ja)
          : [],
        removed: prev
          ? prev.steps
              .filter((s) => !after.has(s.text.ja))
              .map((s) => s.text.ja)
          : [],
      }
    }),
  }
}

/** Printable QR stickers. The URL base comes from VITE_PUBLIC_URL so phones on the LAN can open it. */
export function useQrStickers() {
  const machines = useList('machines')
  const base = import.meta.env.VITE_PUBLIC_URL || 'http://localhost:3000'
  const codes = useQuery({
    queryKey: ['qr', base, machines.map((m) => m.id).join()],
    enabled: machines.length > 0,
    queryFn: async () =>
      Promise.all(
        machines.map(async (m) => {
          // Real API: the shop code lets a phone with no session show this shop's names (?s=)
          const shop = USE_MOCK ? null : getSession()?.user.shopCode
          const url = `${base}/m/${m.id}${shop ? `?s=${encodeURIComponent(shop)}` : ''}`
          return {
            machine: m,
            url,
            dataUrl: await QRCode.toDataURL(url, { margin: 1, width: 320 }),
          }
        }),
      ),
  })
  return { stickers: codes.data ?? [], loading: codes.isPending }
}

/** Senpai pairing and the Support Log export. */
export function useSenpaiAdmin() {
  const pairs = useList('pairs')
  const workers = useList('workers')
  const meetups = useList('meetups', 5000)
  const receipts = useList('receipts')
  const notices = useList('notices')
  const [workerId, setWorkerId] = useState('')
  const [senpaiName, setSenpaiName] = useState('')
  const [kind, setKind] = useState<SenpaiKind>('coworker')
  const add = useAction(addPair)
  const base = import.meta.env.VITE_PUBLIC_URL || 'http://localhost:3000'
  const name = (id: string) => workers.find((w) => w.id === id)?.name ?? ''

  return {
    workers,
    pairs: pairs.map((p) => ({
      ...p,
      workerName: name(p.workerId),
      link: `${base}/senpai/${p.token}`,
      meetups: meetups.filter((m) => m.pairId === p.id),
    })),
    form: { workerId, setWorkerId, senpaiName, setSenpaiName, kind, setKind },
    canAdd: !!(workerId && senpaiName.trim()),
    add: async () => {
      await add.mutateAsync({ workerId, senpaiName: senpaiName.trim(), kind })
      setSenpaiName('')
      setWorkerId('')
    },
    adding: add.isPending,
    /** Support Log CSV with a UTF-8 BOM so Excel shows Japanese correctly. */
    downloadSupportLog: async (
      labels: Record<MeetupKind | 'notice', string>,
    ) => {
      if (!USE_MOCK) {
        // The backend file: grouped by official support item, cells safe from Excel formulas
        const to = tokyoDate(now())
        const from = `${Number(to.slice(0, 4)) - 1}${to.slice(4)}`
        downloadBlob(`support-log-${to}.csv`, await supportLogCsv(from, to))
        return
      }
      const esc = (s: string) => `"${s.replace(/"/g, '""')}"`
      const rows = [
        ['worker', 'date', 'kind', 'detail', 'source'],
        ...meetups.map((m) => {
          const p = pairs.find((x) => x.id === m.pairId)
          return [
            name(p?.workerId ?? ''),
            m.date,
            labels[m.kind],
            m.note,
            `senpai:${p?.senpaiName ?? ''}`,
          ]
        }),
        ...receipts
          .filter((r) => notices.some((n) => n.id === r.targetId))
          .map((r) => {
            const n = notices.find((x) => x.id === r.targetId)!
            return [
              name(r.workerId),
              r.openedAt.slice(0, 10),
              labels.notice,
              `${n.title.ja} / ${r.passed ? 'understood' : 'opened'}`,
              'notice',
            ]
          }),
      ]
      downloadText(
        `support-log-${tokyoDate(now())}.csv`,
        '﻿' + rows.map((r) => r.map(esc).join(',')).join('\r\n'),
        'text/csv;charset=utf-8',
      )
    },
  }
}

// Backend app/schemas/common.py Lang: a Worker's own language can be one the UI does not speak
const ACCOUNT_LANGS = ['en', 'vi', 'id', 'ne', 'my', 'tl', 'zh', 'ja'] // no Bengali (ADR 0016)
type AccountKind = 'worker' | 'foreman'

/** Real API: create Workers and Foremen, reset a PIN (they choose a new one at sign-in), deactivate. */
export function useAccountsAdmin() {
  const list = useQuery({
    queryKey: ['wakatta', 'accounts'],
    queryFn: async () => {
      const [workers, foremen] = await Promise.all([
        listAccounts('worker'),
        listAccounts('foreman'),
      ])
      return [
        ...workers.map((a) => ({ ...a, kind: 'worker' as const })),
        ...foremen.map((a) => ({ ...a, kind: 'foreman' as const })),
      ]
    },
    enabled: !USE_MOCK,
  })
  const empty = {
    kind: 'worker' as AccountKind,
    displayName: '',
    language: 'en',
    pin: '',
  }
  const [form, setForm] = useState(empty)
  const [resetPins, setResetPins] = useState<Record<string, string>>({})
  const create = useAction((f: typeof empty) =>
    createAccount(f.kind, {
      displayName: f.displayName,
      language: f.language,
      pin: f.pin,
    }),
  )
  const update = useAction(
    (a: {
      kind: AccountKind
      id: string
      patch: { pin?: string; active?: boolean }
    }) => updateAccount(a.kind, a.id, a.patch),
  )
  const digits = (v: string) => v.replace(/\D/g, '').slice(0, 6)
  return {
    enabled: !USE_MOCK,
    accounts: list.data ?? [],
    languages: ACCOUNT_LANGS,
    form,
    setKind: (kind: AccountKind) => setForm((f) => ({ ...f, kind })),
    setName: (displayName: string) => setForm((f) => ({ ...f, displayName })),
    setLanguage: (language: string) => setForm((f) => ({ ...f, language })),
    setPin: (pin: string) => setForm((f) => ({ ...f, pin: digits(pin) })),
    canCreate: !!form.displayName.trim() && form.pin.length >= 4,
    create: async () => {
      await create.mutateAsync({
        ...form,
        displayName: form.displayName.trim(),
      })
      setForm(empty)
    },
    creating: create.isPending,
    failed: create.isError || update.isError,
    resetPin: (id: string) => resetPins[id] ?? '',
    setResetPin: (id: string, v: string) =>
      setResetPins((r) => ({ ...r, [id]: digits(v) })),
    saveResetPin: async (a: Account & { kind: AccountKind }) => {
      await update.mutateAsync({
        kind: a.kind,
        id: a.id,
        patch: { pin: resetPins[a.id] ?? '' },
      })
      setResetPins((r) => ({ ...r, [a.id]: '' }))
    },
    setActive: (a: Account & { kind: AccountKind }, active: boolean) =>
      update.mutate({ kind: a.kind, id: a.id, patch: { active } }),
    saving: update.isPending,
  }
}
