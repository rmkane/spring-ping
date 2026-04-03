package org.acme.ping.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Copies servlet HTTP headers into sorted maps (case-insensitive keys) for
 * logging or JSON.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpHeaderMaps {

    public static SortedMap<String, List<String>> fromRequest(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        name -> new ArrayList<>(Collections.list(request.getHeaders(name))),
                        HttpHeaderMaps::mergeValues,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
    }

    /**
     * Copies response headers. Duplicate <em>identical</em> values for the same
     * name are collapsed (Spring often registers {@code Content-Type} twice on the
     * servlet response).
     */
    public static SortedMap<String, List<String>> fromResponse(HttpServletResponse response) {
        return response.getHeaderNames().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        name -> dedupeValuesPreserveOrder(new ArrayList<>(response.getHeaders(name))),
                        HttpHeaderMaps::mergeValues,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
    }

    /**
     * Drops repeated identical strings while keeping order (distinct multi-values
     * like Set-Cookie stay).
     */
    private static List<String> dedupeValuesPreserveOrder(List<String> values) {
        return new ArrayList<>(new LinkedHashSet<>(values));
    }

    /**
     * Merges duplicate keys (e.g. same header name with different casing) into one
     * list.
     */
    private static List<String> mergeValues(List<String> a, List<String> b) {
        List<String> combined = new ArrayList<>(a);
        combined.addAll(b);
        return dedupeValuesPreserveOrder(combined);
    }
}
