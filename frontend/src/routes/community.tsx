import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/community')({
  component: () => <KitScreen screen="community" />,
})
