package com.wifi.wifi_ap.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wifi.wifi_ap.model.LocationReadings
import java.lang.reflect.Type

/**
 * Manages storage and retrieval of Wi-Fi readings data
 */
class WifiDataManager(private val context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    private val locationsKey = "wifi_locations"
    private val selectedNetworkKey = "selected_network"
    
    /**
     * Save a location readings object to storage
     */
    fun saveLocationReadings(locationReadings: LocationReadings) {
        val locations = getAllLocations().toMutableList()
        
        // Replace existing location or add new one
        val index = locations.indexOfFirst { it.locationName == locationReadings.locationName }
        if (index != -1) {
            locations[index] = locationReadings
        } else {
            locations.add(locationReadings)
        }
        
        saveAllLocations(locations)
    }
    
    /**
     * Get all location readings from storage
     */
    fun getAllLocations(): List<LocationReadings> {
        val locationsJson = sharedPreferences.getString(locationsKey, null) ?: return emptyList()
        val type: Type = object : TypeToken<List<LocationReadings>>() {}.type
        return gson.fromJson(locationsJson, type) ?: emptyList()
    }
    
    /**
     * Save all locations to storage
     */
    private fun saveAllLocations(locations: List<LocationReadings>) {
        val locationsJson = gson.toJson(locations)
        sharedPreferences.edit().putString(locationsKey, locationsJson).apply()
    }
    
    /**
     * Get a specific location's readings
     */
    fun getLocationReadings(locationName: String): LocationReadings? {
        return getAllLocations().find { it.locationName == locationName }
    }
    
    /**
     * Save selected network SSID and BSSID
     */
    fun saveSelectedNetwork(ssid: String, bssid: String) {
        sharedPreferences.edit()
            .putString("$selectedNetworkKey-ssid", ssid)
            .putString("$selectedNetworkKey-bssid", bssid)
            .apply()
    }
    
    /**
     * Get selected network as Pair of SSID and BSSID
     */
    fun getSelectedNetwork(): Pair<String, String>? {
        val ssid = sharedPreferences.getString("$selectedNetworkKey-ssid", null)
        val bssid = sharedPreferences.getString("$selectedNetworkKey-bssid", null)
        
        return if (ssid != null && bssid != null) {
            Pair(ssid, bssid)
        } else {
            null
        }
    }
    
    /**
     * Clear all stored data
     */
    fun clearAllData() {
        sharedPreferences.edit()
            .remove(locationsKey)
            .remove("$selectedNetworkKey-ssid")
            .remove("$selectedNetworkKey-bssid")
            .apply()
    }
}
