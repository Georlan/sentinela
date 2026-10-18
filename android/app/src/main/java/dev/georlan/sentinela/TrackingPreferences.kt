package dev.georlan.sentinela

import android.content.Context

class TrackingPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("sentinela_tracking", Context.MODE_PRIVATE)

    var bootResumeEnabled: Boolean
        get() = prefs.getBoolean("boot_resume_enabled", false)
        set(value) = prefs.edit().putBoolean("boot_resume_enabled", value).apply()

    var trackingRequested: Boolean
        get() = prefs.getBoolean("tracking_requested", false)
        set(value) = prefs.edit().putBoolean("tracking_requested", value).apply()
}
