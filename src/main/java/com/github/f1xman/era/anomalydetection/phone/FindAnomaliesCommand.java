package com.github.f1xman.era.anomalydetection.phone;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f1xman.era.anomalydetection.shared.DevicePhoneNumberHistory;
import com.github.f1xman.era.anomalydetection.util.SerDe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.types.SimpleType;
import org.apache.flink.statefun.sdk.java.types.Type;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@ToString
@RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
public class FindAnomaliesCommand {

    public static final Type<FindAnomaliesCommand> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.phone/FindAnomaliesCommand"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, FindAnomaliesCommand.class)
    );

    @JsonProperty("devicePhoneNumberHistory")
    DevicePhoneNumberHistory devicePhoneNumberHistory;
}
