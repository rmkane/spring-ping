package org.acme.security;

import org.acme.security.filter.ConditionalResponseHeaderFilter;
import org.acme.security.filter.LogFilter;
import org.acme.security.properties.AppSecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AppSecurityProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ConditionalResponseHeaderFilter conditionalResponseHeaderFilter;
    private final LogFilter logFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(conditionalResponseHeaderFilter, AuthorizationFilter.class)
                // Outermost: log full exchange (request as received, response after chain).
                .addFilterBefore(logFilter, ConditionalResponseHeaderFilter.class);
        return http.build();
    }
}
