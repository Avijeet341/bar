package com.avi.newact

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HistogramRangeSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Listener to handle price range changes
    interface OnRangeChangeListener {
        fun onRangeChanged(minPrice: Float, maxPrice: Float)
    }

    var onRangeChangeListener: OnRangeChangeListener? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barWidth = 18f
    private val barSpacing = 4f
    private val thumbRadius = 50f
    private val cornerRadius = 5f
    private var histogramData: List<Float> = listOf()

    private var leftThumbX = 0f
    private var rightThumbX = 0f
    private var isDraggingLeft = false
    private var isDraggingRight = false

    private val selectedColor = Color.parseColor("#FF5A5F") // Change to match the pink color from the screenshot
    private val unselectedColor = Color.parseColor("#E4E4E4")
    private val thumbColor = Color.WHITE
    private val shadowColor = Color.parseColor("#20000000")
    private val thumbStrokeWidth = 4f
    private val thumbStrokeColor = Color.parseColor("#E4E4E4")
    private val bottomPadding = 40f // Space between the thumb and the bottom of the layout

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = thumbColor
        setShadowLayer(12f, 0f, 6f, shadowColor)
    }

    private val thumbStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = thumbStrokeColor
        style = Paint.Style.STROKE
        strokeWidth = thumbStrokeWidth
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setHistogramData(data: List<Float>) {
        histogramData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = height.toFloat() - thumbRadius * 2 - bottomPadding
        val maxDataValue = histogramData.maxOrNull() ?: 1f
        val barHeightMultiplier = 0.7f
        val baselineY = height + thumbRadius

        // Draw histogram bars and baseline segments
        histogramData.forEachIndexed { index, value ->
            val left = index * (barWidth + barSpacing) + paddingLeft
            val barHeight = (value / maxDataValue * (height * barHeightMultiplier))
            val top = height - barHeight + thumbRadius
            val right = left + barWidth
            val rect = RectF(left, top, right, height + thumbRadius)

            // Determine if the current bar is within the selected range
            val isSelected = left >= leftThumbX && right <= rightThumbX
            paint.color = if (isSelected) selectedColor else unselectedColor

            // Draw the bar
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }

        // Draw the baseline with color based on the selection
        paint.color = unselectedColor
        paint.strokeWidth = 4f
        canvas.drawLine(paddingLeft.toFloat(), baselineY, width.toFloat() - paddingRight, baselineY, paint)

        // Draw the selected portion of the baseline
        paint.color = selectedColor
        if (leftThumbX < rightThumbX) {
            canvas.drawLine(leftThumbX, baselineY, rightThumbX, baselineY, paint)
        }

        // Draw thumbs
        val thumbYPosition = height + thumbRadius
        canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius, thumbPaint)
        canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)

        canvas.drawCircle(rightThumbX, thumbYPosition, thumbRadius, thumbPaint)
        canvas.drawCircle(rightThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        leftThumbX = paddingLeft.toFloat() + thumbRadius
        rightThumbX = w.toFloat() - paddingRight - thumbRadius
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x in (leftThumbX - thumbRadius)..(leftThumbX + thumbRadius)) {
                    isDraggingLeft = true
                } else if (event.x in (rightThumbX - thumbRadius)..(rightThumbX + thumbRadius)) {
                    isDraggingRight = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDraggingLeft) {
                    leftThumbX = max(
                        paddingLeft.toFloat() + thumbRadius,
                        min(rightThumbX - thumbRadius, event.x)
                    )
                    updateRange()
                } else if (isDraggingRight) {
                    rightThumbX = min(
                        width.toFloat() - paddingRight - thumbRadius,
                        max(leftThumbX + thumbRadius, event.x)
                    )
                    updateRange()
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDraggingLeft = false
                isDraggingRight = false
            }
        }
        return true
    }

    private fun updateRange() {
        val totalWidth = width.toFloat() - paddingLeft - paddingRight - thumbRadius * 2

        // Ensure thumbs are within bounds
        val clampedLeftThumbX = maxOf(paddingLeft.toFloat(), minOf(leftThumbX, rightThumbX - thumbRadius))
        val clampedRightThumbX = maxOf(clampedLeftThumbX + thumbRadius, minOf(rightThumbX, width.toFloat() - paddingRight))

        // Calculate the relative positions of thumbs within the totalWidth
        val relativeLeftX = (clampedLeftThumbX - paddingLeft) / totalWidth
        val relativeRightX = (clampedRightThumbX - paddingLeft) / totalWidth

        // Calculate prices based on thumb positions using the actual price range
        val minAllowedPrice = 24260f
        val maxAllowedPrice = 26771f
        val priceRange = maxAllowedPrice - minAllowedPrice

        val minPrice = minAllowedPrice + (priceRange * relativeLeftX)
        val maxPrice = minAllowedPrice + (priceRange * relativeRightX)

        Log.d("RangeSlider", "Thumb X positions: left=$clampedLeftThumbX, right=$clampedRightThumbX")
        Log.d("RangeSlider", "Calculated Prices: minPrice=$minPrice, maxPrice=$maxPrice")

        // Notify listener of the range change
        onRangeChangeListener?.onRangeChanged(minPrice, maxPrice)
    }



}
