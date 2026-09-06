package com.dockyard.java17features.domain;

import java.util.List;

/**
 * Reservation — demonstrates the record pitfall interviewers love:
 * <b>a record is SHALLOWLY immutable</b>. The generated fields are
 * {@code final}, so the REFERENCE can't be reassigned — but if a component
 * is a mutable object (here, a {@code List}), the object it points at can
 * still be mutated from the outside unless you defend against it.
 *
 * Two defences are applied here:
 * <ol>
 *   <li>The compact constructor copies the incoming list into an
 *       unmodifiable one, so a caller who keeps a reference to the original
 *       list cannot mutate what this record holds.</li>
 *   <li>The {@code guests()} accessor is OVERRIDDEN to hand back an
 *       unmodifiable view, so a caller can't mutate through the accessor
 *       either. (With defence #1 already in place this is belt-and-braces,
 *       but it's the general pattern for a mutable component such as
 *       {@code Date} or an array, where you'd return a copy.)</li>
 * </ol>
 *
 * Without defence #1, {@code new Reservation("A", mutableList)} followed by
 * {@code mutableList.add("gatecrasher")} would silently change the
 * "immutable" record.
 */
public record Reservation(String name, List<String> guests) {

    public Reservation {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        // Defensive COPY — not just List.copyOf of the field later on, but of the
        // incoming parameter, before the compiler assigns it to the final field.
        guests = guests == null ? List.of() : List.copyOf(guests);
    }

    /** Overriding a generated accessor is legal — useful for defensive copies. */
    @Override
    public List<String> guests() {
        return List.copyOf(guests);
    }
}