package com.hestabit.fakelocation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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

@AndroidEntryPoint
class MockLocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var isMocking = false
    private var routePoints: List<LatLng> = emptyList()
    private var speedKmh: Double = 60.0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val points = intent.getParcelableArrayListExtra<LatLng>(EXTRA_ROUTE)
                val speed = intent.getDoubleExtra(EXTRA_SPEED, 60.0)
                if (!points.isNullOrEmpty()) {
                    routePoints = points
                    speedKmh = speed
                    startMocking()
                }
            }
            ACTION_STOP -> {
                stopMocking()
            }
        }
        return START_STICKY
    }

    private fun startMocking() {
        if (isMocking) return
        isMocking = true
        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            try {
                // Remove existing if any, to ensure clean state
                try { locationManager.removeTestProvider(LocationManager.GPS_PROVIDER) } catch (e: Exception) {}

                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, false, false, false, true, true, true, 0, 5
                )
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            } catch (e: SecurityException) {
                stopSelf()
                return@launch
            } catch (e: IllegalArgumentException) {
                 // Provider already exists
            }

            var currentPoint = routePoints[0]
            var targetPointIndex = 1
            // Convert speed to m/s
            val speedMps = speedKmh / 3.6
            val updateInterval = 1000L // 1 second update

            // Push initial point
            pushMockLocation(locationManager, currentPoint)

            while (isMocking && targetPointIndex < routePoints.size) {
                val targetPoint = routePoints[targetPointIndex]
                val results = floatArrayOf(0f)
                Location.distanceBetween(
                    currentPoint.latitude, currentPoint.longitude,
                    targetPoint.latitude, targetPoint.longitude,
                    results
                )
                val distanceToTarget = results[0]
                val distancePerStep = speedMps * (updateInterval / 1000.0)

                if (distanceToTarget > distancePerStep) {
                    // Interpolate
                    val fraction = distancePerStep / distanceToTarget
                    val newLat = currentPoint.latitude + (targetPoint.latitude - currentPoint.latitude) * fraction
                    val newLng = currentPoint.longitude + (targetPoint.longitude - currentPoint.longitude) * fraction
                    currentPoint = LatLng(newLat, newLng)
                    
                    pushMockLocation(locationManager, currentPoint)
                } else {
                    // Reached target (or close enough), snap to target and move to next
                    currentPoint = targetPoint
                    pushMockLocation(locationManager, currentPoint)
                    targetPointIndex++
                }

                delay(updateInterval)
            }
            stopMocking()
        }
    }

    private fun pushMockLocation(locationManager: LocationManager, point: LatLng) {
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
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
        } catch (e: Exception) { 
            // e.printStackTrace() 
        }
    }

    private fun stopMocking() {
        isMocking = false
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "mock_location_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mock Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mock Location Active")
            .setContentText("Simulating location movement ($speedKmh km/h)")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_ROUTE = "EXTRA_ROUTE"
        const val EXTRA_SPEED = "EXTRA_SPEED"
        const val NOTIFICATION_ID = 1
    }
}
