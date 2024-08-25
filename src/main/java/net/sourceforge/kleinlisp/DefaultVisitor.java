/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.*;

/**
 * @author daolivei
 */
public class DefaultVisitor implements LispVisitor<LispObject> {

    @Override
    public LispObject visit(AtomObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(BooleanObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(DoubleObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(IntObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(JavaObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(VoidObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(ListObject obj) {

        if (obj == ListObject.NIL) {
            return obj;
        }

        if (obj.tail() != ListObject.NIL) {
            return new ListObject(obj.head().accept(this), obj.tail().accept(this));
        } else {
            return new ListObject(obj.head().accept(this), ListObject.NIL);
        }
    }

    @Override
    public LispObject visit(StringObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(FunctionObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(ErrorObject obj) {
        return obj;
    }

    @Override
    public LispObject visit(ComputedLispObject obj) {
        return obj;
    }

}
