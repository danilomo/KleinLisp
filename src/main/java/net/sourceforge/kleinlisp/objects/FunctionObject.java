/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.objects;

import java.util.Optional;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.functions.ListFunctions;

/**
 *
 * @author daolivei
 */
public final class FunctionObject implements LispObject {

    private final Function function;

    public FunctionObject(Function function) {
        this.function = function;
    }

    public Function function() {
        return function;
    }

    public Object object() {
        return function;
    }

    @Override
    public String toString() {
        return "Function: " + function.toString();
    }

    @Override
    public Object asObject() {
        return this;
    }

    @Override
    public LispObject evaluate() {
        return this;
    }

    @Override
    public boolean truthness() {
        return true;
    }

    @Override
    public Optional<FunctionObject> asFunction() {
        return Optional.of(this);
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public LispObject call(Object... args) {

        ListObject list = ListObject.NIL;

        for (Object obj : args) {

            LispObject parameter;

            if (obj instanceof Integer) {
                parameter = new IntObject((int) obj);
            } else if (obj instanceof Double) {
                parameter = new DoubleObject((double) obj);
            } else {
                parameter = new JavaObject(obj);
            }

            list = new ListObject(parameter, list);
        }

        list = ListFunctions.reverse(list).asList().get();

        return function.evaluate(list);
    }

    public LispObject call(LispObject... args) {

        ListObject list = ListObject.NIL;

        for (int i = args.length - 1; i >= 0; i--) {
            list = new ListObject(args[i], list);
        }

        return function.evaluate(list);
    }

    @Override
    public boolean error() {
        return false;
    }

}
