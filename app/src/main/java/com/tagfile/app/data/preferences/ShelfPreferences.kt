package com.tagfile.app.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShelfPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("tagfile_prefs", Context.MODE_PRIVATE)

    private val _shelfFolderPath = MutableStateFlow(prefs.getString("shelf_folder_path", null))
    val shelfFolderPath: StateFlow<String?> = _shelfFolderPath.asStateFlow()

    fun setShelfFolderPath(path: String?) {
        prefs.edit().putString("shelf_folder_path", path).apply()
        _shelfFolderPath.value = path
    }

    fun getRecommendationDate(): String = prefs.getString("recommendation_date", "") ?: ""

    fun setRecommendationDate(date: String) {
        prefs.edit().putString("recommendation_date", date).apply()
    }
}
