package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.network.safeApiCall

class RealGlossaryRepository(private val api: () -> SharedDataApi) : GlossaryRepository {

    override suspend fun getTerms(): AppResult<List<GlossaryTerm>> =
        safeApiCall { api().glossary() }.map { list -> list.map { it.toTerm() } }

    override suspend fun addTerm(termJa: String, readingJa: String, termBn: String, termEn: String): AppResult<GlossaryTerm> =
        safeApiCall { api().addTerm(GlossaryInDto(termJa, readingJa, termBn, termEn)) }.map { it.toTerm() }

    private fun GlossaryDto.toTerm() = GlossaryTerm(ja = ja, reading = reading, bn = bn, en = en)
}
