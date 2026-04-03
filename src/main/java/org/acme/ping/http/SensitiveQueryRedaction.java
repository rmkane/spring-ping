package org.acme.ping.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Redacts configured query parameter values (e.g. debug token) from strings
 * used for logging/JSON.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensitiveQueryRedaction {

    /**
     * Replaces {@code paramName=value} segments with {@code paramName=[REDACTED]}
     * (first occurrence per segment; typical for a single secret query param).
     */
    public static String redactQueryParameter(String queryString, String paramName) {
        if (queryString == null || queryString.isEmpty() || paramName == null || paramName.isBlank()) {
            return queryString == null ? "" : queryString;
        }
        Pattern pattern = Pattern.compile("(^|&)" + Pattern.quote(paramName) + "=([^&]*)");
        Matcher matcher = pattern.matcher(queryString);
        String replacement = "$1" + Matcher.quoteReplacement(paramName) + "=[REDACTED]";
        return matcher.replaceAll(replacement);
    }
}
