/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

import java.util.Optional;

/**
 * @author daolivei
 */
public final class IntObject implements NumericObject {

    public final int value;

    public IntObject(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public int toInt() {
        return value;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public Object asObject() {
        return value;
    }

    @Override
    public LispObject evaluate() {
        return this;
    }

    @Override
    public Optional<Integer> asInt() {
        return Optional.of(value);
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.of((double) value);
    }


    @Override
    public boolean truthness() {
        return !(value == 0);
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
