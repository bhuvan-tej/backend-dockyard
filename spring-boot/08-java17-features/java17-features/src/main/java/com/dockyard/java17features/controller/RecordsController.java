package com.dockyard.java17features.controller;

import com.dockyard.java17features.dto.Java17DemoResponse;
import com.dockyard.java17features.dto.RecordSummary;
import com.dockyard.java17features.dto.ValidationOutcome;
import com.dockyard.java17features.service.RecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RecordsController — records (JEP 395, Java 16). See {@link RecordsService}
 * for the detailed "why".
 */
@RestController
@RequestMapping("/java17/records")
@RequiredArgsConstructor
@Validated
public class RecordsController {

    private final RecordsService service;

    @GetMapping("/generated-members")
    public Java17DemoResponse<RecordSummary> generatedMembers() {
        return Java17DemoResponse.<RecordSummary>builder()
                .operation("record — what the compiler generates for you (Java 16, JEP 395)")
                .description("One 'record Point(int x, int y)' declaration generates private final fields, a canonical constructor, component accessors named x()/y() (NOT getX()/getY()), and value-based equals/hashCode/toString. This endpoint reads it all back at runtime with Class.getRecordComponents(), so the claim is demonstrated rather than asserted. Note the superclass is always java.lang.Record and the class is implicitly final.")
                .codeSnippet("public record Point(int x, int y) { }  // then: new Point(3,4).equals(new Point(3,4)) == true")
                .result(service.generatedMembers())
                .build();
    }

    @GetMapping("/compact-constructor")
    public Java17DemoResponse<List<ValidationOutcome>> compactConstructor(
            @RequestParam(defaultValue = "3") int x,
            @RequestParam(defaultValue = "4") int y) {
        return Java17DemoResponse.<List<ValidationOutcome>>builder()
                .operation("compact constructor — where a record's invariants live")
                .description("A compact constructor has NO parameter list and NO field assignments. It runs BEFORE the implicit assignments, so reassigning a parameter changes what actually gets stored — making it the idiomatic spot for validation and normalisation. The second attempt below always uses (-1, 5) so you can see the invariant reject it.")
                .codeSnippet("public record Point(int x, int y) { public Point { if (x < 0 || y < 0) throw new IllegalArgumentException(...); } }")
                .result(service.compactConstructor(x, y))
                .build();
    }

    @GetMapping("/shallow-immutability")
    public Java17DemoResponse<List<String>> shallowImmutability() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("records are SHALLOWLY immutable — the defensive-copy trap")
                .description("A record's generated fields are final, which prevents REASSIGNMENT but not mutation of what they point at. Pass in a mutable List and keep a reference to it, and you can change the 'immutable' record from outside — unless the compact constructor copies it first, as this one does.")
                .codeSnippet("public Reservation { guests = List.copyOf(guests); }  // copy the PARAMETER, before the field is assigned")
                .result(service.shallowImmutability())
                .build();
    }

    @GetMapping("/local-record")
    public Java17DemoResponse<List<String>> localRecord(
            @RequestParam(defaultValue = "spring, java, records, sealed, api") String csv) {
        return Java17DemoResponse.<List<String>>builder()
                .operation("local records — declared inside a method (Java 16)")
                .description("A record can be declared locally, scoped to a single method. It is the clean replacement for the throwaway tuple class you used to write (or the Object[] / Map.Entry hack) just to carry an intermediate result through a stream pipeline.")
                .codeSnippet("record WordStat(String word, int length) { }  // declared INSIDE the method, used only there")
                .result(service.localRecord(csv))
                .build();
    }

    @GetMapping("/limitations")
    public Java17DemoResponse<List<String>> limitations() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("what a record CANNOT do")
                .description("These are compile errors (or bad ideas), so this endpoint documents them as text rather than executing them — no extending another class, no extra instance fields, implicitly final, and no field assignment inside a compact constructor. Records also make poor JPA entities, which need a no-arg constructor and mutable fields.")
                .codeSnippet("record Point(int x, int y) extends Base { } // ERROR — a record already extends java.lang.Record")
                .result(service.limitations())
                .build();
    }

}