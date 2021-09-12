/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * @author daolivei
 */
public class SetForm implements Function {

    private final Environment environment;

    public SetForm(Environment environment) {
        this.environment = environment;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        String symbol = parameters.car().asAtom().get().toString();
        LispObject obj = parameters.cdr().car().evaluate();

        this.environment.set(symbol, obj);

        return ListObject.NIL;
    }

}
