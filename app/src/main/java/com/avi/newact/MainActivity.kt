package com.avi.newact


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val histogramRangeSlider = findViewById<HistogramRangeSlider>(R.id.histogramRangeSlider)

        // Example data - replace with your actual data
        val sampleData = listOf(
            1f, 2f, 3f, 5f, 8f, 12f, 18f, 25f, 33f, 42f,
            52f, 63f, 75f, 88f, 100f, 110f, 118f, 124f, 128f, 130f,
            130f, 128f, 124f, 118f, 110f, 100f, 88f, 75f, 63f, 52f,
            42f, 33f, 25f, 18f, 12f, 8f, 5f, 3f, 2f, 1f
        )




        histogramRangeSlider.setHistogramData(sampleData)
    }
}