package com.wifi.wifi_ap.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.wifi.wifi_ap.R
import com.wifi.wifi_ap.ResultsActivity
import com.wifi.wifi_ap.data.WifiDataManager
import com.wifi.wifi_ap.model.LocationReadings

/**
 * Dialog to display the final collection of readings
 */
class ReadingsResultDialog : DialogFragment() {
    
    companion object {
        private const val ARG_LOCATION = "arg_location"
        
        fun newInstance(locationReadings: LocationReadings): ReadingsResultDialog {
            val args = Bundle().apply {
                putString(ARG_LOCATION, locationReadings.locationName)
            }
            return ReadingsResultDialog().apply {
                arguments = args
                this.locationReadings = locationReadings
            }
        }
    }
    
    private lateinit var locationReadings: LocationReadings
    private lateinit var dataManager: WifiDataManager
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_readings_result, null)
        
        // Initialize data manager
        dataManager = WifiDataManager(requireContext())
        
        val tvReadings = view.findViewById<TextView>(R.id.tvReadingsResult)
        val btnCopy = view.findViewById<Button>(R.id.btnCopyReadings)
        val btnClose = view.findViewById<Button>(R.id.btnCloseDialog)
        val btnViewAll = view.findViewById<Button>(R.id.btnViewAllResults)
        
        // Format the readings data
        val formattedText = formatReadingsData()
        tvReadings.text = formattedText
        
        // Ensure the readings are saved in the database
        dataManager.saveLocationReadings(locationReadings)
        
        // Set up copy button
        btnCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Wi-Fi Readings", formattedText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Readings copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        
        // Set up view all results button
        btnViewAll.setOnClickListener {
            val intent = Intent(requireContext(), ResultsActivity::class.java)
            startActivity(intent)
            dismiss()
        }
        
        return AlertDialog.Builder(requireContext())
            .setTitle("Wi-Fi Signal Readings")
            .setView(view)
            .create()
            .apply {
                btnClose.setOnClickListener { dismiss() }
            }
    }
    
    private fun formatReadingsData(): String {
        val sb = StringBuilder()
        
        // Header with location name
        sb.append("Location: ${locationReadings.locationName}\n")
        sb.append("Total Readings: ${locationReadings.readings.size}\n\n")
        
        // Statistics
        sb.append("Min RSSI: ${locationReadings.getMinRssi()} dBm\n")
        sb.append("Avg RSSI: ${locationReadings.getAverageRssi()} dBm\n")
        sb.append("Max RSSI: ${locationReadings.getMaxRssi()} dBm\n")
        sb.append("Std Dev: ${String.format("%.2f", locationReadings.getStandardDeviation())}\n\n")
        
        // Readings in a 10x10 grid format
        sb.append("RSSI Readings (dBm):\n")
        
        // Format in a grid with 10 columns
        locationReadings.readings.map { it.rssi }.chunked(10).forEachIndexed { rowIndex, row ->
            val rowNum = rowIndex * 10 + 1
            sb.append("${rowNum.toString().padStart(2)}-${(rowNum + row.size - 1).toString().padStart(2)}: ")
            sb.append(row.joinToString(" ") { it.toString().padStart(3) })
            sb.append("\n")
        }
        
        return sb.toString()
    }
}
