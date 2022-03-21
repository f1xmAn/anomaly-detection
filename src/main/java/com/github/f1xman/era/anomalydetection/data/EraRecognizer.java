package com.github.f1xman.era.anomalydetection.data;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface EraRecognizer {

    CompletableFuture<Boolean> isEra(URL data);

}
