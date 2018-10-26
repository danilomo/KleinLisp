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
public class VoidObject implements LispObject {

    public static final VoidObject VOID = new VoidObject();
    
    @Override
    public Object asObject() {
        return this;
    }

    @Override
    public boolean truthness() {
        return false;
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
    public Optional<ListObject> asList() {
        return Optional.empty();
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
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    public LispObject evaluate() {
       return this;
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
       return false;
    }

    @Override
    public boolean isDouble() {
       return false;
    }

    @Override
    public boolean isInt() {
       return false;
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
       return true;
    }

    @Override
    public boolean isFunction() {
       return false;
    }
    
}