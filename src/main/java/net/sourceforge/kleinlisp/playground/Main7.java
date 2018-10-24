/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Lisp;

/**
 *
 * @author daolivei
 */
public class Main7 {

    public static void main(String[] args) {
        Lisp lisp = new Lisp();

        lisp.evaluate("(define else 1)");
        lisp.evaluate("(define (fib n)(cond((= n 0) 1)((= n 1) 1)(else(+ (fib (- n 1))(fib (- n 2))))))");                

        System.out.println(lisp.evaluate("(fib 34)"));

    }
}
