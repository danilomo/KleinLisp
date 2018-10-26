/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import java.util.Optional;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author daolivei
 */
public abstract class LispObjDecorator implements LispObject{

    @Override
    public Object asObject() {
        return evaluate().asObject();
    }

    @Override
    public boolean truthness() {
        return evaluate().truthness();
    }

    @Override
    public Optional<Integer> asInt() {
        return evaluate().asInt();
    }

    @Override
    public Optional<Double> asDouble() {
        return evaluate().asDouble();
    }

    @Override
    public Optional<ListObject> asList() {
        return evaluate().asList();
    }

    @Override
    public Optional<FunctionObject> asFunction() {
        return evaluate().asFunction();
    }

    @Override
    public Optional<AtomObject> asAtom() {
        return evaluate().asAtom();
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return evaluate().asObject(clazz);
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return evaluate().accept(visitor);
    }

    @Override
    public boolean error() {
        return evaluate().error();
    }

    @Override
    public boolean isBoolean() {
        return evaluate().isBoolean();
    }

    @Override
    public boolean isAtom() {
        return evaluate().isAtom();
    }

    @Override
    public boolean isString() {
        return evaluate().isString();
    }

    @Override
    public boolean isNumeric() {
        return evaluate().isNumeric();
    }

    @Override
    public boolean isDouble() {
        return evaluate().isDouble();
    }

    @Override
    public boolean isInt() {
        return evaluate().isInt();
    }

    @Override
    public boolean isList() {
        return evaluate().isList();
    }

    @Override
    public boolean isObject() {
        return evaluate().isObject();
    }

    @Override
    public boolean isVoid() {
        return evaluate().isVoid();
    }

    @Override
    public boolean isFunction() {
        return evaluate().isFunction();
    }
    
    
}
