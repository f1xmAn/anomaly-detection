package com.github.f1xman.era.anomalydetection.phone;

import com.github.f1xman.era.anomalydetection.device.AnomaliesSearchCompletedEvent;
import com.github.f1xman.era.anomalydetection.device.DeviceRegisteredEvent;
import com.github.f1xman.era.anomalydetection.shared.DevicePhoneNumberHistory;
import com.github.f1xman.era.anomalydetection.statefun.SpecProvidedStatefulFunction;
import com.github.f1xman.era.anomalydetection.validation.EqualDevicePhoneHistoriesValidator;
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
import java.util.stream.Stream;

import static com.github.f1xman.era.anomalydetection.shared.DevicePhoneNumberHistory.DatedDevicePhoneNumberEntry;
import static com.github.f1xman.era.anomalydetection.util.FunctionUtils.setupLogging;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PhoneFunction implements SpecProvidedStatefulFunction {

    public static final TypeName TYPE = TypeName.typeNameFromString("com.github.f1xman.era.anomalydetection.phone/Phone");
    private static final ValueSpec<DevicePhoneNumberHistory> PHONE_HISTORY_SPEC = ValueSpec
            .named("phoneHistory")
            .thatExpireAfterWrite(Duration.ofDays(30))
            .withCustomType(DevicePhoneNumberHistory.TYPE);

    List<Validator<DevicePhoneNumberHistory>> phoneHistoryValidators;

    @Override
    public CompletableFuture<Void> apply(Context context, Message message) {
        if (message.is(DeviceRegisteredEvent.TYPE)) {
            addDeviceToPhoneHistory(context, message);
        } else if (message.is(FindAnomaliesCommand.TYPE)) {
            findAnomalies(context, message);
        }
        return context.done();
    }

    private void findAnomalies(Context context, Message message) {
        FindAnomaliesCommand command = message.as(FindAnomaliesCommand.TYPE);
        DevicePhoneNumberHistory thatHistory = command.getDevicePhoneNumberHistory();
        AddressScopedStorage storage = context.storage();
        DevicePhoneNumberHistory thisHistory = storage.get(PHONE_HISTORY_SPEC).orElseGet(DevicePhoneNumberHistory::createEmpty);
        boolean anomalyFound = Stream.concat(
                        phoneHistoryValidators.stream(),
                        Stream.of(new EqualDevicePhoneHistoriesValidator(thatHistory))
                )
                .anyMatch(v -> v.validate(thisHistory));
        context.send(
                MessageBuilder.forAddress(context.caller().orElseThrow())
                        .withCustomType(AnomaliesSearchCompletedEvent.TYPE, new AnomaliesSearchCompletedEvent(anomalyFound))
                        .build()
        );
    }

    private void addDeviceToPhoneHistory(Context context, Message message) {
        DeviceRegisteredEvent event = message.as(DeviceRegisteredEvent.TYPE);
        AddressScopedStorage storage = context.storage();
        DevicePhoneNumberHistory phoneHistory = storage.get(PHONE_HISTORY_SPEC).orElseGet(DevicePhoneNumberHistory::createEmpty);
        DevicePhoneNumberHistory updatedPhoneHistory = phoneHistory.add(DatedDevicePhoneNumberEntry.from(event));
        storage.set(PHONE_HISTORY_SPEC, updatedPhoneHistory);
    }

    @Override
    public StatefulFunctionSpec spec() {
        StatefulFunction instance = setupLogging(this);
        return StatefulFunctionSpec
                .builder(TYPE)
                .withSupplier(() -> instance)
                .withValueSpec(PHONE_HISTORY_SPEC)
                .build();
    }
}
