import { useState } from 'react'
import { DOORS, PLAY_NEEDED, type Door } from '@/domain/course'
import { COURSE } from '@/domain/courseContent'
import { pick } from '@/domain/wakatta'
import { useCourse } from '@/features/course'
import { m } from '@/i18n/paraglide/messages'
import { Card, PageTitle } from '@/ui/v2/components/Card'
import { Button } from '@/ui/v2/components/ui/button'
import { uiLang } from '@/ui/v2/lib/format'
import { cn } from '@/ui/v2/lib/utils'
import { InspectDoor, ReadDoor, WatchDoor } from './Doors'
import { PlayDoor } from './Drills'

const NAME: Record<Door, () => string> = {
  read: m.course_door_read,
  watch: m.course_door_watch,
  play: m.course_door_play,
  inspect: m.course_door_inspect,
}
const SUB: Record<Door, () => string> = {
  read: m.course_door_read_sub,
  watch: m.course_door_watch_sub,
  play: m.course_door_play_sub,
  inspect: m.course_door_inspect_sub,
}

/** One Safety Skill, four Doors; four done earns the Skill Passport stamp. */
export function CourseScreen() {
  const lang = uiLang()
  const c = useCourse()
  const [door, setDoor] = useState<Door | null>(null)

  return (
    <>
      <PageTitle sub={m.course_sub()}>{pick(COURSE.title, lang)}</PageTitle>

      {door ? (
        <div className="space-y-4">
          <Button variant="ghost" onClick={() => setDoor(null)}>
            {m.course_back()}
          </Button>
          <h2 className="display text-2xl">{NAME[door]()}</h2>
          {door === 'read' && (
            <ReadDoor
              done={c.progress.read}
              onFinish={() => c.finish('read')}
            />
          )}
          {door === 'watch' && (
            <WatchDoor
              done={c.progress.watch}
              onFinish={() => c.finish('watch')}
            />
          )}
          {door === 'play' && (
            <PlayDoor
              passed={c.progress.drills}
              need={PLAY_NEEDED}
              onPass={c.passDrill}
            />
          )}
          {door === 'inspect' && (
            <InspectDoor onFinish={() => c.finish('inspect')} />
          )}
        </div>
      ) : (
        <div className="space-y-6">
          <ul className="grid gap-3 sm:grid-cols-2">
            {DOORS.map((d) => (
              <li key={d}>
                <button
                  type="button"
                  onClick={() => setDoor(d)}
                  className="tile flex min-h-24 w-full flex-col gap-1 px-5 py-4 text-left"
                >
                  <span className="display flex items-center justify-between text-xl">
                    {NAME[d]()}
                    {c.done(d) && (
                      <span className="text-success">{m.course_done()}</span>
                    )}
                  </span>
                  <span className="text-muted-foreground">{SUB[d]()}</span>
                </button>
              </li>
            ))}
          </ul>
          <Card
            title={m.course_passport_title()}
            id="passport"
            tone={c.earned ? 'success' : undefined}
          >
            <p className={cn('font-bold', c.earned && 'text-success')}>
              {c.earned
                ? m.course_passport_earned()
                : m.course_passport_locked()}
            </p>
            <Button
              className="mt-3"
              variant="ghost"
              size="sm"
              onClick={c.reset}
            >
              {m.course_reset()}
            </Button>
          </Card>
        </div>
      )}
    </>
  )
}
