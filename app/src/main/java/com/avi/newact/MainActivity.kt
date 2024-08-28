package com.avi.newact

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity(), HistogramRangeSlider.OnRangeChangeListener {
    private lateinit var minPriceTextView: TextView
    private lateinit var maxPriceTextView: TextView

    private val maxAllowedPrice = 50000f
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val histogramRangeSlider = findViewById<HistogramRangeSlider>(R.id.histogramRangeSlider)
        histogramRangeSlider.onRangeChangeListener = this

        minPriceTextView = findViewById(R.id.minPriceTextView)
        maxPriceTextView = findViewById(R.id.maxPriceTextView)

        // Updated sample data to match the image, including the extended right tail
        val sampleData = listOf(
            5f, 8f, 12f, 18f, 25f, 35f, 48f, 64f, 85f, 110f,
            140f, 175f, 215f, 260f, 310f, 365f, 425f, 490f, 560f, 635f,
            715f, 800f, 890f, 985f, 1000f, 985f, 890f, 800f, 715f, 635f,
            560f, 490f, 425f, 365f, 310f, 260f, 215f, 175f, 140f, 110f,
            85f, 64f, 48f, 35f, 25f, 18f, 12f, 8f, 5f, 3f,
            // Extended tail
            2f, 2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f
        )

        histogramRangeSlider.setHistogramData(sampleData)
    }

    override fun onRangeChanged(minPrice: Float, maxPrice: Float) {
        currencyFormat.maximumFractionDigits = 0
        val formattedMinPrice = currencyFormat.format(minPrice.toInt())
        val formattedMaxPrice = if (maxPrice >= maxAllowedPrice) {
            "${currencyFormat.format(maxPrice.toInt())}+"
        } else {
            currencyFormat.format(maxPrice.toInt())
        }

        minPriceTextView.text = formattedMinPrice
        maxPriceTextView.text = formattedMaxPrice
    }
}