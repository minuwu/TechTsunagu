import { createFileRoute } from '@tanstack/react-router'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/foreman/qr')({
  component: () => <KitScreen screen="foremanQr" />,
})
