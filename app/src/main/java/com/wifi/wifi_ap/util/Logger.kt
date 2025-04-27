package com.wifi.wifi_ap.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Logger utility to capture and display logs throughout the app
 */
object Logger {
    private const val TAG = "WifiTracker"
    private val logEntries = mutableListOf<LogEntry>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    data class LogEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val level: Level,
        val message: String
    ) {
        fun getFormattedTimestamp(): String = dateFormat.format(Date(timestamp))
        
        fun getFormattedMessage(): String {
            return "[${getFormattedTimestamp()}] ${level.prefix}: $message"
        }
    }
    
    enum class Level(val prefix: String) {
        INFO("INFO"),
        DEBUG("DEBUG"),
        WARNING("WARN"),
        ERROR("ERROR"),
        RESULT("RESULT")  // New level for results display
    }
    
    fun log(level: Level, message: String) {
        val entry = LogEntry(level = level, message = message)
        logEntries.add(entry)
        
        // Also log to Android's logcat
        when (level) {
            Level.INFO -> Log.i(TAG, message)
            Level.DEBUG -> Log.d(TAG, message)
            Level.WARNING -> Log.w(TAG, message)
            Level.ERROR -> Log.e(TAG, message)
            Level.RESULT -> Log.i("$TAG-RESULT", message)
        }
    }
    
    fun info(message: String) = log(Level.INFO, message)
    fun debug(message: String) = log(Level.DEBUG, message)
    fun warning(message: String) = log(Level.WARNING, message)
    fun error(message: String) = log(Level.ERROR, message)
    fun result(message: String) = log(Level.RESULT, message)
    
    /**
     * Formats and logs the final collection of Wi-Fi readings
     */
    fun logFinalReadings(locationName: String, readings: List<Map<String, Any>>) {
        // Create a header for the final readings
        val header = "===== FINAL READINGS FOR $locationName ====="
        result(header)
        
        // Log each reading with its RSSI value
        readings.forEachIndexed { index, reading ->
            val rssi = reading["rssi"] as? Int ?: 0
            result("#${index + 1}: RSSI: $rssi dBm")
        }
        
        // Log a summary
        result("===== END OF READINGS =====")
    }
    
    /**
     * Formats the readings in a structured way for display
     */
    fun formatReadingResults(locationName: String, readings: List<Map<String, Any>>): String {
        val builder = StringBuilder()
        builder.append("Location: $locationName\n")
        builder.append("Total Readings: ${readings.size}\n\n")
        
        // Calculate statistics
        val rssiValues = readings.mapNotNull { it["rssi"] as? Int }
        if (rssiValues.isNotEmpty()) {
            val min = rssiValues.minOrNull() ?: 0
            val max = rssiValues.maxOrNull() ?: 0
            val avg = rssiValues.average().toInt()
            
            builder.append("Min RSSI: $min dBm\n")
            builder.append("Max RSSI: $max dBm\n")
            builder.append("Avg RSSI: $avg dBm\n\n")
        }
        
        // Format in a grid-like structure (10x10)
        builder.append("Readings (RSSI values in dBm):\n")
        rssiValues.chunked(10).forEachIndexed { rowIndex, row ->
            val rowStart = rowIndex * 10 + 1
            builder.append("${rowStart.toString().padStart(2)}-${(rowStart + row.size - 1).toString().padStart(2)}: ")
            builder.append(row.joinToString(" ") { it.toString().padStart(3) })
            builder.append("\n")
        }
        
        return builder.toString()
    }
    
    fun getLogs(): List<LogEntry> = logEntries.toList()
    
    fun getFormattedLogs(): String {
        return logEntries.joinToString("\n") { it.getFormattedMessage() }
    }
    
    fun clear() {
        logEntries.clear()
    }
    
    fun clearLogs() {
        logEntries.clear()
    }
    
    fun getLastLogEntries(count: Int): List<LogEntry> {
        return if (logEntries.size <= count) {
            logEntries.toList()
        } else {
            logEntries.subList(logEntries.size - count, logEntries.size).toList()
        }
    }
    
    /**
     * Get only RESULT level logs
     */
    fun getResultLogs(): List<LogEntry> {
        return logEntries.filter { it.level == Level.RESULT }
    }
    
    /**
     * Get the formatted string of all result logs
     */
    fun getFormattedResultLogs(): String {
        return getResultLogs().joinToString("\n") { it.message }
    }
}
