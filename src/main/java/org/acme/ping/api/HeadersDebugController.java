package org.acme.ping.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.acme.ping.config.OpenApiConfig;
import org.acme.ping.http.HttpHeaderMaps;
import org.acme.ping.http.HttpHeaderObfuscation;
import org.acme.ping.http.SensitiveQueryRedaction;
import org.acme.security.properties.AppSecurityProperties;

@Slf4j
@RestController
@RequestMapping("/api/debug")
@SecurityRequirements({
        @SecurityRequirement(name = OpenApiConfig.DEBUG_TOKEN_HEADER_SCHEME),
        @SecurityRequirement(name = OpenApiConfig.DEBUG_TOKEN_QUERY_SCHEME)
})
@RequiredArgsConstructor
public class HeadersDebugController {

    private final AppSecurityProperties appSecurity;

    record HeadersDump(
            String method,
            String requestUri,
            String queryString,
            Map<String, List<String>> requestHeaders,
            int responseStatus,
            Map<String, List<String>> responseHeaders) {
    }

    @RequestMapping(path = "headers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HeadersDump> dump(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("X-Debug-Echo", "1");

        // responseStatus / responseHeaders are placeholders;
        // HeadersDebugResponseFinalizeFilter
        // rewrites the JSON body after the full chain so they match the final response
        // (curl -i).
        String queryRaw = Objects.requireNonNullElse(request.getQueryString(), "");
        String queryForClient = appSecurity.debugHeadersAccessConfigured()
                ? SensitiveQueryRedaction.redactQueryParameter(
                        queryRaw, appSecurity.debugHeadersTokenQueryParam())
                : queryRaw;

        HeadersDump body = new HeadersDump(
                request.getMethod(),
                request.getRequestURI(),
                queryForClient,
                HttpHeaderObfuscation.obfuscateValues(
                        HttpHeaderMaps.fromRequest(request), appSecurity.namesForObfuscation()),
                0,
                Map.of());

        log.debug("Headers dump {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.ok(body);
    }
}
