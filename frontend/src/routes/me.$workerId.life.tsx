import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/me/$workerId/life')({
  component: () => <KitScreen screen="workerLife" />,
})
