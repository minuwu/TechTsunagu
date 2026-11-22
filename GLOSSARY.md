# Wakatta?

Understood at work, understood at home. At work, a foreman's spoken know-how is pinned to a machine, turned into plain Japanese, Bengali and English, and a worker must pass a check before the machine unlocks for them. At home, local notices (garbage rules, drills, festivals) reach the same worker as plain steps with dates, and a Japanese senpai connects them to the neighbourhood.

## People

**Foreman**:
The one employer-side role (班長 / manager): records notes, approves them, assigns workers to machines and reads every board.
_Avoid_: Manager, admin, supervisor, employer (as a role name)

**Worker**:
A shop-floor employee, often a foreign national, who operates machines, takes checks and receives Notices at home; one term in both Work and Life.
_Avoid_: Employee, trainee, operator, user, resident

**Veteran**:
A Foreman whose notes are kept in the Knowledge Library as the record of how a machine is really run.
_Avoid_: Senior, expert

## Instructions

**Machine**:
A physical station on the shop floor with its own QR sticker and Machine Panel.
_Avoid_: Station, equipment, device

**Note**:
The living instruction for one Machine, made of successive Note Versions.
_Avoid_: Manual, SOP, instruction sheet

**Note Version**:
One approved snapshot of a Note: やさしい日本語, Bengali and English text, back-translation, icons and Check Questions.
_Avoid_: Revision, draft (a draft is a Note Version not yet approved)

**Glossary Term**:
A shop word (玉掛け, 段取り…) with a fixed translation in every language, which the AI must never change.
_Avoid_: Keyword, jargon, vocab

**Back-translation**:
The Bengali or English text translated back into Japanese so a Foreman who cannot read it can check its meaning.

**Icon**:
A pictogram chosen from the shop's fixed icon set to mark a step (PPE, hot surface, lockout…).
_Avoid_: Pictogram (in code), image

## Checks

**Check**:
A Worker answering the Check Questions of a Note Version, Briefing or Notice in their chosen language.
_Avoid_: Quiz, test, exam, teach-back (in code)

**Check Question**:
A multiple-choice question tied to one Glossary Term or step of a Note Version, Briefing or Notice.

**Sign-off**:
The record that a Worker passed the Check for a Machine's Note Version; valid until a newer **Safety Change** is approved for that Machine or 90 days pass.
_Avoid_: Certification, approval, clearance

**Safety Change**:
A new Note Version that changes what the Worker must do (steps, hazard, tool), so every Worker must pass its Check again. Its opposite, a **Wording Change**, only fixes text or translation and leaves Sign-offs valid. The Foreman chooses when approving.
_Avoid_: Major/minor version, update

**Panel Override**:
A Foreman's time-limited permission (at most 8 hours, with a reason) for a Worker, or anyone, to run a Machine without a valid Sign-off; the Machine Panel shows it in amber with the Foreman's name.
_Avoid_: Bypass, unlock, exception

**Confusion**:
A wrong answer on a Check Question, recorded against its Glossary Term and sent to the Foreman as an alert.
_Avoid_: Failure, error, mistake

**Lock**:
The state of a Machine Panel for a Worker without a valid Sign-off; **Ready** is its opposite.
_Avoid_: Block, disabled

**Machine Panel**:
The screen beside a Machine that shows Lock or Ready for the Worker on it; it simulates a hardware interlock.
_Avoid_: Interlock (in code), kiosk

**Presence**:
The Worker currently at a Machine, set when they pick their name after scanning its QR; the Machine Panel shows that Worker for 10 minutes, then falls back to the Shift Assignment.
_Avoid_: Login, check-in, session

## Shop management

**Shift Assignment**:
A Foreman's decision that a Worker runs a Machine during a shift.
_Avoid_: Schedule, roster entry, responsibility

**Skill Matrix**:
The Worker × Machine grid of Sign-offs (力量表), showing valid, expiring and missing cells.
_Avoid_: Competence table, heatmap

**Skill Passport**:
A Worker's own list of Machines they hold a Sign-off for, with dates.
_Avoid_: Certificate, CV, profile

**Confusion Heatmap**:
Confusions counted by Glossary Term and Machine across all Workers.

**Briefing**:
A Foreman's spoken shift notice (朝礼) sent to all Workers in their languages, with one Check Question.
_Avoid_: Announcement, broadcast, notice

**Near-miss Report**:
A Worker's voice-and-photo report of a near-miss (ヒヤリハット), tied to a Machine and translated for the Foreman.
_Avoid_: Incident, accident report

**Knowledge Library**:
All Note Versions of every Machine, in time order, with who recorded each one.
_Avoid_: History, archive, wiki

## Chat (prototype: prototypes/wakatta/)

**Thread**:
A chat between a Foreman and one or more Workers, where the Foreman's messages are translated for each Worker.
_Avoid_: Channel, room, conversation

**Wakatta Status**:
One Worker's result on the Check Questions of one Foreman message: green (all right, わかった), orange (some wrong or "not sure", needs clarifying), red (all wrong) or grey (waiting).
_Avoid_: Score, grade, read receipt

**Auto-send Countdown**:
The few seconds after dictation ends, shown as a ring on the send button, in which the Foreman can cancel and edit before the message sends itself.
_Avoid_: Delay, timer (in code)

**Re-explanation**:
A simpler version of a Foreman message, aimed at the term one Worker got wrong, sent to that Worker only with one new Check Question.
_Avoid_: Retry, resend, correction

**Follow-up**:
The item a wrong or "not sure" answer creates for the Foreman, moving from open (要フォロー) to clarified (説明済, with the Re-explanation recorded) to understood (理解) when the Worker passes the new Check.
_Avoid_: Ticket, issue, alert (an alert is how a Confusion is shown)

**Learning History**:
One Worker's timeline of terms they missed and later understood, built from their Confusions and Follow-ups.
_Avoid_: Progress report, transcript, record

**Nudge**:
A reminder the Foreman sends to a Worker who has not answered a message's Check Questions before its deadline.
_Avoid_: Ping, poke, reminder (reminders belong to Events)

**Task**:
A piece of work extracted from a Foreman's message (or added by hand) with assignees, due time and Machine, kept on the Board.
_Avoid_: Ticket, todo, job

**Board**:
The shared Kanban of Tasks (未着手 / 作業中 / 完了) that the Foreman and every Worker see and move.
_Avoid_: Tracker, backlog

## Life

**Community Poster**:
A 自治会 or city account that publishes Notices; it can post but not see shop data.
_Avoid_: Admin, city user, publisher

**Notice**:
A local rule or announcement (garbage, disaster drill, festival, 自治会 duty) posted in Japanese as text, voice or flyer photo, delivered as plain steps with one Check Question.
_Avoid_: Post, flyer (the flyer is only the source), announcement

**Event**:
A dated or recurring occurrence taken from a Notice (e.g. burnable garbage Mon/Thu before 8:00), confirmed by the poster before publishing.
_Avoid_: Reminder (a reminder is how an Event is shown), schedule

**Ask**:
A Worker's question about local life, answered only from posted Notices with the source quoted, or forwarded to their Senpai when the Notices do not cover it.
_Avoid_: Chat, query, ticket

**Senpai**:
A Japanese coworker or 自治会 volunteer paired with one Worker by the Foreman to help them into local life.
_Avoid_: Buddy, mentor, volunteer (as a role name)

**Meet-up**:
A logged meeting between a Worker and their Senpai (coffee, event, drill).
_Avoid_: Session, appointment

**Support Log**:
The export of Meet-ups and Notice Check results that evidences an employer's life and exchange support for 特定技能1号 Workers.
_Avoid_: Report, audit log
