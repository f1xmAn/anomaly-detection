package com.github.f1xman.era.anomalydetection.validation;

import com.github.f1xman.era.anomalydetection.device.LocationHistory;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE, makeFinal = true)
@Component
@Slf4j
public class DistanceValidator implements Validator<LocationHistory> {

    private static final int MAX_TRAVEL_SPEED = 150;

    @Override
    public boolean validate(LocationHistory input) {
        List<LocationHistory.DatedLocation> datedLocations = input.getDatedLocations().stream()
                .sorted()
                .toList();
        Iterator<LocationHistory.DatedLocation> iterator = datedLocations.iterator();
        boolean anomalyFound = false;
        while (iterator.hasNext() && !anomalyFound) {
            LocationHistory.DatedLocation locationA = iterator.next();
            if (iterator.hasNext()) {
                LocationHistory.DatedLocation locationB = iterator.next();
                anomalyFound = validateTravelSpeed(locationA, locationB);
            }
        }
        return anomalyFound;
    }

    private boolean validateTravelSpeed(LocationHistory.DatedLocation locationA, LocationHistory.DatedLocation locationB) {
        double distance = DistanceCalculator.distance(
                locationA.getLatitude(), locationA.getLongitude(),
                locationB.getLatitude(), locationB.getLongitude()
        );
        long hoursDiff = ChronoUnit.HOURS.between(locationB.getRegisteredAt(), locationA.getRegisteredAt());
        double averageTravelSpeed = distance / hoursDiff;
        boolean anomalyFound = averageTravelSpeed > MAX_TRAVEL_SPEED;
        if (anomalyFound) {
            log.info(
                    "Distance between {} and {} is {} km. It's too large to travel in {} hours",
                    locationA.toGeoString(), locationB.toGeoString(), distance, hoursDiff
            );
        }
        return anomalyFound;
    }
}
