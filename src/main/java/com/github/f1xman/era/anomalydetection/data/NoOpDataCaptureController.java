package com.github.f1xman.era.anomalydetection.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpDataCaptureController implements DataCaptureController {

    @Override
    public void enable(String imei) {
        log.info("Data capturing enabled for device {}", imei);
    }
}
