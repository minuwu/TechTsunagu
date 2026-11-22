import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/foreman/library/$machineId')({
  component: () => <KitScreen screen="foremanLibrary" />,
})
