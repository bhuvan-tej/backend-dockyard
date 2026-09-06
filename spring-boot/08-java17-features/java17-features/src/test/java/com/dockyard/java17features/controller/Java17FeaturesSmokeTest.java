package com.dockyard.java17features.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Java17FeaturesSmokeTest — one request per controller, proving every demo
 * endpoint actually responds 200 rather than throwing at runtime. A few
 * assertions go further and pin the behaviour the feature is teaching (a
 * record's value equality, a sealed type's permitted set, Stream.toList()
 * being unmodifiable), so a regression in the lesson itself fails the build.
 */
@SpringBootTest
@AutoConfigureMockMvc
class Java17FeaturesSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void recordEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java17/records/generated-members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.equalsByValue").value(true))
                .andExpect(jsonPath("$.result.isFinal").value(true))
                .andExpect(jsonPath("$.result.superclass").value("java.lang.Record"));
        mockMvc.perform(get("/java17/records/compact-constructor?x=3&y=4")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/records/shallow-immutability")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/records/local-record?csv=alpha,beta")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/records/limitations")).andExpect(status().isOk());
    }

    @Test
    void sealedEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java17/sealed/shape-hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sealed").value(true))
                .andExpect(jsonPath("$.result.permittedSubclasses.length()").value(3));
        mockMvc.perform(get("/java17/sealed/vehicle-hierarchy")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/sealed/exhaustive-switch")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/sealed/pitfalls")).andExpect(status().isOk());
    }

    @Test
    void patternMatchingEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java17/pattern-matching/instanceof")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/pattern-matching/flow-scoping?value=dockyard")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/pattern-matching/flow-scoping?value=42")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/pattern-matching/switch-expression?day=saturday"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.output").value("WEEKEND"));
        mockMvc.perform(get("/java17/pattern-matching/yield?month=5")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/pattern-matching/sealed-dispatch")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/pattern-matching/pitfalls")).andExpect(status().isOk());
    }

    @Test
    void textBlockEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java17/text-blocks/incidental-whitespace")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/text-blocks/escapes")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/text-blocks/formatted?name=Bhuvan&count=3")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/text-blocks/pitfalls")).andExpect(status().isOk());
    }

    @Test
    void apiAdditionEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java17/api/stream-to-list"))
                .andExpect(status().isOk())
                // The whole lesson: toList() is unmodifiable, Collectors.toList() is not.
                .andExpect(jsonPath("$.result.streamToListIsModifiable").value(false))
                .andExpect(jsonPath("$.result.collectorsToListIsModifiable").value(true));
        mockMvc.perform(get("/java17/api/teeing?values=1,2,3,4")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/api/teeing")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/api/string-helpers?input=dockyard")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/api/pitfalls")).andExpect(status().isOk());
    }

    @Test
    void jdkAdditionEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java17/jdk/helpful-npe")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/jdk/random-generator?algorithm=L64X128MixRandom&seed=42&count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.values.length()").value(5));
        mockMvc.perform(get("/java17/jdk/compact-numbers")).andExpect(status().isOk());
        mockMvc.perform(get("/java17/jdk/pitfalls")).andExpect(status().isOk());
    }
}

