package com.github.f1xman.era.anomalydetection.validation;

import com.github.f1xman.era.anomalydetection.shared.DevicePhoneNumberHistory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class EqualDevicePhoneHistoriesValidator implements Validator<DevicePhoneNumberHistory> {

    @NonNull
    DevicePhoneNumberHistory history;

    @Override
    public boolean validate(DevicePhoneNumberHistory input) {
        boolean anomalyFound = !this.history.equals(input);
        if (anomalyFound) {
            log.info("Given device / phone number histories are not equal");
        }
        return anomalyFound;
    }
}
