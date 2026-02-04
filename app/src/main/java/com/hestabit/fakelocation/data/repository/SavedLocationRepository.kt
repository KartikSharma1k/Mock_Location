package com.hestabit.fakelocation.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hestabit.fakelocation.data.model.SavedLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface SavedLocationRepository {
    fun getSavedLocations(): Flow<List<SavedLocation>>
    suspend fun saveLocation(name: String, latitude: Double, longitude: Double): Result<Boolean>
    suspend fun deleteLocation(locationId: String): Result<Boolean>
}

@Singleton
class SavedLocationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SavedLocationRepository {

    override fun getSavedLocations(): Flow<List<SavedLocation>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(user.uid)
            .collection("saved_locations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val locations = snapshot?.toObjects(SavedLocation::class.java) ?: emptyList()
                trySend(locations)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun saveLocation(name: String, latitude: Double, longitude: Double): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))
            
            val newLocationRef = firestore.collection("users")
                .document(user.uid)
                .collection("saved_locations")
                .document()

            val location = SavedLocation(
                id = newLocationRef.id,
                name = name,
                latitude = latitude,
                longitude = longitude
            )

            newLocationRef.set(location).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLocation(locationId: String): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("User not logged in"))

            firestore.collection("users")
                .document(user.uid)
                .collection("saved_locations")
                .document(locationId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
