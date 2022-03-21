package com.github.f1xman.era.anomalydetection.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import static com.github.f1xman.era.anomalydetection.util.ListUtils.keepLatest;
import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE, makeFinal = true)
@Getter
@ToString
@RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
public class LocationHistory implements AnomaliesAware<LocationHistory> {

    public static final Type<LocationHistory> TYPE = SimpleType.simpleImmutableTypeFrom(
            TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.device/LocationHistory"),
            SerDe::serialize, bytes -> SerDe.deserialize(bytes, LocationHistory.class)
    );
    private static final int MAX_ENTRIES = 100;

    static LocationHistory createEmpty() {
        return new LocationHistory(new ArrayList<>());
    }

    @JsonProperty("datedLocations")
    List<DatedLocation> datedLocations;

    public LocationHistory add(DatedLocation entry) {
        List<DatedLocation> updatedEntries = keepLatest(this.datedLocations, MAX_ENTRIES - 1);
        updatedEntries.add(entry);
        return new LocationHistory(updatedEntries);
    }

    @Override
    public boolean hasAnomalies(Validator<LocationHistory> validator) {
        return validator.validate(this);
    }

    @FieldDefaults(level = PRIVATE, makeFinal = true)
    @Getter
    @ToString
    @RequiredArgsConstructor(onConstructor_ = {@JsonCreator})
    public static final class DatedLocation implements Comparable<DatedLocation> {

        @JsonProperty("latitude")
        double latitude;
        @JsonProperty("longitude")
        double longitude;
        @JsonProperty("registeredAt")
        OffsetDateTime registeredAt;

        static DatedLocation from(DeviceRegisteredEvent.Station station, OffsetDateTime registeredAt) {
            return new DatedLocation(station.getLatitude(), station.getLongitude(), registeredAt);
        }

        @Override
        public int compareTo(DatedLocation that) {
            return this.registeredAt.compareTo(that.registeredAt);
        }

        public String toGeoString() {
            return "%s, %s".formatted(latitude, longitude);
        }
    }
}
