/*
 * SPDX-FileCopyrightText: 2017 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */

#ifndef ANDROID_HARDWARE_BIOMETRICS_FINGERPRINT_V2_3_BIOMETRICSFINGERPRINT_H
#define ANDROID_HARDWARE_BIOMETRICS_FINGERPRINT_V2_3_BIOMETRICSFINGERPRINT_H

#include <android/hardware/biometrics/fingerprint/2.3/IBiometricsFingerprint.h>
#include <com/motorola/hardware/biometric/fingerprint/1.0/IMotoFingerPrint.h>
#include <hidl/MQDescriptor.h>
#include <hidl/Status.h>

namespace android {
namespace hardware {
namespace biometrics {
namespace fingerprint {
namespace V2_3 {
namespace implementation {

using IBiometricsFingerprint_2_1 =
        ::android::hardware::biometrics::fingerprint::V2_1::IBiometricsFingerprint;
using ::android::sp;
using ::android::hardware::hidl_array;
using ::android::hardware::hidl_memory;
using ::android::hardware::hidl_string;
using ::android::hardware::hidl_vec;
using ::android::hardware::Return;
using ::android::hardware::Void;
using ::android::hardware::biometrics::fingerprint::V2_1::IBiometricsFingerprintClientCallback;
using ::android::hardware::biometrics::fingerprint::V2_1::RequestStatus;
using ::com::motorola::hardware::biometric::fingerprint::V1_0::IMotFodEventResult;
using ::com::motorola::hardware::biometric::fingerprint::V1_0::IMotFodEventType;
using ::com::motorola::hardware::biometric::fingerprint::V1_0::IMotoFingerPrint;

struct BiometricsFingerprint : public IBiometricsFingerprint {
    BiometricsFingerprint();
    // Methods from ::V2_1::IBiometricsFingerprint follow.
    Return<uint64_t> setNotify(
            const sp<IBiometricsFingerprintClientCallback>& clientCallback) override;
    Return<uint64_t> preEnroll() override;
    Return<RequestStatus> enroll(const hidl_array<uint8_t, 69>& hat, uint32_t gid,
                                 uint32_t timeoutSec) override;
    Return<RequestStatus> postEnroll() override;
    Return<uint64_t> getAuthenticatorId() override;
    Return<RequestStatus> cancel() override;
    Return<RequestStatus> enumerate() override;
    Return<RequestStatus> remove(uint32_t gid, uint32_t fid) override;
    Return<RequestStatus> setActiveGroup(uint32_t gid, const hidl_string& storePath) override;
    Return<RequestStatus> authenticate(uint64_t operationId, uint32_t gid) override;

    // ::V2_3::IBiometricsFingerprint follow.
    Return<bool> isUdfps(uint32_t sensorId) override;
    Return<void> onFingerDown(uint32_t x, uint32_t y, float minor, float major) override;
    Return<void> onFingerUp() override;

  private:
    void disableHighBrightFod();
    void enableHighBrightFod();

    bool hbmFodEnabled;
    std::mutex mSetHbmFodMutex;

    sp<IBiometricsFingerprint_2_1> biometrics_2_1_service;
    sp<IMotoFingerPrint> mMotoFingerprint;
};

}  // namespace implementation
}  // namespace V2_3
}  // namespace fingerprint
}  // namespace biometrics
}  // namespace hardware
}  // namespace android
#endif  // ANDROID_HARDWARE_BIOMETRICS_FINGERPRINT_V2_3_BIOMETRICSFINGERPRINT_H
