package com.techtsunagu.wakatta.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    FOREMAN, WORKER, POSTER, UNKNOWN
}

@Serializable
enum class Lang {
    JA, BN, EN, VI, ID, NE, MY, TL, ZH, UNKNOWN
}

@Serializable
enum class Shift {
    DAY, NIGHT, UNKNOWN
}

@Serializable
enum class SkillStatus {
    VALID, EXPIRING, EXPIRED, NONE, UNKNOWN
}

@Serializable
enum class PanelState {
    IDLE, LOCKED, READY, OVERRIDE, UNKNOWN
}

@Serializable
enum class NoteStatus {
    DRAFT, APPROVED, UNKNOWN
}

@Serializable
enum class ChangeKind {
    SAFETY, WORDING, UNKNOWN
}

@Serializable
enum class CheckTargetKind {
    NOTE, BRIEFING, NOTICE, UNKNOWN
}

@Serializable
enum class AiOutcome {
    OK, RETRIED, FALLBACK_SEED, DEGRADED, TYPED, TRANSCRIBED, UNKNOWN
}

@Serializable
enum class NoticeCategory {
    GARBAGE, DISASTER, EVENT, DUTY, OTHER, UNKNOWN
}

@Serializable
enum class NoticeSourceKind {
    TEXT, VOICE, PHOTO, UNKNOWN
}

@Serializable
enum class NoticeStatus {
    DRAFT, PUBLISHED, UNKNOWN
}

@Serializable
enum class PosterKind {
    JICHIKAI, CITY, EMPLOYER, UNKNOWN
}

@Serializable
enum class SenpaiKind {
    COWORKER, JICHIKAI, UNKNOWN
}

@Serializable
enum class MeetupKind {
    COFFEE, EVENT, DRILL, OTHER, UNKNOWN
}

@Serializable
enum class MeetupLoggedBy {
    WORKER, SENPAI, UNKNOWN
}

@Serializable
enum class WakattaStatus {
    GREEN, ORANGE, RED, WAIT, UNKNOWN
}

@Serializable
enum class MessageKind {
    VOICE, TEXT, ASK, REEXPLAIN, SYSTEM, UNKNOWN
}

@Serializable
enum class FollowUpStatus {
    OPEN, CLARIFIED, UNDERSTOOD, UNKNOWN
}

@Serializable
enum class TaskColumn {
    TODO, DOING, DONE, UNKNOWN
}

@Serializable
enum class ThreadKind {
    GROUP, DIRECT, TALK, UNKNOWN
}

@Serializable
enum class ThreadMemberRole {
    ADMIN, MEMBER, UNKNOWN
}

@Serializable
enum class TalkGrade {
    GREEN, ORANGE, RED, UNKNOWN
}

@Serializable
enum class InterviewMethod {
    FACE, ONLINE, UNKNOWN
}

@Serializable
enum class InterviewStatus {
    OK, SOON, OVERDUE, UNKNOWN
}
