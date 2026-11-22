import { DOORS, type Door } from '@/domain/course'
import { COURSES } from '@/domain/courses'
import { pick } from '@/domain/wakatta'
import { useCourse } from '@/features/course'
import { m } from '@/i18n/paraglide/messages'
import { Card, PageTitle } from '@/ui/v1/components/Card'
import { uiLang } from '@/ui/v1/lib/format'

const NAME: Record<Door, () => string> = {
  read: m.course_door_read,
  watch: m.course_door_watch,
  play: m.course_door_play,
  inspect: m.course_door_inspect,
}

function CourseProgress({ id, title }: { id: string; title: string }) {
  const c = useCourse(id)
  return (
    <Card id={`doors-${id}`} title={title}>
      <ul className="space-y-2">
        {DOORS.map((d) => (
          <li key={d}>
            {NAME[d]()} {c.done(d) ? m.course_done() : ''}
          </li>
        ))}
      </ul>
      <p className="mt-3 font-bold">
        {c.earned ? m.course_passport_earned() : m.course_passport_locked()}
      </p>
    </Card>
  )
}

// shortcut: v1 shows progress only; the Doors themselves are built in v2
export function CourseScreen() {
  const lang = uiLang()
  return (
    <>
      <PageTitle sub={m.course_library_sub()}>
        {m.course_library_title()}
      </PageTitle>
      {COURSES.map((c) => (
        <CourseProgress key={c.id} id={c.id} title={pick(c.title, lang)} />
      ))}
    </>
  )
}
