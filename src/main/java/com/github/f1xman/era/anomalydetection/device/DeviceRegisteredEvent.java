package com.github.f1xman.era.anomalydetection.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.era.anomalydetection.util.SerDe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

import java.time.OffsetDateTime;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Getter
@ToString
@RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
public class DeviceRegisteredEvent {

    public static final Type<DeviceRegisteredEvent> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.device/DeviceRegisteredEvent"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, DeviceRegisteredEvent.class)
    );

    @JsonProperty("imei")
    String imei;
    @JsonProperty("phoneNumber")
    String phoneNumber;
    @JsonProperty("station")
    Station station;
    @JsonProperty("registeredAt")
    OffsetDateTime registeredAt;

    @Getter
    @ToString
    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
    public static class Station {

        @JsonProperty("latitude")
        double latitude;
        @JsonProperty("longitude")
        double longitude;

    }
}
