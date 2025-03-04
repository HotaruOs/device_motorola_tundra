#
# Copyright (C) 2024 The LineageOs Project 
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from tundra device
$(call inherit-product, device/motorola/tundra/device.mk)

# Inherit common infinity configurations
$(call inherit-product, vendor/derp/config/common_full_phone.mk)

# Quick Tap
TARGET_SUPPORTS_QUICK_TAP := true

PRODUCT_NAME := derp_tundra
PRODUCT_DEVICE := tundra
PRODUCT_MANUFACTURER := motorola
PRODUCT_BRAND := motorola
PRODUCT_MODEL := motorola edge 30 fusion

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="tundra_g-user 14 U1SJS34.2-92-10-3 c8794-4238a9 release-keys MUR1-0.262" \
    BuildFingerprint=motorola/tundra_g/tundra:14/U1SJS34.2-92-10-3/c8794-4238a9:user/release-keys \
    DeviceProduct=tundra_g

PRODUCT_GMS_CLIENTID_BASE := android-motorola
 
