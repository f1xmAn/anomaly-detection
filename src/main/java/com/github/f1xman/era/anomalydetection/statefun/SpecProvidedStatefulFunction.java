package com.github.f1xman.era.anomalydetection.statefun;

import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.StatefulFunctionSpec;

public interface SpecProvidedStatefulFunction extends StatefulFunction {

    StatefulFunctionSpec spec();
}
