package com.rakcwc.presentation.ui.screens.settings

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.rakcwc.data.remote.local.TokenManager
import com.rakcwc.domain.models.UserInfo
import com.rakcwc.utils.JWTDecoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _settings = MutableStateFlow<UserInfo?>(null)
    val settings: StateFlow<UserInfo?> = _settings.asStateFlow()

    init {
        getProfile()
    }
    fun getProfile() {
        _settings.value = tokenManager.getToken()?.let { JWTDecoder.decode(it) }
    }

    fun signOut() {
        tokenManager.clearToken()
    }
}