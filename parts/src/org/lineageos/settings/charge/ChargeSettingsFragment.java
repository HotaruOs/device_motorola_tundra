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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import org.lineageos.settings.R;
import androidx.preference.PreferenceFragmentCompat;

public class ChargeSettingsFragment extends PreferenceFragmentCompat
    implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "ChargeSettingsFragment";

    // Preference keys
    private static final String KEY_BYPASS_CHARGE_MASTER = "bypass_charge_master";
    private static final String KEY_BYPASS_MODE_MANUAL = "bypass_mode_manual";
    private static final String KEY_BYPASS_MODE_SMART = "bypass_mode_smart";
    private static final String KEY_BATTERY_TARGET_SLIDER = "battery_target_slider";
    private static final String KEY_BYPASS_CONFIGURATION_CATEGORY = "bypass_configuration_category";

    // Preferences
    private SwitchPreference mMasterToggle;
    private CheckBoxPreference mManualMode;
    private CheckBoxPreference mSmartMode;
    private MinMaxSeekBarPreference mBatteryTarget;
    private PreferenceCategory mConfigurationCategory;
    
    private ChargeUtils mChargeUtils;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.charge_settings, rootKey);
        
        mChargeUtils = new ChargeUtils(getActivity());
        
        // Initialize preferences
        mMasterToggle = (SwitchPreference) findPreference(KEY_BYPASS_CHARGE_MASTER);
        mManualMode = (CheckBoxPreference) findPreference(KEY_BYPASS_MODE_MANUAL);
        mSmartMode = (CheckBoxPreference) findPreference(KEY_BYPASS_MODE_SMART);
        mBatteryTarget = (MinMaxSeekBarPreference) findPreference(KEY_BATTERY_TARGET_SLIDER);
        mConfigurationCategory = (PreferenceCategory) findPreference(KEY_BYPASS_CONFIGURATION_CATEGORY);

        boolean bypassSupported = mChargeUtils.isBypassChargeSupported();
        
        if (!bypassSupported) {
            // Disable all preferences if bypass is not supported
            mMasterToggle.setEnabled(false);
            mMasterToggle.setSummary(R.string.charge_bypass_unavailable);
            mConfigurationCategory.setEnabled(false);
            return;
        }

        // Set up master toggle
        mMasterToggle.setChecked(mChargeUtils.isMasterToggleEnabled());
        mMasterToggle.setOnPreferenceChangeListener(this);

        // Set up mode preferences
        int currentMode = mChargeUtils.getBypassMode();
        mManualMode.setChecked(currentMode == ChargeUtils.MODE_MANUAL);
        mSmartMode.setChecked(currentMode == ChargeUtils.MODE_SMART);
        mManualMode.setOnPreferenceChangeListener(this);
        mSmartMode.setOnPreferenceChangeListener(this);

        // Set up battery target slider - Let the preference handle all mapping internally
        int batteryTarget = mChargeUtils.getBatteryTarget();
        mBatteryTarget.setBatteryTarget(batteryTarget); // Use the new method from MinMaxSeekBarPreference
        mBatteryTarget.setOnPreferenceChangeListener(this);
        updateBatteryTargetSummary(batteryTarget);
        
        Log.d(TAG, "Initial battery target: " + batteryTarget + "%");

        // Update UI state based on master toggle
        updateConfigurationVisibility(mChargeUtils.isMasterToggleEnabled());
        updateBatteryTargetVisibility(currentMode == ChargeUtils.MODE_SMART);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        
        switch (key) {
            case KEY_BYPASS_CHARGE_MASTER:
                boolean masterEnabled = (Boolean) newValue;
                if (masterEnabled) {
                    showMasterToggleWarning(() -> {
                        mChargeUtils.setMasterToggleEnabled(true);
                        updateConfigurationVisibility(true);
                        startBatteryMonitorService();
                    });
                    return false; // Don't update UI until user confirms
                } else {
                    mChargeUtils.setMasterToggleEnabled(false);
                    mChargeUtils.enableBypassCharge(false); // Disable bypass immediately
                    updateConfigurationVisibility(false);
                    stopBatteryMonitorService();
                    return true;
                }
                
            case KEY_BYPASS_MODE_MANUAL:
                if ((Boolean) newValue) {
                    mSmartMode.setChecked(false);
                    mChargeUtils.setBypassMode(ChargeUtils.MODE_MANUAL);
                    updateBatteryTargetVisibility(false);
                    stopBatteryMonitorService();
                    return true;
                }
                return false; // Don't allow unchecking without selecting another mode
                
            case KEY_BYPASS_MODE_SMART:
                if ((Boolean) newValue) {
                    mManualMode.setChecked(false);
                    mChargeUtils.setBypassMode(ChargeUtils.MODE_SMART);
                    updateBatteryTargetVisibility(true);
                    startBatteryMonitorService();
                    return true;
                }
                return false; // Don't allow unchecking without selecting another mode
                
            case KEY_BATTERY_TARGET_SLIDER:
                // FIXED: Calculate the battery target based on the new slider value
                int sliderValue = (Integer) newValue;
                
                // Create a temporary preference object to calculate the mapped value
                // since the preference hasn't been updated yet
                int actualBatteryTarget = mBatteryTarget.calculateBatteryTargetFromSlider(sliderValue);
                
                Log.d(TAG, "Slider changed - Position: " + sliderValue + "%, Battery target: " + actualBatteryTarget + "%");
                
                mChargeUtils.setBatteryTarget(actualBatteryTarget);
                updateBatteryTargetSummary(actualBatteryTarget);
                return true;
        }
        
        return false;
    }

    private void showMasterToggleWarning(Runnable onConfirm) {
        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.charge_bypass_master_title)
            .setMessage(R.string.charge_bypass_warning)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                mMasterToggle.setChecked(true);
                if (onConfirm != null) {
                    onConfirm.run();
                }
            })
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                mMasterToggle.setChecked(false);
            })
            .show();
    }

    private void updateConfigurationVisibility(boolean visible) {
        mConfigurationCategory.setVisible(visible);
        if (visible) {
            int currentMode = mChargeUtils.getBypassMode();
            updateBatteryTargetVisibility(currentMode == ChargeUtils.MODE_SMART);
        } else {
            updateBatteryTargetVisibility(false);
        }
    }

    private void updateBatteryTargetVisibility(boolean visible) {
        mBatteryTarget.setVisible(visible);
    }

    private void updateBatteryTargetSummary(int value) {
        String summaryText = getString(R.string.charge_bypass_battery_target_summary, value);
        mBatteryTarget.setSummary(summaryText);
    }

    private void startBatteryMonitorService() {
        Intent serviceIntent = new Intent(getActivity(), ChargeBatteryMonitorService.class);
        getActivity().startService(serviceIntent);
    }

    private void stopBatteryMonitorService() {
        Intent serviceIntent = new Intent(getActivity(), ChargeBatteryMonitorService.class);
        getActivity().stopService(serviceIntent);
    }
}
