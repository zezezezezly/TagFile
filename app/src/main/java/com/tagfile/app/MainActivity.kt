package com.tagfile.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.tagfile.app.data.filesystem.FileIndexer
import com.tagfile.app.data.preferences.AppearancePreferences
import com.tagfile.app.data.preferences.WallpaperPreferences
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
    lateinit var appearancePreferences: AppearancePreferences

    @Inject
    lateinit var wallpaperPreferences: WallpaperPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val darkMode by appearancePreferences.isDarkMode.collectAsState()
            TagFileTheme(
                appearancePreferences = appearancePreferences,
                darkTheme = darkMode
            ) {
                WallpaperBackground(wallpaperPreferences = wallpaperPreferences) {
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
