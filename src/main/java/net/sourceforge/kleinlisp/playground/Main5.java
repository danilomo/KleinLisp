/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.objects.IntObject;

/**
 *
 * @author daolivei
 */
public class Main5 {
    public static void main(String[] args) {
        String code = "(begin (print 1) (print 2) (print 3) 4 5)";
        Lisp lisp = new Lisp();
        
        System.out.println(lisp.evaluate(code));
        
        lisp.environment().define("x", new IntObject(1));
        
        lisp.evaluate("(set! x 1000)");
        
        System.out.println(lisp.evaluate("x"));
    }
}
