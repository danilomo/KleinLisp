/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functions;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * @author daolivei
 */
public class IOFunctions {


    public static LispObject print(ListObject parameters) {

        for (LispObject obj : parameters) {
            System.out.print("---> " + obj);
        }

        System.out.println();

        return ListObject.NIL;
    }
}
