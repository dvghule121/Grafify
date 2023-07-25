package com.dynocodes.grafify

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt

class SimplePieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Attributes
    private var textSize = 0f
    private var iconSize = 0f
    private var showCenteredLabel = DEFAULT_SHOW_LABEL
    private var decorRingWeight = DEFAULT_DECOR_RING_WEIGHT
    private var innerHoleWeight = DEFAULT_INNER_HOLE_WEIGHT
    private var labelSpacing = DEFAULT_LABEL_SPACING

    @ColorInt
    private var decorRingColor = DEFAULT_DECOR_RING_COLOR

    @ColorInt
    private var textColor = DEFAULT_TEXT_COLOR

    // Helpers
    private val slices = mutableListOf<Slice>()
    private var chartRadius = 0f
    private val contentBounds = RectF()
    private val innerHoleBounds = RectF()

    // Drawing helpers
    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var slicePath: Path? = Path()


    init {
        initDefaultAttrs(context)
        attrs?.let { initAttrs(context, it) }
        initPaints()
    }

    val total: Float
        get() {
            var total = 0f
            for (slice in slices!!) {
                total += slice.value
            }
            return total
        }


    private fun initDefaultAttrs(context: Context) {
        // These attributes are depending on Display Metrics
        val dm = context.resources.displayMetrics
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP.toFloat(), dm
        )
        iconSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ICON_SIZE_DP.toFloat(), dm
        )
    }

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        val array = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SimplePieChart,
            0, 0
        )
        try {
            val dm = context.resources.displayMetrics
            textSize = array.getDimensionPixelSize(
                R.styleable.SimplePieChart_textSize,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    DEFAULT_TEXT_SIZE_SP.toFloat(),
                    dm
                ).toInt()
            ).toFloat()
            textColor = array.getColor(R.styleable.SimplePieChart_textColor, Color.WHITE)
            iconSize = array.getDimensionPixelSize(
                R.styleable.SimplePieChart_iconSize,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_ICON_SIZE_DP.toFloat(),
                    dm
                ).toInt()
            ).toFloat()
            showCenteredLabel = array.getBoolean(R.styleable.SimplePieChart_showCenterLabel,DEFAULT_SHOW_LABEL)
            decorRingColor =
                array.getColor(R.styleable.SimplePieChart_decorRingColor, DEFAULT_DECOR_RING_COLOR)
            decorRingWeight =
                array.getFloat(
                    R.styleable.SimplePieChart_decorRingWeight,
                    DEFAULT_DECOR_RING_WEIGHT
                )
            innerHoleWeight =
                array.getFloat(
                    R.styleable.SimplePieChart_innerHoleWeight,
                    DEFAULT_INNER_HOLE_WEIGHT
                )

        } finally {
            array.recycle()
        }
    }

    private fun initPaints() {
        slicePaint.style = Paint.Style.FILL
        ringPaint.style = Paint.Style.FILL
        ringPaint.color = decorRingColor
        ringPaint.alpha = Color.alpha(decorRingColor)
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        drawSlices(canvas)
        drawPercentageValues(canvas)
    }

    private fun drawSlices(canvas: Canvas) {
        var sliceStartAngle = START_ANGLE_OFFSET.toFloat()
        getCenteredSquareBounds((chartRadius * 1.25).toFloat(), contentBounds)
        getCenteredSquareBounds((chartRadius * innerHoleWeight * 2.5).toFloat(), innerHoleBounds)
        for (slice in slices) {
            slicePath = PathUtils.getSolidArcPath(
                slicePath!!, contentBounds, innerHoleBounds,
                sliceStartAngle, getSliceAngle(slice)
            )

            slicePaint.color = slice.color
            canvas.drawPath(slicePath!!, slicePaint)
            sliceStartAngle += getSliceAngle(slice)
        }
    }

    private fun drawPercentageValues(canvas: Canvas) {
        var sliceStartAngle = START_ANGLE_OFFSET.toFloat()
        val textDistance = chartRadius * (0.8f) + labelSpacing
        var sliceHalfAngle: Float
        var textCenterX: Float
        var textCenterY: Float
        textPaint.color = textColor
        textPaint.textSize = textSize
        var sum = 0
        for (i in 0..slices.size - 1) {
            sum += slices[i].value.toInt()
            sliceHalfAngle = sliceStartAngle + getSliceAngle(slices[i]) / 2f
            textCenterX = MathUtils.getPointX(
                contentBounds.centerX(), textDistance, sliceHalfAngle
            )
            textCenterY = MathUtils.getPointY(
                contentBounds.centerY(), textDistance, sliceHalfAngle
            )

            if (showCenteredLabel) {
                drawCenteredLabel(canvas, slices[i], textCenterX, textCenterY, textPaint)
            }
            else{
                // Draw lable at bottom with respective colored square
                drawBottomLabel(canvas, slices[i], textPaint, i,textCenterX,textCenterY)
            }




            sliceStartAngle += getSliceAngle(slices[i])
        }

        // Draw "Total" text
        textPaint.textSize = textSize
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Total", contentBounds.centerX(), contentBounds.centerY() - 20f, textPaint)

        // Draw total sum
        val totalText = "₹ $sum"
        textPaint.textSize = textSize
        textPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(
            totalText,
            contentBounds.centerX(),
            contentBounds.centerY() + 20f,
            textPaint
        )
    }

    private fun drawBottomLabel(
        canvas: Canvas, slice: Slice, textPaint: Paint, index: Int, textCenterX: Float,
        textCenterY: Float,
    ) {
        // Draw label at the bottom of the view with respective color
        val label = slice.label
        val labelColor = slice.color

        textPaint.textSize = textSize

        val labelBounds = Rect()
        textPaint.getTextBounds(label, 0, label.length, labelBounds)

        var labelX = 0
        var labelY = contentBounds.bottom + DEFAULT_LABEL_BOTTOM_MARGIN + 200f
        if (index < 3) {

            labelX = 40 + width / 2 - width / 5 + (index * width / 5)

        } else if (index < 7) {
            labelX = width / 2 - width / 3 + ((index - 3) * width / 5)
            labelY = contentBounds.bottom + DEFAULT_LABEL_BOTTOM_MARGIN + 300f
        }


        // Draw colored square
        val labelSquareSize = textSize
        val squareLeft = (labelX - labelSquareSize / 2f) - 80f
        val squareTop = labelY - labelSquareSize / 2f
        val squareRight = squareLeft + labelSquareSize
        val squareBottom = squareTop + labelSquareSize
        val text = slice.value.toInt().toString()
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        val squarePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawText(
            text,
            textCenterX,
            textCenterY,
            this.textPaint
        )

        squarePaint.color = labelColor
        canvas.drawRect(squareLeft, squareTop, squareRight, squareBottom, squarePaint)


        canvas.drawText(
            label,
            labelX.toFloat(),
            labelY - labelBounds.exactCenterY(),
            textPaint
        )

    }

    fun drawCenteredLabel(
        canvas: Canvas,
        slice: Slice,
        textCenterX: Float,
        textCenterY: Float,
        textPaint: Paint,
    ) {

        val text = slice.value.toInt().toString()
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(
            text,
            textCenterX,
            textCenterY,
            this.textPaint
        )
        canvas.drawText(
            slice.label,
            textCenterX,
            textCenterY + (1.5f * textSize),
            this.textPaint!!
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Finding radius of largest circle area inside View.
        chartRadius = Math.min(
            w - paddingLeft - paddingRight,
            h - paddingTop - paddingBottom
        ) / 2f
    }

    protected fun getCenteredSquareBounds(squareSize: Float, bounds: RectF) {
        bounds.left = (paddingLeft
                + (width - paddingLeft - paddingRight - squareSize) / 2)
        bounds.top = (paddingTop
                + (height - paddingTop - paddingBottom - squareSize) / 2)
        bounds.right = (width - paddingRight
                - (width - paddingLeft - paddingRight - squareSize) / 2)
        bounds.bottom = (height - paddingBottom
                - (height - paddingTop - paddingBottom - squareSize) / 2)
    }

    private fun getSliceAngle(slice: Slice): Float {
        return slice.value / total * 360
    }

    fun addSlice(slice: Slice) {
        slices.add(slice)
        invalidate()
    }

    fun removeAllSlices() {
        slices.clear()
        invalidate()
    }

    fun setTextColor(@ColorInt color: Int) {
        textColor = color
        invalidate()
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        invalidate()
    }

    fun setIconSize(iconSize: Float) {
        this.iconSize = iconSize
        invalidate()
    }

    fun setDecorRingColor(@ColorInt color: Int) {
        decorRingColor = color
        ringPaint.color = decorRingColor
        ringPaint.alpha = Color.alpha(decorRingColor)
        invalidate()
    }

    fun setDecorRingWeight(decorRingWeight: Float) {
        this.decorRingWeight = decorRingWeight
        invalidate()
    }

    fun setInnerHoleWeight(innerHoleWeight: Float) {
        this.innerHoleWeight = innerHoleWeight
        invalidate()
    }

    fun setSlicePaintColor(sliceIndex: Int, @ColorInt color: Int) {
        if (sliceIndex >= 0 && sliceIndex < slices.size) {
            slices[sliceIndex].color = color
            invalidate()
        }
    }

    fun setSliceValue(sliceIndex: Int, value: Float) {
        if (sliceIndex >= 0 && sliceIndex < slices.size) {
            slices[sliceIndex].value = value
            invalidate()
        }
    }

    fun setSliceLabel(sliceIndex: Int, label: String) {
        if (sliceIndex >= 0 && sliceIndex < slices.size) {
            slices[sliceIndex].label = label
            invalidate()
        }
    }

    fun setSliceDrawable(sliceIndex: Int, drawable: Drawable?) {
        if (sliceIndex >= 0 && sliceIndex < slices.size) {
            slices[sliceIndex].drawable = drawable
            invalidate()
        }
    }

    class Slice(
        @ColorInt @get:ColorInt
        var color: Int,
        var value: Float,
        var label: String,
        var drawable: Drawable? = null
    )

    companion object {
        // Slices start at 12 o'clock
        private const val START_ANGLE_OFFSET = 270
        private const val DEFAULT_TEXT_SIZE_SP = 14
        private var DEFAULT_TEXT_COLOR = Color.BLUE
        private const val DEFAULT_ICON_SIZE_DP = 48
        private val DEFAULT_DECOR_RING_COLOR = Color.RED
        private const val DEFAULT_DECOR_RING_WEIGHT = 0.2f
        private const val DEFAULT_INNER_HOLE_WEIGHT = 0.25f
        private const val DEFAULT_LABEL_SPACING = 0f
        private const val DEFAULT_LABEL_BOTTOM_MARGIN = 16f // Adjust this value as needed
        private const val DEFAULT_SHOW_LABEL = true

    }
}
