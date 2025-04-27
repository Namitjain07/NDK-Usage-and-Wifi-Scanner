package com.wifi.wifi_ap

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.wifi.wifi_ap.adapter.WifiNetworkAdapter
import com.wifi.wifi_ap.data.WifiDataManager
import com.wifi.wifi_ap.databinding.ActivityMainBinding
import com.wifi.wifi_ap.dialog.ReadingsResultDialog
import com.wifi.wifi_ap.model.LocationReadings
import com.wifi.wifi_ap.model.WifiReading
import com.wifi.wifi_ap.util.Logger
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var wifiManager: WifiManager
    private lateinit var dataManager: WifiDataManager
    private var selectedNetworkSSID: String? = null
    private var selectedNetworkBSSID: String? = null
    private var isRecording = false
    private var currentLocationReadings: LocationReadings? = null
    private val handler = Handler(Looper.getMainLooper())
    private var scanTimer: Timer? = null
    private lateinit var networkAdapter: WifiNetworkAdapter
    private var selectedLocation: String = ""
    private val CUSTOM_LOCATION_INDEX = 5  // Index of "Custom Location" in the predefined_locations array
    
    // Broadcast receiver for Wi-Fi scan results
    private val wifiScanReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                Logger.info("Wi-Fi scan results received")
                scanSuccess()
            }
        }
    }

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.all { it.value }) {
            Logger.info("Location permissions granted")
            scanWifiNetworks()
        } else {
            Logger.error("Location permissions denied")
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Logger.info("MainActivity created")
        
        // Replace findViewById(R.id.main) with binding.root
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        dataManager = WifiDataManager(this)
        
        // Setup RecyclerView and adapter
        setupRecyclerView()
        
        // Restore selected network if available
        dataManager.getSelectedNetwork()?.let { (ssid, bssid) ->
            selectedNetworkSSID = ssid
            selectedNetworkBSSID = bssid
            Logger.info("Restored selected network: $ssid")
            updateSelectedNetworkDisplay()
        }
        
        setupUI()
        setupLocationSpinner()
        setupLogDisplay()
    }
    
    private fun setupRecyclerView() {
        Logger.debug("Setting up RecyclerView")
        networkAdapter = WifiNetworkAdapter(emptyList()) { scanResult ->
            // When a network is selected from the list
            selectedNetworkSSID = scanResult.SSID
            selectedNetworkBSSID = scanResult.BSSID
            dataManager.saveSelectedNetwork(scanResult.SSID, scanResult.BSSID)
            Logger.info("Selected network: ${scanResult.SSID} (${scanResult.BSSID})")
            updateSelectedNetworkDisplay()
            updateLogDisplay()
        }
        
        binding.rvWifiNetworks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = networkAdapter
        }
    }
    
    private fun updateSelectedNetworkDisplay() {
        if (selectedNetworkSSID != null) {
            binding.tvSelectedNetwork.text = getString(R.string.selected_network, selectedNetworkSSID)
            networkAdapter.selectedBssid = selectedNetworkBSSID
        } else {
            binding.tvSelectedNetwork.text = getString(R.string.no_network_selected)
        }
    }
    
    private fun updateLogDisplay() {
        // Get all log entries instead of just the last 20
        val logEntries = Logger.getLastLogEntries(Int.MAX_VALUE) // Using MAX_VALUE to get all entries
        val formattedLogs = buildFormattedLogHtml(logEntries)
        
        // Use WebView to display HTML content
        binding.webViewLogs.loadDataWithBaseURL(null, formattedLogs, "text/html", "UTF-8", null)
        
        // Ensure smooth scrolling to the bottom of the log
        smoothScrollLogsToBottom()
    }
    
    private fun buildFormattedLogHtml(logEntries: List<Logger.LogEntry>): String {
        // Create HTML formatted text with colors based on log level
        val stringBuilder = StringBuilder()
        
        // Add CSS styling with dark blue background and optimizations for large content
        stringBuilder.append("""
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: monospace;
                        font-size: 13px;
                        color: #E0E0E0;
                        margin: 0;
                        padding: 8px;
                        background: #0A1929; /* Dark blue background */
                    }
                    .log-entry {
                        margin-bottom: 4px;
                        white-space: pre-wrap;
                        line-height: 1.3;
                    }
                    .info { color: #8BE9FD; }
                    .debug { color: #50FA7B; }
                    .warning { color: #FFB86C; }
                    .error { color: #FF5555; }
                    .result { color: #BD93F9; }
                </style>
            </head>
            <body>
        """)
        
        for (entry in logEntries) {
            val cssClass = when (entry.level) {
                Logger.Level.INFO -> "info"
                Logger.Level.DEBUG -> "debug"
                Logger.Level.WARNING -> "warning"
                Logger.Level.ERROR -> "error"
                Logger.Level.RESULT -> "result"
                else -> ""
            }
            
            stringBuilder.append("<div class='log-entry ${cssClass}'>${entry.getFormattedMessage()}</div>")
        }
        
        stringBuilder.append("</body></html>")
        return stringBuilder.toString()
    }
    
    private fun smoothScrollLogsToBottom() {
        // JavaScript to scroll to the bottom of the WebView content
        binding.webViewLogs.post {
            binding.webViewLogs.evaluateJavascript(
                "window.scrollTo(0, document.body.scrollHeight);", null
            )
        }
    }
    
    private fun setupLogDisplay() {
        // Configure WebView settings for better performance with large content
        binding.webViewLogs.settings.apply {
            javaScriptEnabled = true
            displayZoomControls = false
            setSupportZoom(false)
            
            // Optimize WebView for large content
            setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
        }
        
        // Add clear logs button functionality
        binding.btnClearLogs.setOnClickListener {
            Logger.clearLogs()
            updateLogDisplay()
            Toast.makeText(this, R.string.logs_cleared, Toast.LENGTH_SHORT).show()
        }
        
        // Update log display initially
        updateLogDisplay()
        
        // Set up auto-refresh for logs every 2 seconds while app is running
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    updateLogDisplay()
                    handler.postDelayed(this, 2000)
                }
            }
        }, 2000)
    }
    
    override fun onResume() {
        super.onResume()
        Logger.debug("MainActivity resumed")
        try {
            registerReceiver(wifiScanReceiver, 
                IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        } catch (e: SecurityException) {
            Logger.error("Failed to register WiFi scan receiver due to permission: ${e.message}")
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
        }
        
        // Initial scan for networks
        checkPermissionAndScanNetworks()
    }
    
    override fun onPause() {
        super.onPause()
        Logger.debug("MainActivity paused")
        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
            Logger.error("Failed to unregister receiver: ${e.message}")
        }
        stopSignalMonitoring()
    }
    
    private fun setupUI() {
        // Wi-Fi network scan button
        binding.btnScanNetworks.setOnClickListener {
            Logger.info("Scan networks button clicked")
            checkPermissionAndScanNetworks()
        }
        
        // View results button
        binding.btnViewResults.setOnClickListener {
            Logger.info("Opening saved results")
            val intent = Intent(this, ResultsActivity::class.java)
            startActivity(intent)
        }
        
        // Start/stop reading button
        binding.btnStartReading.setOnClickListener {
            if (isRecording) {
                Logger.info("Stopping signal recording")
                stopRecording()
            } else {
                Logger.info("Starting signal recording")
                startRecording()
            }
            updateLogDisplay()
        }
        
        // Ensure location input field is enabled and properly handles focus
        binding.etLocationName.apply {
            isEnabled = true
            isFocusableInTouchMode = true
            
            setOnClickListener {
                requestFocus()
                showKeyboard()
            }
            
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showKeyboard()
                }
            }
        }
    }
    
    private fun setupLocationSpinner() {
        // Get locations array from resources
        val locations = resources.getStringArray(R.array.predefined_locations)
        
        // Update the spinner setup to use the custom style
        val adapter = ArrayAdapter(this, R.layout.spinner_item, locations)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerLocations.adapter = adapter
        
        // Handle selection events
        binding.spinnerLocations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                
                // Show custom field if "Custom Location" is selected
                if (position == CUSTOM_LOCATION_INDEX) {
                    binding.etLocationContainer.visibility = View.VISIBLE
                    binding.etLocationName.text?.clear()
                    binding.etLocationName.requestFocus()
                    showKeyboard()
                } else {
                    binding.etLocationContainer.visibility = View.GONE
                    hideKeyboard()
                    selectedLocation = selectedItem
                }
                
                Logger.info("Selected location: $selectedItem")
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etLocationName.windowToken, 0)
    }
    
    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etLocationName, InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun checkPermissionAndScanNetworks() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            scanWifiNetworks()
        } else {
            Logger.warning("Requesting location permissions")
            requestPermissionLauncher.launch(permissions)
        }
    }
    
    private fun scanWifiNetworks() {
        Logger.info("Scanning for Wi-Fi networks")
        
        // Add explicit permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != 
            PackageManager.PERMISSION_GRANTED) {
            Logger.warning("Location permission not granted")
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, R.string.scanning, Toast.LENGTH_SHORT).show()
        try {
            wifiManager.startScan()
        } catch (e: SecurityException) {
            Logger.error("Security exception when scanning networks: ${e.message}")
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
        }
        updateLogDisplay()
    }
    
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun scanSuccess() {
        // Verify permission is actually granted at runtime
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != 
            PackageManager.PERMISSION_GRANTED) {
            Logger.warning("Location permission not granted when processing scan results")
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val results = wifiManager.scanResults
            Logger.info("Found ${results.size} Wi-Fi networks")
            
            // Sort results by signal strength
            val sortedResults = results.sortedByDescending { it.level }
            
            // Update the RecyclerView
            networkAdapter.updateNetworks(sortedResults)
            updateLogDisplay()
        } catch (e: SecurityException) {
            Logger.error("Security exception when accessing scan results: ${e.message}")
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startSignalMonitoring() {
        stopSignalMonitoring() // Stop any existing monitoring
        
        Logger.info("Starting signal monitoring for $selectedNetworkSSID")
        scanTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        checkWifiSignalStrength()
                    }
                }
            }, 0, 1000) // Check every second
        }
    }
    
    private fun stopSignalMonitoring() {
        scanTimer?.cancel()
        scanTimer = null
        if (selectedNetworkSSID != null) {
            Logger.info("Stopped signal monitoring for $selectedNetworkSSID")
        }
    }
    
    private fun checkWifiSignalStrength() {
        if (selectedNetworkBSSID == null) return
        
        // Add explicit permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != 
            PackageManager.PERMISSION_GRANTED) {
            Logger.warning("Location permission not granted")
            stopRecording()
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            wifiManager.startScan() // Request fresh scan
            
            val currentNetwork = wifiManager.scanResults
                .find { it.BSSID == selectedNetworkBSSID }
            
            if (currentNetwork != null && isRecording && currentLocationReadings != null) {
                val rssi = currentNetwork.level
                
                val reading = WifiReading(
                    ssid = currentNetwork.SSID,
                    bssid = currentNetwork.BSSID,
                    rssi = rssi,
                    locationName = binding.etLocationName.text.toString()
                )
                
                currentLocationReadings?.addReading(reading)
                
                // Update UI
                val readingsCount = currentLocationReadings?.readings?.size ?: 0
                val readingsText = "$readingsCount/${currentLocationReadings?.maxReadings ?: 100} readings collected"
                binding.tvSelectedNetwork.text = readingsText
                
                // Log every 10th reading or the last one
                if (readingsCount % 10 == 0 || readingsCount == currentLocationReadings?.maxReadings) {
                    Logger.debug("Collected $readingsCount readings, RSSI: $rssi dBm")
                    updateLogDisplay()
                }
                
                // Check if we're done
                if (currentLocationReadings?.isComplete() == true) {
                    Logger.info("Readings complete for ${binding.etLocationName.text}")
                    stopRecording()
                    Toast.makeText(
                        this,
                        getString(R.string.readings_complete, binding.etLocationName.text),
                        Toast.LENGTH_LONG
                    ).show()
                    updateLogDisplay()
                }
            }
        } catch (e: SecurityException) {
            Logger.error("Security exception when checking signal strength: ${e.message}")
            stopRecording()
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startRecording() {
        // Verify we have a network selected
        if (selectedNetworkSSID.isNullOrEmpty()) {
            Logger.warning("Attempted to start recording without selecting a network")
            Toast.makeText(this, R.string.network_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check that location is entered or selected
        val locationName = if (binding.spinnerLocations.selectedItemPosition == CUSTOM_LOCATION_INDEX) {
            binding.etLocationName.text.toString()
        } else {
            selectedLocation
        }
        
        if (locationName.isEmpty()) {
            Logger.warning("Attempted to start recording without entering a location")
            Toast.makeText(this, R.string.location_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get or create location readings
        currentLocationReadings = dataManager.getLocationReadings(locationName)
            ?: LocationReadings(locationName)
        
        Logger.info("Starting recording at location: $locationName")
        
        // Reset progress if readings aren't already complete
        if (!currentLocationReadings!!.isComplete()) {
            val readingsCount = currentLocationReadings!!.readings.size
            val readingsText = "$readingsCount/${currentLocationReadings?.maxReadings ?: 100} readings collected"
            binding.tvSelectedNetwork.text = readingsText
            
            // Start recording
            isRecording = true
            binding.btnStartReading.text = getString(R.string.stop_reading)
            binding.spinnerLocations.isEnabled = false
            binding.etLocationName.isEnabled = false
            
            // Start monitoring the selected network
            startSignalMonitoring()
        } else {
            Logger.info("Location $locationName already has complete readings")
            Toast.makeText(
                this,
                getString(R.string.readings_complete, locationName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun stopRecording() {
        isRecording = false
        binding.btnStartReading.text = getString(R.string.start_reading)
        binding.spinnerLocations.isEnabled = true
        binding.etLocationName.isEnabled = true
        
        stopSignalMonitoring()
        
        // Save the current readings
        currentLocationReadings?.let { readings ->
            Logger.info("Saving ${readings.readings.size} readings for location: ${readings.locationName}")
            dataManager.saveLocationReadings(readings)
            
            // If readings are complete, show the dialog with full results
            if (readings.isComplete()) {
                // Log the final readings in a formatted way
                val readingMaps = readings.readings.map { 
                    mapOf("rssi" to it.rssi, "timestamp" to it.timestamp) 
                }
                Logger.logFinalReadings(readings.locationName, readingMaps)
                
                // Show dialog with the results
                val dialog = ReadingsResultDialog.newInstance(readings)
                dialog.show(supportFragmentManager, "ReadingsResultDialog")
            }
        }
        
        updateLogDisplay() // Make sure to update the log display
    }
}