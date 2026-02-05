package com.dnavarro.turismoapp.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.turismoapp.data.Notification
import com.dnavarro.turismoapp.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    fun getNotifications(token: String) {
        viewModelScope.launch {
            try {
                _notifications.value = Api.retrofitService.getMyNotifications("Bearer $token")
            } catch (t: Throwable) {
                Log.e("NotificationsViewModel", "Failed to get notifications", t)
            }
        }
    }
}
