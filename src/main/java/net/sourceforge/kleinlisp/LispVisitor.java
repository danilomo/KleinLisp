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
 * @param <T>
 */
public interface LispVisitor<T> {
    public T visit(AtomObject obj);
    public T visit(BooleanObject obj);
    public T visit(DoubleObject obj);
    public T visit(IntObject obj);
    public T visit(JavaObject obj);
    public T visit(ListObject obj);
    public T visit(StringObject obj);
    public T visit(FunctionObject obj);
    
    public static class Void{}
    public static Void NONE = new Void();
}
