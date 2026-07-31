package com.dockyard.springai.dto;

import java.util.List;

/**
 * The structured-output target. When you call {@code .entity(CountryFacts.class)},
 * Spring AI asks the model to reply as JSON shaped like this record, then parses
 * it for you — you get a real Java object, never a String you have to unpick.
 *
 * @param name        the country's common name
 * @param capital     its capital city
 * @param population  approximate population
 * @param languages   its official language(s)
 */
public record CountryFacts(
        String name,
        String capital,
        long population,
        List<String> languages
) {}