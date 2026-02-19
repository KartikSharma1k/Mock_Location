package com.hestabit.fakelocation.data.model

import com.google.firebase.Timestamp

data class SavedLocation(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val favourite: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
