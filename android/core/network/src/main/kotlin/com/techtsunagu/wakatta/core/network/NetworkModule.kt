package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/** Nothing here touches the network until an API is first used. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        config: AppConfig,
        sessionStore: SessionStore,
        languageManager: LanguageManager,
    ): OkHttpClient = ApiClientFactory.okHttp(
        versionName = config.versionName,
        language = { languageManager.currentLanguage.value },
        sessionStore = sessionStore,
        debug = config.isDebug,
    )

    @Provides
    @Singleton
    fun provideRetrofit(config: AppConfig, client: OkHttpClient): Retrofit =
        ApiClientFactory.retrofit(ApiConfig.baseUrl(config.apiBaseUrl), client)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}
