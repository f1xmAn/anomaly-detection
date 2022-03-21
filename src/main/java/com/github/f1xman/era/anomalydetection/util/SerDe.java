package com.github.f1xman.era.anomalydetection.util;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class SerDe {

    private static final JsonMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @SneakyThrows
    public static <T> T deserialize(byte[] raw, Class<T> clazz) {
        return mapper.readValue(raw, clazz);
    }

    @SneakyThrows
    public static <T> byte[] serialize(T value) {
        return mapper.writeValueAsBytes(value);
    }

}
