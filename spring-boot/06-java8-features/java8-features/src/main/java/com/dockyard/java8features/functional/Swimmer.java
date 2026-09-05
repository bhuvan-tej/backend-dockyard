package com.dockyard.java8features.functional;

/** See {@link Flyer} for the full explanation of this diamond-problem demo. */
public interface Swimmer {
    default String move() {
        return "swims through the water";
    }
}

