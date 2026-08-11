package com.dockyard.javastreams.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StreamsSmokeTest — one request per controller, proving every category of
 * endpoint is wired up correctly and returns the {@code StreamDemoResponse}
 * shape (operation/description/codeSnippet/result).
 */
@SpringBootTest
@AutoConfigureMockMvc
class StreamsSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void creationEndpointWorks() throws Exception {
        mockMvc.perform(get("/streams/creation/int-range").param("start", "0").param("end", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.codeSnippet").exists());
    }

    @Test
    void intermediateEndpointWorks() throws Exception {
        mockMvc.perform(get("/streams/intermediate/filter").param("department", "Sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("filter"));
    }

    @Test
    void terminalEndpointWorks() throws Exception {
        mockMvc.perform(get("/streams/terminal/count").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    void collectorsEndpointWorks() throws Exception {
        mockMvc.perform(get("/streams/collectors/groupingby-counting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.Engineering").value(5));
    }

    @Test
    void parallelCompareEndpointWorks() throws Exception {
        mockMvc.perform(get("/streams/parallel/compare").param("elements", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.elementCount").value(500));
    }

    @Test
    void modernEndpointWorks() throws Exception {
        mockMvc.perform(get("/streams/modern/teeing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.count").value(20));
    }

    @Test
    void primitivesEndpointWorks() throws Exception {
        mockMvc.perform(get("/streams/primitives/maptoint-sum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isNumber());
    }

    @Test
    void outOfRangeParamIsRejected() throws Exception {
        mockMvc.perform(get("/streams/creation/generate").param("count", "99999"))
                .andExpect(status().isBadRequest());
    }
}

