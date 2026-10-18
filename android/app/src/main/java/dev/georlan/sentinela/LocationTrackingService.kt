package dev.georlan.sentinela

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.Executors

class LocationTrackingService : Service() {
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val storageExecutor = Executors.newSingleThreadExecutor()
    private val store by lazy { LocationStore(this) }
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) saveIfUsable(location)
        }
    }

    private fun saveIfUsable(location: Location) {
        if (!location.hasAccuracy() ||
            !TrackingPolicy.isUsable(location.latitude, location.longitude, location.accuracy)
        ) return
        val item = SavedPosition(
            location.latitude, location.longitude, location.accuracy, location.time
        )
        storageExecutor.execute {
            try {
                store.save(item)
            } catch (error: Exception) {
                Log.e(TAG, "Falha ao persistir localizacao", error)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID, "Monitoramento de localizacao", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            TrackingPreferences(this).trackingRequested = false
            stopSelf()
            return START_NOT_STICKY
        }
        if (!PermissionChecks.hasLocation(this)) {
            TrackingPreferences(this).trackingRequested = false
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            running = true
            TrackingPreferences(this).trackingRequested = true
            val request = LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, TrackingPolicy.INTERVAL_MS
            )
                .setMinUpdateIntervalMillis(TrackingPolicy.MIN_INTERVAL_MS)
                .setMinUpdateDistanceMeters(TrackingPolicy.MIN_DISTANCE_METERS)
                .setMaxUpdateDelayMillis(TrackingPolicy.MAX_DELAY_MS)
                .build()

            @Suppress("MissingPermission")
            if (PermissionChecks.hasLocation(this)) {
                locationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Falha ao iniciar localizacao", error)
                        TrackingPreferences(this).trackingRequested = false
                        stopSelf()
                    }
            }
            return START_STICKY
        } catch (error: RuntimeException) {
            Log.e(TAG, "Android recusou o servico de localizacao", error)
            TrackingPreferences(this).trackingRequested = false
            stopSelf()
            return START_NOT_STICKY
        }
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, LocationTrackingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Sentinela: monitoramento ativo")
            .setContentText("Localizacao registrada apenas neste aparelho")
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(null, "Parar", stop).build())
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        running = false
        locationClient.removeLocationUpdates(callback)
        storageExecutor.shutdown()
        store.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "dev.georlan.sentinela.START"
        const val ACTION_STOP = "dev.georlan.sentinela.STOP"
        private const val CHANNEL_ID = "tracking"
        private const val NOTIFICATION_ID = 100
        private const val TAG = "SentinelaService"
        @Volatile var running: Boolean = false
            private set
    }
}
