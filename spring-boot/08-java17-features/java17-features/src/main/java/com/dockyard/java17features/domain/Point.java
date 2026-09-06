package com.dockyard.java17features.domain;

/**
 * Point — a RECORD (JEP 395, Java 16): a transparent carrier for immutable
 * data. This one declaration makes the compiler generate, automatically:
 *
 * <ul>
 *   <li>{@code private final} fields {@code x} and {@code y};</li>
 *   <li>a canonical constructor {@code Point(int x, int y)};</li>
 *   <li>accessors named {@code x()} and {@code y()} — note: NOT
 *       {@code getX()}/{@code getY()}; records are not JavaBeans;</li>
 *   <li>{@code equals}/{@code hashCode} derived from ALL components, so two
 *       points with the same x and y are equal;</li>
 *   <li>{@code toString()} → {@code Point[x=3, y=4]}.</li>
 * </ul>
 *
 * <h2>The compact constructor</h2>
 * The {@code public Point { ... }} block below has NO parameter list and NO
 * assignments. It runs BEFORE the implicit field assignments, which lets you
 * validate or NORMALISE the incoming parameters (reassigning the parameter
 * variable changes what gets stored). It is the idiomatic place for
 * invariants, and it's the single most-asked record detail in interviews.
 *
 * <h2>What a record cannot do</h2>
 * It cannot extend another class (it already extends {@link Record}), it
 * cannot declare instance fields beyond its components, and it is implicitly
 * {@code final}. It CAN implement interfaces, declare static fields/methods,
 * add extra instance methods, and override any generated member.
 */
public record Point(int x, int y) {

    /** Records may declare STATIC fields — just not extra instance fields. */
    public static final Point ORIGIN = new Point(0, 0);

    /**
     * Compact constructor — validation only. No parameter list, no explicit
     * {@code this.x = x} assignments; the compiler appends those for you
     * using the (possibly reassigned) parameter values.
     */
    public Point {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Point components must be >= 0, got (" + x + ", " + y + ")");
        }
    }

    /** A STATIC FACTORY — a common addition, often paired with a private-ish canonical constructor. */
    public static Point of(int x, int y) {
        return new Point(x, y);
    }

    /** A DERIVED value — computed from the components, not stored as an extra field. */
    public double distanceFromOrigin() {
        return Math.sqrt((double) x * x + (double) y * y);
    }
}