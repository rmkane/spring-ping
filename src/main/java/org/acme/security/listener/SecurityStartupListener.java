package org.acme.security.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.acme.security.properties.AppSecurityProperties;

/**
 * Logs a snapshot of security header configuration at application startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityStartupListener {

    private final AppSecurityProperties appSecurityProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void logAtStartup() {
        log.debug(
                "AppSecurityProperties: triggerRequestHeader={}, responseHeader={}, debugHeadersTokenHeader={}, debugHeadersTokenQueryParam={}, debugHeadersAccessConfigured={}, obfuscatedHeaderNameCount={}",
                appSecurityProperties.triggerRequestHeader(),
                appSecurityProperties.responseHeader(),
                appSecurityProperties.debugHeadersTokenHeader(),
                appSecurityProperties.debugHeadersTokenQueryParam(),
                appSecurityProperties.debugHeadersAccessConfigured(),
                appSecurityProperties.namesForObfuscation().size());
    }
}