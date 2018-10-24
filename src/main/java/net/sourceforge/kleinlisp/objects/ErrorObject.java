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
    public Optional<FunctionObject> asFunction() {
        return Optional.empty();
    }

    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
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

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isAtom() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isInt() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }
}
