/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author daolivei
 */
public class IfForm implements Function {

    private static final IfForm INSTANCE = new IfForm();

    static IfForm instance() {
        return INSTANCE;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        LispObject cond = parameters.car();
        LispObject trueForm = parameters.cdr().car();
        LispObject elseForm = parameters.cdr().cdr().car();

        if (cond.evaluate().truthness()) {
            return trueForm.evaluate();
        } else {
            return elseForm.evaluate();
        }
    }

}
