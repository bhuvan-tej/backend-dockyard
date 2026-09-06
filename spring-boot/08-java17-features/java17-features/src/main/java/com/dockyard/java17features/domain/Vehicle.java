package com.dockyard.java17features.domain;

/**
 * Vehicle — a SEALED ABSTRACT CLASS demonstrating the rule that trips people
 * up in interviews: <b>every permitted subtype must itself declare one of
 * {@code final}, {@code sealed} or {@code non-sealed}</b>. There is no
 * "unspecified" option — the compiler forces you to say, explicitly, whether
 * the hierarchy stops here, continues in a still-controlled way, or is
 * deliberately reopened.
 *
 * <ul>
 *   <li>{@link Car} is {@code final} — the hierarchy STOPS here.</li>
 *   <li>{@link Truck} is {@code sealed} — the hierarchy CONTINUES, but still
 *       under control ({@code permits PickupTruck}).</li>
 *   <li>{@link Motorcycle} is {@code non-sealed} — the hierarchy is
 *       deliberately REOPENED; anyone may now extend {@code Motorcycle}.
 *       This is the escape hatch that keeps sealing from being all-or-nothing.</li>
 * </ul>
 *
 * Note that {@code non-sealed} is the only hyphenated keyword in Java, and
 * that {@code sealed}/{@code permits}/{@code non-sealed} are CONTEXTUAL
 * keywords — a variable named {@code sealed} still compiles fine.
 */
public abstract sealed class Vehicle permits Vehicle.Car, Vehicle.Truck, Vehicle.Motorcycle {

    private final String name;

    protected Vehicle(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    /** final — this branch of the hierarchy is closed for good. */
    public static final class Car extends Vehicle {
        public Car(String name) {
            super(name);
        }
    }

    /** sealed — the hierarchy continues, but only in ways declared right here. */
    public abstract static sealed class Truck extends Vehicle permits Truck.PickupTruck {
        protected Truck(String name) {
            super(name);
        }

        public static final class PickupTruck extends Truck {
            public PickupTruck(String name) {
                super(name);
            }
        }
    }

    /** non-sealed — deliberately reopened; any class anywhere may extend this. */
    public static non-sealed class Motorcycle extends Vehicle {
        public Motorcycle(String name) {
            super(name);
        }
    }

}