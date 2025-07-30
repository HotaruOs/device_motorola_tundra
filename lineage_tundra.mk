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
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_NAME := lineage_tundra
PRODUCT_DEVICE := tundra
PRODUCT_MANUFACTURER := motorola
PRODUCT_BRAND := motorola
PRODUCT_MODEL := Edge S30 Pro

PRODUCT_GMS_CLIENTID_BASE := android-motorola

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="tundra_g-user 14 U1SJS34.2-92-10-6 c9587-03599 release-keys" \
    BuildFingerprint=motorola/tundra_g/tundra:14/U1SJS34.2-92-10-6/c9587-03599:user/release-keys \
    DeviceProduct=tundra_g

# Lunch banner maintainer variable
RISING_MAINTAINER="HotaruOs"

# Chipset/Maintainer properties
PRODUCT_BUILD_PROP_OVERRIDES += \
    RisingChipset="Snapdragon 888+" \
    RisingMaintainer="HotaruOs"

RISING_MAINTAINER := HotaruOs

# Disable/enable blur support, false by default
TARGET_ENABLE_BLUR := true

# Whether to ship aperture camera, false by default
PRODUCT_NO_CAMERA := false

# Whether to ship lawnchair launcher, false by default
TARGET_PREBUILT_LAWNCHAIR_LAUNCHER := false

# CORE build flags
WITH_GMS := true
TARGET_USES_PICO_GAPPS := true
