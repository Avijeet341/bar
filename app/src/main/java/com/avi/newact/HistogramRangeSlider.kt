package com.avi.newact

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HistogramRangeSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barWidth = 18f
    private val barSpacing = 4f
    private val thumbRadius = 50f
    private val cornerRadius = 5f
    private var histogramData: List<Float> = listOf()
    private var minValue = 0f
    private var maxValue = 1f
    private var leftThumbX = 0f
    private var rightThumbX = 0f
    private var isDraggingLeft = false
    private var isDraggingRight = false

    private val selectedColor = Color.parseColor("#FF5A5F")
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

        // Increase this value to make the bars taller
        val height = height.toFloat() - thumbRadius * 2 - bottomPadding
        val maxDataValue = histogramData.maxOrNull() ?: 1f

        // Increased the height multiplier for the bars
        val barHeightMultiplier = 0.7f // Increase this to make bars taller (e.g., 0.7f, 0.8f)

        // Draw histogram bars
        histogramData.forEachIndexed { index, value ->
            val left = index * (barWidth + barSpacing) + paddingLeft
            val barHeight = (value / maxDataValue * (height * barHeightMultiplier))
            val top = height - barHeight + thumbRadius
            val right = left + barWidth
            val rect = RectF(left, top, right, height + thumbRadius)

            paint.color = if (left >= leftThumbX - thumbRadius && right <= rightThumbX + thumbRadius)
                selectedColor else unselectedColor

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
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
                } else if (isDraggingRight) {
                    rightThumbX = min(
                        width.toFloat() - paddingRight - thumbRadius,
                        max(leftThumbX + thumbRadius, event.x)
                    )
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
}
