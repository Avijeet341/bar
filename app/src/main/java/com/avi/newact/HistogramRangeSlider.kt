package com.avi.newact

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

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
    private var isOverlapped = false

    private val selectedColor = Color.parseColor("#FF5A5F")
    private val unselectedColor = Color.parseColor("#E4E4E4")
    private val thumbColor = Color.WHITE
    private val shadowColor = Color.parseColor("#20000000")
    private val thumbStrokeWidth = 4f
    private val thumbStrokeColor = Color.parseColor("#E4E4E4")
    private val bottomPadding = 40f

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
        if (isOverlapped) {
            // Draw only one thumb when overlapped
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius, thumbPaint)
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)
        } else {
            // Draw both thumbs when not overlapped
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius, thumbPaint)
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)
            canvas.drawCircle(rightThumbX, thumbYPosition, thumbRadius, thumbPaint)
            canvas.drawCircle(rightThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        leftThumbX = paddingLeft.toFloat() + thumbRadius
        rightThumbX = w.toFloat() - paddingRight - thumbRadius
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchArea = thumbRadius * 1.5f
                when {
                    event.x in (leftThumbX - touchArea)..(leftThumbX + touchArea) -> {
                        isDraggingLeft = true
                        isDraggingRight = false
                    }
                    event.x in (rightThumbX - touchArea)..(rightThumbX + touchArea) -> {
                        isDraggingRight = true
                        isDraggingLeft = false
                    }
                    else -> {
                        isDraggingLeft = false
                        isDraggingRight = false
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val minX = paddingLeft.toFloat()
                val maxX = width.toFloat() - paddingRight

                when {
                    isDraggingLeft -> {
                        leftThumbX = event.x.coerceIn(minX, maxX)
                        if (leftThumbX > rightThumbX) {
                            rightThumbX = leftThumbX
                            isOverlapped = true
                        } else {
                            isOverlapped = false
                        }
                    }
                    isDraggingRight -> {
                        rightThumbX = event.x.coerceIn(minX, maxX)
                        if (rightThumbX < leftThumbX) {
                            leftThumbX = rightThumbX
                            isOverlapped = true
                        } else {
                            isOverlapped = false
                        }
                    }
                }
                updateRange()
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
        val totalWidth = width.toFloat() - paddingLeft - paddingRight

        // Calculate the relative positions of thumbs within the totalWidth
        val relativeLeftX = (leftThumbX - paddingLeft) / totalWidth
        val relativeRightX = (rightThumbX - paddingLeft) / totalWidth

        // Calculate prices based on thumb positions
        val minAllowedPrice = 25760f
        val maxAllowedPrice = 25900f
        val priceRange = maxAllowedPrice - minAllowedPrice

        val minPrice = minAllowedPrice + (priceRange * relativeLeftX)
        val maxPrice = minAllowedPrice + (priceRange * relativeRightX)

        // Ensure that minPrice and maxPrice are within the allowed range
        val adjustedMinPrice = minPrice.coerceIn(minAllowedPrice, maxAllowedPrice)
        val adjustedMaxPrice = maxPrice.coerceIn(minAllowedPrice, maxAllowedPrice)

        Log.d("RangeSlider", "Thumb X positions: left=$leftThumbX, right=$rightThumbX")
        Log.d("RangeSlider", "Calculated Prices: minPrice=$adjustedMinPrice, maxPrice=$adjustedMaxPrice")

        // Notify listener of the range change
        onRangeChangeListener?.onRangeChanged(adjustedMinPrice, adjustedMaxPrice)
    }
}