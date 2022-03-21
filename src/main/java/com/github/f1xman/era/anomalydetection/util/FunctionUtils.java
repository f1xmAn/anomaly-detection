package com.github.f1xman.era.anomalydetection.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.statefun.sdk.java.Address;
import org.apache.flink.statefun.sdk.java.Context;
import org.apache.flink.statefun.sdk.java.StatefulFunction;
import org.apache.flink.statefun.sdk.java.TypeName;
import org.apache.flink.statefun.sdk.java.message.Message;
import org.slf4j.MDC;

import java.lang.reflect.Proxy;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Slf4j
public class FunctionUtils {

    public static StatefulFunction setupLogging(StatefulFunction target) {
        return (StatefulFunction) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class[]{StatefulFunction.class},
                (proxy, method, args) -> {
                    Context context = (Context) args[0];
                    String name = context.self().type().name();
                    String id = context.self().id();
                    MDC.put("function", String.format("%s (%s)", name, id));
                    if ("apply".equals(method.getName())) {
                        Message message = (Message) args[1];
                        String caller = context.caller()
                                .map(Address::type)
                                .map(TypeName::name)
                                .orElse("Ingress");
                        log.info("Received incoming message {} from {}", message.valueTypeName().name(), caller);
                    }
                    Object result = method.invoke(target, args);
                    MDC.remove("function");
                    return result;
                });
    }

}
