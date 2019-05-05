/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 *
 * @author daolivei
 */
public final class DoubleObject implements NumericObject {

    private final double value;

    public DoubleObject(double value) {
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public int toInt() {
        return (int) value;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public Object asObject() {
        return value;
    }

    @Override
    public Optional<Integer> asInt() {
        return Optional.of((int) value);
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.of(value);
    }


    @Override
    public boolean truthness() {
        return true;
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean error() {
        return false;
    }

}
