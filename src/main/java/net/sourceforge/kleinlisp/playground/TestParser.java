/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;

import java.util.List;

/**
 * @author danilo
 */
public class TestParser {
    public static void main(String[] args) {
        Lisp l = new Lisp();

        AtomObject a1 = l.environment().atomOf("+");
        AtomObject a2 = l.environment().atomOf("<");

        l.environment().define(a1, new FunctionObject(TestParser::add));
        l.environment().define(a2, new FunctionObject(TestParser::lt));

        LispObject obj = l.parse("(if (< 3 2) 1 2)");

        Evaluator eval = new Evaluator(l.environment());

        LispObject res = eval.evaluate(obj);

        System.out.println(res);
    }

    public static LispObject add(List<LispObject> params) {
        int sum = 0;

        for (LispObject i : params) {
            sum += i.asInt().get();
        }

        return new IntObject(sum);
    }

    public static LispObject lt(List<LispObject> params) {
        Integer i1 = params.get(0).asInt().get();
        Integer i2 = params.get(1).asInt().get();

        return new BooleanObject(i1 < i2);
    }
}
