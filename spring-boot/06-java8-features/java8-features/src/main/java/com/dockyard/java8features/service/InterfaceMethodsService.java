package com.dockyard.java8features.service;

import com.dockyard.java8features.functional.FlyingFish;
import com.dockyard.java8features.functional.Greetable;
import org.springframework.stereotype.Service;

/**
 * InterfaceMethodsService — Java 8 let interfaces carry executable code, not
 * just abstract signatures, via two new kinds of interface member:
 *
 * <ul>
 *   <li>{@code default} methods — an instance method WITH a body, inherited
 *       by every implementer that doesn't override it. Purpose-built to let
 *       library authors add methods to existing interfaces without breaking
 *       every class that already implements them (the textbook example:
 *       {@code Collection.stream()} and {@code Iterable.forEach()} were both
 *       added this way, retroactively, to interfaces implemented by
 *       thousands of pre-existing classes).</li>
 *   <li>{@code static} methods — a utility method that belongs to the
 *       interface TYPE itself, called as {@code InterfaceName.method()}, never
 *       via an instance, and not inherited/overridable by implementers.</li>
 * </ul>
 *
 * <h2>The diamond problem</h2>
 * If a class implements two interfaces that both declare a default method
 * with the identical signature, Java refuses to silently pick one — it's a
 * COMPILE ERROR until the implementing class overrides the method itself,
 * typically resolving it via {@code InterfaceName.super.method()}. See
 * {@link FlyingFish} for the worked example.
 */
@Service
public class InterfaceMethodsService {

    /** A default method, used as-is without any override. */
    public String defaultMethodAsIs() {
        Greetable greetable = () -> "Ada"; // Greetable's sole abstract method is name() -> String
        return greetable.greet(); // greet() is a DEFAULT method — never implemented by this lambda, yet available
    }

    /** The same default method, explicitly overridden by a different implementer. */
    public String defaultMethodOverridden() {
        Greetable formalGreetable = new Greetable() {
            @Override
            public String name() { return "Grace"; }
            @Override
            public String greet() { return "Distinguished colleague " + name() + "."; } // overrides the default
        };
        return formalGreetable.greet();
    }

    /** A static interface method — called on the interface TYPE, not an instance. */
    public String staticInterfaceMethod(String name) {
        return Greetable.formalGreeting(name); // NOT someGreetable.formalGreeting(name) — static methods aren't inherited
    }

    /** The diamond problem: two interfaces, same default method signature, forced explicit resolution. */
    public String diamondProblemResolution() {
        FlyingFish fish = new FlyingFish();
        // FlyingFish.move() MUST override Flyer.move()/Swimmer.move() — the class would not
        // compile otherwise, because Java can't guess which default implementation you meant.
        return "A flying fish " + fish.move() + " (resolved explicitly via Flyer.super.move() + Swimmer.super.move()).";
    }

}