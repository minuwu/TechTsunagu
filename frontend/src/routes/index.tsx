import { createFileRoute } from '@tanstack/react-router'
import { healthQuery } from '@/services/queries/health'
import { KitScreen } from '@/shell/KitScreen'

export const Route = createFileRoute('/')({
  // prefetchQuery never throws: the screen renders its own error state with a retry (S-15, S-27)
  loader: ({ context }) => context.queryClient.prefetchQuery(healthQuery()),
  component: () => <KitScreen screen="home" />,
})
