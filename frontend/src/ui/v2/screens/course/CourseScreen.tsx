import { useState } from 'react'
import { DOORS, PLAY_NEEDED, type Door } from '@/domain/course'
import { COURSES, courseById } from '@/domain/courses'
import type { CourseData } from '@/domain/courses/types'
import { pick } from '@/domain/wakatta'
import { useCourse } from '@/features/course'
import { m } from '@/i18n/paraglide/messages'
import { Card, PageTitle } from '@/ui/v2/components/Card'
import { Button } from '@/ui/v2/components/ui/button'
import { uiLang } from '@/ui/v2/lib/format'
import { cn } from '@/ui/v2/lib/utils'
import { InspectDoor, QuizDoor, ReadDoor, WatchDoor } from './Doors'
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
function CoursePlayer({
  course,
  onExit,
}: {
  course: CourseData
  onExit: () => void
}) {
  const lang = uiLang()
  const c = useCourse(course.id)
  const [door, setDoor] = useState<Door | 'quiz' | null>(null)

  return (
    <>
      <PageTitle sub={pick(course.sub, lang)}>
        {pick(course.title, lang)}
      </PageTitle>
      <Button variant="ghost" onClick={onExit}>
        {m.course_all()}
      </Button>

      {door ? (
        <div className="space-y-4">
          <Button variant="ghost" onClick={() => setDoor(null)}>
            {m.course_back()}
          </Button>
          <h2 className="display text-2xl">
            {door === 'quiz' ? m.course_door_quiz() : NAME[door]()}
          </h2>
          {door === 'read' && (
            <ReadDoor
              course={course}
              done={c.progress.read}
              onFinish={() => c.finish('read')}
            />
          )}
          {door === 'watch' && (
            <WatchDoor
              course={course}
              done={c.progress.watch}
              onFinish={() => c.finish('watch')}
            />
          )}
          {door === 'play' && (
            <PlayDoor
              drills={course.drills}
              passed={c.progress.drills}
              need={PLAY_NEEDED}
              onPass={c.passDrill}
            />
          )}
          {door === 'quiz' && (
            <QuizDoor
              course={course}
              best={c.progress.quiz}
              onScore={c.saveQuiz}
            />
          )}
          {door === 'inspect' && (
            <InspectDoor course={course} onFinish={() => c.finish('inspect')} />
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
          <button
            type="button"
            onClick={() => setDoor('quiz')}
            className="tile flex min-h-16 w-full items-center justify-between px-5 py-3 text-left"
          >
            <span className="display text-lg">
              {m.course_door_quiz()}
              <span className="ml-3 text-sm font-normal text-muted-foreground">
                {m.course_door_quiz_sub()}
              </span>
            </span>
            {c.progress.quiz > 0 && (
              <span className="text-success">
                {m.course_quiz_best({ n: c.progress.quiz })}
              </span>
            )}
          </button>
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

const CATS = ['core', 'trade', 'language'] as const
const CAT_NAME: Record<(typeof CATS)[number], () => string> = {
  core: m.course_cat_core,
  trade: m.course_cat_trade,
  language: m.course_cat_language,
}

/** The course library: every course, grouped by category. */
function CourseLibrary({ onOpen }: { onOpen: (id: string) => void }) {
  const lang = uiLang()
  return (
    <>
      <PageTitle sub={m.course_library_sub()}>
        {m.course_library_title()}
      </PageTitle>
      <div className="space-y-8">
        {CATS.map((cat) => (
          <section key={cat} className="space-y-3">
            <h2 className="display text-xl">{CAT_NAME[cat]()}</h2>
            <ul className="grid gap-3 sm:grid-cols-2">
              {COURSES.filter((c) => c.category === cat).map((c) => (
                <li key={c.id}>
                  <button
                    type="button"
                    onClick={() => onOpen(c.id)}
                    className="tile flex min-h-24 w-full flex-col gap-1 px-5 py-4 text-left"
                  >
                    <span className="display text-lg">
                      {pick(c.title, lang)}
                    </span>
                    <span className="text-muted-foreground">
                      {pick(c.sub, lang)}
                    </span>
                    <span className="text-sm text-muted-foreground">
                      {m.course_level({ n: c.level })} ·{' '}
                      {m.course_minutes({ n: c.minutes })}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          </section>
        ))}
      </div>
    </>
  )
}

/** Library first; opening a course shows its four Doors. The id lives in the URL hash-free state only. */
export function CourseScreen() {
  const [id, setId] = useState<string | null>(null)
  const course = courseById(id)
  return course ? (
    <CoursePlayer course={course} onExit={() => setId(null)} />
  ) : (
    <CourseLibrary onOpen={setId} />
  )
}
