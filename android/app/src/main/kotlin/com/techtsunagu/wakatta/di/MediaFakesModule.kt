package com.techtsunagu.wakatta.di

import com.techtsunagu.wakatta.core.model.PhotoCapture
import com.techtsunagu.wakatta.core.model.QrScanner
import com.techtsunagu.wakatta.core.model.ShareFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.emptyFlow
import java.io.File
import javax.inject.Singleton

/**
 * Stand-ins for the device features (microphone, camera, text to speech,
 * share sheet) until their real implementations land in `:core:media`.
 * They do nothing, so screens fall back to typing or a manual code.
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaFakesModule {

    @Provides @Singleton
    fun qrScanner(): QrScanner = object : QrScanner {
        override fun scanResults() = emptyFlow<String>()
    }

    @Provides @Singleton
    fun photoCapture(): PhotoCapture = object : PhotoCapture {
        override suspend fun captureOrPick(): File? = null
    }

    @Provides @Singleton
    fun shareFile(): ShareFile = object : ShareFile {
        override fun share(file: File, mimeType: String) = Unit
    }
}
