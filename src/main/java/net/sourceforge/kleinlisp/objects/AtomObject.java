/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author daolivei
 */
public final class AtomObject implements LispObject {

    private final String value;
    private Environment env;
    
    public AtomObject(String value) {
        this.value = value;
    }

    public AtomObject(String value, Environment env) {
        this.value = value;
        this.env = env;
    } 

    public Environment environment() {
        return env;
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
        return Optional.empty();
    } 
    
    @Override
    public LispObject evaluate() {
        try {
            return env.lookup(value);
        } catch (Exception e) {
            System.out.println(">>> " + value);
            e.printStackTrace();
            throw e;
        }
    } 
    
    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.of(this);
    }     
}
