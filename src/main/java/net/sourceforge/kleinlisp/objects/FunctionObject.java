/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

import java.util.Optional;

/**
 * @author daolivei
 */
public final class FunctionObject implements LispObject {

    private final Function function;

    public FunctionObject(Function function) {
        this.function = function;
    }

    public Function function() {
        return function;
    }

    public Object object() {
        return function;
    }

    @Override
    public String toString() {
        return "Function: " + function.toString();
    }

    @Override
    public Object asObject() {
        return this;
    }

    @Override
    public boolean truthiness() {
        return true;
    }

    @Override
    public Optional<FunctionObject> asFunction() {
        return Optional.of(this);
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
