package com.github.f1xman.era.anomalydetection.validation;

import com.github.f1xman.era.anomalydetection.shared.DevicePhoneNumberHistory;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.util.Comparator.naturalOrder;
import static lombok.AccessLevel.PRIVATE;

@Component
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class SuspiciousRegistrationValidator implements Validator<DevicePhoneNumberHistory> {

    OffsetDateTime suspiciousAfter;

    public SuspiciousRegistrationValidator(@Value("${suspicious-registrations-after}") String suspiciousAfterRaw) {
        this.suspiciousAfter = OffsetDateTime.parse(suspiciousAfterRaw);
    }

    @Override
    public boolean validate(DevicePhoneNumberHistory input) {
        Optional<OffsetDateTime> firstRegisteredAt = input.getEntries().stream()
                .min(naturalOrder())
                .map(DevicePhoneNumberHistory.DatedDevicePhoneNumberEntry::getRegisteredAt);
        Boolean anomalyFound = firstRegisteredAt
                .map(r -> r.isAfter(suspiciousAfter))
                .orElse(FALSE);
        if (anomalyFound) {
            log.info("First registration at {} is suspicious", firstRegisteredAt.orElse(OffsetDateTime.MIN));
        }
        return anomalyFound;
    }
}
