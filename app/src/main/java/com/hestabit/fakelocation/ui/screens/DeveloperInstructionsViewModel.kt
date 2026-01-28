package com.hestabit.fakelocation.ui.screens

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hestabit.fakelocation.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperInstructionsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun completeInstructions() {
        viewModelScope.launch {
            dataStoreManager.setDevInstructionsCompleted(true)
        }
    }

    fun isMockLocationEnabled(): Boolean {
        // Checking if THIS app is selected as the mock location app is tricky programmatically 
        // without actually trying to set a mock location and catching the SecurityException.
        // For instructions purposes, we mostly rely on user confirmation, 
        // but we can check if Developer Options are enabled at least.
        return Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
    }
}
