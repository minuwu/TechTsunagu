import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/senpai/$token')({
  component: () => <KitScreen screen="senpai" />,
})
