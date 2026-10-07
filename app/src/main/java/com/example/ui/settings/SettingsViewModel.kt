package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ExamTrackApplication
import com.example.data.preferences.ThemeMode
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsPreferences = (application as ExamTrackApplication).settingsPreferences
    private val repository = (application as ExamTrackApplication).repository

    val notificationsEnabled: StateFlow<Boolean> = settingsPreferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeMode: StateFlow<ThemeMode> = settingsPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setNotificationsEnabled(enabled)
            val context = getApplication<Application>()
            val subjects = repository.getAllSubjects()
            if (enabled) {
                // Re-schedule reminders for all subjects
                for (subject in subjects) {
                    NotificationHelper.scheduleExamReminders(context, subject)
                }
            } else {
                // Cancel all reminders
                for (subject in subjects) {
                    NotificationHelper.cancelExamReminders(context, subject.id)
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsPreferences.setThemeMode(mode)
        }
    }
}
