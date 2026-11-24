package com.techtsunagu.wakatta.core.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.techtsunagu.wakatta.core.model.PhotoCapture
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class PhotoRequest(val cameraUri: Uri, val reply: CompletableDeferred<Uri?>)

/** What a ViewModel injects. It holds no Activity: [PhotoCaptureHost] serves the request. */
@Singleton
class PhotoCaptureBroker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val processor: PhotoProcessor,
) : PhotoCapture {
    internal val request = MutableStateFlow<PhotoRequest?>(null)

    override suspend fun captureOrPick(): File? {
        processor.outputDir.mkdirs()
        val raw = File(processor.outputDir, "camera.jpg")
        val mine = PhotoRequest(
            FileProvider.getUriForFile(context, "${context.packageName}.media.fileprovider", raw),
            CompletableDeferred(),
        )
        if (!request.compareAndSet(null, mine)) return null
        return try {
            mine.reply.await()?.let { processor.process(it).getOrNull() }
        } finally {
            request.value = null
            raw.delete()
        }
    }
}
