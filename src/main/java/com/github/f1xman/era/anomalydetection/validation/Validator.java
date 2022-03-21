package com.github.f1xman.era.anomalydetection.validation;

public interface Validator<T> {

    boolean validate(T input);

}
