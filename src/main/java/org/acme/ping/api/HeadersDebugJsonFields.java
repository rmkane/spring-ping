package org.acme.ping.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JSON property names patched by
 * {@link org.acme.ping.filter.HeadersDebugResponseFinalizeFilter} onto the
 * headers debug endpoint body. Must match {@code HeadersDump} record field
 * names as serialized by Jackson.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeadersDebugJsonFields {

    public static final String RESPONSE_STATUS = "responseStatus";
    public static final String RESPONSE_HEADERS = "responseHeaders";
}
