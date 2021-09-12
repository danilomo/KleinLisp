/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.*;

/**
 * @param <T>
 * @author daolivei
 */
public interface LispVisitor<T> {
    Void NONE = new Void();

    T visit(AtomObject obj);

    T visit(BooleanObject obj);

    T visit(DoubleObject obj);

    T visit(IntObject obj);

    T visit(JavaObject obj);

    T visit(ListObject obj);

    T visit(StringObject obj);

    T visit(FunctionObject obj);

    T visit(ErrorObject obj);

    T visit(VoidObject obj);

    class Void {
    }
}
