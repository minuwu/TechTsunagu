package com.techtsunagu.wakatta.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoPlanTest {
    @Test fun fitKeepsShapeAndNeverEnlarges() {
        assertEquals(Dim(2048, 1536), PhotoPlan.fit(Dim(4000, 3000)))
        assertEquals(Dim(1536, 2048), PhotoPlan.fit(Dim(3000, 4000)))
        assertEquals(Dim(800, 600), PhotoPlan.fit(Dim(800, 600)))
    }

    @Test fun sampleSizeKeepsDecodedEdgeAtOrAboveTarget() {
        assertEquals(1, PhotoPlan.sampleSize(Dim(4000, 3000)))
        assertEquals(2, PhotoPlan.sampleSize(Dim(4096, 3000)))
        assertEquals(4, PhotoPlan.sampleSize(Dim(8192, 6000)))
        assertEquals(1, PhotoPlan.sampleSize(Dim(1000, 800)))
    }

    @Test fun orientationMapsExifValues() {
        assertEquals(0 to false, PhotoPlan.orientation(1))
        assertEquals(90 to false, PhotoPlan.orientation(6))
        assertEquals(270 to false, PhotoPlan.orientation(8))
        assertEquals(180 to false, PhotoPlan.orientation(3))
        assertEquals(0 to false, PhotoPlan.orientation(0))
        assertTrue(PhotoPlan.swapsAxes(6))
        assertFalse(PhotoPlan.swapsAxes(3))
    }

    @Test fun ladderTriesQualitiesThenShrinksByAQuarter() {
        val steps = PhotoPlan.ladder(Dim(4000, 3000)).take(6).toList()
        assertEquals(listOf(85, 75, 65, 85, 75, 65), steps.map { it.quality })
        assertEquals(Dim(2048, 1536), Dim(steps[0].width, steps[0].height))
        assertEquals(Dim(1536, 1152), Dim(steps[3].width, steps[3].height))
    }

    @Test fun ladderEndsBeforeTinyImages() {
        val last = PhotoPlan.ladder(Dim(4000, 3000)).last()
        assertTrue(maxOf(last.width, last.height) >= PhotoLimits.MIN_EDGE_PX)
    }

    @Test fun compressStopsAtFirstResultWithinLimit() {
        val tried = mutableListOf<EncodeStep>()
        val result = PhotoPlan.compress(Dim(4000, 3000)) { step ->
            tried += step
            ByteArray(if (step.quality > 65 || step.width > 1600) PhotoLimits.MAX_BYTES + 1 else 1_000)
        }!!
        assertEquals(EncodeStep(1536, 1152, 65), result.first)
        assertEquals(1_000, result.second.size)
        assertEquals(6, tried.size)
    }

    @Test fun compressReturnsNullWhenNothingFits() {
        assertNull(PhotoPlan.compress(Dim(4000, 3000)) { ByteArray(PhotoLimits.MAX_BYTES + 1) })
    }

    @Test fun smallPhotoIsNotEnlargedAndFitsFirstTry() {
        val result = PhotoPlan.compress(Dim(800, 600)) { ByteArray(10) }!!
        assertEquals(EncodeStep(800, 600, 85), result.first)
    }
}
