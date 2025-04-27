package com.wifi.wifi_ap.model

import java.util.Date
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents a single Wi-Fi signal strength reading
 */
data class WifiReading(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val timestamp: Long = Date().time,
    val locationName: String
)

/**
 * Groups multiple readings from a single location
 */
data class LocationReadings(
    val locationName: String,
    val readings: MutableList<WifiReading> = mutableListOf(),
    val maxReadings: Int = 100
) {
    fun addReading(reading: WifiReading) {
        if (readings.size < maxReadings) {
            readings.add(reading)
        }
    }

    fun isComplete(): Boolean = readings.size >= maxReadings
    
    fun getAverageRssi(): Int {
        return if (readings.isEmpty()) 0 else readings.sumOf { it.rssi } / readings.size
    }
    
    fun getMinRssi(): Int {
        return readings.minOfOrNull { it.rssi } ?: 0
    }
    
    fun getMaxRssi(): Int {
        return readings.maxOfOrNull { it.rssi } ?: 0
    }
    
    fun getStandardDeviation(): Double {
        if (readings.isEmpty()) return 0.0
        val mean = getAverageRssi()
        val variance = readings.sumOf { (it.rssi - mean).toDouble().pow(2) } / readings.size
        return sqrt(variance)
    }
}
