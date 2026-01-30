package com.hestabit.fakelocation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hestabit.fakelocation.data.local.DataStoreManager
import com.hestabit.fakelocation.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    companion object{
        val isLoading = MutableStateFlow(false)
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val onBoardingCompleted = dataStoreManager.onboardingCompleted.first()
            val devInstructionsCompleted = dataStoreManager.devInstructionsCompleted.first()
            
            // TODO: Add Auth check here when AuthRepository is ready
            // For now assuming:
            // 1. Intro -> Auth (skipped for now) -> Permissions -> DevInstructions -> Dashboard

            if (!onBoardingCompleted) {
                _startDestination.value = Screen.Intro.route
            } else if (!devInstructionsCompleted) {
                 // Check permissions here as well ideally
                _startDestination.value = Screen.DeveloperInstructions.route
            } else {
                _startDestination.value = Screen.Dashboard.route
            }
        }
    }
}
