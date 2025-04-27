package com.wifi.wifi_ap

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.wifi.wifi_ap.data.WifiDataManager
import com.wifi.wifi_ap.databinding.ActivityResultsBinding
import com.wifi.wifi_ap.model.LocationReadings
import com.wifi.wifi_ap.model.WifiReading
import kotlin.math.abs

class ResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultsBinding
    private lateinit var dataManager: WifiDataManager
    private var locations: List<LocationReadings> = listOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dataManager = WifiDataManager(this)
        locations = dataManager.getAllLocations()
        
        setupUI()
        populateLocationsSpinner()
    }
    
    private fun setupUI() {
        // Set up toolbar navigation
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Set up compare locations button
        binding.btnCompareLocations.setOnClickListener {
            binding.btnCompareLocations.shrink()
            showLocationComparison()
        }
        
        binding.spinnerLocations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                if (pos >= 0 && locations.isNotEmpty()) {
                    displayLocationData(locations[pos])
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    
    private fun populateLocationsSpinner() {
        if (locations.isEmpty()) {
            binding.spinnerLocations.visibility = View.GONE
            binding.cardStats.visibility = View.GONE
            binding.chartCard.visibility = View.GONE
            binding.btnCompareLocations.visibility = View.GONE
            
            // Show empty state message
            val emptyText = TextView(this)
            emptyText.text = "No locations with readings available yet"
            emptyText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyText.textSize = 16f
            emptyText.setPadding(0, 100, 0, 0)
            
            binding.scrollView.removeAllViews()
            binding.scrollView.addView(emptyText)
        } else {
            val adapter = ArrayAdapter(
                this,
                R.layout.spinner_item,
                locations.map { it.locationName }
            )
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.spinnerLocations.adapter = adapter
            
            // Select the first location by default
            if (locations.isNotEmpty()) {
                displayLocationData(locations[0])
            }
        }
    }

    private fun displayLocationData(location: LocationReadings) {
        val readings = location.readings
        if (readings.isEmpty()) return
        
        // Update stats display with animation
        animateTextChange(binding.tvMinRssi, "${readings.minByOrNull { it.rssi }?.rssi ?: 0} dBm")
        animateTextChange(binding.tvMaxRssi, "${readings.maxByOrNull { it.rssi }?.rssi ?: 0} dBm")
        animateTextChange(binding.tvAvgRssi, "${calculateAverage(readings)} dBm")
        animateTextChange(binding.tvStdDev, String.format("%.2f", calculateStandardDeviation(readings)))
        
        // Update reading count
        binding.tvReadingCount.text = "${readings.size} readings collected"
        
        // Update chart with readings data
        updateSignalChart(readings)
    }
    
    private fun updateSignalChart(readings: List<WifiReading>) {
        val entries = readings.mapIndexed { index, reading ->
            Entry(index.toFloat(), reading.rssi.toFloat())
        }
        
        val dataSet = LineDataSet(entries, "Signal Strength").apply {
            color = ContextCompat.getColor(this@ResultsActivity, R.color.purple_500)
            lineWidth = 2f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(this@ResultsActivity, R.color.purple_700))
            circleRadius = 3f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@ResultsActivity, R.color.purple_200)
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 10f
        }
        
        val lineData = LineData(dataSet)
        binding.signalChart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            
            // Customize X axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                axisLineColor = Color.LTGRAY
                textColor = Color.DKGRAY
                labelCount = 5
            }
            
            // Customize Y axis
            axisLeft.apply {
                setDrawGridLines(true)
                axisLineColor = Color.LTGRAY
                textColor = Color.DKGRAY
                axisMaximum = 0f
                axisMinimum = -100f
            }
            
            axisRight.isEnabled = false
            
            // Animate chart
            animateX(500)
            invalidate()
        }
    }

    // Helper function to animate text changes
    private fun animateTextChange(textView: TextView, newText: String) {
        textView.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                textView.text = newText
                textView.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    // Calculate average signal strength
    private fun calculateAverage(readings: List<WifiReading>): Int {
        if (readings.isEmpty()) return 0
        return readings.sumOf { it.rssi } / readings.size
    }
    
    // Calculate standard deviation for signal strength
    private fun calculateStandardDeviation(readings: List<WifiReading>): Double {
        if (readings.isEmpty()) return 0.0
        val avg = calculateAverage(readings)
        val variance = readings.sumOf { (it.rssi - avg) * (it.rssi - avg) } / readings.size.toDouble()
        return kotlin.math.sqrt(variance)
    }
    
    private fun showLocationComparison() {
        if (locations.size < 2) {
            AlertDialog.Builder(this)
                .setTitle("Comparison Not Available")
                .setMessage("You need at least 2 locations to compare results.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        // Group locations by WiFi SSID
        val networkMap = mutableMapOf<String, MutableList<LocationReadings>>()
        
        // Build a map of SSID -> List of locations with readings for that SSID
        locations.forEach { location ->
            // Get first reading's SSID to identify the network
            val ssid = location.readings.firstOrNull()?.ssid ?: ""
            
            if (ssid.isNotEmpty()) {
                if (!networkMap.containsKey(ssid)) {
                    networkMap[ssid] = mutableListOf()
                }
                networkMap[ssid]?.add(location)
            }
        }
        
        // Filter out SSIDs with only one location (can't compare)
        val comparableNetworks = networkMap.filter { it.value.size > 1 }
        
        if (comparableNetworks.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No Comparable Data")
                .setMessage("You need at least 2 locations with readings for the same WiFi network.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        // If only one network to compare, show it directly
        if (comparableNetworks.size == 1) {
            showNetworkComparisonResults(comparableNetworks.keys.first(), comparableNetworks.values.first())
        } else {
            // Show network selection dialog
            val networks = comparableNetworks.keys.toList()
            AlertDialog.Builder(this)
                .setTitle("Select WiFi Network to Compare")
                .setItems(networks.toTypedArray()) { _, which ->
                    val selectedNetwork = networks[which]
                    val locationsForNetwork = comparableNetworks[selectedNetwork] ?: return@setItems
                    showNetworkComparisonResults(selectedNetwork, locationsForNetwork)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        // Re-extend the FAB after dialog shows
        binding.btnCompareLocations.extend()
    }

    private fun showNetworkComparisonResults(ssid: String, locationsForNetwork: List<LocationReadings>) {
        // Use RecyclerView instead of just text for better presentation
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comparison_results, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvComparisonResults)
        val tvNetworkName = dialogView.findViewById<TextView>(R.id.tvComparisonNetworkName)
        
        // Set network name
        tvNetworkName.text = "Network: $ssid"
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        
        // Sort locations by average signal strength (descending)
        val sortedLocations = locationsForNetwork.sortedByDescending { it.getAverageRssi() }
        
        // Set up adapter
        val adapter = LocationComparisonAdapter(sortedLocations)
        recyclerView.adapter = adapter
        
        // Show dialog with results
        AlertDialog.Builder(this)
            .setTitle("Location Comparison")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private class LocationComparisonAdapter(
        private val locations: List<LocationReadings>
    ) : RecyclerView.Adapter<LocationComparisonAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvLocation: TextView = itemView.findViewById(R.id.tvComparisonLocation)
            val tvAvgSignal: TextView = itemView.findViewById(R.id.tvComparisonAvgSignal)
            val tvMinMaxSignal: TextView = itemView.findViewById(R.id.tvComparisonMinMaxSignal)
            val signalIndicator: View = itemView.findViewById(R.id.viewSignalIndicator)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comparison_result, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = locations.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val location = locations[position]
            val avgSignal = location.getAverageRssi()
            
            holder.tvLocation.text = location.locationName
            holder.tvAvgSignal.text = "$avgSignal dBm"
            holder.tvMinMaxSignal.text = "Min: ${location.getMinRssi()} dBm | Max: ${location.getMaxRssi()} dBm"
            
            // Set signal strength indicator color
            val color = when {
                avgSignal > -65 -> Color.parseColor("#4CAF50") // Good - green
                avgSignal > -80 -> Color.parseColor("#FFC107") // Medium - yellow
                else -> Color.parseColor("#F44336") // Poor - red
            }
            holder.signalIndicator.setBackgroundColor(color)
            
            // Highlight the best location
            if (position == 0) {
                holder.itemView.setBackgroundColor(Color.parseColor("#E8F5E9")) // Light green
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
}
