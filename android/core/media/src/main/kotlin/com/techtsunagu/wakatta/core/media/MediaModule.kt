package com.techtsunagu.wakatta.core.media

import com.techtsunagu.wakatta.core.model.PhotoCapture
import com.techtsunagu.wakatta.core.model.QrScanner
import com.techtsunagu.wakatta.core.model.ShareFile
import com.techtsunagu.wakatta.core.model.Speaker
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {
    @Binds @Singleton abstract fun bindDictationSource(impl: DeviceDictation): DictationSource

    @Binds abstract fun bindSpeaker(impl: TtsSpeaker): Speaker

    @Binds abstract fun bindPhotoCapture(impl: PhotoCaptureBroker): PhotoCapture

    @Binds abstract fun bindQrScanner(impl: QrScanBroker): QrScanner

    @Binds abstract fun bindShareFile(impl: AndroidShareFile): ShareFile
}

/** Lets the media composables work without a ViewModel. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaEntryPoint {
    fun qrBroker(): QrScanBroker

    fun dictation(): DictationSource
}
