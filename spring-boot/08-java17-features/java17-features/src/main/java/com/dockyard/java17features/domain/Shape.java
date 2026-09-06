package com.dockyard.java17features.domain;

/**
 * Shape — a SEALED INTERFACE (JEP 409, Java 17) whose implementations are a
 * closed, compiler-known set: {@code Circle}, {@code Square},
 * {@code Rectangle}. Nothing outside this file can implement it.
 *
 * <h2>Why sealed matters</h2>
 * An ordinary {@code interface} is open — anyone, anywhere, can implement
 * it, so the compiler can never know the full list of subtypes. A SEALED
 * interface names its permitted implementations up front, which turns the
 * hierarchy into an algebraic "one of these N things" type. That's what lets
 * a {@code switch} over {@code Shape} be checked for EXHAUSTIVENESS: cover
 * all three and you need no {@code default} branch, and if a fourth shape is
 * ever added, every such switch stops compiling until you handle it. An
 * unchecked {@code default} branch would have silently swallowed it at
 * runtime instead.
 *
 * <h2>The permits clause is optional here</h2>
 * Because all permitted subtypes are declared in the SAME file, the
 * {@code permits} clause could be omitted and inferred. It is written out
 * explicitly for teaching value. Permitted subtypes must be in the same
 * module (or same package, for the unnamed module).
 *
 * <h2>Records + sealed = the classic pairing</h2>
 * Each implementation is a {@code record}, so the whole hierarchy is
 * immutable data with generated {@code equals}/{@code hashCode}/
 * {@code toString}. Records are implicitly {@code final}, which satisfies
 * sealed's rule that every permitted subtype must be {@code final},
 * {@code sealed} or {@code non-sealed}.
 */
public sealed interface Shape permits Shape.Circle, Shape.Square, Shape.Rectangle {

    /** Every shape can report its area — the switch demos compute it via pattern matching instead. */
    double area();

    record Circle(double radius) implements Shape {
        public Circle {
            if (radius <= 0) {
                throw new IllegalArgumentException("radius must be > 0, was " + radius);
            }
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    record Square(double side) implements Shape {
        public Square {
            if (side <= 0) {
                throw new IllegalArgumentException("side must be > 0, was " + side);
            }
        }

        @Override
        public double area() {
            return side * side;
        }
    }

    record Rectangle(double width, double height) implements Shape {
        public Rectangle {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("width and height must be > 0, were " + width + " x " + height);
            }
        }

        @Override
        public double area() {
            return width * height;
        }
    }
}