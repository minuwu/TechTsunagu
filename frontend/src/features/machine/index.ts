import { useEffect, useState } from 'react'
import { useParams } from '@tanstack/react-router'
import {
  isCleared,
  latestSignOff,
  skillStatus,
  type CheckQuestion,
  type GlossaryTerm,
  type Lang,
} from '@/domain/wakatta'
import { useAction } from '@/lib/action'
import { useListQuery } from '@/services/queries/wakatta'
import { scanMachine, submitCheck, type CheckResult } from '@/services/wakatta'

export type MachineStep = 'read' | 'check' | 'result'

/** Worker's page behind a Machine's QR: read the instruction, pass the Check, unlock the Panel. */
export function useMachineCheck(workerId: string | null) {
  const { machineId = '' } = useParams({ strict: false })
  const machines = useListQuery('machines')
  const notes = useListQuery('notes')
  const workers = useListQuery('workers')
  const signOffs = useListQuery('signOffs')
  const glossary = useListQuery('glossary')

  const machine = machines.data?.find((m) => m.id === machineId)
  const note = notes.data?.find((n) => n.id === machine?.currentVersionId)
  const worker = workers.data?.find((w) => w.id === workerId)

  const [lang, setLang] = useState<Lang | null>(null)
  const [step, setStep] = useState<MachineStep>('read')
  const [answers, setAnswers] = useState<number[]>([])
  const [result, setResult] = useState<CheckResult | null>(null)

  const scan = useAction((id: string) => scanMachine(machineId, id))
  const check = useAction(submitCheck)

  // Opening the QR page marks this Worker as the one at the Machine, so the Panel shows them
  const scanKey = machine && worker ? `${machine.id}|${worker.id}` : ''
  const { mutate: scanMutate } = scan
  useEffect(() => {
    if (scanKey) scanMutate(scanKey.split('|')[1]!)
  }, [scanKey, scanMutate])

  const status =
    machine && worker
      ? skillStatus(
          latestSignOff(signOffs.data ?? [], worker.id, machine.id),
          machine,
          new Date(),
        )
      : null

  const termOf = (q: CheckQuestion): GlossaryTerm | undefined =>
    glossary.data?.find((g) => g.id === q.termId)

  return {
    loading: machines.isPending || notes.isPending || workers.isPending,
    notFound: machines.isSuccess && !machine,
    machine,
    note,
    worker,
    workers: workers.data ?? [],
    status,
    cleared: status ? isCleared(status.status) : false,
    lang: lang ?? worker?.language ?? 'en',
    setLang,
    step,
    answers,
    result,
    termOf,
    submitting: check.isPending,
    startCheck: () => {
      setAnswers([])
      setResult(null)
      setStep('check')
    },
    answer: (index: number, option: number) =>
      setAnswers((a) => Object.assign([...a], { [index]: option })),
    canSubmit:
      !!note && note.questions.every((_, i) => answers[i] !== undefined),
    submit: async () => {
      if (!note || !worker) return
      const r = await check.mutateAsync({
        workerId: worker.id,
        target: { kind: 'note', id: note.id },
        language: lang ?? worker.language,
        answers,
      })
      setResult(r)
      setStep('result')
    },
    backToRead: () => setStep('read'),
  }
}
