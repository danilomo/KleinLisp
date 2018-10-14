/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 *
 * @author daolivei
 */
public final class DoubleForm implements NumericForm {

    private final double value;

    public DoubleForm(double value) {
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
    public Form evaluate() {
        return this;
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
    public Optional<ListForm> asList() {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }
    

    @Override
    public boolean truthness() {
        return true;
    }    
}
