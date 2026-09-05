package com.dockyard.java11features.service;

import com.dockyard.java11features.dto.CollectionToArrayResult;
import com.dockyard.java11features.dto.PredicateNotResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * FunctionalService — small Java 11 additions to existing functional and
 * collection types that didn't warrant a whole new class, but come up
 * constantly in code review and interviews.
 *
 * <ul>
 *   <li>{@code Predicate.not(Predicate)} — a static factory that negates a
 *       predicate. Its real purpose is negating a METHOD REFERENCE directly
 *       (e.g. {@code Predicate.not(String::isBlank)}) — a method reference
 *       has no {@code .negate()} to call until it's already been assigned to
 *       a {@code Predicate} variable.</li>
 *   <li>{@code Optional.isEmpty()} — the logical inverse of
 *       {@code isPresent()}, added purely for readability:
 *       {@code if (opt.isEmpty())} reads better than
 *       {@code if (!opt.isPresent())}.</li>
 *   <li>{@code Collection.toArray(IntFunction<T[]> generator)} — lets you
 *       write {@code list.toArray(String[]::new)} instead of the older,
 *       easy-to-get-wrong {@code list.toArray(new String[0])} (the JIT
 *       actually optimizes the zero-length-array idiom well, but the
 *       generator form reads more clearly about intent).</li>
 * </ul>
 */
@Service
public class FunctionalService {

    /** Predicate.not(predicate) vs predicate.negate() — same result, different ergonomics. */
    public PredicateNotResult predicateNot(String input) {
        Predicate<String> isBlank = String::isBlank;

        boolean viaNegate = isBlank.negate().test(input);           // requires isBlank to already be a Predicate variable
        boolean viaStaticNot = Predicate.not(isBlank).test(input);  // works the same on a bare method reference too

        return PredicateNotResult.builder()
                .input(input)
                .viaNegate(viaNegate)
                .viaStaticNot(viaStaticNot)
                .note("Both negate the SAME predicate and give identical results. Predicate.not(...) matters most when negating "
                        + "a method reference directly, e.g. list.stream().filter(Predicate.not(String::isBlank)) — "
                        + "String::isBlank has no .negate() to call until it's already stored as a Predicate.")
                .build();
    }

    /** Optional.isEmpty() — the readable inverse of isPresent(), added in Java 11. */
    public String optionalIsEmpty(String value) {
        Optional<String> optional = Optional.ofNullable(value);
        return String.format("Optional.ofNullable(%s) -> isPresent()=%s, isEmpty()=%s (isEmpty is just '!isPresent()', added purely for readability)",
                value == null ? "null" : "\"" + value + "\"", optional.isPresent(), optional.isEmpty());
    }

    /** Collection.toArray(IntFunction<T[]>) — Java 11's cleaner way to convert a Collection to a typed array. */
    public CollectionToArrayResult collectionToArrayGenerator(List<String> items) {
        String[] array = items.toArray(String[]::new); // the generator allocates the array of exactly the right size and type

        return CollectionToArrayResult.builder()
                .sourceCollection(items)
                .arrayRuntimeType(array.getClass().getSimpleName())
                .arrayLength(array.length)
                .arrayContents(List.of(array))
                .note("toArray(String[]::new) reads clearly as 'give me a String[]' — the IntFunction generator is called with "
                        + "the collection's size and creates a properly-sized, correctly-typed array in one step.")
                .build();
    }

}