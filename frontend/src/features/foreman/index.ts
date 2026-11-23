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
import { downloadText } from '@/lib/files'
import { useRecorder } from '@/lib/recorder'
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
  const create = useAction(createBriefingDraft)
  const publish = useAction(publishBriefing)
  const latest = briefings[0]
  return {
    recorder,
    step,
    draft,
    process: async (useSample: boolean) => {
      setStep('processing')
      setDraft(await create.mutateAsync(useSample ? null : recorder.audio))
      setStep('review')
    },
    publish: async () => {
      if (!draft) return
      await publish.mutateAsync(draft)
      setStep('done')
    },
    publishing: publish.isPending,
    again: () => {
      recorder.reset()
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
          const url = `${base}/m/${m.id}`
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
    downloadSupportLog: (labels: Record<MeetupKind | 'notice', string>) => {
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
