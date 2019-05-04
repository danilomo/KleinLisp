/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

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
    public Optional<AtomObject> asAtom() {
        return Optional.of(this);
    }
    
    

    @Override
    public boolean truthness() {
        return true;
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
