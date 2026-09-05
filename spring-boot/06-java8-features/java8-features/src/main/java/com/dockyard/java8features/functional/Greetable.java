package com.dockyard.java8features.functional;

/**
 * Greetable — a plain interface with ONE abstract method plus a
 * {@code default} method, used to demonstrate that Java 8 interfaces can now
 * ship behavior, not just a contract. Implementers get {@link #greet()} for
 * free but may override it.
 */
public interface Greetable {

    String name();

    /**
     * A default method — a method body living directly on the interface.
     * Before Java 8, adding a new method to a widely-implemented interface
     * broke every existing implementation; default methods let library
     * authors evolve an interface (this is exactly how {@code Iterable.forEach}
     * and {@code Collection.stream()} were retrofitted onto every collection
     * ever written, without breaking them).
     */
    default String greet() {
        return "Hello, " + name() + "!";
    }

    /**
     * A static method — a utility method that belongs to the interface
     * itself, not to any implementing instance. Called as
     * {@code Greetable.formalGreeting(name)}, never via an instance, and
     * cannot be overridden by implementers.
     */
    static String formalGreeting(String name) {
        return "Good day, " + name + ".";
    }

}