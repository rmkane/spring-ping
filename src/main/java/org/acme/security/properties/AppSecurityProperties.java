package org.acme.security.properties;

import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(
        @DefaultValue("X-Echo-Request") String triggerRequestHeader,
        @DefaultValue("X-Echo-Response") String responseHeader,
        /**
         * Extra header names (case-insensitive) to obfuscate in logs and {@code /api/debug/headers},
         * in addition to {@link org.acme.ping.http.HttpHeaderObfuscation#DEFAULT_OBFUSCATED_HEADER_NAMES}.
         */
        List<String> obfuscatedHeaderNames) {

    public AppSecurityProperties {
        obfuscatedHeaderNames = Objects.requireNonNullElse(obfuscatedHeaderNames, List.of());
    }
}
