import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/demo')({
  component: () => <KitScreen screen="demo" />,
})
