package com.github.f1xman.era.anomalydetection.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@Slf4j
public class ListUtils {

    public static <E> List<E> keepLatest(List<E> list, int limit) {
        int originalSize = list.size();
        List<E> updatedList = list.stream()
                .sorted(reverseOrder())
                .limit(limit)
                .collect(toList());
        log.info("Dropped {} entries", originalSize - updatedList.size());
        return updatedList;
    }

}
