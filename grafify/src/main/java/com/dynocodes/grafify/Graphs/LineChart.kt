package com.dynocodes.beoffline.Graphs

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun LineChart(
    modifier: Modifier,
    data: List<Float>,
    labels: List<String>,
    unit: String
) {
    val maxValue = data.maxOrNull() ?: 0f
    val padding = 36.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Draw the x-axis
        drawLine(
            start = Offset(0f, size.height - padding.toPx()),
            end = Offset(size.width, size.height - padding.toPx()),
            color = Color.Gray,
            strokeWidth = 2f
        )

        // Draw the y-axis
        drawLine(
            start = Offset(padding.toPx(), 0f),
            end = Offset(padding.toPx(), size.height - padding.toPx()),
            color = Color.Gray,
            strokeWidth = 2f
        )

        // Draw the data points and lines
        val xStep = (size.width - padding.toPx() * 2) / (data.size - 1)
        val yStep = (size.height - padding.toPx() * 2) / maxValue
        val path = Path()
        path.moveTo(padding.toPx(), size.height - padding.toPx() - data[0] * yStep)
        for (i in 1 until data.size) {
            val x = padding.toPx() + i * xStep
            val y = size.height - padding.toPx() - data[i] * yStep
            path.lineTo(x, y)
            drawCircle(color = Color.Blue, radius = 8f, center = Offset(x, y))
        }
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw the labels
        val labelXStep = (size.width - padding.toPx() * 2) / (labels.size - 1)
        for (i in labels.indices) {
            val x = padding.toPx() + i * labelXStep
            drawLine(
                start = Offset(x, size.height - padding.toPx()),
                end = Offset(x, size.height - padding.toPx() + 16.dp.toPx()),
                color = Color.Gray,
                strokeWidth = 2f
            )
            drawIntoCanvas {


                val paint = Paint().apply {
                    color = Color.White.hashCode()
                    textSize = 12.sp.toPx()
                    textAlign = Paint.Align.CENTER
                }
                it.nativeCanvas.drawText(
                    labels[i],
                    x,
                    size.height - padding.toPx() + 28.dp.toPx(),
                    paint
                )
                it.nativeCanvas.drawText(
                    unit,
                    (size.width.absoluteValue).dp.toPx(),
                    padding.toPx() / 4,
                    paint
                )

                // Draw the values
                for (i in data.indices) {
                    val x = padding.toPx() + i * xStep
                    val y = size.height - padding.toPx() - data[i] * yStep
                    it.nativeCanvas.drawText(
                        String.format("%.1f", data[i]),
                        x - 1.dp.toPx(),
                        y - 10.dp.toPx(),
                        paint
                    )

                }


                val value = maxValue * i / 6
                val y = size.height - padding.toPx() - value * yStep
                val text = String.format("%.1f", value)
                val xnew = padding.toPx() - 16.dp.toPx()
                val ynew = y - 8.dp.toPx()

                drawLine(
                    start = Offset(padding.toPx() - 16.dp.toPx(), y),
                    end = Offset(padding.toPx(), y),
                    color = Color.Gray,
                    strokeWidth = 2f
                )
                it.nativeCanvas.drawText(text, xnew, ynew, paint)


            }


//            // Draw the labels
//            val labelXStep = (size.width - padding.toPx() * 2) / (labels.size - 1)
//            for (i in labels.indices) {
//                val x = padding.toPx() + i * labelXStep
//                drawLine(
//                    start = Offset(x, size.height - padding.toPx()),
//                    end = Offset(x, size.height - padding.toPx() + 16.dp.toPx()),
//                    color = Color.Gray,
//                    strokeWidth = 2f
//                )
////                drawText(
////                    text = labels[i],
////                    color = Color.Gray,
////                    fontSize = 12.sp,
////                    textAlign = TextAlign.Center,
////                    modifier = Modifier.offset(x = x - 16.dp.toPx(), y = size.height - padding.toPx() + 28.dp.toPx())
////                )
//            }
//
//
//
//            // Draw the unit
//            drawText(
//                text = unit,
//                color = Color.Gray,
//                fontSize = 12.sp,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.offset(x = padding.toPx() / 2, y = padding.toPx() / 2)
//            )
//        }
        }
    }
}


@Composable
fun BarChart(
    data: SnapshotStateList<Float>,
    labels: SnapshotStateList<String>,
    unit: String,
    modifier: Modifier,
    maincolor: Color = Color.LightGray
) {
    val maxValue = data.maxOrNull() ?: 0f
    val padding = 16.dp

    Canvas(modifier = modifier.padding(padding)) {

        drawIntoCanvas {

            val paint = Paint().apply {
                color = Color.White.hashCode()
                textSize = 12.sp.toPx()
                textAlign = Paint.Align.CENTER
            }

            val xStep = (size.width - padding.toPx() * 2) / data.size + 16
            val yStep = (size.height - padding.toPx() * 2) / maxValue


            // Draw the x-axis
            drawLine(
                start = Offset(0f, size.height - padding.toPx()),
                end = Offset(size.width, size.height - padding.toPx()),
                color = Color.Gray,
                strokeWidth = 2f
            )

            // Draw x-axis labels
            for (i in labels.indices) {
                val x = (3 * padding.toPx() + i * xStep) + 8
                it.nativeCanvas.drawText(
                    labels[i],
                    x - padding.toPx()+16,
                    size.height - padding.toPx() + 16.dp.toPx(),
                    paint
                )

            }

            // Draw the y-axis
            drawLine(
                start = Offset(padding.toPx(), 0f),
                end = Offset(padding.toPx(), size.height + padding.toPx()),
                color = Color.Gray,
                strokeWidth = 2f
            )

            // Draw points on y axis
            for (i in 0..4) {
                val value = maxValue * i / 4
                val y = size.height - padding.toPx() - value * yStep
                val text = String.format("%.1f H", value)
                it.nativeCanvas.drawText(text, padding.toPx() - 16.dp.toPx(), y - 8, paint)
                drawLine(
                    start = Offset(-padding.toPx(), y),
                    end = Offset(size.width + padding.toPx(), y),
                    color = Color.Gray,
                    strokeWidth = 2f
                )
            }

            // Draw the bars
            for (i in data.indices) {
                val x = padding.toPx() + i * xStep
                val barWidth = xStep * 0.8f
                val barHeight = data[i] * yStep
                drawRoundRect(
                    color = maincolor,
                    topLeft = Offset(
                        x + (xStep - barWidth) / 2,
                        size.height - padding.toPx() - barHeight
                    ),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 14f)
                )
            }





            // Draw the values
            for (i in data.indices) {
                val x = padding.toPx() + i * xStep
                val y = size.height - padding.toPx() - data[i] * yStep
                val text = String.format("%.1f H", data[i])
                val xnew = x + padding.toPx() + 26
                val ynew = y - 8.dp.toPx()
                it.nativeCanvas.drawText(text, xnew, ynew, paint)

            }

        }

    }
}


