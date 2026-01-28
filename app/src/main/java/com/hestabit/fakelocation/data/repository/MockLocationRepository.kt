package com.hestabit.fakelocation.data.repository

import com.google.android.gms.maps.model.LatLng

interface MockLocationRepository {
    fun startMocking(route: List<LatLng>)
    fun stopMocking()
}
