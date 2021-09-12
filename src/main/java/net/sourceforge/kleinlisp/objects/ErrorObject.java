/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * @author daolivei
 */
public final class ErrorObject implements LispObject {

    private final Exception error;

    public ErrorObject(String message) {
        this.error = new Exception(message);
    }

    public ErrorObject(Exception error) {
        this.error = error;
    }

    @Override
    public Object asObject() {
        return error;
    }

    @Override
    public boolean truthness() {
        throw new RuntimeException("Cannot evaluate error as boolean");
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
    public String toString() {
        return "LispError: " + error.getMessage();
    }

    @Override
    public boolean error() {
        return true;
    }

}
