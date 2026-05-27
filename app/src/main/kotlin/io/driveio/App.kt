package io.driveio

import kotlin.math.max

data class SpeedSample(
    val timestampMillis: Long,
    val speedMetersPerSecond: Double,
)

data class DrivingAnalysis(
    val averageSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val distanceKm: Double,
    val overspeedEvents: Int,
    val harshBrakingEvents: Int,
)

class DriveTracker(private val speedLimitKmh: Double = 100.0) {
    private val samples = mutableListOf<SpeedSample>()

    fun addSample(sample: SpeedSample) {
        require(sample.timestampMillis >= 0) { "timestampMillis must be non-negative" }
        require(sample.speedMetersPerSecond >= 0.0) { "speedMetersPerSecond must be non-negative" }
        if (samples.isNotEmpty()) {
            require(sample.timestampMillis > samples.last().timestampMillis) {
                "Samples must be added in strictly increasing timestamp order"
            }
        }
        samples += sample
    }

    fun analyze(): DrivingAnalysis {
        if (samples.isEmpty()) {
            return DrivingAnalysis(0.0, 0.0, 0.0, 0, 0)
        }

        val kmhSpeeds = samples.map { it.speedMetersPerSecond * 3.6 }
        val averageSpeedKmh = kmhSpeeds.average()
        val maxSpeedKmh = kmhSpeeds.maxOrNull() ?: 0.0
        val overspeedEvents = kmhSpeeds.count { it > speedLimitKmh }

        var distanceMeters = 0.0
        var harshBrakingEvents = 0

        for (index in 1 until samples.size) {
            val previous = samples[index - 1]
            val current = samples[index]
            val deltaTimeSeconds = (current.timestampMillis - previous.timestampMillis) / 1000.0

            val averageIntervalSpeed = (previous.speedMetersPerSecond + current.speedMetersPerSecond) / 2.0
            distanceMeters += averageIntervalSpeed * deltaTimeSeconds

            val acceleration = (current.speedMetersPerSecond - previous.speedMetersPerSecond) / deltaTimeSeconds
            if (acceleration < -3.5) {
                harshBrakingEvents += 1
            }
        }

        return DrivingAnalysis(
            averageSpeedKmh = averageSpeedKmh,
            maxSpeedKmh = maxSpeedKmh,
            distanceKm = distanceMeters / 1000.0,
            overspeedEvents = overspeedEvents,
            harshBrakingEvents = harshBrakingEvents,
        )
    }
}

class App {
    fun summaryLine(analysis: DrivingAnalysis): String {
        val roundedAverage = "%.1f".format(analysis.averageSpeedKmh)
        val roundedMax = "%.1f".format(analysis.maxSpeedKmh)
        val roundedDistance = "%.2f".format(analysis.distanceKm)
        return "Avg ${roundedAverage}km/h | Max ${roundedMax}km/h | Distance ${roundedDistance}km"
    }
}

fun main() {
    val tracker = DriveTracker(speedLimitKmh = 90.0)
    tracker.addSample(SpeedSample(0L, 0.0))
    tracker.addSample(SpeedSample(10_000L, 15.0))
    tracker.addSample(SpeedSample(20_000L, 22.0))

    val analysis = tracker.analyze()
    println(App().summaryLine(analysis))
}
