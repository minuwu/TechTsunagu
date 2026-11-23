package com.techtsunagu.wakatta.feature.notifications

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.NotificationsKey
import com.techtsunagu.wakatta.core.network.ApiConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: notifications
        entry<NotificationsKey> {
            NotificationsScreen(
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds
    @IntoSet
    abstract fun bindNotificationsEntries(entries: NotificationsEntries): FeatureEntries

    @Binds
    abstract fun bindSenderNames(fake: FakeSenderNames): SenderNames
}

@Module
@InstallIn(SingletonComponent::class)
object NotificationsProvidesModule {

    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: dagger.Lazy<Retrofit>): DeviceApi =
        retrofit.get().create(DeviceApi::class.java)

    @Provides
    @Singleton
    fun provideDeviceRegistrar(
        real: dagger.Lazy<RealDeviceRegistrar>,
        fake: dagger.Lazy<FakeDeviceRegistrar>,
    ): DeviceRegistrar =
        if (ApiConfig.USE_LIVE_API) {
            real.get()
        } else {
            fake.get()
        }

    @Provides
    @Singleton
    fun provideNotificationsRepository(
        local: dagger.Lazy<LocalNotificationsRepository>,
        fake: dagger.Lazy<FakeNotificationsRepository>,
    ): NotificationsRepository =
        if (ApiConfig.USE_LIVE_API) {
            local.get()
        } else {
            fake.get()
        }
}
