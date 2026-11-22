import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/m/$machineId')({
  component: () => <KitScreen screen="machine" />,
})
