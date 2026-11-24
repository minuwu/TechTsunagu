package com.techtsunagu.wakatta.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Turns any picked or captured image (JPEG, HEIC, PNG, WebP) into an upright JPEG with no EXIF or
 * location data, long edge at most 2048 px, at most 1.8 MB (platform contract 4.4).
 * All decoding and encoding runs on the IO dispatcher; at most the sampled source and one scaled copy are alive at once.
 */
@Singleton
class PhotoProcessor @Inject constructor(@ApplicationContext context: Context) {
    private val resolver = context.contentResolver

    /** Served by the FileProvider, see `media_file_paths.xml`. */
    val outputDir: File = File(context.cacheDir, "media")

    suspend fun process(source: Uri): AppResult<File> = withContext(Dispatchers.IO) {
        try {
            run(source)
        } catch (_: IOException) {
            AppResult.Failure(AppError.UnsupportedMedia)
        } catch (_: OutOfMemoryError) {
            AppResult.Failure(AppError.Unknown("photo too large to decode"))
        }
    }

    private fun run(source: Uri): AppResult<File> {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return AppResult.Failure(AppError.UnsupportedMedia)

        val exif = readOrientation(source)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = PhotoPlan.sampleSize(Dim(bounds.outWidth, bounds.outHeight))
        }
        val decoded = resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return AppResult.Failure(AppError.UnsupportedMedia)

        val (degrees, mirror) = PhotoPlan.orientation(exif)
        val oriented = if (PhotoPlan.swapsAxes(exif)) Dim(decoded.height, decoded.width) else Dim(decoded.width, decoded.height)
        var scaled: Bitmap? = null
        var renderedWidth = -1
        try {
            val best = PhotoPlan.compress(oriented) { step ->
                if (scaled == null || renderedWidth != step.width) {
                    release(scaled, decoded)
                    scaled = render(decoded, degrees, mirror, step.width, oriented.width)
                    renderedWidth = step.width
                }
                ByteArrayOutputStream().also { scaled!!.compress(Bitmap.CompressFormat.JPEG, step.quality, it) }.toByteArray()
            } ?: return AppResult.Failure(AppError.PayloadTooLarge)

            outputDir.mkdirs()
            val out = File.createTempFile("photo-", ".jpg", outputDir)
            out.writeBytes(best.second)
            return AppResult.Success(out)
        } finally {
            release(scaled, decoded)
            decoded.recycle()
        }
    }

    private fun readOrientation(source: Uri): Int = try {
        resolver.openInputStream(source)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (_: IOException) {
        ExifInterface.ORIENTATION_NORMAL
    }

    /** One allocation: rotate, mirror and scale the decoded source to [targetWidth] (oriented width). */
    private fun render(src: Bitmap, degrees: Int, mirror: Boolean, targetWidth: Int, orientedWidth: Int): Bitmap {
        val m = Matrix()
        if (degrees != 0) m.postRotate(degrees.toFloat())
        if (mirror) m.postScale(-1f, 1f)
        val s = minOf(1f, targetWidth.toFloat() / orientedWidth)
        if (s < 1f) m.postScale(s, s)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    private fun release(bitmap: Bitmap?, keep: Bitmap) {
        if (bitmap != null && bitmap !== keep) bitmap.recycle()
    }
}
