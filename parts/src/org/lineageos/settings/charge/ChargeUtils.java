/*
 * Copyright (C) 2025 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.charge;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

public class ChargeUtils {

    private static final String TAG = "ChargeUtils";
    
    // Sysfs node for bypass control
    public static final String BYPASS_CHARGE_NODE = "/sys/class/power_supply/battery/batt_slate_mode";
    
    // Bypass modes
    public static final int MODE_MANUAL = 0;
    public static final int MODE_SMART = 1;
    
    // Bypass states
    public static final int BYPASS_DISABLED = 0;
    public static final int BYPASS_ENABLED = 1;
    
    // SharedPreferences keys
    private static final String PREF_MASTER_TOGGLE = "bypass_charge_master";
    private static final String PREF_BYPASS_MODE = "bypass_mode";
    private static final String PREF_BATTERY_TARGET = "battery_target";
    private static final String PREF_MANUAL_BYPASS_STATE = "manual_bypass_state";
    
    // Default values
    private static final int DEFAULT_BATTERY_TARGET = 80;
    private static final int BATTERY_HYSTERESIS = 5; // 5% hysteresis for smart mode

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    public ChargeUtils(Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Master toggle methods
    public boolean isMasterToggleEnabled() {
        return mSharedPrefs.getBoolean(PREF_MASTER_TOGGLE, false);
    }

    public void setMasterToggleEnabled(boolean enabled) {
        mSharedPrefs.edit().putBoolean(PREF_MASTER_TOGGLE, enabled).apply();
        if (!enabled) {
            // Disable bypass immediately when master toggle is turned off
            enableBypassCharge(false);
        }
    }

    // Mode management methods
    public int getBypassMode() {
        return mSharedPrefs.getInt(PREF_BYPASS_MODE, MODE_MANUAL);
    }

    public void setBypassMode(int mode) {
        mSharedPrefs.edit().putInt(PREF_BYPASS_MODE, mode).apply();
        
        // Reset bypass state when changing modes
        if (mode == MODE_MANUAL) {
            setManualBypassState(false);
        }
        
        // Update bypass state based on new mode
        updateBypassState();
    }

    // Battery target methods (for smart mode)
    public int getBatteryTarget() {
        return mSharedPrefs.getInt(PREF_BATTERY_TARGET, DEFAULT_BATTERY_TARGET);
    }

    public void setBatteryTarget(int target) {
        mSharedPrefs.edit().putInt(PREF_BATTERY_TARGET, target).apply();
        // Immediately update bypass state if we're in smart mode
        if (getBypassMode() == MODE_SMART) {
            updateBypassState();
        }
    }

    // Manual bypass state methods
    public boolean getManualBypassState() {
        return mSharedPrefs.getBoolean(PREF_MANUAL_BYPASS_STATE, false);
    }

    public void setManualBypassState(boolean enabled) {
        mSharedPrefs.edit().putBoolean(PREF_MANUAL_BYPASS_STATE, enabled).apply();
        if (getBypassMode() == MODE_MANUAL) {
            updateBypassState();
        }
    }

    // Core bypass control methods
    public boolean isBypassChargeEnabled() {
        try {
            String value = FileUtils.readOneLine(BYPASS_CHARGE_NODE);
            return value != null && value.equals("1");
        } catch (Exception e) {
            Log.e(TAG, "Failed to read bypass charge status", e);
            return false;
        }
    }

    public void enableBypassCharge(boolean enable) {
        try {
            FileUtils.writeLine(BYPASS_CHARGE_NODE, enable ? "1" : "0");
            Log.d(TAG, "Bypass charge " + (enable ? "enabled" : "disabled"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to write bypass charge status", e);
        }
    }

    // Support check methods
    public boolean isNodeAccessible(String node) {
        try {
            FileUtils.readOneLine(node);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Node " + node + " not accessible", e);
            return false;
        }
    }
    
    public boolean isBypassChargeSupported() {
        return isNodeAccessible(BYPASS_CHARGE_NODE);
    }

    // Main logic for updating bypass state
    public void updateBypassState() {
        if (!isMasterToggleEnabled()) {
            enableBypassCharge(false);
            return;
        }

        int mode = getBypassMode();
        boolean shouldBypass = false;

        switch (mode) {
            case MODE_MANUAL:
                shouldBypass = getManualBypassState();
                break;
                
            case MODE_SMART:
                shouldBypass = shouldBypassInSmartMode();
                break;
        }

        enableBypassCharge(shouldBypass);
    }

    // Smart mode logic
    private boolean shouldBypassInSmartMode() {
        int currentBattery = getCurrentBatteryLevel();
        int target = getBatteryTarget();
        boolean currentlyBypassed = isBypassChargeEnabled();
        
        if (currentBattery >= target && !currentlyBypassed) {
            // Enable bypass when we reach target
            return true;
        } else if (currentBattery <= (target - BATTERY_HYSTERESIS) && currentlyBypassed) {
            // Disable bypass when we drop below target minus hysteresis
            return false;
        }
        
        // Keep current state if we're between thresholds
        return currentlyBypassed;
    }

    // Battery level helper
    public int getCurrentBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);
        
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            
            if (level != -1 && scale != -1) {
                return (int) ((level / (float) scale) * 100);
            }
        }
        
        return -1; // Unknown battery level
    }

    // Charging state helper
    public boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);
        
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                   status == BatteryManager.BATTERY_STATUS_FULL;
        }
        
        return false;
    }

    // Utility methods for external components
    public String getBypassModeString() {
        switch (getBypassMode()) {
            case MODE_MANUAL:
                return "Manual";
            case MODE_SMART:
                return "Smart";
            default:
                return "Unknown";
        }
    }

    public boolean canToggleManually() {
        return isMasterToggleEnabled() && getBypassMode() == MODE_MANUAL;
    }
}
