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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import org.lineageos.settings.R;

public class ChargeBatteryMonitorService extends Service {

    private static final String TAG = "ChargeBatteryMonitorService";
    private static final String NOTIFICATION_CHANNEL_ID = "charge_bypass_channel";
    private static final int NOTIFICATION_ID = 1001;

    private ChargeUtils mChargeUtils;
    private BatteryReceiver mBatteryReceiver;
    private NotificationManager mNotificationManager;
    private boolean mIsMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mChargeUtils = new ChargeUtils(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mChargeUtils.isMasterToggleEnabled()) {
            Log.d(TAG, "Master toggle disabled, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!mIsMonitoring) {
            startMonitoring();
        }

        return START_STICKY; // Restart service if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't provide binding
    }

    private void startMonitoring() {
        if (mIsMonitoring) return;

        mBatteryReceiver = new BatteryReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        
        registerReceiver(mBatteryReceiver, filter);
        mIsMonitoring = true;
        
        // Initial check
        mChargeUtils.updateBypassState();
        updateNotification();
        
        Log.d(TAG, "Started battery monitoring");
    }

    private void stopMonitoring() {
        if (!mIsMonitoring) return;

        if (mBatteryReceiver != null) {
            unregisterReceiver(mBatteryReceiver);
            mBatteryReceiver = null;
        }
        
        mIsMonitoring = false;
        stopForeground(true);
        
        Log.d(TAG, "Stopped battery monitoring");
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Charging Bypass",
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Charging bypass monitoring service");
        channel.setShowBadge(false);
        mNotificationManager.createNotificationChannel(channel);
    }

    private void updateNotification() {
        if (!mIsMonitoring) return;

        boolean bypassActive = mChargeUtils.isBypassChargeEnabled();
        int batteryLevel = mChargeUtils.getCurrentBatteryLevel();
        int mode = mChargeUtils.getBypassMode();
        
        // If bypass is not active, hide notification completely regardless of mode
        if (!bypassActive) {
            stopForeground(true);
            mNotificationManager.cancel(NOTIFICATION_ID);
            return;
        }
        
        String title = getString(R.string.charge_bypass_notification_title);
        String text;
        
        if (mode == ChargeUtils.MODE_SMART) {
            int target = mChargeUtils.getBatteryTarget();
            if (bypassActive) {
                text = getString(R.string.charge_bypass_smart_enabled, target);
            } else {
                text = "Smart mode active - Target: " + target + "% (Current: " + batteryLevel + "%)";
            }
        } else {
            text = getString(R.string.charge_bypass_notification_text);
        }

        // Create a simple settings intent instead of ChargeActivity
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, settingsIntent, PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage) // Use system icon as fallback
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .build();

        if (bypassActive) {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (Intent.ACTION_BATTERY_CHANGED.equals(action) ||
                Intent.ACTION_POWER_CONNECTED.equals(action) ||
                Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                
                Log.d(TAG, "Battery state changed: " + action);
                
                // Check if we should still be running
                if (!mChargeUtils.isMasterToggleEnabled()) {
                    Log.d(TAG, "Master toggle disabled, stopping service");
                    stopSelf();
                    return;
                }
                
                // Update bypass state based on current conditions
                boolean wasActive = mChargeUtils.isBypassChargeEnabled();
                mChargeUtils.updateBypassState();
                boolean isActive = mChargeUtils.isBypassChargeEnabled();
                
                // Update notification
                updateNotification();
                
                // Log state changes
                if (wasActive != isActive) {
                    int batteryLevel = mChargeUtils.getCurrentBatteryLevel();
                    Log.d(TAG, "Bypass state changed: " + isActive + 
                          " (Battery: " + batteryLevel + "%)");
                }
            }
        }
    }
}
