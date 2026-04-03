package org.acme.security.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.acme.security.properties.AppSecurityProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionalResponseHeaderFilter extends OncePerRequestFilter {

    private final AppSecurityProperties appSecurity;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String value = request.getHeader(appSecurity.triggerRequestHeader());
        if (StringUtils.hasText(value)) {
            response.setHeader(appSecurity.responseHeader(), value);
            log.debug(
                    "Set response header {}={} (trigger {})",
                    appSecurity.responseHeader(),
                    value,
                    appSecurity.triggerRequestHeader());
        }
        filterChain.doFilter(request, response);
    }
}
