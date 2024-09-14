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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Danilo Oliveira
 */
public class BasicFunctionsTest  extends BaseTestClass {



    @Test
    public void testArithmetic() {
        Object[][] expressions = {
                {"(+ 14 5)", 19},
                {"(+ 1 2 3 4)", 10},
                {"(- (+ 3 4) 7)", 0},
                {"(* (+ 2 5) (- 7 (/ 21 7)))", 28}
        };

        for (Object[] arr : expressions) {
            int result = lisp.evaluate(arr[0].toString()).asInt().value;
            int expected = (Integer) arr[1];

            System.out.println(result + ", " + expected);

            assertEquals(result, expected);
        }
    }

    @Test
    public void testComparison() {
        Object[][] expressions = {
            {"(= (+ 2 3) 5)", true},
            {"(> (* 5 6) (+ 4 5))", true}
        };

        for (Object[] arr : expressions) {
            boolean result = lisp.evaluate(arr[0].toString()).truthiness();
            boolean expected = (Boolean) arr[1];

            System.out.println(result + ", " + expected);

            assertEquals(result, expected);
        }
    }
}
