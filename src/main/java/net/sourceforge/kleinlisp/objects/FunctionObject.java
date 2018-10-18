/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 *
 * @author daolivei
 */
public class FunctionObject implements LispObject {

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
    public LispObject evaluate() {
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
    public Optional<ListObject> asList() {
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
    public Optional<FunctionObject> asFunction() {
        return Optional.of(this);
    }
    
    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.empty();
    }   
    
    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }
  
}