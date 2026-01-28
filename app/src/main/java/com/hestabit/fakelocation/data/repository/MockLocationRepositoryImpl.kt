package com.hestabit.fakelocation.data.repository

import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import com.hestabit.fakelocation.service.MockLocationService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockLocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MockLocationRepository {

    override fun startMocking(route: List<LatLng>) {
        val intent = Intent(context, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_START
            putParcelableArrayListExtra(MockLocationService.EXTRA_ROUTE, ArrayList(route))
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun stopMocking() {
        val intent = Intent(context, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_STOP
        }
        context.startService(intent)
    }
}
