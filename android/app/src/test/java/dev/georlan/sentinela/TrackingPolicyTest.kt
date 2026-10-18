package dev.georlan.sentinela

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingPolicyTest {
    @Test fun acceptsBrazilianCoordinates() {
        assertTrue(TrackingPolicy.isUsable(-5.144, -38.099, 25f))
    }

    @Test fun permitsApproximateLocation() {
        assertTrue(TrackingPolicy.isUsable(-5.144, -38.099, 2_000f))
    }

    @Test fun rejectsInvalidCoordinates() {
        assertFalse(TrackingPolicy.isUsable(91.0, -38.0, 10f))
        assertFalse(TrackingPolicy.isUsable(-5.0, -181.0, 10f))
        assertFalse(TrackingPolicy.isUsable(Double.NaN, -38.0, 10f))
    }

    @Test fun rejectsInvalidAccuracy() {
        assertFalse(TrackingPolicy.isUsable(-5.0, -38.0, 0f))
        assertFalse(TrackingPolicy.isUsable(-5.0, -38.0, Float.NaN))
        assertFalse(TrackingPolicy.isUsable(-5.0, -38.0, 20_000f))
    }
}
