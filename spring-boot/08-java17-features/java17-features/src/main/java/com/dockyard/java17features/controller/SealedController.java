package com.dockyard.java17features.controller;

import com.dockyard.java17features.dto.Java17DemoResponse;
import com.dockyard.java17features.dto.SealedHierarchy;
import com.dockyard.java17features.service.SealedService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SealedController — sealed classes and interfaces (JEP 409, Java 17). See
 * {@link SealedService} for the detailed "why".
 */
@RestController
@RequestMapping("/java17/sealed")
@RequiredArgsConstructor
@Validated
public class SealedController {

    private final SealedService service;

    @GetMapping("/shape-hierarchy")
    public Java17DemoResponse<SealedHierarchy> shapeHierarchy() {
        return Java17DemoResponse.<SealedHierarchy>builder()
                .operation("sealed interface — a CLOSED set of implementations (Java 17, JEP 409)")
                .description("Before sealing you had only 'final' (nobody may extend) or open (anybody may). Sealing adds the missing middle ground: exactly these types and no others. The permitted list is recorded in the class file, so Class.getPermittedSubclasses() can read it back at runtime — as this endpoint does.")
                .codeSnippet("public sealed interface Shape permits Shape.Circle, Shape.Square, Shape.Rectangle { double area(); }")
                .result(service.shapeHierarchy())
                .build();
    }

    @GetMapping("/vehicle-hierarchy")
    public Java17DemoResponse<SealedHierarchy> vehicleHierarchy() {
        return Java17DemoResponse.<SealedHierarchy>builder()
                .operation("final / sealed / non-sealed — every permitted subtype must pick one")
                .description("There is no 'unspecified' option: the compiler forces each permitted subtype to declare whether the hierarchy STOPS (final), CONTINUES under control (sealed), or is deliberately REOPENED (non-sealed). 'non-sealed' is the only hyphenated keyword in Java, and sealed/permits/non-sealed are all CONTEXTUAL keywords.")
                .codeSnippet("abstract sealed class Vehicle permits Car, Truck, Motorcycle { }  // Car final, Truck sealed, Motorcycle non-sealed")
                .result(service.vehicleHierarchy())
                .build();
    }

    @GetMapping("/exhaustive-switch")
    public Java17DemoResponse<List<String>> exhaustiveSwitch() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("exhaustive switch over a sealed type — no default branch")
                .description("Because the compiler knows the complete set of subtypes, covering all of them is PROVABLY exhaustive and no default branch is needed. Add a fourth Shape and this switch stops compiling until you handle it — a default branch would have swallowed the new case silently at runtime instead. (Type patterns in switch were preview in 17, standard in 21; this repo compiles with Java 21.)")
                .codeSnippet("switch (shape) { case Circle c -> ...; case Square s -> ...; case Rectangle r -> ...; }  // no default")
                .result(service.exhaustiveSwitch())
                .build();
    }

    @GetMapping("/pitfalls")
    public Java17DemoResponse<List<String>> pitfalls() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("sealing rules and misconceptions")
                .description("Permitted subtypes must be in the same module (same package in the unnamed module), must extend the sealed type DIRECTLY, and must each declare final/sealed/non-sealed. Sealing is a modelling tool for 'this is one of exactly N things' — not a security mechanism.")
                .codeSnippet("class Rogue implements Shape { } // ERROR — Shape is sealed and does not permit Rogue")
                .result(service.pitfalls())
                .build();
    }

}