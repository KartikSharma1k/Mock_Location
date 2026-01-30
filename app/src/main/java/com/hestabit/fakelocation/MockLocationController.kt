package com.hestabit.fakelocation

import android.content.Context
import android.location.Location
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.hestabit.fakelocation.data.repository.MockLocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class MockLocationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mockLocationRepository: MockLocationRepository
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun enableMockMode(enable: Boolean): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.setMockMode(enable)
                    .addOnSuccessListener {
                        Log.d("MockLocation", "Mock mode ${if (enable) "enabled" else "disabled"}")
                        continuation.resume(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MockLocation", "Failed to set mock mode", exception)
                        continuation.resume(false)
                    }
            } catch (e: SecurityException) {
                 continuation.resume(false)
            }
        }
    }

    suspend fun setMockLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float = 5.0f
    ): Boolean {
        val mockLocation = Location("fused").apply {
            this.latitude = latitude
            this.longitude = longitude
            this.accuracy = accuracy
            this.time = System.currentTimeMillis()
            this.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.setMockLocation(mockLocation)
                    .addOnSuccessListener {
                        continuation.resume(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MockLocation", "Failed to set mock location", exception)
                        continuation.resume(false)
                    }
            } catch (e: SecurityException) {
                continuation.resume(false)
            }
        }
    }
    
    suspend fun forceLocationRefresh(): Boolean {
         return suspendCancellableCoroutine { cont ->
             fusedLocationClient.lastLocation.addOnCompleteListener { 
                 cont.resume(it.isSuccessful)
             }
         }
    }

    fun simulateRoute(
        points: List<LatLng>,
        speedKmh: Double,
        onLocationUpdate: (LatLng, Int, Int) -> Unit,
        onRouteComplete: () -> Unit
    ) {
        mockLocationRepository.startMocking(points, speedKmh)
    }

    fun stopRouteSimulation() {
        mockLocationRepository.stopMocking()
    }
}
