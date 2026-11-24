package com.techtsunagu.wakatta.core.media

/** Platform contract 4.4: JPEG, long edge at most 2048 px, at most 1.8 MB (the server limit is 2 MB). */
object PhotoLimits {
    const val MAX_EDGE_PX = 2048
    const val MAX_BYTES = 1_800_000
    const val MIN_EDGE_PX = 320
    val QUALITIES = intArrayOf(85, 75, 65)
    const val SHRINK = 0.75f
}

data class Dim(val width: Int, val height: Int) {
    val longEdge: Int get() = maxOf(width, height)
}

data class EncodeStep(val width: Int, val height: Int, val quality: Int)

/** Pure size, rotation and quality-ladder logic. No Android types. */
object PhotoPlan {
    /** Same shape, long edge at most [maxEdge]. Never enlarges. */
    fun fit(src: Dim, maxEdge: Int = PhotoLimits.MAX_EDGE_PX): Dim {
        if (src.longEdge <= maxEdge) return src
        val s = maxEdge.toFloat() / src.longEdge
        return Dim(maxOf(1, Math.round(src.width * s)), maxOf(1, Math.round(src.height * s)))
    }

    /** Largest power of two that keeps the decoded long edge at or above [target] (bounded decode memory). */
    fun sampleSize(src: Dim, target: Int = PhotoLimits.MAX_EDGE_PX): Int {
        var s = 1
        while (src.longEdge / (s * 2) >= target) s *= 2
        return s
    }

    /** EXIF orientation (1..8) to clockwise degrees plus a horizontal mirror applied after the rotation. */
    fun orientation(exif: Int): Pair<Int, Boolean> = when (exif) {
        2 -> 0 to true
        3 -> 180 to false
        4 -> 180 to true
        5 -> 90 to true
        6 -> 90 to false
        7 -> 270 to true
        8 -> 270 to false
        else -> 0 to false
    }

    fun swapsAxes(exif: Int): Boolean = orientation(exif).first % 180 != 0

    /**
     * The tries in order: quality 85, 75, 65 at the fitted size, then the size shrinks by 25 percent
     * and the qualities start again, until the edge would drop under [PhotoLimits.MIN_EDGE_PX].
     */
    fun ladder(src: Dim): Sequence<EncodeStep> = sequence {
        var d = fit(src)
        while (true) {
            for (q in PhotoLimits.QUALITIES) yield(EncodeStep(d.width, d.height, q))
            val next = Dim(Math.round(d.width * PhotoLimits.SHRINK), Math.round(d.height * PhotoLimits.SHRINK))
            if (next.longEdge < PhotoLimits.MIN_EDGE_PX) return@sequence
            d = next
        }
    }

    /** Runs [encode] down the ladder; the first result within [maxBytes] wins, null if none fits. */
    fun compress(
        src: Dim,
        maxBytes: Int = PhotoLimits.MAX_BYTES,
        encode: (EncodeStep) -> ByteArray,
    ): Pair<EncodeStep, ByteArray>? {
        for (step in ladder(src)) {
            val bytes = encode(step)
            if (bytes.size <= maxBytes) return step to bytes
        }
        return null
    }
}
