package org.acme.ping.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acme.ping.http.HttpHeaderMaps;
import org.acme.ping.http.HttpHeaderObfuscation;
import org.acme.security.properties.AppSecurityProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/debug/headers")
@RequiredArgsConstructor
public class HeadersDebugController {

    private final AppSecurityProperties appSecurity;

    record HeadersDump(
            String method,
            String requestUri,
            String queryString,
            Map<String, List<String>> requestHeaders,
            int responseStatus,
            Map<String, List<String>> responseHeaders
    ) {}

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HeadersDump> dump(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("X-Debug-Echo", "1");

        // responseStatus / responseHeaders are placeholders; HeadersDebugResponseFinalizeFilter
        // rewrites the JSON body after the full chain so they match the final response (curl -i).
        HeadersDump body = new HeadersDump(
                request.getMethod(),
                request.getRequestURI(),
                Objects.requireNonNullElse(request.getQueryString(), ""),
                HttpHeaderObfuscation.obfuscateValues(
                        HttpHeaderMaps.fromRequest(request), appSecurity.obfuscatedHeaderNames()),
                0,
                Map.of());

        log.debug("Headers dump {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(body);
    }
}
