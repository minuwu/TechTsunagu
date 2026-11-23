package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.feature.machines.domain.NoteRepository
import com.techtsunagu.wakatta.feature.machines.domain.PanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.ShiftAssignmentRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import retrofit2.Retrofit

/** Real (live backend) when `ApiConfig.USE_LIVE_API`, else the Fake. Nothing touches the network until first use. */
@Module
@InstallIn(SingletonComponent::class)
object MachinesDataModule {

    @Provides
    @Singleton
    fun providePanelRepository(
        fake: Provider<FakePanelRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
        workers: WorkerRepository,
        signOffs: SignOffRepository,
    ): PanelRepository =
        if (ApiConfig.USE_LIVE_API) RealPanelRepository(apiOf(retrofit), session, workers, signOffs) else fake.get()

    @Provides
    @Singleton
    fun provideShiftAssignmentRepository(
        fake: Provider<FakeShiftAssignmentRepository>,
        retrofit: Lazy<Retrofit>,
        workers: WorkerRepository,
    ): ShiftAssignmentRepository =
        if (ApiConfig.USE_LIVE_API) RealShiftAssignmentRepository(apiOf(retrofit), workers) else fake.get()

    @Provides
    @Singleton
    fun provideNoteRepository(
        fake: Provider<FakeNoteRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
    ): NoteRepository =
        if (ApiConfig.USE_LIVE_API) RealNoteRepository(apiOf(retrofit), session) else fake.get()

    private fun apiOf(retrofit: Lazy<Retrofit>): () -> MachinesApi {
        val api = lazy { retrofit.get().create(MachinesApi::class.java) }
        return { api.value }
    }
}
