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
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import org.lineageos.settings.R;

public class ChargeQSTile extends TileService {

    private static final String TAG = "ChargeQSTile";
    private ChargeUtils mChargeUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        mChargeUtils = new ChargeUtils(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        
        if (!mChargeUtils.canToggleManually()) {
            // Show inactive state if not in manual mode or master toggle is off
            updateTile();
            return;
        }

        // Toggle manual bypass state
        boolean currentState = mChargeUtils.getManualBypassState();
        mChargeUtils.setManualBypassState(!currentState);
        
        // Update tile immediately
        updateTile();
        
        Log.d(TAG, "Manual bypass toggled to: " + !currentState);
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        boolean masterEnabled = mChargeUtils.isMasterToggleEnabled();
        boolean isManualMode = mChargeUtils.getBypassMode() == ChargeUtils.MODE_MANUAL;
        boolean canToggle = masterEnabled && isManualMode;
        boolean bypassActive = mChargeUtils.isBypassChargeEnabled();

        // Set tile icon
        Icon icon = Icon.createWithResource(this, R.drawable.ic_charge_bypass);
        tile.setIcon(icon);

        // Set tile label
        tile.setLabel(getString(R.string.charge_bypass_qs_tile_label));

        if (!masterEnabled) {
            // Master toggle is off
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setSubtitle(getString(R.string.charge_bypass_qs_disabled));
        } else if (!isManualMode) {
            // In smart mode
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setSubtitle("Smart Mode");
        } else if (canToggle) {
            // Manual mode and can toggle
            if (bypassActive) {
                tile.setState(Tile.STATE_ACTIVE);
                tile.setSubtitle(getString(R.string.charge_bypass_qs_active));
            } else {
                tile.setState(Tile.STATE_INACTIVE);
                tile.setSubtitle(getString(R.string.charge_bypass_qs_inactive));
            }
        } else {
            // Fallback state
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setSubtitle(getString(R.string.charge_bypass_qs_disabled));
        }

        tile.updateTile();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        updateTile();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
