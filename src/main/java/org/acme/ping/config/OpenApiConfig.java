package org.acme.ping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.acme.security.properties.AppSecurityProperties;

@Configuration
public class OpenApiConfig {

    /** Name referenced by {@code @SecurityRequirement} on protected operations. */
    public static final String DEBUG_TOKEN_HEADER_SCHEME = "debugTokenHeader";
    public static final String DEBUG_TOKEN_QUERY_SCHEME = "debugTokenQuery";

    @Bean
    OpenAPI openAPI(AppSecurityProperties appSecurity) {
        String headerName = appSecurity.debugHeadersTokenHeader();
        String queryName = appSecurity.debugHeadersTokenQueryParam();
        return new OpenAPI()
                .info(new Info().title("Ping API").version("1.0.0"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        DEBUG_TOKEN_HEADER_SCHEME,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.HEADER)
                                                .name(headerName)
                                                .description(
                                                        "Same secret as DEBUG_HEADERS_ACCESS_TOKEN / app.security.debug-headers-access-token. "
                                                                + "Set once via **Authorize**; applies to locked operations. Alternative: `?%s=…`."
                                                                        .formatted(queryName)))
                                .addSecuritySchemes(
                                        DEBUG_TOKEN_QUERY_SCHEME,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.QUERY)
                                                .name(queryName)
                                                .description(
                                                        "Same value as the header token; use **Authorize** to set once for try-it-out requests.")));
    }
}
