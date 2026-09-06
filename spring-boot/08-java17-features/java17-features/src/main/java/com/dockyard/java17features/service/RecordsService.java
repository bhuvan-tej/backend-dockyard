package com.dockyard.java17features.service;

import com.dockyard.java17features.domain.Point;
import com.dockyard.java17features.domain.Reservation;
import com.dockyard.java17features.dto.RecordSummary;
import com.dockyard.java17features.dto.ValidationOutcome;
import org.springframework.stereotype.Service;

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RecordsService — records (JEP 395, standard in <b>Java 16</b>) are
 * "transparent carriers for immutable data": you declare the STATE, and the
 * compiler derives the API from it.
 *
 * <h2>What one line generates</h2>
 * {@code record Point(int x, int y) {}} produces private final fields, a
 * canonical constructor, component accessors ({@code x()}, {@code y()} —
 * NOT {@code getX()}), and value-based {@code equals}/{@code hashCode}/
 * {@code toString}. That's roughly 40 lines of hand-written boilerplate — or
 * a Lombok {@code @Value} — replaced by a language feature.
 *
 * <h2>The hard rules</h2>
 * <ul>
 *   <li>Implicitly {@code final} and implicitly extends {@link Record}, so a
 *       record can NEVER extend another class. It CAN implement interfaces.</li>
 *   <li>No additional INSTANCE fields — the components are the entire state.
 *       Static fields and methods are fine.</li>
 *   <li>Fields are final, so a record is <b>shallowly</b> immutable: a
 *       mutable component (a {@code List}, an array, a {@code Date}) still
 *       needs a defensive copy — see {@link Reservation}.</li>
 * </ul>
 *
 * <h2>Record vs Lombok @Data — the interview framing</h2>
 * A record is a LANGUAGE feature (no annotation processor, understood by the
 * compiler, javadoc, IDEs and pattern matching) and is immutable by design.
 * Lombok's {@code @Data} generates mutable getters/setters via an annotation
 * processor. Use a record for immutable value/DTO types; you still need a
 * regular class for JPA entities, which require a no-arg constructor and
 * mutable fields.
 */
@Service
public class RecordsService {

    /** Reads back, via reflection, what the compiler actually generated for a record. */
    public RecordSummary generatedMembers() {
        Point a = new Point(3, 4);
        Point b = new Point(3, 4);   // a DIFFERENT object with the SAME components

        List<String> components = Arrays.stream(Point.class.getRecordComponents())
                .map(rc -> rc.getType().getSimpleName() + " " + rc.getName())
                .toList();

        List<String> generated = new ArrayList<>();
        for (RecordComponent rc : Point.class.getRecordComponents()) {
            generated.add(rc.getAccessor().getName() + "() -> " + rc.getType().getSimpleName() + "  (accessor, NOT get" + capitalize(rc.getName()) + "())");
        }
        generated.add("Point(int, int)  (canonical constructor)");
        generated.add("equals(Object) / hashCode()  (derived from ALL components)");
        generated.add("toString()  (derived from ALL components)");

        return RecordSummary.builder()
                .recordClass(Point.class.getName())
                .components(components)
                .generatedMembers(generated)
                .toStringValue(a.toString())                      // Point[x=3, y=4]
                .equalsByValue(a.equals(b))                       // true — value semantics, not identity
                .hashCodesMatch(a.hashCode() == b.hashCode())     // true — required by the equals contract
                .isFinal(Modifier.isFinal(Point.class.getModifiers()))  // records are implicitly final
                .superclass(Point.class.getSuperclass().getName())      // always java.lang.Record
                .build();
    }

    /**
     * The COMPACT CONSTRUCTOR — no parameter list, no assignments. It runs
     * before the implicit field assignments, so it's where invariants live.
     * Here the same construction is attempted twice to show accept vs reject.
     */
    public List<ValidationOutcome> compactConstructor(int x, int y) {
        List<ValidationOutcome> outcomes = new ArrayList<>();
        outcomes.add(attempt(x, y));
        outcomes.add(attempt(-1, 5));   // always fails — proves the invariant is enforced
        return outcomes;
    }

    private ValidationOutcome attempt(int x, int y) {
        try {
            Point p = Point.of(x, y);
            return ValidationOutcome.builder()
                    .attempted("Point.of(" + x + ", " + y + ")")
                    .accepted(true)
                    .value(p + " — distanceFromOrigin=" + String.format("%.3f", p.distanceFromOrigin()))
                    .build();
        } catch (IllegalArgumentException ex) {
            return ValidationOutcome.builder()
                    .attempted("Point.of(" + x + ", " + y + ")")
                    .accepted(false)
                    .rejectionMessage(ex.getMessage())
                    .build();
        }
    }

    /**
     * Shallow immutability — mutating the list that was passed IN must not
     * change the record, because the compact constructor copied it.
     */
    public List<String> shallowImmutability() {
        List<String> mutable = new ArrayList<>(List.of("alice", "bob"));
        Reservation reservation = new Reservation("Table 4", mutable);

        mutable.add("gatecrasher");   // mutating the caller's original list...

        return List.of(
                "Caller's original list after mutation : " + mutable,
                "Record's guests() after that mutation : " + reservation.guests(),
                "Unchanged, because the compact constructor did List.copyOf(guests) BEFORE the field was assigned.",
                "WITHOUT that defensive copy, the record's final field would point at the SAME list object and 'gatecrasher' would appear here too.",
                "Records are SHALLOWLY immutable: final fields stop REASSIGNMENT, not mutation of what they point at."
        );
    }

    /** Records can be declared LOCALLY — inside a method — since Java 16. */
    public List<String> localRecord(String csv) {
        // A local record: scoped to this method only. Perfect for an intermediate
        // tuple in a stream pipeline that would otherwise need a throwaway class.
        record WordStat(String word, int length) { }

        return Arrays.stream(csv.split(","))
                .map(String::strip)
                .filter(w -> !w.isBlank())
                .map(w -> new WordStat(w, w.length()))
                .sorted((l, r) -> Integer.compare(r.length(), l.length()))
                .map(WordStat::toString)
                .toList();
    }

    /** Restrictions — none of these compile, so this documents rather than executes them. */
    public List<String> limitations() {
        return List.of(
                "record Point(int x, int y) extends Base {}   // COMPILE ERROR — a record already extends java.lang.Record; it cannot extend anything else",
                "record Point(int x, int y) { int z; }        // COMPILE ERROR — no instance fields beyond the components (static fields ARE allowed)",
                "final record Point(int x, int y) {}          // REDUNDANT — records are implicitly final; you cannot make one non-final or abstract",
                "record Point(int x, int y) { public Point { this.x = x; } }  // COMPILE ERROR — a compact constructor must not assign to the fields",
                "@Entity record User(Long id, String name) {} // BAD IDEA — JPA needs a no-arg constructor and mutable fields; use a class for entities",
                "record Point(int x, int y) implements Serializable {}  // LEGAL — records cannot extend, but they CAN implement interfaces"
        );
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}