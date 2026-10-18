package dev.georlan.sentinela

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val preferences = TrackingPreferences(context)
        if (!preferences.bootResumeEnabled || !preferences.trackingRequested) return

        // O Android exige localizacao em segundo plano para iniciar um servico
        // de localizacao a partir do boot, alem das restricoes do fabricante.
        if (!PermissionChecks.hasLocation(context) ||
            !PermissionChecks.hasBackgroundLocation(context)
        ) {
            Log.w(TAG, "Boot: permissao de localizacao em segundo plano ausente")
            return
        }
        try {
            val serviceIntent = Intent(context, LocationTrackingService::class.java)
                .setAction(LocationTrackingService.ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (error: RuntimeException) {
            Log.e(TAG, "Boot: Android impediu o reinicio automatico", error)
        }
    }

    companion object {
        private const val TAG = "SentinelaBoot"
    }
}
