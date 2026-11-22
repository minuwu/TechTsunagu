import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/foreman/near-misses')({
  component: () => <KitScreen screen="foremanNearMisses" />,
})
