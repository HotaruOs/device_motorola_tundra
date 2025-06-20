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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ChargeReceiver extends BroadcastReceiver {

    private static final String TAG = "ChargeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {
            
            Log.d(TAG, "Boot completed, restoring charge bypass settings");
            
            ChargeUtils chargeUtils = new ChargeUtils(context);
            
            // Check if bypass is supported on this device
            if (!chargeUtils.isBypassChargeSupported()) {
                Log.d(TAG, "Bypass not supported on this device");
                return;
            }
            
            // Restore bypass state based on saved preferences
            if (chargeUtils.isMasterToggleEnabled()) {
                Log.d(TAG, "Master toggle enabled, restoring bypass state");
                
                // Reset bypass to disabled state first
                chargeUtils.enableBypassCharge(false);
                
                // Start monitoring service if needed
                int mode = chargeUtils.getBypassMode();
                if (mode == ChargeUtils.MODE_SMART) {
                    Intent serviceIntent = new Intent(context, ChargeBatteryMonitorService.class);
                    context.startService(serviceIntent);
                    Log.d(TAG, "Started battery monitoring service for smart mode");
                } else if (mode == ChargeUtils.MODE_MANUAL) {
                    // In manual mode, respect the saved manual state
                    // but don't automatically enable bypass on boot for safety
                    chargeUtils.setManualBypassState(false);
                    Log.d(TAG, "Manual mode restored, bypass disabled for safety");
                }
                
                // Update bypass state based on current conditions
                chargeUtils.updateBypassState();
                
            } else {
                Log.d(TAG, "Master toggle disabled, ensuring bypass is off");
                chargeUtils.enableBypassCharge(false);
            }
        }
    }
}
