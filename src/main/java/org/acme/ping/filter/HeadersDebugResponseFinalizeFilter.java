package org.acme.ping.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;

import org.acme.ping.http.HttpHeaderMaps;
import org.acme.ping.http.HttpHeaderObfuscation;
import org.acme.security.properties.AppSecurityProperties;

/**
 * Runs immediately before the dispatcher (low order) so after the MVC chain the
 * cached JSON body can be updated with the final {@code responseHeaders} /
 * {@code responseStatus} (what the client actually receives), matching
 * {@code curl -i} and {@link org.acme.security.filter.LogFilter}.
 */
@RequiredArgsConstructor
public class HeadersDebugResponseFinalizeFilter implements Filter {

    private static final String PATH = "/api/debug/headers";
    private static final Set<String> SKIP_ON_REPLAY = Set.of("content-length", "content-type", "transfer-encoding",
            "trailer", "trailers");

    private final ObjectMapper objectMapper;
    private final AppSecurityProperties appSecurity;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        var httpRequest = Objects.requireNonNull((HttpServletRequest) request, "request");
        var httpResponse = Objects.requireNonNull((HttpServletResponse) response, "response");

        if (!PATH.equals(httpRequest.getServletPath())) {
            chain.doFilter(request, response);
            return;
        }

        var caching = new ContentCachingResponseWrapper(httpResponse);
        chain.doFilter(request, caching);

        byte[] body = caching.getContentAsByteArray();
        if (body.length == 0 || httpResponse.isCommitted()) {
            caching.copyBodyToResponse();
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            if (!root.isObject()) {
                caching.copyBodyToResponse();
                return;
            }
            ObjectNode obj = (ObjectNode) root;
            obj.set(
                    "responseHeaders",
                    objectMapper.valueToTree(
                            HttpHeaderObfuscation.obfuscateValues(
                                    HttpHeaderMaps.fromResponse(caching),
                                    appSecurity.namesForObfuscation())));
            obj.put("responseStatus", caching.getStatus());

            byte[] out = objectMapper.writeValueAsBytes(root);
            replayResponse(httpResponse, caching, out);
        } catch (Exception ignored) {
            caching.copyBodyToResponse();
        }
    }

    private void replayResponse(
            HttpServletResponse rawResponse,
            ContentCachingResponseWrapper caching,
            byte[] jsonBody) throws IOException {
        // Snapshot before reset(): the wrapper delegates to rawResponse, so reset()
        // clears headers
        // we would otherwise read from caching.getHeaderNames().
        List<HeaderToReplay> headersToReplay = new ArrayList<>();
        for (String name : caching.getHeaderNames()) {
            if (skipHeaderOnReplay(name)) {
                continue;
            }
            for (String value : caching.getHeaders(name)) {
                headersToReplay.add(new HeaderToReplay(name, value));
            }
        }
        int status = caching.getStatus();

        try {
            rawResponse.reset();
        } catch (IllegalStateException ex) {
            caching.copyBodyToResponse();
            return;
        }

        rawResponse.setStatus(status);
        for (HeaderToReplay h : headersToReplay) {
            rawResponse.addHeader(h.name(), h.value());
        }

        rawResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        rawResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        rawResponse.setContentLength(jsonBody.length);
        rawResponse.getOutputStream().write(jsonBody);
        rawResponse.getOutputStream().flush();
    }

    private record HeaderToReplay(String name, String value) {
    }

    private static boolean skipHeaderOnReplay(String name) {
        return SKIP_ON_REPLAY.contains(name.toLowerCase(Locale.ROOT));
    }
}
