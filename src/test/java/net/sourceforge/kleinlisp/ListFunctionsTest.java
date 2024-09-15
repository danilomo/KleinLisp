/*
 * MIT License
 * 
 * Copyright (c) 2018 Danilo Oliveira
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sourceforge.kleinlisp;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author daolivei
 */
public class ListFunctionsTest extends BaseTestClass {

    @Test
    public void testLen() {
        assertEquals(
                lisp.evaluate("(length (list 1 2 3 4 5 6))").asInt().value,
                6
        );
    }

    @Test
    public void testCarAndCDR() {
        assertEquals(
                lisp.evaluate("(car (list 6 5 4 3 2 1))").asInt().value,
                6
        );

        assertEquals(
                lisp.evaluate("(length (cdr (list 6 5 4 3 2 1)))").asInt().value,
                5
        );

        assertEquals(
                lisp.evaluate("(car (cdr (list 6 5 4 3 2 1)))").asInt().value,
                5
        );

    }
    
    @Test 
    public void testxxx() {
        LispObject list = lisp.evaluate("(- 4 3)");
        debug(list);
    }

}
