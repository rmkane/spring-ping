package org.acme.security.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        @DefaultValue("X-Echo-Request") String triggerRequestHeader,
        @DefaultValue("X-Echo-Response") String responseHeader,
        /**
         * Request header that must carry {@link #debugHeadersAccessToken()} to call
         * {@code /api/debug/headers}.
         */
        @DefaultValue("X-Debug-Token") String debugHeadersTokenHeader,
        /**
         * Name of the query parameter whose <strong>value</strong> must equal
         * {@link #debugHeadersAccessToken()} when the header is not used. Only the
         * parameter name is configured here; there is no separate query secret.
         */
        @DefaultValue("debug-token") String debugHeadersTokenQueryParam,
        /**
         * Single secret for the debug endpoint: send as
         * {@link #debugHeadersTokenHeader()} value or as
         * {@link #debugHeadersTokenQueryParam()} value. If empty, the debug endpoint is
         * denied for everyone.
         */
        String debugHeadersAccessToken,
        /**
         * Extra header names (case-insensitive) to obfuscate in logs and
         * {@code /api/debug/headers}, in addition to
         * {@link org.acme.ping.http.HttpHeaderObfuscation#DEFAULT_OBFUSCATED_HEADER_NAMES}.
         */
        List<String> obfuscatedHeaderNames) {

    public AppSecurityProperties {
        obfuscatedHeaderNames = Objects.requireNonNullElse(obfuscatedHeaderNames, List.of());
        debugHeadersAccessToken = debugHeadersAccessToken == null ? "" : debugHeadersAccessToken.trim();
        if (debugHeadersTokenHeader == null || debugHeadersTokenHeader.isBlank()) {
            debugHeadersTokenHeader = "X-Debug-Token";
        }
        if (debugHeadersTokenQueryParam == null || debugHeadersTokenQueryParam.isBlank()) {
            debugHeadersTokenQueryParam = "debug-token";
        }
    }

    /**
     * Never includes {@link #debugHeadersAccessToken()} — the value is sensitive.
     */
    @Override
    public String toString() {
        return "AppSecurityProperties[triggerRequestHeader=%s, responseHeader=%s, debugHeadersTokenHeader=%s, debugHeadersTokenQueryParam=%s, debugHeadersAccessConfigured=%s, obfuscatedHeaderNames=%s]"
                .formatted(
                        triggerRequestHeader,
                        responseHeader,
                        debugHeadersTokenHeader,
                        debugHeadersTokenQueryParam,
                        debugHeadersAccessConfigured(),
                        obfuscatedHeaderNames);
    }

    public boolean debugHeadersAccessConfigured() {
        return !debugHeadersAccessToken.isEmpty();
    }

    /**
     * Header names for {@link org.acme.ping.http.HttpHeaderObfuscation} (includes
     * debug token header when configured).
     */
    public List<String> namesForObfuscation() {
        if (!debugHeadersAccessConfigured()) {
            return obfuscatedHeaderNames;
        }
        List<String> names = new ArrayList<>(obfuscatedHeaderNames.size() + 1);
        names.add(debugHeadersTokenHeader);
        names.addAll(obfuscatedHeaderNames);
        return names;
    }
}
