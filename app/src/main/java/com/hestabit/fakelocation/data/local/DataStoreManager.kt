package com.hestabit.fakelocation.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_DEV_INSTRUCTIONS_COMPLETED = booleanPreferencesKey("dev_instructions_completed")
        val KEY_AUTH_COMPLETED = booleanPreferencesKey("auth_completed")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    val devInstructionsCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_DEV_INSTRUCTIONS_COMPLETED] ?: false
        }

    val authCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_AUTH_COMPLETED] ?: false
        }


    suspend fun setAuthCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_COMPLETED] = completed
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDevInstructionsCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEV_INSTRUCTIONS_COMPLETED] = completed
        }
    }
}
