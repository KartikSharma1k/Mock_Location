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
    private var currentIndex = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val points = intent.getParcelableArrayListExtra<LatLng>(EXTRA_ROUTE)
                if (!points.isNullOrEmpty()) {
                    routePoints = points
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
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, false, false, false, true, true, true, 0, 5
                )
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            } catch (e: SecurityException) {
                // Mock location permission not granted or app not selected
                stopSelf()
                return@launch
            } catch (e: IllegalArgumentException) {
                 // Provider already exists?
            }

            while (isMocking && currentIndex < routePoints.size) {
                val point = routePoints[currentIndex]
                val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = point.latitude
                    longitude = point.longitude
                    altitude = 0.0
                    time = System.currentTimeMillis()
                    accuracy = 5.0f
                    elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        bearingAccuracyDegrees = 0.1f
                        verticalAccuracyMeters = 0.1f
                        speedAccuracyMetersPerSecond = 0.1f
                    }
                }

                try {
                    locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation)
                } catch (e: Exception) {
                    // Handle error
                }

                currentIndex++
                // Loop or stop? For now let's just stop at end or loop?
                // Step 0: Linear interpolation would yield many points. 
                // Currently just jumping between defined points. 
                // Ideally we interpolate here.
                delay(1000) 
            }
            stopMocking()
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
            .setContentText("Simulating location movement...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_ROUTE = "EXTRA_ROUTE"
        const val NOTIFICATION_ID = 1
    }
}
