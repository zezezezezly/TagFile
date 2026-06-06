package com.tagfile.app.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun PsColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val hsv = remember(selectedColor) { selectedColor.toHsv() }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var svSize by remember { mutableFloatStateOf(0f) }
    var hueHeight by remember { mutableFloatStateOf(0f) }

    fun pickSaturationValue(x: Float, y: Float) {
        saturation = (x / svSize).coerceIn(0f, 1f)
        value = (1f - y / svSize).coerceIn(0f, 1f)
        onColorSelected(Color.hsv(hue, saturation, value))
    }

    fun pickHue(y: Float) {
        hue = (y / hueHeight).coerceIn(0f, 1f) * 360f
        onColorSelected(Color.hsv(hue, saturation, value))
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pureHueColor = Color.hsv(hue, 1f, 1f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .onSizeChanged { svSize = it.width.toFloat() }
                    .pointerInput(Unit) {
                        detectTapGestures { offset -> pickSaturationValue(offset.x, offset.y) }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            pickSaturationValue(change.position.x, change.position.y)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    drawRect(pureHueColor)
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.White,
                            1f to Color.Transparent
                        ),
                        size = Size(w, h)
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to Color.Black
                        ),
                        size = Size(w, h)
                    )

                    val sx = saturation * w
                    val sy = (1f - value) * h

                    drawCircle(Color.White, radius = 7f, center = Offset(sx, sy))
                    drawCircle(
                        Color.Black.copy(alpha = 0.3f),
                        radius = 7f,
                        center = Offset(sx, sy),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .onSizeChanged { hueHeight = it.height.toFloat() }
                    .pointerInput(Unit) {
                        detectTapGestures { offset -> pickHue(offset.y) }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            pickHue(change.position.y)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val steps = 36
                    val stepH = size.height / steps
                    for (i in 0 until steps) {
                        drawRect(
                            color = Color.hsv(i * 360f / steps, 1f, 1f),
                            topLeft = Offset(0f, i * stepH),
                            size = Size(size.width, stepH + 1f)
                        )
                    }

                    val indicatorY = hue / 360f * size.height
                    val cx = size.width / 2f
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(cx - 7f, indicatorY - 5f),
                        size = Size(14f, 10f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.3f),
                        topLeft = Offset(cx - 7f, indicatorY - 5f),
                        size = Size(14f, 10f),
                        cornerRadius = CornerRadius(3f, 3f),
                        style = Stroke(width = 1f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            val currentColor = Color.hsv(hue, saturation, value)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(currentColor)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "H:${hue.toInt()}° S:${(saturation * 100).toInt()}% B:${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Color.toHsv(): FloatArray {
    val arr = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt(),
        arr
    )
    return arr
}
