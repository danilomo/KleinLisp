/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author daolivei
 */
public class Main6 {

    public static void main(String[] args) {
        String code = "(lambda (x) (lambda () (begin (set! x (+ x 1)) x)))";
        Lisp lisp = new Lisp();

        FunctionObject counter = lisp.evaluate(code).asFunction().get();

        FunctionObject myCounter = counter.call(1).asFunction().get();

        System.out.println(myCounter.call());
        System.out.println(myCounter.call());
        System.out.println(myCounter.call());
        System.out.println(myCounter.call());
        System.out.println(myCounter.call());

        FunctionObject myCounter2 = counter.call(5000).asFunction().get();

        System.out.println(myCounter2.call());
        System.out.println(myCounter2.call());
        System.out.println(myCounter2.call());
        System.out.println(myCounter2.call());
        System.out.println(myCounter2.call());

    }

    public static LispObject call(FunctionObject function, int arg) {
        return function.function().evaluate(new ListObject(new IntObject(arg), ListObject.NIL));
    }

    public static LispObject call(FunctionObject function) {
        return function.function().evaluate(ListObject.NIL);
    }
}
