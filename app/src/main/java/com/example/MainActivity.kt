package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.data.preferences.ThemeMode
import com.example.notification.NotificationHelper
import com.example.ui.navigation.Destinations
import com.example.ui.navigation.ExamTrackNavHost
import com.example.ui.theme.ExamTrackTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ExamTrackApplication
        val initialSubjectId = intent?.getStringExtra(NotificationHelper.EXTRA_SUBJECT_ID)
            ?: intent?.getStringExtra("extra_subject_id")

        setContent {
            val themeMode by app.settingsPreferences.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )

            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            ExamTrackTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    LaunchedEffect(initialSubjectId) {
                        if (!initialSubjectId.isNullOrEmpty()) {
                            navController.navigate(Destinations.subjectDetail(initialSubjectId))
                        }
                    }

                    ExamTrackNavHost(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
