package com.tagfile.app.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PermissionDialog(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要存储权限") },
        text = {
            Text(
                "TagFile 需要访问您的文件存储才能浏览和管理文件。请前往设置授予所有文件访问权限。"
            )
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("前往设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
