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

        // Example data - replace with your actual data
        val sampleData = listOf(
            1f, 2f, 3f, 5f, 8f, 12f, 18f, 25f, 33f, 42f,
            52f, 63f, 75f, 88f, 100f, 110f, 118f, 124f, 128f, 130f,
            130f, 128f, 124f, 118f, 110f, 100f, 88f, 75f, 63f, 52f,
            42f, 33f, 25f, 18f, 12f, 8f, 5f, 3f, 2f, 1f
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