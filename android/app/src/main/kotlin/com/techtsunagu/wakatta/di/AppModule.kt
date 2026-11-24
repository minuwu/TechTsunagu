package com.techtsunagu.wakatta.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.techtsunagu.wakatta.BuildConfig
import com.techtsunagu.wakatta.FakeSessionStore
import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.common.LanguageManagerImpl
import com.techtsunagu.wakatta.core.datastore.DataStoreSettingsStore
import com.techtsunagu.wakatta.core.datastore.EncryptedSessionStore
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SettingsStore
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.TopLevelBackStack
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = AppConfig(
        useMock = BuildConfig.USE_MOCK,
        apiBaseUrl = BuildConfig.API_BASE_URL,
        updateManifestUrl = BuildConfig.UPDATE_MANIFEST_URL,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        isDebug = BuildConfig.DEBUG,
    )

    @Provides
    @Singleton
    fun provideTopLevelBackStack(): TopLevelBackStack = TopLevelBackStack()

    @Provides
    @Singleton
    fun provideNavigator(backStack: TopLevelBackStack): Navigator = backStack

    @Provides
    @Singleton
    fun provideLanguageManager(): LanguageManager = LanguageManagerImpl(initialLang = "ja")

    @Provides
    @Singleton
    fun provideSettingsStore(@ApplicationContext context: Context): SettingsStore =
        DataStoreSettingsStore(PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("settings.preferences_pb") }))

    @Provides
    @Singleton
    fun provideSessionStore(
        @ApplicationContext context: Context,
        fakeSessionStore: FakeSessionStore,
    ): SessionStore {
        if (ApiConfig.USE_LIVE_API || !BuildConfig.USE_MOCK) {
            AeadConfig.register()
            val keyset = {
                AndroidKeysetManager.Builder()
                    .withSharedPref(context, "session_keyset", KEYSET_PREFS)
                    .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                    .withMasterKeyUri("android-keystore://session_master_key")
                    .build()
                    .keysetHandle
            }
            // A keyset the Keystore cannot open any more (restored backup, reset Keystore) means: sign in again.
            val handle = try {
                keyset()
            } catch (_: Exception) {
                context.deleteSharedPreferences(KEYSET_PREFS)
                keyset()
            }
            return EncryptedSessionStore(
                dataStore = PreferenceDataStoreFactory.create(
                    produceFile = { context.preferencesDataStoreFile("session.preferences_pb") },
                ),
                aead = handle.getPrimitive(Aead::class.java),
            )
        }
        return fakeSessionStore
    }

    private const val KEYSET_PREFS = "session_keyset_prefs"
}
