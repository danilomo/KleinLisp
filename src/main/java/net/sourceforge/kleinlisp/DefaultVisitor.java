/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.JavaObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;

/**
 *
 * @author daolivei
 */
public class DefaultVisitor implements LispVisitor<LispObject>{

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
    public LispObject visit(ListObject obj) {
        if (obj.tail() != ListObject.NIL) {
            return new ListObject(obj.head().accept(this), obj.tail().accept(this));
        } else {
            return new ListObject(obj.head().accept(this), ListObject.NIL );
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
    
}
