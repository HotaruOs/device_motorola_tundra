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
PRODUCT_MODEL := motorola edge 30 fusion

PRODUCT_GMS_CLIENTID_BASE := android-motorola

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="tundra_g-user 14 U1SJS34.2-92-10-3 c8794-4238a9 release-keys MUR1-0.262" \
    BuildFingerprint=motorola/tundra_g/tundra:14/U1SJS34.2-92-10-3/c8794-4238a9:user/release-keys \
    DeviceProduct=tundra_g

# Define rear camera specs
 AXION_CAMERA_REAR_INFO := 50,13,2
 
 # Define front camera specs
 AXION_CAMERA_FRONT_INFO := 32
 
 # Maintainer name (use "_" for spaces, e.g., "rmp_22" → "rmp 22" in UI)
 AXION_MAINTAINER := HotaruOs
 
 # Processor name (use "_" for spaces)
 AXION_PROCESSOR := Snapdragon_888_+
 
 # Define small and big core groups
 AXION_CPU_SMALL_CORES := 0,1,2,3
 AXION_CPU_BIG_CORES := 4,5,6
 AXION_MODERN_KERNEL := true
 
 # AxionOS scheduling properties
 PRODUCT_SYSTEM_PROPERTIES += \
     persist.sys.axion_cpu_big=$(AXION_CPU_BIG_CORES) \
     persist.sys.axion_cpu_small=$(AXION_CPU_SMALL_CORES) \
     persist.sys.axion_is_modern_kernel=$(AXION_MODERN_KERNEL)
