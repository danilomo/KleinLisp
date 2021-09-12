/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.NumericObject;

/**
 * @author daolivei
 */
public class MiscFunctions {

    public static LispObject sum(ListObject parameters) {
        int sum = 0;

        ListObject it = parameters;
        while (it != ListObject.NIL) {
            sum += ((NumericObject) it.car()).toInt();
            it = it.cdr();
        }

        return new IntObject(sum);
    }

    public static LispObject minus(ListObject parameters) {

        ListObject it = parameters;

        int sum = ((NumericObject) it.car()).toInt();
        it = it.cdr();

        while (it != ListObject.NIL) {
            sum -= ((NumericObject) it.car()).toInt();
            it = it.cdr();
        }

        return new IntObject(sum);
    }
}
