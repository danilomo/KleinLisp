/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 *
 * @author danilo
 */
public final class BooleanObject implements LispObject {
    
    public static final BooleanObject TRUE  = new BooleanObject(true);
    public static final BooleanObject FALSE = new BooleanObject(true);

    private boolean value;

    public BooleanObject(boolean value) {
        this.value = value;
    }

    @Override
    public Object asObject() {
        return value;
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
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean error() {
        return false;
    }

}
