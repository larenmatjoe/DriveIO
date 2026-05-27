package io.driveio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AppTest {
    @Test
    fun analyzeCalculatesDrivingMetrics() {
        val tracker = DriveTracker(speedLimitKmh = 80.0)
        tracker.addSample(SpeedSample(0L, 0.0))
        tracker.addSample(SpeedSample(10_000L, 20.0))
        tracker.addSample(SpeedSample(20_000L, 10.0))

        val analysis = tracker.analyze()

        assertEquals(36.0, analysis.averageSpeedKmh, 0.001)
        assertEquals(72.0, analysis.maxSpeedKmh, 0.001)
        assertEquals(0.25, analysis.distanceKm, 0.001)
        assertEquals(0, analysis.overspeedEvents)
        assertEquals(0, analysis.harshBrakingEvents)
    }

    @Test
    fun analyzeTracksOverspeedAndHarshBraking() {
        val tracker = DriveTracker(speedLimitKmh = 50.0)
        tracker.addSample(SpeedSample(0L, 25.0))
        tracker.addSample(SpeedSample(5_000L, 5.0))

        val analysis = tracker.analyze()

        assertEquals(1, analysis.overspeedEvents)
        assertEquals(1, analysis.harshBrakingEvents)
    }

    @Test
    fun addSampleRejectsOutOfOrderTimestamp() {
        val tracker = DriveTracker()
        tracker.addSample(SpeedSample(5_000L, 5.0))

        assertThrows(IllegalArgumentException::class.java) {
            tracker.addSample(SpeedSample(5_000L, 10.0))
        }
    }

    @Test
    fun addSampleRejectsNegativeSpeed() {
        val tracker = DriveTracker()

        assertThrows(IllegalArgumentException::class.java) {
            tracker.addSample(SpeedSample(1_000L, -1.0))
        }
    }
}
