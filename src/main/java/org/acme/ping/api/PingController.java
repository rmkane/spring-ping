package org.acme.ping.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/ping", produces = MediaType.APPLICATION_JSON_VALUE)
public class PingController {

    record PingResponse(String message) {
    }

    @GetMapping
    public ResponseEntity<PingResponse> ping() {
        return ResponseEntity.ok(new PingResponse("pong"));
    }
}
