package com.dynocodes.grafify.Graphs


import android.content.Context
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.dynocodes.grafify.ScreenTimeUtils.ScreenTimeHelper

import kotlin.math.cos
import kotlin.math.sin

class Graphs {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Composable
    fun PieChartWithLabels(data: List<Slice>, modifier: Modifier = Modifier, context: Context) {

        val screenTimeHelper = ScreenTimeHelper(context)
        val total = data.sumBy { it.value }
        var startAngle = -90f

        Canvas(modifier = modifier) {
            val canvasWidth = size.width// subtract padding on both sides
            val canvasHeight = size.height  // subtract padding on both sides
            val radius = ((min(canvasWidth.toDp(), canvasHeight.toDp()) / 2) * 0.5f).toPx()
            val centerX = (canvasWidth / 2 ) // add padding on the left,
            val centerY = (canvasHeight / 2 ) // add padding on the top
            val oval =
                Rect((centerX - radius), centerY - radius, centerX + radius, centerY + radius)

            data.forEach { slice ->
                val sweepAngle = (slice.value / total.toFloat()) * 360f
                drawArc(
                    color = Color(slice.color),
                    style = Stroke(25f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )

                // Calculate the position of the text label
                val labelRadius = radius * 1.55f
                val labelX = centerX + labelRadius * cos(Math.toRadians((startAngle + sweepAngle / 2f).toDouble()).toFloat())
                val labelY = centerY + labelRadius * sin(Math.toRadians((startAngle + sweepAngle / 2f).toDouble()).toFloat())

                // Draw the text label
                drawIntoCanvas {
                    val paint = Paint().apply {
                        color =Color.White.hashCode()
                        textSize = 14.sp.toPx()
                        textAlign = Paint.Align.CENTER
                    }
                    it.nativeCanvas.drawText(slice.label, labelX, labelY, paint)
                    it.nativeCanvas.drawText(screenTimeHelper.formatDuration(total.toLong()), centerX, centerY, paint)
                    paint.textSize = 12.sp.toPx()
                    if (slice.value > 1000*60*30){
                        it.nativeCanvas.drawText(screenTimeHelper.formatDuration(slice.value.toLong()), labelX, labelY+50,paint )
                    }


                }

                startAngle += sweepAngle
            }
        }
    }






}

class Slice(val value: Int, val label: String = "lol", val color: Int)