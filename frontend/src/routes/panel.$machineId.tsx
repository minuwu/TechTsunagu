import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/panel/$machineId')({
  component: () => <KitScreen screen="panel" />,
})
