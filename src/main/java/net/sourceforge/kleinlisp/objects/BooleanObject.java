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
 * @author danilo
 */
public class BooleanObject implements LispObject {

    private boolean value;

    public BooleanObject(boolean value) {
        this.value = value;
    }        
    
    @Override
    public Object asObject() {
        return new Boolean(value);
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
    public Optional<AtomObject> asAtom() {
        return Optional.empty();
    }        

    @Override
    public LispObject evaluate() {
        return this;
    }

    @Override
    public boolean truthness() {
        return value;
    } 

    @Override
    public String toString() {
        return "" + value;
    }
   
    @Override
    public Optional<FunctionObject> asFunction() {
        return Optional.empty();
    }        
}
