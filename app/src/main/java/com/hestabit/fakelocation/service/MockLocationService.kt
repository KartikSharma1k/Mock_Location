package com.hestabit.fakelocation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class MockLocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var isMocking = false
    private var routePoints: List<LatLng> = emptyList()
    private var speedKmh: Double = 60.0

    private val prefs by lazy { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If intent is null (service restarted by system), try to load persisted route
        if (intent == null) {
            val persisted = loadPersistedRoute()
            if (persisted != null) {
                routePoints = persisted.first
                speedKmh = persisted.second
                // start from persisted index
                startMocking(persisted.third)
            } else {
                // Nothing to do, let the service stay alive but idle
            }
            return START_STICKY
        }

        when (intent.action) {
            ACTION_START -> {
                val points = intent.getParcelableArrayListExtra<LatLng>(EXTRA_ROUTE)
                val speed = intent.getDoubleExtra(EXTRA_SPEED, 60.0)
                if (!points.isNullOrEmpty()) {
                    routePoints = points
                    speedKmh = speed
                    persistRoute(routePoints, speedKmh)
                    startMocking(0)
                }
            }
            ACTION_STOP -> {
                stopMocking()
            }
        }
        return START_STICKY
    }

    private fun persistRoute(points: List<LatLng>, speed: Double) {
        try {
            val arr = JSONArray()
            for (p in points) {
                val o = JSONObject()
                o.put("lat", p.latitude)
                o.put("lng", p.longitude)
                arr.put(o)
            }
            prefs.edit().putString(KEY_ROUTE_JSON, arr.toString()).putFloat(KEY_SPEED, speed.toFloat()).putInt(KEY_INDEX, 0).apply()
            Log.d("MockLocationService", "Persisted route with ${points.size} points, speed=$speed")
        } catch (e: Exception) {
            Log.w("MockLocationService", "Failed to persist route: ${e.message}")
        }
    }

    private fun persistIndex(index: Int) {
        try {
            prefs.edit().putInt(KEY_INDEX, index).apply()
        } catch (e: Exception) {
            Log.w("MockLocationService", "Failed to persist index: ${e.message}")
        }
    }

    private fun clearPersistedRoute() {
        prefs.edit().remove(KEY_ROUTE_JSON).remove(KEY_SPEED).remove(KEY_INDEX).apply()
    }

    // returns Triple(points, speed, index)
    private fun loadPersistedRoute(): Triple<List<LatLng>, Double, Int>? {
        try {
            val json = prefs.getString(KEY_ROUTE_JSON, null) ?: return null
            val speed = prefs.getFloat(KEY_SPEED, 60.0f).toDouble()
            val index = prefs.getInt(KEY_INDEX, 0)
            val arr = JSONArray(json)
            val list = mutableListOf<LatLng>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val lat = o.getDouble("lat")
                val lng = o.getDouble("lng")
                list.add(LatLng(lat, lng))
            }
            Log.d("MockLocationService", "Loaded persisted route with ${list.size} points, speed=$speed, index=$index")
            return Triple(list, speed, index)
        } catch (e: Exception) {
            Log.w("MockLocationService", "Failed to load persisted route: ${e.message}")
            return null
        }
    }

    // startIndex: index of the target point we should head to next (0 means start from point 0 -> target 1)
    private fun startMocking(startIndex: Int) {
        if (isMocking) return
        isMocking = true
        startForeground(NOTIFICATION_ID, createNotification())

        serviceScope.launch {
            val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MockLocationService)
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Try fused mock mode first (best-effort)
            val useFused = tryEnableFusedMockMode(fusedClient)
            if (!useFused) {
                Log.w("MockLocationService", "Fused mock mode unavailable — falling back to LocationManager test provider")
                try {
                    try { locationManager.removeTestProvider(LocationManager.GPS_PROVIDER) } catch (e: Exception) {}
                    locationManager.addTestProvider(
                        LocationManager.GPS_PROVIDER,
                        false, false, false, false, true, true, true, 0, 5
                    )
                    locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
                } catch (e: SecurityException) {
                    Log.e("MockLocationService", "No permission to add test provider: ${e.message}")
                } catch (e: Exception) {
                    Log.e("MockLocationService", "Failed to add test provider: ${e.message}")
                }
            }

            if (!useFused && !locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                Log.e("MockLocationService", "No available mock method (fused disabled and test provider not available). Stopping service.")
                stopMocking()
                return@launch
            }

            var currentPoint: LatLng
            var targetPointIndex: Int
            if (startIndex <= 0) {
                currentPoint = routePoints.getOrNull(0) ?: run {
                    Log.w("MockLocationService", "No route points available to simulate")
                    stopMocking()
                    return@launch
                }
                targetPointIndex = 1
            } else {
                // startIndex refers to the next target index
                targetPointIndex = startIndex.coerceAtMost(routePoints.size - 1)
                currentPoint = routePoints.getOrNull(targetPointIndex - 1) ?: routePoints.getOrNull(0) ?: run {
                    Log.w("MockLocationService", "No route points available to simulate")
                    stopMocking()
                    return@launch
                }
            }

            val speedMps = speedKmh / 3.6
            val updateInterval = 1000L

            // notify UI that simulation started
            try {
                val startedIntent = Intent(ACTION_SIMULATION_STARTED).apply { putExtra(EXTRA_POINTS_COUNT, routePoints.size) }
                sendBroadcast(startedIntent)
            } catch (e: Exception) {
                Log.w("MockLocationService", "Failed to send simulation started broadcast: ${e.message}")
            }

            // Push initial point
            if (useFused) pushFusedMockLocation(fusedClient, currentPoint, speedMps) else pushTestProviderLocation(locationManager, currentPoint)

            while (isMocking && targetPointIndex < routePoints.size) {
                val targetPoint = routePoints[targetPointIndex]
                val results = floatArrayOf(0f)
                android.location.Location.distanceBetween(
                    currentPoint.latitude, currentPoint.longitude,
                    targetPoint.latitude, targetPoint.longitude,
                    results
                )
                val distanceToTarget = results[0]
                val distancePerStep = speedMps * (updateInterval / 1000.0)

                if (distanceToTarget > distancePerStep) {
                    val fraction = distancePerStep / distanceToTarget
                    val newLat = currentPoint.latitude + (targetPoint.latitude - currentPoint.latitude) * fraction
                    val newLng = currentPoint.longitude + (targetPoint.longitude - currentPoint.longitude) * fraction
                    currentPoint = LatLng(newLat, newLng)

                    if (useFused) pushFusedMockLocation(fusedClient, currentPoint, speedMps) else pushTestProviderLocation(locationManager, currentPoint)
                } else {
                    currentPoint = targetPoint
                    if (useFused) pushFusedMockLocation(fusedClient, currentPoint, speedMps) else pushTestProviderLocation(locationManager, currentPoint)
                    targetPointIndex++
                    // persist progress (the next target index)
                    persistIndex(targetPointIndex)
                }

                delay(updateInterval)
            }

            // notify UI that simulation stopped
            try {
                val stoppedIntent = Intent(ACTION_SIMULATION_STOPPED)
                sendBroadcast(stoppedIntent)
            } catch (e: Exception) {
                Log.w("MockLocationService", "Failed to send simulation stopped broadcast: ${e.message}")
            }

            // Cleanup
            if (useFused) {
                try { fusedClient.setMockMode(false) } catch (e: Exception) { Log.w("MockLocationService","Failed to disable fused mock mode: ${e.message}") }
            } else {
                try { locationManager.removeTestProvider(LocationManager.GPS_PROVIDER) } catch (e: Exception) { Log.w("MockLocationService","Failed to remove test provider: ${e.message}") }
            }

            // clear persisted route when finished
            clearPersistedRoute()

            stopMocking()
        }
    }

    private suspend fun tryEnableFusedMockMode(fused: FusedLocationProviderClient, retries: Int = 3, delayMs: Long = 250L): Boolean {
        repeat(retries) { attempt ->
            val ok = enableFusedMockMode(fused)
            if (ok) return true
            Log.w("MockLocationService", "enableFusedMockMode attempt ${attempt + 1} failed, retrying in ${delayMs}ms")
            delay(delayMs)
        }
        return false
    }

    private suspend fun enableFusedMockMode(fused: FusedLocationProviderClient): Boolean {
        return suspendCancellableCoroutine { cont ->
            try {
                fused.setMockMode(true)
                    .addOnSuccessListener { cont.resume(true) }
                    .addOnFailureListener { e -> Log.e("MockLocationService","setMockMode(true) failed", e); cont.resume(false) }
            } catch (e: Exception) { Log.e("MockLocationService","enableFusedMockMode exception: ${e.message}"); cont.resume(false) }
        }
    }

    private suspend fun pushFusedMockLocation(fused: FusedLocationProviderClient, point: LatLng, speedMps: Double) {
        val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = point.latitude
            longitude = point.longitude
            altitude = 0.0
            time = System.currentTimeMillis()
            accuracy = 5.0f
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bearingAccuracyDegrees = 10f
                verticalAccuracyMeters = 1f
                speedAccuracyMetersPerSecond = 1f
            }
            try { this.speed = speedMps.toFloat() } catch (e: Exception) {}
        }
        try {
            Log.d("MockLocationService", "Pushing fused mock location: ${mockLocation.latitude}, ${mockLocation.longitude}, speed=${mockLocation.speed}")
            suspendCancellableCoroutine<Unit> { cont ->
                fused.setMockLocation(mockLocation)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { e -> Log.e("MockLocationService","setMockLocation failed", e); cont.resume(Unit) }
            }
        } catch (e: Exception) {
            Log.e("MockLocationService","Exception pushFusedMockLocation: ${e.message}", e)
        }
    }

    private fun pushTestProviderLocation(locationManager: LocationManager, point: LatLng) {
        val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = point.latitude
            longitude = point.longitude
            altitude = 0.0
            time = System.currentTimeMillis()
            accuracy = 5.0f
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bearingAccuracyDegrees = 10f
                verticalAccuracyMeters = 1f
                speedAccuracyMetersPerSecond = 1f
            }
        }
        try {
            Log.d("MockLocationService", "Pushing test-provider mock location: ${mockLocation.latitude}, ${mockLocation.longitude}")
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
        } catch (e: Exception) {
            Log.e("MockLocationService", "setTestProviderLocation failed: ${e.message}")
        }
    }

    private fun stopMocking() {
        isMocking = false
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (e: Exception) {}
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "mock_location_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Mock Location Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        // Create stop intent for notification action
        val stopIntent = Intent(this, MockLocationService::class.java).apply { action = ACTION_STOP }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        val stopPending = PendingIntent.getService(this, 0, stopIntent, flags)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mock Location Active")
            .setContentText("Simulating location movement ($speedKmh km/h)")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_ROUTE = "EXTRA_ROUTE"
        const val EXTRA_SPEED = "EXTRA_SPEED"
        const val NOTIFICATION_ID = 1
        const val ACTION_SIMULATION_STARTED = "ACTION_SIMULATION_STARTED"
        const val ACTION_SIMULATION_STOPPED = "ACTION_SIMULATION_STOPPED"
        const val ACTION_SIMULATION_ERROR = "ACTION_SIMULATION_ERROR"
        const val EXTRA_POINTS_COUNT = "EXTRA_POINTS_COUNT"
        const val EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE"

        // persistence keys so the service can resume after process death
        private const val PREFS_NAME = "mock_location_service_prefs"
        private const val KEY_ROUTE_JSON = "key_route_json"
        private const val KEY_SPEED = "key_speed"
        private const val KEY_INDEX = "key_index"
    }
}
