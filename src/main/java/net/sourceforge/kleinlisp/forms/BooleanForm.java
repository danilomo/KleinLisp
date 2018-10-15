/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.forms;

import net.sourceforge.kleinlisp.Form;
import java.util.Optional;

/**
 *
 * @author danilo
 */
public class BooleanForm implements Form {

    private boolean value;

    public BooleanForm(boolean value) {
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
    public Optional<ListForm> asList() {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    public Form evaluate() {
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
    public Optional<FunctionForm> asFunction() {
        return Optional.empty();
    }        
}
