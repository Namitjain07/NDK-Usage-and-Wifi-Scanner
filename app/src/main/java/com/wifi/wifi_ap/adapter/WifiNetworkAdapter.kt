package com.wifi.wifi_ap.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wifi.wifi_ap.R
import com.wifi.wifi_ap.databinding.ItemWifiNetworkBinding
import com.wifi.wifi_ap.util.Logger

class WifiNetworkAdapter(
    private var networks: List<ScanResult> = emptyList(),
    private val onNetworkSelected: (ScanResult) -> Unit
) : RecyclerView.Adapter<WifiNetworkAdapter.WifiNetworkViewHolder>() {

    var selectedBssid: String? = null
        set(value) {
            field = value
            notifyDataSetChanged() // Refresh all items to update selection
        }

    class WifiNetworkViewHolder(val binding: ItemWifiNetworkBinding) : RecyclerView.ViewHolder(binding.root) {
        val networkName: TextView = binding.tvNetworkName
        val networkDetails: TextView = binding.tvNetworkDetails
        val signalStrength: TextView = binding.tvSignalStrength
        val signalIcon: ImageView = binding.ivSignalStrength
        val selectedIndicator: View = binding.selectedIndicator
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiNetworkViewHolder {
        val binding = ItemWifiNetworkBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return WifiNetworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WifiNetworkViewHolder, position: Int) {
        val network = networks[position]
        
        // Set network name (SSID)
        val ssid = network.SSID.ifEmpty { "<Hidden Network>" }
        holder.networkName.text = ssid
        
        // Set BSSID (MAC address)
        holder.networkDetails.text = network.BSSID
        
        // Set signal strength
        holder.signalStrength.text = "${network.level} dBm"
        
        // Set appropriate icon and background based on signal strength
        val signalStrength = network.level
        val context = holder.itemView.context
        
        when {
            signalStrength > -50 -> {
                holder.signalIcon.setImageResource(R.drawable.ic_signal_high)
                holder.signalIcon.background = context.getDrawable(R.drawable.signal_high_background)
            }
            signalStrength > -70 -> {
                holder.signalIcon.setImageResource(R.drawable.ic_signal_medium)
                holder.signalIcon.background = context.getDrawable(R.drawable.signal_medium_background)
            }
            else -> {
                holder.signalIcon.setImageResource(R.drawable.ic_signal_low)
                holder.signalIcon.background = context.getDrawable(R.drawable.signal_low_background)
            }
        }
        
        // Handle selection state
        if (network.BSSID == selectedBssid) {
            holder.selectedIndicator.visibility = View.VISIBLE
            holder.binding.cardNetworks.strokeWidth = 2
            holder.binding.cardNetworks.strokeColor = context.getColor(R.color.colorPrimary)
        } else {
            holder.selectedIndicator.visibility = View.INVISIBLE
            holder.binding.cardNetworks.strokeWidth = 0
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onNetworkSelected(network)
            // Add subtle animation feedback
            it.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
        
        // Copy MAC address on long click
        holder.itemView.setOnLongClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WiFi MAC Address", network.BSSID)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "MAC address copied to clipboard", Toast.LENGTH_SHORT).show()
            Logger.info("Copied MAC address to clipboard: ${network.BSSID}")
            true
        }
    }

    override fun getItemCount(): Int = networks.size
    
    fun updateNetworks(newNetworks: List<ScanResult>) {
        networks = newNetworks
        notifyDataSetChanged()
    }
}
