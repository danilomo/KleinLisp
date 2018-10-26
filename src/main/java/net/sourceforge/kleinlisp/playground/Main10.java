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
public class Main10 {
    public static void main(String[] args) {
        Lisp lisp = new Lisp();
        
        lisp.evaluate("(define else 1)");
        
        lisp.evaluate("(define (test n) (cond ((= n 0) 1) ((= n 1) 1) (else \"ronaldo\")))");
        
        System.out.println(lisp.evaluate("(test 10)"));
    }
}
