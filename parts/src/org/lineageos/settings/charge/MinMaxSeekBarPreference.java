package org.lineageos.settings.charge;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import androidx.preference.SeekBarPreference;

public class MinMaxSeekBarPreference extends SeekBarPreference {
    private static final String TAG = "MinMaxSeekBarPreference";
    
    private int mMinValue = 0;
    private int mMaxValue = 100;

    public MinMaxSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        // Get min/max values from XML attributes
        if (attrs != null) {
            for (int i = 0; i < attrs.getAttributeCount(); i++) {
                String attrName = attrs.getAttributeName(i);
                if ("min".equals(attrName)) {
                    mMinValue = attrs.getAttributeIntValue(i, 0);
                } else if ("max".equals(attrName)) {
                    mMaxValue = attrs.getAttributeIntValue(i, 100);
                }
            }
        }
        
        // Set the SeekBar to use full 0-100 range for UI, but map values internally
        setMin(0);
        setMax(100);
        
        Log.d(TAG, "MinMaxSeekBarPreference initialized - Min: " + mMinValue + ", Max: " + mMaxValue);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        int value = defaultValue == null ? 80 : (Integer) defaultValue;
        // Ensure value is within bounds
        value = Math.max(mMinValue, Math.min(mMaxValue, value));
        
        // Convert the actual battery target value to slider position
        int sliderPosition = valueToSliderPosition(value);
        super.setValue(sliderPosition);
        
        Log.i(TAG, "Battery target initial value set: " + value + "% (Slider position: " + sliderPosition + "%)");
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        int defaultValue = a.getInt(index, 80);
        Log.d(TAG, "Getting default battery target value: " + defaultValue + "%");
        return defaultValue;
    }
    
    @Override
    public void setValue(int seekBarValue) {
        // seekBarValue here is the slider position (0-100)
        // Just store it as-is since the slider represents the UI position
        super.setValue(seekBarValue);
        
        // Calculate and log the actual mapped battery target
        int mappedValue = sliderPositionToValue(seekBarValue);
        Log.i(TAG, "Battery target changed - Slider: " + seekBarValue + "%, Actual target: " + mappedValue + "% (Range: " + mMinValue + "-" + mMaxValue + "%)");
    }
    
    // Convert slider position (0-100) to actual battery target value
    private int sliderPositionToValue(int sliderPosition) {
        return mMinValue + ((sliderPosition * (mMaxValue - mMinValue)) / 100);
    }
    
    // Convert actual battery target value to slider position (0-100)
    private int valueToSliderPosition(int value) {
        if (mMaxValue == mMinValue) return 0; // Avoid division by zero
        return ((value - mMinValue) * 100) / (mMaxValue - mMinValue);
    }
    
    // Get the actual mapped battery target value
    public int getBatteryTarget() {
        int sliderValue = getValue();
        return sliderPositionToValue(sliderValue);
    }
    
    // Set the battery target value (converts to appropriate slider position)
    public void setBatteryTarget(int targetValue) {
        targetValue = Math.max(mMinValue, Math.min(mMaxValue, targetValue));
        int sliderPosition = valueToSliderPosition(targetValue);
        setValue(sliderPosition);
    }
    
    // Calculate battery target from a given slider position (used during preference change)
    public int calculateBatteryTargetFromSlider(int sliderPosition) {
        return sliderPositionToValue(sliderPosition);
    }
}
