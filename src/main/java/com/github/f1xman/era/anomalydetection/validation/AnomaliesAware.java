package com.github.f1xman.era.anomalydetection.validation;

public interface AnomaliesAware<T> {
    boolean hasAnomalies(Validator<T> validator);
}
