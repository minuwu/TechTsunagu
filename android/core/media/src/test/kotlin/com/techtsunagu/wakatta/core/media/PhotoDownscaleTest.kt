package com.techtsunagu.wakatta.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoDownscaleTest {

    @Test
    fun calculateTargetDimensions_alreadyWithinLimit_returnsOriginal() {
        val dims = PhotoDownscale.calculateTargetDimensions(1024, 768)
        assertEquals(1024, dims.width)
        assertEquals(768, dims.height)
    }

    @Test
    fun calculateTargetDimensions_squareAtLimit_returnsLimit() {
        val dims = PhotoDownscale.calculateTargetDimensions(2048, 2048)
        assertEquals(2048, dims.width)
        assertEquals(2048, dims.height)
    }

    @Test
    fun calculateTargetDimensions_widerLandscape_scalesWidthToLimit() {
        val dims = PhotoDownscale.calculateTargetDimensions(4096, 2048)
        assertEquals(2048, dims.width)
        assertEquals(1024, dims.height)
    }

    @Test
    fun calculateTargetDimensions_tallerPortrait_scalesHeightToLimit() {
        val dims = PhotoDownscale.calculateTargetDimensions(1000, 4000)
        assertEquals(512, dims.width)
        assertEquals(2048, dims.height)
    }

    @Test
    fun isWithinSizeLimit_checksFileBounds() {
        assertTrue(PhotoDownscale.isWithinSizeLimit(1000L))
        assertTrue(PhotoDownscale.isWithinSizeLimit(2 * 1024 * 1024L)) // 2 MB
        assertFalse(PhotoDownscale.isWithinSizeLimit(2 * 1024 * 1024L + 1L))
        assertFalse(PhotoDownscale.isWithinSizeLimit(0L))
        assertFalse(PhotoDownscale.isWithinSizeLimit(-10L))
    }
}
