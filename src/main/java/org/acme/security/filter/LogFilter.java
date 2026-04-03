package org.acme.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.acme.ping.http.HttpHeaderMaps;
import org.acme.ping.http.HttpHeaderObfuscation;
import org.acme.ping.http.SensitiveQueryRedaction;
import org.acme.security.properties.AppSecurityProperties;

/**
 * Logs each exchange as two events (curl {@code -v}-style): first {@code >}
 * request line and headers, then after the chain {@code <} status line and
 * response headers. Sensitive header values are obfuscated per
 * {@link AppSecurityProperties#namesForObfuscation()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogFilter extends OncePerRequestFilter {

    private static final String REQUEST_PREFIX = ">";
    private static final String RESPONSE_PREFIX = "<";

    private final AppSecurityProperties appSecurity;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!log.isInfoEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("HTTP request (curl-style)\n{}", formatRequestBlock(request));
        filterChain.doFilter(request, response);
        log.debug("HTTP response (curl-style)\n{}", formatResponseBlock(request, response));
    }

    private String formatRequestBlock(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(REQUEST_PREFIX).append(" ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURI());
        String query = request.getQueryString();
        if (query != null) {
            sb.append("?")
                    .append(
                            appSecurity.debugHeadersAccessConfigured()
                                    ? SensitiveQueryRedaction.redactQueryParameter(
                                            query, appSecurity.debugHeadersTokenQueryParam())
                                    : query);
        }
        sb.append(" ")
                .append(request.getProtocol())
                .append('\n');
        appendHeaders(
                sb,
                REQUEST_PREFIX,
                HttpHeaderObfuscation.obfuscateValues(
                        HttpHeaderMaps.fromRequest(request), appSecurity.namesForObfuscation()));
        return sb.toString().stripTrailing();
    }

    private String formatResponseBlock(HttpServletRequest request, HttpServletResponse response) {
        String protocol = request.getProtocol() != null ? request.getProtocol() : "HTTP/1.1";
        StringBuilder sb = new StringBuilder();
        sb.append(RESPONSE_PREFIX)
                .append(" ")
                .append(protocol)
                .append(" ")
                .append(response.getStatus())
                .append('\n');
        appendHeaders(
                sb,
                RESPONSE_PREFIX,
                HttpHeaderObfuscation.obfuscateValues(
                        HttpHeaderMaps.fromResponse(response), appSecurity.namesForObfuscation()));
        return sb.toString().stripTrailing();
    }

    private static void appendHeaders(StringBuilder sb, String prefix, Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            for (String value : e.getValue()) {
                sb.append(prefix)
                        .append(" ")
                        .append(e.getKey())
                        .append(": ")
                        .append(value)
                        .append('\n');
            }
        }
    }
}
