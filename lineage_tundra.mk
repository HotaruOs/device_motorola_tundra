#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
TARGET_SUPPORTS_OMX_SERVICE := false
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit from tundra device
$(call inherit-product, device/motorola/tundra/device.mk)

# Inherit some common Lineage stuff.
TARGET_DISABLE_EPPE := true
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_NAME := lineage_tundra
PRODUCT_DEVICE := tundra
PRODUCT_MANUFACTURER := motorola
PRODUCT_BRAND := motorola
PRODUCT_MODEL := Edge S30 Pro

PRODUCT_GMS_CLIENTID_BASE := android-motorola

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="tundra_g-user 14 U1SJS34.2-92-10-9 40971b release-keys" \
    BuildFingerprint=motorola/tundra_g/tundra:14/U1SJS34.2-92-10-9/40971b:user/release-keys \
    DeviceProduct=tundra_g

TARGET_ENABLE_BLUR := true
# Camera information 
AXION_CAMERA_REAR_INFO := 50,13,2
AXION_CAMERA_FRONT_INFO := 32

# Maintainer/Soc name 
AXION_MAINTAINER := HotaruOs
AXION_PROCESSOR := Snapdragon_888+

# CPU governor support
PERF_GOV_SUPPORTED := true
PERF_DEFAULT_GOV := schedutil

persist.sys.perf.scroll_opt = true
persist.sys.perf.scroll_opt.heavy_app = 0