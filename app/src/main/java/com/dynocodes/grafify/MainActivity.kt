package com.dynocodes.grafify

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChart = findViewById<SimplePieChart>(R.id.piechart_view)
        pieChart.addSlice(com.dynocodes.grafify.SimplePieChart.Slice(Color.BLUE, 20f,"ram"))
        pieChart.addSlice(com.dynocodes.grafify.SimplePieChart.Slice(Color.GREEN, 40f,"shyam"))
        pieChart.addSlice(com.dynocodes.grafify.SimplePieChart.Slice(Color.RED, 40f,"shyam"))
        pieChart.addSlice(com.dynocodes.grafify.SimplePieChart.Slice(Color.CYAN, 40f,"shyam"))
        pieChart.addSlice(com.dynocodes.grafify.SimplePieChart.Slice(Color.YELLOW, 30f,"shyam"))
    }
}