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

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Getter
@ToString
class Status {

    public static final Type<Status> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.device/Status"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, Status.class)
    );

    @JsonProperty("value")
    StatusValue value;

    public static Status evaluating() {
        return new Status(StatusValue.EVALUATING);
    }

    public static Status trusted(boolean trusted) {
        return new Status(StatusValue.trusted(trusted));
    }

    public boolean isEvaluating() {
        return value == StatusValue.EVALUATING;
    }

    public boolean isUntrusted() {
        return value == StatusValue.UNTRUSTED;
    }

    public String toValueString() {
        return value.name();
    }

    private enum StatusValue {
        EVALUATING, TRUSTED, UNTRUSTED;

        public static StatusValue trusted(boolean trusted) {
            return trusted ? TRUSTED : UNTRUSTED;
        }
    }
}
