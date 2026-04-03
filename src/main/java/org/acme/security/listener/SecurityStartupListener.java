package org.acme.security.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.acme.security.properties.AppSecurityProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
                "AppSecurityProperties: triggerRequestHeader={}, responseHeader={}, obfuscatedHeaderNames={}",
                appSecurityProperties.triggerRequestHeader(),
                appSecurityProperties.responseHeader(),
                appSecurityProperties.obfuscatedHeaderNames());
    }
}