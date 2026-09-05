package com.dockyard.java8features.functional;

/**
 * Flyer / Swimmer / FlyingFish — the classic "diamond problem" demo.
 *
 * Both {@link Flyer} and {@link Swimmer} declare a DEFAULT method with the
 * SAME signature, {@code move()}. A class implementing both interfaces
 * inherits two conflicting default implementations — Java refuses to guess
 * which one you meant and forces a compile error UNLESS the class explicitly
 * overrides the method, typically delegating to one or both parents via
 * {@code InterfaceName.super.method()}.
 *
 * This is Java's deliberate, safer answer to C++'s multiple-inheritance
 * diamond problem: state can never be inherited this way (interfaces have no
 * fields), and behavior conflicts must be resolved explicitly, in code, at
 * the implementing class — never silently.
 */
public interface Flyer {
    default String move() {
        return "flies through the air";
    }
}