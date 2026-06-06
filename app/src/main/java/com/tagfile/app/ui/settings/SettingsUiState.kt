package com.tagfile.app.ui.settings

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isContinuousEnhance: Boolean = false,
    val isResettingShelf: Boolean = false,
    val isScanningShelf: Boolean = false,
    val message: String? = null,
    val shelfFolderPath: String? = null
)

sealed class SettingsEvent {
    object ToggleDarkMode : SettingsEvent()
    object ToggleContinuousEnhance : SettingsEvent()
    object ClearMessage : SettingsEvent()
    object ResetShelfDatabase : SettingsEvent()
    object ScanShelfBooks : SettingsEvent()
    data class UpdateShelfFolderPath(val path: String) : SettingsEvent()
}
