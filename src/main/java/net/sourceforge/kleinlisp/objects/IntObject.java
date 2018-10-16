/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author daolivei
 */
public final class IntObject implements NumericObject {

    private final int value;

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
        return new Integer(value);
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
    public Optional<ListObject> asList() {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }
    

    @Override
    public boolean truthness() {
        return ! (value == 0) ;
    }  

    @Override
    public Optional<FunctionObject> asFunction() {
        return Optional.empty();
    }   
    
    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.empty();
    }     
}
