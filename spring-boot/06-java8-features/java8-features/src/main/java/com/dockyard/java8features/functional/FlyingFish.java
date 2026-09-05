package com.dockyard.java8features.functional;

/**
 * FlyingFish — implements both {@link Flyer} and {@link Swimmer}, which
 * declare conflicting default {@code move()} methods. Java FORCES this class
 * to override {@code move()} explicitly (it will not compile otherwise) —
 * here we resolve it by combining both parents via
 * {@code InterfaceName.super.method()}, which is the only way to reach a
 * specific interface's default implementation once you've overridden it.
 */
public class FlyingFish implements Flyer, Swimmer {

    @Override
    public String move() {
        return Flyer.super.move() + " AND " + Swimmer.super.move();
    }

}