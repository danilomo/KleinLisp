/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.playground;

import net.sourceforge.kleinlisp.Lisp;

/**
 *
 * @author danilo
 */
public class TestParser {
    public static void main(String[] args) {
        Lisp l = new Lisp();
        System.out.println(l.parse("(+ 1 2 3 (+ 1 2 3))"));
    }
}
