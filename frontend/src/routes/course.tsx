import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/course')({
  component: () => <KitScreen screen="course" />,
})
