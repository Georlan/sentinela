package dev.georlan.sentinela

/** Metas para modo economico, nao garantias de intervalo impostas ao Android. */
object TrackingPolicy {
    const val INTERVAL_MS = 120_000L
    const val MIN_INTERVAL_MS = 60_000L
    const val MAX_DELAY_MS = 300_000L
    const val MIN_DISTANCE_METERS = 50f
    const val MAX_ACCEPTED_ACCURACY_METERS = 10_000f

    fun isUsable(latitude: Double, longitude: Double, accuracyMeters: Float): Boolean {
        return latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0 &&
            accuracyMeters.isFinite() && accuracyMeters > 0f &&
            accuracyMeters <= MAX_ACCEPTED_ACCURACY_METERS
    }
}
