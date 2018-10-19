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
public class Main8 {

    public static void main(String[] args) {
        Lisp lisp = new Lisp();

        System.out.println(lisp.evaluate("(takewhile (lambda (x) (<= x 5)) [1 2 3 4 5 6 7 8 9 10])"));
    }
}
