import type { UiKit } from '@/contract/ui-kit'
import { Layout } from './Layout'
import { HomeScreen } from './screens/home/HomeScreen'
import { NotFoundScreen } from './screens/not-found/NotFoundScreen'

export default {
  Layout,
  screens: { home: HomeScreen, notFound: NotFoundScreen },
} satisfies UiKit
