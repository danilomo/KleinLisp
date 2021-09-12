/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.Optional;

/**
 * @author daolivei
 */
public class CondForm implements Function {

    private static final CondForm INSTANCE = new CondForm();

    static CondForm instance() {
        return INSTANCE;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {

        for (LispObject item : parameters) {
            Optional<ListObject> list = item.asList();

            Optional<Tuple2<LispObject, LispObject>> tuple
                    = list.flatMap(l -> l.unpack(LispObject.class, LispObject.class));

            if (tuple.isPresent() && tuple.get().first().evaluate().truthness()) {
                return tuple.get().second().evaluate();
            }
        }

        return ListObject.NIL;

    }

}
