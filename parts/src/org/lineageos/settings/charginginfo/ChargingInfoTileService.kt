package org.lineageos.settings.charginginfo

import org.lineageos.settings.R

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import java.io.File
import java.util.Timer
import java.util.TimerTask

class ChargingInfoTileService : TileService() {
    private var updateTimer: Timer? = null
    private var batteryReceiver: BroadcastReceiver? = null
    private var isCharging = false
    
    companion object {
        private const val TAG = "ChargingTileService"
        private const val UPDATE_INTERVAL = 2000L // Update every 2 seconds
        
        // Battery sysfs paths
        private const val CURRENT_NOW_PATH = "/sys/class/power_supply/battery/current_now"
        private const val VOLTAGE_NOW_PATH = "/sys/class/power_supply/battery/voltage_now"
        private const val STATUS_PATH = "/sys/class/power_supply/battery/status"
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "Tile started listening")
        
        // Register battery receiver
        registerBatteryReceiver()
        
        // Start periodic updates
        startPeriodicUpdates()
        
        // Initial update
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "Tile stopped listening")
        
        // Stop updates
        stopPeriodicUpdates()
        
        // Unregister receiver
        unregisterBatteryReceiver()
    }

    override fun onClick() {
        super.onClick()
        // Force update when clicked
        updateTile()
    }

    private fun registerBatteryReceiver() {
        if (batteryReceiver == null) {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_BATTERY_CHANGED,
                        Intent.ACTION_POWER_CONNECTED,
                        Intent.ACTION_POWER_DISCONNECTED -> {
                            updateChargingStatus(intent)
                            updateTile()
                        }
                    }
                }
            }
            
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            
            registerReceiver(batteryReceiver, filter)
        }
    }

    private fun unregisterBatteryReceiver() {
        batteryReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Receiver not registered")
            }
            batteryReceiver = null
        }
    }

    private fun startPeriodicUpdates() {
        stopPeriodicUpdates()
        updateTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (isCharging) {
                        updateTile()
                    }
                }
            }, 0, UPDATE_INTERVAL)
        }
    }

    private fun stopPeriodicUpdates() {
        updateTimer?.cancel()
        updateTimer = null
    }

    private fun updateChargingStatus(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        
        try {
            val chargingInfo = getChargingInfo()
            
            tile.apply {
                when {
                    chargingInfo.isCharging -> {
                        state = Tile.STATE_ACTIVE
                        label = "Charging"
                        subtitle = "${chargingInfo.power}W"
                        contentDescription = "Charging at ${chargingInfo.power}W (${chargingInfo.current}A, ${chargingInfo.voltage}V)"
                        icon = Icon.createWithResource(this@ChargingInfoTileService, 
                            R.drawable.ic_charging_speed)
                    }
                    chargingInfo.isDischarging -> {
                        state = Tile.STATE_INACTIVE
                        label = "Discharging"
                        subtitle = "${chargingInfo.power}W"
                        contentDescription = "Discharging at ${chargingInfo.power}W"
                        icon = Icon.createWithResource(this@ChargingInfoTileService, 
                            R.drawable.ic_battery_discharge)
                    }
                    else -> {
                        state = Tile.STATE_UNAVAILABLE
                        label = "Not Charging"
                        subtitle = "0W"
                        contentDescription = "Battery not charging or discharging"
                        icon = Icon.createWithResource(this@ChargingInfoTileService, 
                            R.drawable.ic_battery_unknown)
                    }
                }
            }
            
            tile.updateTile()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating tile", e)
            
            // Fallback tile state
            tile.apply {
                state = Tile.STATE_UNAVAILABLE
                label = "Error"
                subtitle = "N/A"
                contentDescription = "Unable to read charging information"
                icon = Icon.createWithResource(this@ChargingInfoTileService, 
                    R.drawable.ic_error)
            }
            tile.updateTile()
        }
    }

    private fun getChargingInfo(): ChargingInfo {
        val currentNow = readSysfsValue(CURRENT_NOW_PATH)
        val voltageNow = readSysfsValue(VOLTAGE_NOW_PATH)
        val status = readSysfsString(STATUS_PATH)
        
        // Convert from milliamps to amps and microvolts to volts
        val currentA = currentNow / 1000.0        // mA → A
        val voltageV = voltageNow / 1000000.0     // µV → V
        
        // Calculate power in watts
        val powerW = Math.abs(currentA * voltageV)
        
        val isCharging = status.equals("Charging", ignoreCase = true) && currentNow > 0
        val isDischarging = currentNow < 0
        
        return ChargingInfo(
            current = String.format("%.1f", Math.abs(currentA)),
            voltage = String.format("%.1f", voltageV),
            power = String.format("%.1f", powerW),
            isCharging = isCharging,
            isDischarging = isDischarging
        )
    }

    private fun readSysfsValue(path: String): Long {
        return try {
            File(path).readText().trim().toLong()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read $path", e)
            0L
        }
    }

    private fun readSysfsString(path: String): String {
        return try {
            File(path).readText().trim()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read $path", e)
            "Unknown"
        }
    }

    data class ChargingInfo(
        val current: String,
        val voltage: String,
        val power: String,
        val isCharging: Boolean,
        val isDischarging: Boolean
    )
}
