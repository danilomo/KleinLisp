/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

import java.util.Optional;
import net.sourceforge.kleinlisp.special_forms.SpecialFormEnum;

/**
 * @author daolivei
 */
public final class AtomObject implements LispObject {

    private final Environment env;
    private SpecialFormEnum specialForm;
    
    AtomObject(Environment env, SpecialFormEnum specialForm) {
        this.env = env;
        this.specialForm = specialForm;
    }    

    public Environment environment() {
        return env;
    }

    public String value() {
        return env.valueOf(this);
    }

    @Override
    public String toString() {
        return value();
    }

    @Override
    public Object asObject() {
        return value();
    }

    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.of(this);
    }

    @Override
    public boolean truthiness() {
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

    public SpecialFormEnum specialForm() {
        return specialForm;
    }

    
}
