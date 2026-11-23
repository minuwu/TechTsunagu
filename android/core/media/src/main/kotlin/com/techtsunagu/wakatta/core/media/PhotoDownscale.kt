package com.techtsunagu.wakatta.core.media

data class TargetDimensions(val width: Int, val height: Int)

object PhotoDownscale {
    const val MAX_DIMENSION: Int = 2048
    const val MAX_FILE_BYTES: Long = 2 * 1024 * 1024L // 2 MB
    const val FORMAT: String = "JPEG"

    /**
     * Calculates target dimensions preserving aspect ratio such that neither width nor height exceeds maxDimension.
     */
    fun calculateTargetDimensions(width: Int, height: Int, maxDimension: Int = MAX_DIMENSION): TargetDimensions {
        require(width > 0 && height > 0) { "Dimensions must be positive: width=$width, height=$height" }
        if (width <= maxDimension && height <= maxDimension) {
            return TargetDimensions(width, height)
        }
        val ratio = width.toDouble() / height.toDouble()
        return if (width >= height) {
            val targetWidth = maxDimension
            val targetHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
            TargetDimensions(targetWidth, targetHeight)
        } else {
            val targetHeight = maxDimension
            val targetWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
            TargetDimensions(targetWidth, targetHeight)
        }
    }

    fun isWithinSizeLimit(fileSizeBytes: Long): Boolean = fileSizeBytes in 1..MAX_FILE_BYTES
}
