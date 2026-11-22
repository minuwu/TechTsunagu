import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/foreman/record/$machineId')({
  component: () => <KitScreen screen="foremanRecord" />,
})
