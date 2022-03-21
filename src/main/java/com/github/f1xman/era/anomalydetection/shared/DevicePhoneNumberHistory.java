package com.github.f1xman.era.anomalydetection.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.era.anomalydetection.device.DeviceRegisteredEvent;
import com.github.f1xman.era.anomalydetection.util.SerDe;
import com.github.f1xman.era.anomalydetection.validation.AnomaliesAware;
import com.github.f1xman.era.anomalydetection.validation.Validator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.f1xman.era.anomalydetection.util.ListUtils.keepLatest;
import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@ToString
@RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
public class DevicePhoneNumberHistory implements AnomaliesAware<DevicePhoneNumberHistory> {

    public static final Type<DevicePhoneNumberHistory> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.shared/DevicePhoneNumberHistory"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, DevicePhoneNumberHistory.class)
    );
    private static final int MAX_ENTRIES = 100;

    public static DevicePhoneNumberHistory createEmpty() {
        return new DevicePhoneNumberHistory(new ArrayList<>(), null);
    }

    @JsonProperty("entries")
    List<DatedDevicePhoneNumberEntry> entries;
    @JsonProperty("firstRegistrationAt")
    OffsetDateTime firstRegistrationAt;

    public DevicePhoneNumberHistory add(DatedDevicePhoneNumberEntry entry) {
        OffsetDateTime firstRegistration = Objects.requireNonNullElse(firstRegistrationAt, entry.registeredAt);
        List<DatedDevicePhoneNumberEntry> updatedEntries = keepLatest(this.entries, MAX_ENTRIES - 1);
        updatedEntries.add(entry);
        return new DevicePhoneNumberHistory(updatedEntries, firstRegistration);
    }

    @Override
    public boolean hasAnomalies(Validator<DevicePhoneNumberHistory> validator) {
        return validator.validate(this);
    }

    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter
    @ToString
    @RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
    public static final class DatedDevicePhoneNumberEntry implements Comparable<DatedDevicePhoneNumberEntry> {

        @JsonProperty("imei")
        String imei;
        @JsonProperty("phoneNumber")
        String phoneNumber;
        @JsonProperty("registeredAt")
        OffsetDateTime registeredAt;

        public static DatedDevicePhoneNumberEntry from(DeviceRegisteredEvent event) {
            return new DatedDevicePhoneNumberEntry(event.getImei(), event.getPhoneNumber(), event.getRegisteredAt());
        }

        @Override
        public int compareTo(DatedDevicePhoneNumberEntry that) {
            return this.registeredAt.compareTo(that.registeredAt);
        }
    }
}
