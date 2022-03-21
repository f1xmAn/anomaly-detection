package com.github.f1xman.era.anomalydetection.data;

import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static java.lang.Boolean.TRUE;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Component
public class FakeEraRecognizer implements EraRecognizer {
    @Override
    public CompletableFuture<Boolean> isEra(URL data) {
        return completedFuture(TRUE);
    }
}
