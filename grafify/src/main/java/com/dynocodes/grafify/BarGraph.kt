package com.dynocodes.grafify

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


class BarGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {


    private var barColor = DEFAULT_BAR_COLOR
    private var dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private var salesData = listOf(60, 70, 60, 100, 60, 80, 40)

    private var barWidth = 40f
    private var barSpacing = 50f
    private var barMaxHeight = 0f
    private var cornerRadius = 20f
    private var textColor = Color.WHITE
    private var textSize = 30f

    private val dashedLineColor = Color.GRAY
    private val dashedLineWidth = 4f
    private val dashedLineDashWidth = 10f
    private val dashedLineDashGap = 4f

    private val dashedLinePaint = Paint().apply {
        color = dashedLineColor
        style = Paint.Style.STROKE
        strokeWidth = dashedLineWidth
        pathEffect = DashPathEffect(floatArrayOf(dashedLineDashWidth, 0f), 0f)
    }

    private var showDashedLine = true

    fun setDashedLine(visible: Boolean) {
        showDashedLine = visible
        invalidate()
    }


    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarGraphView)
        textColor = typedArray.getColor(R.styleable.BarGraphView_labelTextColor, DEFAULT_TEXT_COLOR)
        barColor = typedArray.getColor(R.styleable.BarGraphView_barColor, DEFAULT_BAR_COLOR)
        textSize = typedArray.getDimension(R.styleable.BarGraphView_labelTextSize, 30f)
        typedArray.recycle()
    }

    fun setSalesData(data: BarData) {
        dayLabels = data.label
        salesData = data.value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the dashed line if enabled
        if (showDashedLine) {
            val centerX = 0
            val startX = width * 0.1f
            val endY = height.toFloat()

            val heightX = height - 76f
            canvas.drawLine(startX, 0f, startX, endY, dashedLinePaint)
            canvas.drawLine(0f, heightX, width.toFloat(), heightX, dashedLinePaint)
        }

        val totalWidth = width.toFloat()
        val totalHeight = height.toFloat()
        barSpacing = (totalWidth * 0.6f / (salesData.size ))
        barMaxHeight = (height.toFloat() - (height * 0.2f))
        barWidth = (totalWidth * 0.2f) / salesData.size
        val totalBarsWidth = (barWidth * salesData.size) + (barSpacing * (salesData.size - 1))
        val startX = (totalWidth - totalBarsWidth ) / 2 + 48

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = textSize

        for (i in salesData.indices) {
            val barHeight = (salesData[i] / 100f) * barMaxHeight
            val left = startX + (i * (barWidth + barSpacing))
            val right = left + barWidth
            val top = totalHeight - barHeight
            val bottom = totalHeight - paint.textSize - 48f

            paint.color = barColor
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paint)
        }
        paint.color = textColor

        for (i in dayLabels.indices) {
            val x = startX + (i * (barWidth + barSpacing)) + (barWidth / 2)
            val y = totalHeight - paint.textSize
            canvas.drawText(dayLabels[i], x, y, paint)

        }
    }

    companion object{
        private const val DEFAULT_BAR_COLOR = Color.CYAN
        private  const val DEFAULT_TEXT_COLOR =  Color.BLACK
    }
}

class BarData (val label: List<String>, val value: List<Int>)
