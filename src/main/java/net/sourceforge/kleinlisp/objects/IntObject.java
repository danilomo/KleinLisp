/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

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
        return !(value == 0);
    }

    @Override
    public Optional<FunctionObject> asFunction() {
        return Optional.empty();
    }

    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.empty();
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean error() {
        return false;
    }
    
    @Override
    public boolean isBoolean() {
        return false;
    }
        
    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }  
    
    @Override
    public boolean isFunction() {
        return false;
    }    
}
