package com.dockyard.java17features.service;

import com.dockyard.java17features.domain.Shape;
import com.dockyard.java17features.domain.Vehicle;
import com.dockyard.java17features.dto.SealedHierarchy;
import org.springframework.stereotype.Service;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * SealedService — sealed classes and interfaces (JEP 409, standard in
 * <b>Java 17</b>) let a type declare EXACTLY which types may extend or
 * implement it.
 *
 * <h2>The problem it solves</h2>
 * Before sealing you had two blunt options: {@code final} (nobody may extend)
 * or open (anybody may extend). There was no way to say "these three types
 * and no others." That middle ground is what modelling a domain properly
 * needs — a payment is CARD or UPI or NETBANKING, and nothing else.
 *
 * <h2>Why the compiler cares</h2>
 * A sealed hierarchy is a CLOSED set, so the compiler can check a
 * {@code switch} over it for EXHAUSTIVENESS. Cover every permitted subtype
 * and no {@code default} branch is needed; add a fourth subtype later and
 * every such switch fails to compile until it's handled. A {@code default}
 * branch would have swallowed the new case silently at runtime — that
 * compile-time nudge is the entire payoff.
 *
 * <h2>The rules</h2>
 * <ul>
 *   <li>Every permitted subtype must be in the same MODULE (same PACKAGE if
 *       in the unnamed module) and must directly extend/implement the sealed
 *       type.</li>
 *   <li>Every permitted subtype must itself be {@code final},
 *       {@code sealed}, or {@code non-sealed} — there is no unspecified
 *       option. See {@link Vehicle}.</li>
 *   <li>{@code permits} may be omitted if all subtypes are in the same
 *       source file (they're then inferred).</li>
 *   <li>{@code sealed}, {@code permits} and {@code non-sealed} are
 *       CONTEXTUAL keywords — an existing variable named {@code sealed}
 *       still compiles.</li>
 * </ul>
 */
@Service
public class SealedService {

    /** Reflects over a sealed interface to list the closed set the compiler knows about. */
    public SealedHierarchy shapeHierarchy() {
        return SealedHierarchy.builder()
                .type(Shape.class.getName())
                .sealed(Shape.class.isSealed())
                .permittedSubclasses(Arrays.stream(Shape.class.getPermittedSubclasses())
                        .map(Class::getSimpleName)
                        .toList())
                .subclassModifiers(Arrays.stream(Shape.class.getPermittedSubclasses())
                        .map(c -> c.getSimpleName() + " -> record (records are implicitly final, which satisfies the sealed rule)")
                        .toList())
                .whyItMatters("A closed set of three shapes means a switch over Shape can be checked for exhaustiveness — no default branch needed, and adding a 4th shape breaks the build until every switch handles it.")
                .build();
    }

    /** The final / sealed / non-sealed rule, shown on a sealed abstract CLASS. */
    public SealedHierarchy vehicleHierarchy() {
        List<String> modifiers = Arrays.stream(Vehicle.class.getPermittedSubclasses())
                .map(c -> {
                    String kind = c.isSealed() ? "sealed"
                            : Modifier.isFinal(c.getModifiers()) ? "final"
                            : "non-sealed";
                    return c.getSimpleName() + " -> " + kind;
                })
                .toList();

        return SealedHierarchy.builder()
                .type(Vehicle.class.getName())
                .sealed(Vehicle.class.isSealed())
                .permittedSubclasses(Arrays.stream(Vehicle.class.getPermittedSubclasses())
                        .map(Class::getSimpleName)
                        .toList())
                .subclassModifiers(modifiers)
                .whyItMatters("Every permitted subtype MUST pick one of final (hierarchy stops), sealed (continues, still controlled) or non-sealed (deliberately reopened). There is no unspecified option — the compiler forces the decision.")
                .build();
    }

    /**
     * EXHAUSTIVE switch over a sealed type — note the complete absence of a
     * {@code default} branch. The compiler accepts this only because
     * {@link Shape} is sealed and all three permitted subtypes are covered.
     *
     * <p><b>Version note:</b> sealing itself is final in Java 17, but the
     * TYPE PATTERNS in a {@code switch} used below ({@code case Shape.Circle c ->})
     * were a PREVIEW feature in 17 (JEP 406) and only became standard in
     * Java 21 (JEP 441). This repo compiles with Java 21, so it runs as
     * written; on a strict Java 17 compiler you'd need {@code --enable-preview}
     * or a chain of {@code if (shape instanceof Shape.Circle c)} instead.
     * Sealing was designed hand-in-hand with pattern matching — this is what
     * it was always building towards.</p>
     */
    public List<String> exhaustiveSwitch() {
        List<Shape> shapes = List.of(
                new Shape.Circle(2),
                new Shape.Square(3),
                new Shape.Rectangle(2, 5));

        return shapes.stream()
                .map(shape -> {
                    // No 'default' — exhaustiveness is PROVEN by the sealed hierarchy.
                    // Add a 4th Shape and this switch stops compiling. That's the point.
                    String label = switch (shape) {
                        case Shape.Circle c -> "Circle(r=" + c.radius() + ")";
                        case Shape.Square s -> "Square(side=" + s.side() + ")";
                        case Shape.Rectangle r -> "Rectangle(" + r.width() + "x" + r.height() + ")";
                    };
                    return label + " area=" + String.format("%.3f", shape.area());
                })
                .toList();
    }

    /** Common misconceptions and compile errors around sealing. */
    public List<String> pitfalls() {
        return List.of(
                "class Rogue implements Shape {}          // COMPILE ERROR — Shape is sealed and does not permit Rogue",
                "sealed interface Shape permits Circle {} // COMPILE ERROR if Circle is in a different module (or a different package in the unnamed module)",
                "sealed class A permits B {} class B extends A {}  // COMPILE ERROR — B must declare final, sealed, or non-sealed",
                "A permitted subtype must extend the sealed type DIRECTLY — you cannot permit a grandchild",
                "'sealed', 'permits' and 'non-sealed' are CONTEXTUAL keywords — 'int sealed = 1;' still compiles",
                "Sealing is a COMPILE-TIME and RUNTIME guarantee: the permitted list is stored in the class file (see Class.getPermittedSubclasses()), so it can't be bypassed by a hand-rolled class file either",
                "Sealed is about MODELLING a closed set, not about security — use it where 'this is one of exactly N things' is a real domain fact"
        );
    }

}