/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author daolivei
 */
public class ListFunctionsTest {

    private Lisp lisp;

    public ListFunctionsTest() {
    }

    @Before
    public void setup() {
        lisp = new Lisp();
    }

    @Test
    public void testLen() {
        assertEquals(
                lisp.evaluate("(length (list 1 2 3 4 5 6))").asInt().get().intValue(),
                6
        );
    }

    @Test
    public void testCarAndCDR() {
        assertEquals(
                lisp.evaluate("(car (list 6 5 4 3 2 1))").asInt().get().intValue(),
                6
        );

        assertEquals(
                lisp.evaluate("(length (cdr (list 6 5 4 3 2 1)))").asInt().get().intValue(),
                5
        );

        assertEquals(
                lisp.evaluate("(car (cdr (list 6 5 4 3 2 1)))").asInt().get().intValue(),
                5
        );

    }

}
