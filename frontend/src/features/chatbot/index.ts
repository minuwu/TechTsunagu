import { useState } from 'react'
import type { KnowledgeAnswer } from '@/domain/wakatta'
import { useAction } from '@/lib/action'
import { useRecorder } from '@/lib/recorder'
import { askChatbot, askQuestion, forwardAsk } from '@/services/wakatta'

export interface ChatTurn {
  id: number
  reply: KnowledgeAnswer
  picked: number | null // the Worker's answer to the practice question, graded here (it changes no Sign-off)
  forwarded: boolean
}

/** The Worker's chatbot (XREQ-016). On a Machine screen it sends machineId, so that Machine's instruction ranks first.
 * The conversation lives only in this screen: the backend stores nothing. */
export function useChatbot(workerId: string | null, machineId: string | null) {
  const [open, setOpen] = useState(false)
  const [text, setText] = useState('')
  const [turns, setTurns] = useState<ChatTurn[]>([])
  const recorder = useRecorder(30)
  const ask = useAction(askChatbot)
  const forward = useAction(async (question: string) => {
    if (!workerId) return
    const a = await askQuestion({ workerId, text: question, audio: null })
    await forwardAsk(a.id)
  })

  const patch = (id: number, change: Partial<ChatTurn>) =>
    setTurns((all) => all.map((t) => (t.id === id ? { ...t, ...change } : t)))

  return {
    open,
    setOpen,
    text,
    setText,
    recorder,
    turns,
    asking: ask.isPending,
    failed: ask.isError,
    /** The last question's error (a 429 carries when to try again) */
    error: ask.error,
    canAsk: !!(text.trim() || recorder.audio) && !ask.isPending,
    send: async () => {
      const reply = await ask.mutateAsync({
        text: text.trim(),
        audio: recorder.audio,
        machineId,
      })
      setTurns((all) => [
        ...all,
        { id: Date.now(), reply, picked: null, forwarded: false },
      ])
      setText('')
      recorder.reset()
    },
    pick: (id: number, option: number) => patch(id, { picked: option }),
    forwarding: forward.isPending,
    forward: async (turn: ChatTurn) => {
      await forward.mutateAsync(turn.reply.question)
      patch(turn.id, { forwarded: true })
    },
  }
}
