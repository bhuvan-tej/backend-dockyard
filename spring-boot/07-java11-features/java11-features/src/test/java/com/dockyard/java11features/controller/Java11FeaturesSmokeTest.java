package com.dockyard.java11features.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Java11FeaturesSmokeTest — one request per controller, proving every demo
 * endpoint actually responds 200 rather than throwing at runtime.
 */
@SpringBootTest
@AutoConfigureMockMvc
class Java11FeaturesSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void varEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java11/var/local-variable")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/var/enhanced-for-loop")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/var/lambda-params?a=3&b=4")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/var/try-with-resources")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/var/pitfalls")).andExpect(status().isOk());
    }

    @Test
    void stringEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java11/string/is-blank?value=   ")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/string/strip-variants")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/string/repeat?value=ab&count=3")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/string/lines")).andExpect(status().isOk());
    }

    @Test
    void functionalEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java11/functional/predicate-not?input=   ")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/functional/optional-is-empty?value=hi")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/functional/optional-is-empty")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/functional/collection-to-array?items=alpha,beta")).andExpect(status().isOk());
    }

    @Test
    void nioEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java11/nio/path-of")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/nio/read-write-string?content=hello")).andExpect(status().isOk());
    }

    @Test
    void httpClientEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java11/httpclient/synchronous?value=hi")).andExpect(status().isOk());
        mockMvc.perform(get("/java11/httpclient/asynchronous?value=hi")).andExpect(status().isOk());
    }
}

