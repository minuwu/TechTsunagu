package com.techtsunagu.wakatta.feature.machines.domain

import com.techtsunagu.wakatta.core.model.AppResult

interface NoteRepository {
    suspend fun getNoteVersion(machineId: String): AppResult<NoteVersionDto>
    suspend fun getVersions(machineId: String): AppResult<List<NoteVersionDto>>
    suspend fun getVersion(versionId: String): AppResult<NoteVersionDto>
}
