package com.hestabit.fakelocation

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.hestabit.fakelocation.data.repository.MockLocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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

    // Track whether this controller successfully enabled fused mock mode so we can disable it later
    private var fusedMockModeEnabledByController: Boolean = false

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
                Log.e("MockLocation", "SecurityException enabling mock mode: ${e.message}")
                continuation.resume(false)
            } catch (e: Exception) {
                Log.e("MockLocation", "Exception enabling mock mode: ${e.message}", e)
                continuation.resume(false)
            }
        }
    }

    /**
     * Try enabling mock mode with a few retries and short delays. This helps when Play Services
     * needs a moment to apply the change.
     */
    private suspend fun ensureMockModeEnabled(retries: Int = 3, delayMs: Long = 250L): Boolean {
        repeat(retries) { attempt ->
            val ok = enableMockMode(true)
            if (ok) return true
            Log.w("MockLocation", "enableMockMode attempt ${attempt + 1} failed, retrying in ${delayMs}ms")
            delay(delayMs)
        }
        Log.e("MockLocation", "Failed to enable mock mode after $retries attempts. Make sure this app is allowed to mock locations in Developer Options or via adb appops.")
        return false
    }

    /**
     * Checks whether the app is allowed to set mock locations via AppOps (developer option or adb).
     * This is a helpful hint to show the user when enabling mock mode fails.
     */
    fun isMockLocationAllowed(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val op = AppOpsManager.OPSTR_MOCK_LOCATION
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // checkOpNoThrow with package name is supported
                appOps.unsafeCheckOpNoThrow(op, android.os.Process.myUid(), context.packageName)
            } else {
                appOps.checkOpNoThrow(op, android.os.Process.myUid(), context.packageName)
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Log.w("MockLocation", "isMockLocationAllowed check failed: ${e.message}")
            false
        }
    }

    suspend fun setMockLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float = 5.0f
    ): Boolean {
        // Ensure mock mode is enabled first (with retries)
        val mockEnabled = ensureMockModeEnabled()
        fusedMockModeEnabledByController = mockEnabled
        if (!mockEnabled) return false

        val mockLocation = Location(LocationManager.GPS_PROVIDER).apply {
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
                        Log.d("MockLocation", "setMockLocation success: $latitude, $longitude")
                        continuation.resume(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MockLocation", "Failed to set mock location", exception)
                        continuation.resume(false)
                    }
            } catch (e: SecurityException) {
                Log.e("MockLocation", "SecurityException setting mock location: ${e.message}")
                continuation.resume(false)
            } catch (e: Exception) {
                Log.e("MockLocation", "Exception setting mock location: ${e.message}", e)
                continuation.resume(false)
            }
        }
    }

    suspend fun forceLocationRefresh(): Boolean {
        return suspendCancellableCoroutine { cont ->
            try {
                // Check location permissions before accessing lastLocation
                val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (!fine && !coarse) {
                    Log.w("MockLocation", "Location permission not granted for forceLocationRefresh")
                    cont.resume(false)
                    return@suspendCancellableCoroutine
                }

                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    cont.resume(task.isSuccessful)
                }
            } catch (e: SecurityException) {
                Log.e("MockLocation", "SecurityException reading lastLocation: ${e.message}")
                cont.resume(false)
            } catch (e: Exception) {
                Log.e("MockLocation", "Exception reading lastLocation: ${e.message}", e)
                cont.resume(false)
            }
        }
    }

    /**
     * Start route simulation: try to enable fused mock mode, but don't block service-based mocking if that fails.
     * Returns true if repository start was requested successfully.
     */
    suspend fun startRouteSimulation(points: List<LatLng>, speedKmh: Double): Boolean {
        // Try to enable fused mock mode (best-effort)
        try {
            val ok = ensureMockModeEnabled()
            fusedMockModeEnabledByController = ok
            if (!ok) Log.w("MockLocation", "Fused mock mode not enabled; starting repository-based mocking as fallback")
        } catch (e: Exception) {
            Log.w("MockLocation", "Exception while trying to enable fused mock mode: ${e.message}")
        }

        return try {
            mockLocationRepository.startMocking(points, speedKmh)
            Log.d("MockLocation", "Route simulation started (repository invoked)")
            true
        } catch (e: Exception) {
            Log.e("MockLocation", "Failed to start route simulation: ${e.message}", e)
            false
        }
    }

    /**
     * Stops repository mocking and attempts to disable fused mock mode if this controller enabled it.
     */
    suspend fun stopRouteSimulationAndDisable(): Boolean {
        try {
            mockLocationRepository.stopMocking()
        } catch (e: Exception) {
            Log.w("MockLocation", "stopMocking threw: ${e.message}")
        }
        return try {
            if (fusedMockModeEnabledByController) {
                val ok = enableMockMode(false)
                fusedMockModeEnabledByController = false
                ok
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("MockLocation", "Failed to disable mock mode: ${e.message}", e)
            false
        }
    }

    @Suppress("UNUSED_PARAMETER")
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
