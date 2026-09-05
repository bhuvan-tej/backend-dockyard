package com.dockyard.java8features.controller;

import com.dockyard.java8features.dto.Java8DemoResponse;
import com.dockyard.java8features.service.InterfaceMethodsService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * InterfaceMethodsController — default & static interface methods, and the
 * diamond problem. See {@link InterfaceMethodsService} for the detailed "why".
 */
@RestController
@RequestMapping("/java8/interface-methods")
@RequiredArgsConstructor
@Validated
public class InterfaceMethodsController {

    private final InterfaceMethodsService service;

    @GetMapping("/default-method")
    public Java8DemoResponse<String> defaultMethod() {
        return Java8DemoResponse.<String>builder()
                .operation("A default method, used as-is")
                .description("The lambda implementing Greetable only supplies name() — greet() is a DEFAULT method with a body already on the interface, inherited for free.")
                .codeSnippet("interface Greetable { String name(); default String greet() { return \"Hello, \" + name() + \"!\"; } }")
                .result(service.defaultMethodAsIs())
                .build();
    }

    @GetMapping("/default-method-overridden")
    public Java8DemoResponse<String> defaultMethodOverridden() {
        return Java8DemoResponse.<String>builder()
                .operation("The same default method, explicitly overridden")
                .description("A default method is just an inherited default — any implementer is free to override it with its own behavior, exactly like overriding any other inherited method.")
                .codeSnippet("new Greetable() { public String name() {...} public String greet() { return \"Distinguished colleague \" + name(); } }")
                .result(service.defaultMethodOverridden())
                .build();
    }

    @GetMapping("/static-method")
    public Java8DemoResponse<String> staticMethod(
            @RequestParam(defaultValue = "Linus") @NotBlank String name) {
        return Java8DemoResponse.<String>builder()
                .operation("A static interface method")
                .description("Called on the INTERFACE TYPE (Greetable.formalGreeting(...)), never through an instance — static interface methods are not inherited and cannot be overridden by implementers.")
                .codeSnippet("static String formalGreeting(String name) { return \"Good day, \" + name + \".\"; } // called as Greetable.formalGreeting(name)")
                .result(service.staticInterfaceMethod(name))
                .build();
    }

    @GetMapping("/diamond-problem")
    public Java8DemoResponse<String> diamondProblem() {
        return Java8DemoResponse.<String>builder()
                .operation("The diamond problem: two interfaces, one conflicting default method")
                .description("Flyer and Swimmer both declare a default move() method. FlyingFish implements both, so it is FORCED to override move() (a compile error otherwise) — here resolved by combining both parents via InterfaceName.super.method().")
                .codeSnippet("class FlyingFish implements Flyer, Swimmer { public String move() { return Flyer.super.move() + \" AND \" + Swimmer.super.move(); } }")
                .result(service.diamondProblemResolution())
                .build();
    }

}