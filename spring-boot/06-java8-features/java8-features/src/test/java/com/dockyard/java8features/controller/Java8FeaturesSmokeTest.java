package com.dockyard.java8features.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Java8FeaturesSmokeTest — one request per controller, proving every demo
 * endpoint actually responds 200 rather than throwing at runtime.
 */
@SpringBootTest
@AutoConfigureMockMvc
class Java8FeaturesSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void lambdaEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java8/lambda/no-arg")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/lambda/single-param?name=Ada")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/lambda/two-arg/expression-body?a=3&b=4")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/lambda/two-arg/block-body?a=3&b=4")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/lambda/effectively-final-capture?message=hi")).andExpect(status().isOk());
    }

    @Test
    void functionalInterfaceEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java8/functional-interfaces/predicate?value=4")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/functional-interfaces/function?input=hello")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/functional-interfaces/supplier")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/functional-interfaces/consumer?text=hello")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/functional-interfaces/bifunction?a=3&b=4")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/functional-interfaces/custom?a=3&b=4")).andExpect(status().isOk());
    }

    @Test
    void methodReferenceEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java8/method-references/static-method?number=42")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/method-references/bound-instance-method?text=world")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/method-references/unbound-instance-method?words=alpha,beta")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/method-references/constructor?seeds=alpha,beta")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/method-references/two-arg-unbound?a=Hello&b=HELLO")).andExpect(status().isOk());
    }

    @Test
    void interfaceMethodsEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java8/interface-methods/default-method")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/interface-methods/default-method-overridden")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/interface-methods/static-method?name=Linus")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/interface-methods/diamond-problem")).andExpect(status().isOk());
    }

    @Test
    void optionalEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java8/optional/creation-variants?value=hi")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/optional/creation-variants")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/optional/map-filter-pipeline?value=  hello  ")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/optional/or-else-vs-or-else-get")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/optional/or-else-throw?value=hi")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/optional/if-present?value=hi")).andExpect(status().isOk());
    }

    @Test
    void dateTimeEndpointsRespond() throws Exception {
        mockMvc.perform(get("/java8/datetime/local-date")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/datetime/local-time")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/datetime/local-date-time")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/datetime/period-vs-duration?start=2026-01-01&end=2026-03-15&startTime=09:00&endTime=17:30"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/java8/datetime/formatter?pattern=yyyy-MM-dd")).andExpect(status().isOk());
        mockMvc.perform(get("/java8/datetime/zoned-and-instant")).andExpect(status().isOk());
    }
}

