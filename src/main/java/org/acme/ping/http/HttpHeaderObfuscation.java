package org.acme.ping.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Copies header maps for safe logging: sensitive names stay visible, values are
 * replaced with a placeholder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpHeaderObfuscation {

    public static final String PLACEHOLDER = "[REDACTED]";

    /** Used when {@code app.security.obfuscated-header-names} is empty. */
    public static final List<String> DEFAULT_OBFUSCATED_HEADER_NAMES = List.of(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "Proxy-Authorization",
            "X-Api-Key",
            "X-Auth-Token",
            "X-Access-Token",
            "Refresh-Token",
            "X-CSRF-TOKEN");

    /**
     * Returns a new map with the same keys and order; values for headers whose
     * names match {@code namesToObfuscate} (case-insensitive) are replaced with
     * {@link #PLACEHOLDER}. The built-in {@link #DEFAULT_OBFUSCATED_HEADER_NAMES}
     * always applies; extra names from config are added on top.
     */
    public static SortedMap<String, List<String>> obfuscateValues(
            SortedMap<String, List<String>> headers, Collection<String> namesToObfuscate) {
        Set<String> match = resolveNames(namesToObfuscate);
        SortedMap<String, List<String>> out = new TreeMap<>(headers.comparator());
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String name = e.getKey();
            List<String> values = e.getValue();
            if (isSensitive(name, match)) {
                out.put(
                        name,
                        values.stream()
                                .map(v -> PLACEHOLDER)
                                .collect(Collectors.toCollection(ArrayList::new)));
            } else {
                out.put(name, new ArrayList<>(values));
            }
        }
        return out;
    }

    private static Set<String> resolveNames(Collection<String> namesToObfuscate) {
        Stream<String> stream = DEFAULT_OBFUSCATED_HEADER_NAMES.stream();
        if (namesToObfuscate != null && !namesToObfuscate.isEmpty()) {
            stream = Stream.concat(stream, namesToObfuscate.stream());
        }
        return stream.map(String::toLowerCase).collect(Collectors.toSet());
    }

    private static boolean isSensitive(String headerName, Set<String> lowercaseNames) {
        return lowercaseNames.contains(headerName.toLowerCase());
    }
}
