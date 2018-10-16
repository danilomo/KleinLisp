/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author Danilo Oliveira
 */
public class ListFunctions {

    public static LispObject length(ListObject parameters) {
        ListObject arg = parameters.car().asList().get();

        return new IntObject(arg.length());
    }

    public static LispObject car(ListObject parameters) {
        ListObject arg = parameters.car().asList().get();

        return arg.head();
    }

    public static LispObject cdr(ListObject parameters) {
        ListObject arg = parameters.car().asList().get();

        System.out.println(">> arg " + arg);

        return arg.tail();
    }

    public static LispObject list(ListObject parameters) {
        return parameters;
    }
}
