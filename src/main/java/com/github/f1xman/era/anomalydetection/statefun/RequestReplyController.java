package com.github.f1xman.era.anomalydetection.statefun;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.flink.statefun.sdk.java.handler.RequestReplyHandler;
import org.apache.flink.statefun.sdk.java.slice.Slice;
import org.apache.flink.statefun.sdk.java.slice.Slices;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PRIVATE;

@RestController
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RequestReplyController {

    RequestReplyHandler handler;

    @PostMapping(
            value = "/statefun",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public CompletableFuture<byte[]> handle(@RequestBody byte[] request) {
        return handler
                .handle(Slices.wrap(request))
                .thenApply(Slice::toByteArray);
    }

}
