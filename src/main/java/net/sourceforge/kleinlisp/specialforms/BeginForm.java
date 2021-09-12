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
 * @author daolivei
 */
public class BeginForm implements Function {

    private static final BeginForm INSTANCE = new BeginForm();

    static BeginForm instance() {
        return INSTANCE;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        LispObject obj = ListObject.NIL;

        for (LispObject param : parameters) {
            obj = param.evaluate();
        }

        return obj;
    }

}
