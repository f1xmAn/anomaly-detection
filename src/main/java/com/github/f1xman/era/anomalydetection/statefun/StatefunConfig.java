package com.github.f1xman.era.anomalydetection.statefun;

import org.apache.flink.statefun.sdk.java.StatefulFunctions;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class StatefunConfig {

    @Bean
    RequestReplyHandler requestReplyHandler(List<SpecProvidedStatefulFunction> functions) {
        StatefulFunctions statefulFunctions = new StatefulFunctions();
        functions.stream()
                .map(SpecProvidedStatefulFunction::spec)
                .forEach(statefulFunctions::withStatefulFunction);
        return statefulFunctions.requestReplyHandler();
    }
}
