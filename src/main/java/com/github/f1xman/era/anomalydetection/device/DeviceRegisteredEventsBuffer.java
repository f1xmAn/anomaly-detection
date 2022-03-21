package com.github.f1xman.era.anomalydetection.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.era.anomalydetection.util.SerDe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@ToString
@Getter
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
public class DeviceRegisteredEventsBuffer {

    public static final Type<DeviceRegisteredEventsBuffer> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.device/DeviceRegisteredEventsBuffer"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, DeviceRegisteredEventsBuffer.class)
    );

    @JsonProperty("events")
    List<DeviceRegisteredEvent> events;

    public static DeviceRegisteredEventsBuffer createEmpty() {
        return new DeviceRegisteredEventsBuffer(new ArrayList<>());
    }

    public DeviceRegisteredEventsBuffer add(DeviceRegisteredEvent event) {
        log.info("Buffering event {}", event);
        ArrayList<DeviceRegisteredEvent> updatedList = new ArrayList<>(events);
        updatedList.add(event);
        return new DeviceRegisteredEventsBuffer(updatedList);
    }

    public Stream<DeviceRegisteredEvent> eventStream() {
        return events.stream();
    }

}
