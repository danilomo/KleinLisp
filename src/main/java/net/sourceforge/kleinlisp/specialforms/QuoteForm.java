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
class QuoteForm implements Function {

    private static final QuoteForm INSTANCE = new QuoteForm();

    static QuoteForm instance() {
        return INSTANCE;
    }

    @Override
    public LispObject evaluate(ListObject parameters) {
        return parameters.head();
    }

}
