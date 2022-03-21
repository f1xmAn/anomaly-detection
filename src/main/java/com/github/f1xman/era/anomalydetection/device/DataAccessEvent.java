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

import java.net.URL;

import static lombok.AccessLevel.PRIVATE;

@ToString
@Getter
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
public class DataAccessEvent {

    public static final Type<DataAccessEvent> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.device/DataAccessEvent"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, DataAccessEvent.class)
    );

    @JsonProperty("imei")
    String imei;
    @JsonProperty("dataUrl")
    URL dataUrl;

}
