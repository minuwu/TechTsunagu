import { useState } from 'react'
import { DOORS, PLAY_NEEDED, type Door } from '@/domain/course'
import { COURSES, courseById } from '@/domain/courses'
import type { CourseData } from '@/domain/courses/types'
import { pick } from '@/domain/wakatta'
import { useCourse } from '@/features/course'
import { m } from '@/i18n/paraglide/messages'
import { CourseArt } from '@/ui/v4/art/courses'
import { PageTitle } from '@/ui/v4/components/Card'
import { PosterRow, PosterSection } from '@/ui/v4/components/Poster'
import { Button } from '@/ui/v4/components/ui/button'
import { uiLang } from '@/ui/v4/lib/format'
import { cn } from '@/ui/v4/lib/utils'
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

/** Five flat segments: four Doors and the quiz; a finished one fills cobalt. */
function Progress({ done }: { done: boolean[] }) {
  return (
    <ol className="grid grid-cols-5 gap-px bg-[var(--rule)]" aria-hidden="true">
      {done.map((d, i) => (
        <li key={i} className={cn('h-3', d ? 'bg-primary' : 'bg-background')} />
      ))}
    </ol>
  )
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
  const doneList = [...DOORS.map((d) => c.done(d)), c.progress.quiz > 0]

  return (
    <>
      <Button variant="ghost" size="sm" onClick={onExit} className="mb-6 -ml-4">
        {m.course_all()}
      </Button>

      {door ? (
        <div className="space-y-6">
          <div className="flex flex-wrap items-end justify-between gap-4 border-b border-[var(--rule)] pb-6">
            <h2 className="poster-h2">
              {door === 'quiz' ? m.course_door_quiz() : NAME[door]()}
            </h2>
            <Button variant="outline" onClick={() => setDoor(null)}>
              {m.course_back()}
            </Button>
          </div>
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
        <>
          <section className="grid gap-8 border-y border-[var(--rule)] md:grid-cols-12">
            <div className="space-y-6 py-8 md:col-span-7">
              <p className="poster-label">
                {m.course_level({ n: course.level })} ·{' '}
                {m.course_minutes({ n: course.minutes })}
              </p>
              <h1 className="poster-h2">{pick(course.title, lang)}</h1>
              <p className="max-w-[40ch] text-lg text-pretty text-muted-foreground">
                {pick(course.sub, lang)}
              </p>
              {course.refs.length > 0 && (
                <p className="text-sm text-muted-foreground">
                  <span className="poster-label mr-2">{m.course_refs()}</span>
                  {course.refs.join(' · ')}
                </p>
              )}
            </div>
            <div className="border-[var(--rule)] md:col-span-5 md:border-l">
              <CourseArt id={course.id} className="block size-full min-h-56" />
            </div>
          </section>
          <Progress done={doneList} />

          <PosterSection label={m.course_nav()}>
            <div>
              {DOORS.map((d, i) => (
                <PosterRow
                  key={d}
                  n={i + 1}
                  title={NAME[d]()}
                  sub={SUB[d]()}
                  aside={c.done(d) ? m.course_done() : undefined}
                  onClick={() => setDoor(d)}
                />
              ))}
              <PosterRow
                n={5}
                title={m.course_door_quiz()}
                sub={m.course_door_quiz_sub()}
                aside={
                  c.progress.quiz > 0
                    ? m.course_quiz_best({ n: c.progress.quiz })
                    : undefined
                }
                onClick={() => setDoor('quiz')}
              />
            </div>
          </PosterSection>

          <PosterSection label={m.course_passport_title()}>
            <div
              className={cn(
                'flex flex-wrap items-center justify-between gap-6 p-6 sm:p-10',
                c.earned
                  ? 'bg-primary text-primary-foreground'
                  : 'bg-foreground text-background',
              )}
            >
              <div className="flex items-center gap-6">
                <svg
                  viewBox="0 0 64 64"
                  aria-hidden="true"
                  className="size-20 shrink-0"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="4"
                  strokeLinecap="square"
                >
                  <rect x="4" y="4" width="56" height="56" />
                  {c.earned ? (
                    <path d="m16 33 10 10 22-24" strokeWidth="6" />
                  ) : (
                    <path d="M20 32h24" strokeWidth="6" />
                  )}
                </svg>
                <p className="display max-w-[22ch] text-2xl">
                  {c.earned
                    ? m.course_passport_earned()
                    : m.course_passport_locked()}
                </p>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={c.reset}
                className="border-current text-current hover:bg-background hover:text-foreground"
              >
                {m.course_reset()}
              </Button>
            </div>
          </PosterSection>
        </>
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

// Library order, so the printed index runs 01, 02, 03 down the page
const ORDER = CATS.flatMap((cat) => COURSES.filter((c) => c.category === cat))

/** The course library: every course as a poster tile, grouped by category. */
function CourseLibrary({ onOpen }: { onOpen: (id: string) => void }) {
  const lang = uiLang()
  return (
    <>
      <PageTitle sub={m.course_library_sub()}>
        {m.course_library_title()}
      </PageTitle>
      {CATS.map((cat) => (
        <PosterSection key={cat} label={CAT_NAME[cat]()}>
          <ul className="grid border-t border-l border-[var(--rule)] sm:grid-cols-2 xl:grid-cols-3">
            {COURSES.filter((c) => c.category === cat).map((c) => {
              const n = ORDER.indexOf(c) + 1
              return (
                <li
                  key={c.id}
                  className="border-r border-b border-[var(--rule)]"
                >
                  <button
                    type="button"
                    onClick={() => onOpen(c.id)}
                    className="group flex size-full cursor-pointer flex-col text-left transition-colors duration-300 hover:bg-white/20"
                  >
                    <CourseArt
                      id={c.id}
                      className="block aspect-[4/3] w-full border-b border-[var(--rule)]"
                    />
                    <span className="flex flex-1 flex-col gap-2 p-4">
                      <span className="poster-index tnum">
                        {String(n).padStart(2, '0')}
                      </span>
                      <span className="display text-xl transition-colors duration-300 group-hover:text-primary">
                        {pick(c.title, lang)}
                      </span>
                      <span className="text-sm text-pretty text-muted-foreground">
                        {pick(c.sub, lang)}
                      </span>
                      <span className="poster-label mt-auto pt-2">
                        {m.course_level({ n: c.level })} ·{' '}
                        {m.course_minutes({ n: c.minutes })}
                      </span>
                    </span>
                  </button>
                </li>
              )
            })}
          </ul>
        </PosterSection>
      ))}
    </>
  )
}

/** Library first; opening a course shows its four Doors. The id lives in component state only. */
export function CourseScreen() {
  const [id, setId] = useState<string | null>(null)
  const course = courseById(id)
  return course ? (
    <CoursePlayer course={course} onExit={() => setId(null)} />
  ) : (
    <CourseLibrary onOpen={setId} />
  )
}
