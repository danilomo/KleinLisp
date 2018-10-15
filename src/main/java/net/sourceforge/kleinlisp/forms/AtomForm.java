/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.forms;

import net.sourceforge.kleinlisp.Form;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 *
 * @author daolivei
 */
public final class AtomForm implements Form {

    private final String value;

    public AtomForm(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
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
        return Optional.empty();
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.empty();
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
    
    @Override
    public Optional<FunctionForm> asFunction() {
        return Optional.empty();
    }    
}
