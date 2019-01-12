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
    
}
