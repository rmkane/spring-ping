package org.acme.ping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.acme.ping.api.HeadersDebugJsonFields;
import org.acme.ping.app.App;

@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void debugHeadersReturnsJsonWithFinalizedResponseFields() throws Exception {
        mockMvc.perform(
                get("/api/debug/headers")
                        .header("X-Debug-Token", "test-secret")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + HeadersDebugJsonFields.RESPONSE_STATUS).value(200))
                .andExpect(jsonPath("$." + HeadersDebugJsonFields.RESPONSE_HEADERS).exists())
                .andExpect(jsonPath("$.method").value("GET"));
    }
}
