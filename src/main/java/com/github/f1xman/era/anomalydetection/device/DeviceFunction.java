package com.github.f1xman.era.anomalydetection.device;

import com.github.f1xman.era.anomalydetection.data.DataCaptureController;
import com.github.f1xman.era.anomalydetection.data.EraRecognizer;
import com.github.f1xman.era.anomalydetection.phone.FindAnomaliesCommand;
import com.github.f1xman.era.anomalydetection.phone.PhoneFunction;
import com.github.f1xman.era.anomalydetection.shared.DevicePhoneNumberHistory;
import com.github.f1xman.era.anomalydetection.statefun.SpecProvidedStatefulFunction;
import com.github.f1xman.era.anomalydetection.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.statefun.sdk.java.*;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.apache.flink.statefun.sdk.java.message.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.f1xman.era.anomalydetection.device.LocationHistory.DatedLocation;
import static com.github.f1xman.era.anomalydetection.util.FunctionUtils.setupLogging;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class DeviceFunction implements SpecProvidedStatefulFunction {

    public static final TypeName TYPE = TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.device/Device");
    private static final ValueSpec<LocationHistory> LOCATION_HISTORY_SPEC = ValueSpec
            .named("locationHistory")
            .thatExpireAfterWrite(Duration.ofDays(30))
            .withCustomType(LocationHistory.TYPE);
    private static final ValueSpec<DevicePhoneNumberHistory> PHONE_HISTORY_SPEC = ValueSpec
            .named("phoneHistorySpec")
            .thatExpireAfterWrite(Duration.ofDays(30))
            .withCustomType(DevicePhoneNumberHistory.TYPE);
    private static final ValueSpec<Status> STATUS_SPEC = ValueSpec
            .named("status")
            .thatExpiresAfterCall(Duration.ofDays(365))
            .withCustomType(Status.TYPE);
    private static final ValueSpec<DeviceRegisteredEventsBuffer> BUFFER_SPEC = ValueSpec
            .named("buffer")
            .thatExpireAfterWrite(Duration.ofDays(1))
            .withCustomType(DeviceRegisteredEventsBuffer.TYPE);

    List<Validator<LocationHistory>> locationValidators;
    List<Validator<DevicePhoneNumberHistory>> deviceHistoryValidators;
    DataCaptureController dataCaptureController;
    EraRecognizer eraRecognizer;

    @Override
    public CompletableFuture<Void> apply(Context context, Message message) {
        AddressScopedStorage storage = context.storage();
        Status status = storage.get(STATUS_SPEC).orElse(Status.evaluating());
        if (message.is(DeviceRegisteredEvent.TYPE)) {
            DeviceRegisteredEvent event = message.as(DeviceRegisteredEvent.TYPE);
            shareRegistrationWithPhoneFunction(context, message, event);
            updateLocationHistory(event, context);
            DevicePhoneNumberHistory phoneHistory = updatePhoneNumberHistory(event, context);
            if (status.isEvaluating()) {
                requestAnomalies(context, event, phoneHistory);
                bufferEvent(context, event);
            } else if (status.isUntrusted()) {
                forwardSuspiciousEvent(context, event);
            }
        } else if (status.isEvaluating() && message.is(AnomaliesSearchCompletedEvent.TYPE)) {
            handleAnomalies(context, message);
        } else if (status.isEvaluating() && message.is(DataAccessEvent.TYPE)) {
            return recognizeEra(context, message);
        }
        return context.done();
    }

    private void bufferEvent(Context context, DeviceRegisteredEvent event) {
        AddressScopedStorage storage = context.storage();
        DeviceRegisteredEventsBuffer buffer = storage.get(BUFFER_SPEC).orElseGet(DeviceRegisteredEventsBuffer::createEmpty);
        DeviceRegisteredEventsBuffer updatedBuffer = buffer.add(event);
        storage.set(BUFFER_SPEC, updatedBuffer);
    }

    private CompletableFuture<Void> recognizeEra(Context context, Message message) {
        DataAccessEvent event = message.as(DataAccessEvent.TYPE);
        CompletableFuture<Boolean> eraDetected = eraRecognizer.isEra(event.getDataUrl());
        return eraDetected.thenAccept(untrusted -> {
            AddressScopedStorage storage = context.storage();
            Status status = Status.trusted(!untrusted);
            storage.set(STATUS_SPEC, status);
            log.info("Device considered as {}", status.toValueString());
            if (untrusted) {
                storage.get(BUFFER_SPEC)
                        .stream()
                        .flatMap(DeviceRegisteredEventsBuffer::eventStream)
                        .forEach(e -> forwardSuspiciousEvent(context, e));
            }
            storage.remove(BUFFER_SPEC);
        });
    }

    private void forwardSuspiciousEvent(Context context, DeviceRegisteredEvent event) {
        context.send(
                SuspiciousDeviceRegisteredEgress.messageBuilder()
                        .withCustomType(DeviceRegisteredEvent.TYPE, event)
                        .build()
        );
    }

    private void handleAnomalies(Context context, Message message) {
        AddressScopedStorage storage = context.storage();
        LocationHistory locationHistory = storage.get(LOCATION_HISTORY_SPEC).orElseGet(LocationHistory::createEmpty);
        DevicePhoneNumberHistory phoneHistory = storage.get(PHONE_HISTORY_SPEC).orElseGet(DevicePhoneNumberHistory::createEmpty);
        boolean locationAnomalyFound = locationValidators.stream().anyMatch(v -> v.validate(locationHistory));
        boolean deviceAnomalyFound = deviceHistoryValidators.stream().anyMatch(v -> v.validate(phoneHistory));
        AnomaliesSearchCompletedEvent event = message.as(AnomaliesSearchCompletedEvent.TYPE);
        if (locationAnomalyFound || deviceAnomalyFound || event.isAnomalyFound()) {
            String imei = context.self().id();
            dataCaptureController.enable(imei);
        }
    }

    private void requestAnomalies(Context context, DeviceRegisteredEvent event, DevicePhoneNumberHistory phoneHistory) {
        context.send(
                MessageBuilder.forAddress(PhoneFunction.TYPE, event.getPhoneNumber())
                        .withCustomType(FindAnomaliesCommand.TYPE, new FindAnomaliesCommand(phoneHistory))
                        .build()
        );
    }

    private void shareRegistrationWithPhoneFunction(Context context, Message message, DeviceRegisteredEvent event) {
        context.send(MessageBuilder
                .fromMessage(message)
                .withTargetAddress(PhoneFunction.TYPE, event.getPhoneNumber())
                .build()
        );
    }

    private void updateLocationHistory(DeviceRegisteredEvent event, Context context) {
        AddressScopedStorage storage = context.storage();
        LocationHistory locationHistory = storage.get(LOCATION_HISTORY_SPEC).orElseGet(LocationHistory::createEmpty);
        LocationHistory updatedLocationHistory = locationHistory.add(DatedLocation.from(event.getStation(), event.getRegisteredAt()));
        storage.set(LOCATION_HISTORY_SPEC, updatedLocationHistory);
        log.info("Updated location history of device {}\n{}", context.self().id(), updatedLocationHistory);
    }

    private DevicePhoneNumberHistory updatePhoneNumberHistory(DeviceRegisteredEvent event, Context context) {
        AddressScopedStorage storage = context.storage();
        DevicePhoneNumberHistory phoneHistory = storage.get(PHONE_HISTORY_SPEC).orElseGet(DevicePhoneNumberHistory::createEmpty);
        DevicePhoneNumberHistory updatedPhoneHistory = phoneHistory.add(DevicePhoneNumberHistory.DatedDevicePhoneNumberEntry.from(event));
        storage.set(PHONE_HISTORY_SPEC, updatedPhoneHistory);
        log.info("Updated phone history of device {}\n{}", context.self().id(), updatedPhoneHistory);
        return updatedPhoneHistory;
    }

    @Override
    public StatefulFunctionSpec spec() {
        StatefulFunction instance = setupLogging(this);
        return StatefulFunctionSpec.builder(TYPE)
                .withSupplier(() -> instance)
                .withValueSpecs(LOCATION_HISTORY_SPEC, PHONE_HISTORY_SPEC, STATUS_SPEC, BUFFER_SPEC)
                .build();
    }
}
