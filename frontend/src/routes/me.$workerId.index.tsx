import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/me/$workerId/')({
  component: () => <KitScreen screen="workerWork" />,
})
