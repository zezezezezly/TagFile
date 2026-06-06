package com.tagfile.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.tagfile.app.data.filesystem.FileIndexer
import com.tagfile.app.data.preferences.PreferencesManager
import com.tagfile.app.navigation.NavGraph
import com.tagfile.app.ui.theme.TagFileTheme
import com.tagfile.app.ui.theme.WallpaperBackground
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var fileIndexer: FileIndexer

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val darkMode by preferencesManager.isDarkMode.collectAsState()
            TagFileTheme(
                preferencesManager = preferencesManager,
                darkTheme = darkMode
            ) {
                WallpaperBackground(preferencesManager = preferencesManager) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavGraph(
        navController = navController
    )
}
