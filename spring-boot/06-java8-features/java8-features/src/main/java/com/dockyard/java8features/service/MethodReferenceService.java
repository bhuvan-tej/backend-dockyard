package com.dockyard.java8features.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * MethodReferenceService — a method reference is shorthand for a lambda that
 * does nothing but call an existing method. The JDK recognizes four distinct
 * shapes:
 *
 * <table>
 *   <caption>The four method reference kinds</caption>
 *   <tr><th>Kind</th><th>Syntax</th><th>Equivalent lambda</th></tr>
 *   <tr><td>1. Static method</td><td>{@code ClassName::staticMethod}</td><td>{@code args -> ClassName.staticMethod(args)}</td></tr>
 *   <tr><td>2. Bound instance method</td><td>{@code particularObject::instanceMethod}</td><td>{@code args -> particularObject.instanceMethod(args)}</td></tr>
 *   <tr><td>3. Unbound instance method</td><td>{@code ClassName::instanceMethod}</td><td>{@code (obj, args) -> obj.instanceMethod(args)}</td></tr>
 *   <tr><td>4. Constructor</td><td>{@code ClassName::new}</td><td>{@code args -> new ClassName(args)}</td></tr>
 * </table>
 *
 * The compiler picks the right kind purely from the target functional
 * interface's method signature — the SAME {@code ClassName::method} syntax
 * can be kind 1 or kind 3 depending on context.
 */
@Service
public class MethodReferenceService {

    /** Kind 1 — static method reference: ClassName::staticMethod. */
    public int staticMethodReference(String number) {
        Function<String, Integer> parser = Integer::parseInt; // same as: s -> Integer.parseInt(s)
        return parser.apply(number);
    }

    /** Kind 2 — bound instance method reference: a SPECIFIC, already-existing object's method. */
    public String boundInstanceMethodReference(String text) {
        String greeting = "Hello, " + text;      // 'greeting' is a particular, already-constructed instance
        Supplier<String> upperCaser = greeting::toUpperCase; // same as: () -> greeting.toUpperCase()
        return upperCaser.get();
    }

    /**
     * Kind 3 — unbound instance method reference: {@code ClassName::instanceMethod},
     * where the instance to call it on becomes the FIRST parameter of the
     * functional interface, supplied at call time — not fixed in advance.
     */
    public List<String> unboundInstanceMethodReference(List<String> words) {
        Function<String, String> upperCaser = String::toUpperCase; // same as: s -> s.toUpperCase()
        List<String> result = new ArrayList<>();
        for (String w : words) {
            result.add(upperCaser.apply(w)); // a DIFFERENT 'w' supplies the instance each time
        }
        return result;
    }

    /** Kind 4 — constructor reference: ClassName::new. */
    public List<StringBuilder> constructorReference(List<String> seeds) {
        Function<String, StringBuilder> builderFactory = StringBuilder::new; // same as: s -> new StringBuilder(s)
        List<StringBuilder> result = new ArrayList<>();
        for (String seed : seeds) {
            result.add(builderFactory.apply(seed));
        }
        return result;
    }

    /** A two-argument unbound instance method reference — ClassName::instanceMethod with a BiFunction. */
    public int twoArgUnboundReference(String a, String b) {
        BiFunction<String, String, Boolean> equalsIgnoreCase = String::equalsIgnoreCase; // (s1, s2) -> s1.equalsIgnoreCase(s2)
        return equalsIgnoreCase.apply(a, b) ? 1 : 0;
    }

}