package com.techtsunagu.wakatta.feature.update

import android.content.Context
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.UpdateKey
import com.techtsunagu.wakatta.core.network.ApiConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.techtsunagu.wakatta.core.model.VersionGate as ModelVersionGate

@Singleton
class UpdateEntries @Inject constructor(private val sessionStore: SessionStore) : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: update
        entry<UpdateKey> {
            val scope = rememberCoroutineScope()
            UpdateScreen(
                onBackClick = { navigator.pop() },
                onSignOutClick = { scope.launch { sessionStore.clear() } },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UpdateModule {
    @Binds
    @IntoSet
    abstract fun bindUpdateEntries(entries: UpdateEntries): FeatureEntries

    @Binds
    abstract fun bindVersionGate(gate: UpdateVersionGate): ModelVersionGate
}

/** Provides update dependencies; chosen by ApiConfig.USE_LIVE_API. */
@Module
@InstallIn(SingletonComponent::class)
object UpdateProvidesModule {
    private val client by lazy { OkHttpClient() }

    @Provides
    @Singleton
    fun provideAppConfigApi(retrofit: dagger.Lazy<Retrofit>): AppConfigApi =
        retrofit.get().create(AppConfigApi::class.java)

    @Provides
    @Singleton
    fun provideAppConfigRepository(
        config: AppConfig,
        api: dagger.Lazy<AppConfigApi>,
    ): AppConfigRepository =
        if (ApiConfig.USE_LIVE_API) {
            FallbackAppConfigRepository(
                primary = ApiAppConfigRepository(
                    api = { api.get() },
                    currentVersionName = config.versionName,
                ),
                fallback = StaticManifestAppConfigRepository(
                    client = client,
                    manifestUrl = config.updateManifestUrl,
                    currentVersionCode = config.versionCode.toLong(),
                ),
            )
        } else {
            FakeAppConfigRepository()
        }

    @Provides
    fun provideApkDownloader(@ApplicationContext context: Context, config: AppConfig): ApkDownloader =
        if (!ApiConfig.USE_LIVE_API && config.isDebug && config.updateManifestUrl.isBlank()) {
            FakeApkDownloader()
        } else {
            OkHttpApkDownloader(client, File(context.cacheDir, "update"))
        }

    @Provides
    fun provideApkInstaller(@ApplicationContext context: Context, config: AppConfig): ApkInstaller =
        if (!ApiConfig.USE_LIVE_API && config.isDebug && config.updateManifestUrl.isBlank()) {
            FakeApkInstaller()
        } else {
            PackageInstallerApkInstaller(context)
        }
}
