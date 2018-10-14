/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import java.util.Optional;

/**
 *
 * @author daolivei
 */
public class FunctionForm implements Form {

    private final Function function;

    public FunctionForm(Function function) {
        this.function = function;
    }

    public Object object() {
        return function;
    }

    @Override
    public String toString() {
        return function.toString();
    }

    @Override
    public Object asObject() {
        return this;
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
}
