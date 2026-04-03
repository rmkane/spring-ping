package org.acme.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import org.acme.security.properties.AppSecurityProperties;

/**
 * Requires {@link AppSecurityProperties#debugHeadersAccessToken()} (one shared
 * secret). The client may send that value either as the
 * {@link AppSecurityProperties#debugHeadersTokenHeader()} header or, if the
 * header is absent/blank, as the
 * {@link AppSecurityProperties#debugHeadersTokenQueryParam()} query value —
 * same string, not a second credential.
 */
@Component
@RequiredArgsConstructor
public class DebugHeadersTokenAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final AppSecurityProperties props;

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        if (!props.debugHeadersAccessConfigured()) {
            return new AuthorizationDecision(false);
        }
        String provided = firstNonBlank(
                context.getRequest().getHeader(props.debugHeadersTokenHeader()),
                context.getRequest().getParameter(props.debugHeadersTokenQueryParam()));
        byte[] a = provided.getBytes(StandardCharsets.UTF_8);
        byte[] b = props.debugHeadersAccessToken().getBytes(StandardCharsets.UTF_8);
        return new AuthorizationDecision(MessageDigest.isEqual(a, b));
    }

    private static String firstNonBlank(String header, String query) {
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        if (query != null && !query.isBlank()) {
            return query.trim();
        }
        return "";
    }
}
