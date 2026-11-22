import type { UiKit } from '@/contract/ui-kit'
import { Layout } from './Layout'
import { HomeScreen } from './screens/home/HomeScreen'
import { MachineScreen } from './screens/machine/MachineScreen'
import { PanelScreen } from './screens/panel/PanelScreen'
import { WorkerWorkScreen } from './screens/worker/WorkerWorkScreen'
import { WorkerLifeScreen } from './screens/worker/WorkerLifeScreen'
import { ForemanBoardScreen } from './screens/foreman/ForemanBoardScreen'
import { ForemanRecordScreen } from './screens/foreman/ForemanRecordScreen'
import {
  ForemanAssignScreen,
  ForemanSkillsScreen,
  ForemanHeatmapScreen,
  ForemanGlossaryScreen,
} from './screens/foreman/ForemanPeopleScreens'
import {
  ForemanBriefingScreen,
  ForemanNearMissesScreen,
  ForemanLibraryScreen,
  ForemanQrScreen,
  ForemanSenpaiScreen,
} from './screens/foreman/ForemanOpsScreens'
import { CommunityScreen } from './screens/community/CommunityScreen'
import { SenpaiScreen } from './screens/senpai/SenpaiScreen'
import { CourseScreen } from './screens/course/CourseScreen'
import { NotFoundScreen } from './screens/not-found/NotFoundScreen'

export default {
  Layout,
  screens: {
    home: HomeScreen,
    machine: MachineScreen,
    panel: PanelScreen,
    workerWork: WorkerWorkScreen,
    workerLife: WorkerLifeScreen,
    foremanBoard: ForemanBoardScreen,
    foremanRecord: ForemanRecordScreen,
    foremanAssign: ForemanAssignScreen,
    foremanSkills: ForemanSkillsScreen,
    foremanHeatmap: ForemanHeatmapScreen,
    foremanGlossary: ForemanGlossaryScreen,
    foremanBriefing: ForemanBriefingScreen,
    foremanNearMisses: ForemanNearMissesScreen,
    foremanLibrary: ForemanLibraryScreen,
    foremanQr: ForemanQrScreen,
    foremanSenpai: ForemanSenpaiScreen,
    community: CommunityScreen,
    senpai: SenpaiScreen,
    course: CourseScreen,
    notFound: NotFoundScreen,
  },
} satisfies UiKit
