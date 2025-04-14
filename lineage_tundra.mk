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
    BuildDesc="tundra_g-user 14 U1SJS34.2-92-10-5 ee7f9e-2bf813 release-keys" \
    BuildFingerprint=motorola/tundra_g/tundra:14/U1SJS34.2-92-10-5/ee7f9e-2bf813:user/release-keys \
    DeviceProduct=tundra_g

# Define rear camera specs
AXION_CAMERA_REAR_INFO := 50,13,2

# Define front camera specs
AXION_CAMERA_FRONT_INFO := 32

# Maintainer name
AXION_MAINTAINER := HotaruOs

# Processor name
AXION_PROCESSOR := Snapdragon_888_+

# Blur
TARGET_ENABLE_BLUR := true

# Define small and big core groups
AXION_CPU_SMALL_CORES := 0,1,2,3
AXION_CPU_BIG_CORES := 4,5,6
AXION_CPU_BG := 0-2
AXION_CPU_FG := 0-5
AXION_CPU_LIMIT_BG := 0-1
AXION_DEBUGGING_ENABLED := false

# AxionOS scheduling properties
 PRODUCT_SYSTEM_PROPERTIES += \
    persist.sys.axion_cpu_big=$(AXION_CPU_BIG_CORES) \
    persist.sys.axion_cpu_small=$(AXION_CPU_SMALL_CORES) \
    persist.sys.axion_cpu_bg=$(AXION_CPU_BG) \
    persist.sys.axion_cpu_limit_bg=$(AXION_CPU_LIMIT_BG) \
    persist.sys.axion_cpu_fg=$(AXION_CPU_FG) \
    ro.sys.axion_userdebug_enabled=$(AXION_DEBUGGING_ENABLED)
