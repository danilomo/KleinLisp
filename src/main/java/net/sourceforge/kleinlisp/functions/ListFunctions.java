/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.Form;
import net.sourceforge.kleinlisp.forms.IntForm;
import net.sourceforge.kleinlisp.forms.ListForm;

/**
 *
 * @author Danilo Oliveira
 */
public class ListFunctions {

    public static Form length(ListForm parameters) {
        ListForm arg = parameters.car().asList().get();

        return new IntForm(arg.length());
    }

    public static Form car(ListForm parameters) {
        ListForm arg = parameters.car().asList().get();

        return arg.head();
    }

    public static Form cdr(ListForm parameters) {
        ListForm arg = parameters.car().asList().get();

        System.out.println(">> arg " + arg);
        
        return arg.tail();
    }
}
