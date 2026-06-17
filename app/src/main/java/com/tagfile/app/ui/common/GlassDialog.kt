package com.tagfile.app.ui.common

import android.os.Build
import android.view.View
import android.view.ViewParent
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ApplyBlurBehind()

        Surface(
            modifier = modifier.fillMaxWidth(0.88f),
            shape = shape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Column {
                Box(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 24.dp,
                        bottom = if (text != null) 0.dp else 12.dp
                    )
                ) {
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        title()
                    }
                }

                text?.let { content ->
                    Box(
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 8.dp,
                            bottom = 0.dp
                        )
                    ) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            content()
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

@Composable
private fun ApplyBlurBehind() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = findDialogWindow(view)
        @Suppress("DEPRECATION")
        window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)

        onDispose {
            @Suppress("DEPRECATION")
            window?.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
    }
}

private fun findDialogWindow(view: View): Window? {
    var current: ViewParent? = view.parent
    while (current != null) {
        if (current is DialogWindowProvider) {
            return current.window
        }
        current = current.parent
    }
    return null
}
