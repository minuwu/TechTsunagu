package com.techtsunagu.wakatta.core.sync

import android.content.Context
import com.techtsunagu.wakatta.core.database.OutboxStore
import com.techtsunagu.wakatta.core.database.WakattaStores
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.OutboxSender
import com.techtsunagu.wakatta.core.model.OutboxWriter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

class OutboxWriterImpl @Inject constructor(
    private val store: OutboxStore,
    @ApplicationContext private val context: Context,
) : OutboxWriter {
    override suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String) {
        store.enqueue(kind, idempotencyKey, payloadJson)
        OutboxWorker.schedule(context)
    }

    override fun pendingCount(kind: String): Flow<Int> = store.pendingCount(kind)

    override suspend fun takeFailed(kind: String): String? = store.oldestFailed(kind)?.let {
        store.remove(it.key)
        it.payloadJson
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class OutboxModule {
    @Binds abstract fun writer(impl: OutboxWriterImpl): OutboxWriter

    @Multibinds abstract fun senders(): Set<OutboxSender>

    companion object {
        @Provides @Singleton
        fun stores(@ApplicationContext context: Context): WakattaStores = WakattaStores(context)

        @Provides fun outbox(stores: WakattaStores): OutboxStore = stores.outbox

        @Provides fun cache(stores: WakattaStores): CacheStore = stores.cache
    }
}
