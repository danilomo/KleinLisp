/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.Optional;

/**
 * @author daolivei
 */
public abstract class LispObjDecorator implements LispObject {

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


}
