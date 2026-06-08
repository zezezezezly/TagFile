package com.tagfile.app.ui.settings

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isContinuousEnhance: Boolean = false,
    val isResettingShelf: Boolean = false,
    val isScanningShelf: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
    val shelfFolderPath: String? = null,
    val showImportModeDialog: Boolean = false
)

sealed class SettingsEvent {
    object ToggleDarkMode : SettingsEvent()
    object ToggleContinuousEnhance : SettingsEvent()
    object ClearMessage : SettingsEvent()
    object ResetShelfDatabase : SettingsEvent()
    object ScanShelfBooks : SettingsEvent()
    data class UpdateShelfFolderPath(val path: String) : SettingsEvent()

    // 导入导出
    object ExportDatabase : SettingsEvent()
    object ImportDatabase : SettingsEvent()
    object DismissImportModeDialog : SettingsEvent()
    data class ConfirmImportMode(val isReplace: Boolean) : SettingsEvent()
}
