package com.github.f1xman.era.anomalydetection.device;

import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.EgressMessageBuilder;

public class SuspiciousDeviceRegisteredEgress {

    private static final TypeName TYPE = TypeName
            .typeNameFromString("com.github.f1xman.era.anomalydetection.device/SuspiciousDeviceRegisteredEgress");

    public static EgressMessageBuilder messageBuilder() {
        return EgressMessageBuilder.forEgress(TYPE);
    }

}
