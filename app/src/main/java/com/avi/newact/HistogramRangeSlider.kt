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
    private var lastDragDirection = 0 // -1 for left, 1 for right, 0 for no drag

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

        histogramData.forEachIndexed { index, value ->
            val left = index * (barWidth + barSpacing) + paddingLeft
            val barHeight = (value / maxDataValue * (height * barHeightMultiplier))
            val top = height - barHeight + thumbRadius
            val right = left + barWidth
            val rect = RectF(left, top, right, height + thumbRadius)

            val isSelected = left >= leftThumbX && right <= rightThumbX
            paint.color = if (isSelected) selectedColor else unselectedColor

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }

        paint.color = unselectedColor
        paint.strokeWidth = 4f
        canvas.drawLine(paddingLeft.toFloat(), baselineY, width.toFloat() - paddingRight, baselineY, paint)

        paint.color = selectedColor
        if (leftThumbX < rightThumbX) {
            canvas.drawLine(leftThumbX, baselineY, rightThumbX, baselineY, paint)
        }

        val thumbYPosition = height + thumbRadius
        if (isOverlapped) {
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius, thumbPaint)
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)
        } else {
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius, thumbPaint)
            canvas.drawCircle(leftThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)
            canvas.drawCircle(rightThumbX, thumbYPosition, thumbRadius, thumbPaint)
            canvas.drawCircle(rightThumbX, thumbYPosition, thumbRadius - thumbStrokeWidth / 2, thumbStrokePaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val minX = paddingLeft.toFloat() + thumbRadius
        val maxX = w.toFloat() - paddingRight - thumbRadius
        leftThumbX = minX
        rightThumbX = maxX
        updateRange()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val minX = paddingLeft.toFloat() + thumbRadius
        val maxX = width.toFloat() - paddingRight - thumbRadius

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
                lastDragDirection = 0
            }
            MotionEvent.ACTION_MOVE -> {
                val currentDragDirection = if (event.x > leftThumbX) 1 else -1

                when {
                    isDraggingLeft -> {
                        if (isOverlapped && lastDragDirection > 0) {
                            rightThumbX = event.x.coerceIn(minX, maxX)
                            leftThumbX = rightThumbX
                        } else {
                            leftThumbX = event.x.coerceIn(minX, rightThumbX)
                            if (leftThumbX == rightThumbX) {
                                isOverlapped = true
                            }
                        }
                    }
                    isDraggingRight -> {
                        if (isOverlapped && lastDragDirection < 0) {
                            leftThumbX = event.x.coerceIn(minX, maxX)
                            rightThumbX = leftThumbX
                        } else {
                            rightThumbX = event.x.coerceIn(leftThumbX, maxX)
                            if (rightThumbX == leftThumbX) {
                                isOverlapped = true
                            }
                        }
                    }
                }

                leftThumbX = leftThumbX.coerceIn(minX, maxX)
                rightThumbX = rightThumbX.coerceIn(minX, maxX)

                if (leftThumbX != rightThumbX) {
                    isOverlapped = false
                }

                lastDragDirection = currentDragDirection
                updateRange()
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDraggingLeft = false
                isDraggingRight = false
                lastDragDirection = 0
            }
        }
        return true
    }

    private fun updateRange() {
        val minAllowedPrice = 5000f
        val maxAllowedPrice = 50000f
        val priceRange = maxAllowedPrice - minAllowedPrice

        val totalWidth = width.toFloat() - paddingLeft - paddingRight - 2 * thumbRadius
        val leftPosition = leftThumbX - (paddingLeft + thumbRadius)
        val rightPosition = rightThumbX - (paddingLeft + thumbRadius)

        val minPrice = minAllowedPrice + (priceRange * (leftPosition / totalWidth))
        val maxPrice = minAllowedPrice + (priceRange * (rightPosition / totalWidth))

        val adjustedMinPrice = minPrice.coerceIn(minAllowedPrice, maxAllowedPrice)
        val adjustedMaxPrice = maxPrice.coerceIn(minAllowedPrice, maxAllowedPrice)

        Log.d("RangeSlider", "Thumb X positions: left=$leftThumbX, right=$rightThumbX")
        Log.d("RangeSlider", "Calculated Prices: minPrice=$adjustedMinPrice, maxPrice=$adjustedMaxPrice")

        onRangeChangeListener?.onRangeChanged(adjustedMinPrice, adjustedMaxPrice)
    }
}