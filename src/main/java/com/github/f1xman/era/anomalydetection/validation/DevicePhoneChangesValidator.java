package com.github.f1xman.era.anomalydetection.validation;

import com.github.f1xman.era.anomalydetection.shared.DevicePhoneNumberHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class DevicePhoneChangesValidator implements Validator<DevicePhoneNumberHistory> {

    @Override
    public boolean validate(DevicePhoneNumberHistory input) {
        long differentPairsCount = input.getEntries().stream()
                .map(e -> Map.entry(e.getImei(), e.getPhoneNumber()))
                .distinct()
                .count();
        log.info("Device / phone number history has {} different pairs", differentPairsCount);
        return differentPairsCount > 1;
    }
}
