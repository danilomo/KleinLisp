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

import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author danilo
 */
public class SpecialFormsTest extends BaseTestClass {

    @Test
    public void testIfForm() {
        assertEquals(
                lisp.evaluate("(if (< 3 2) 1 (+ 2 2 2))").asInt().get().intValue(),
                6
        );
    }

    @Test
    public void testLetForm() {
        lisp.evaluate("(define foo 10)");
        lisp.evaluate("(define bar 10)");
        assertEquals(
                lisp.evaluate("(let ((x 1) (y (+ foo bar))) (+ x y 1))").asInt().get().intValue(),
                22
        );
    }

    @Test
    public void testNestedLetWithShadowing() {
        lisp.evaluate("(define foo 10)");
        lisp.evaluate("(define bar 10)");
        assertEquals( //
                evalAsInt("(let ((x 1) (y (+ foo bar))) (+ x y 1 (let ((x 2) (y 3)) (+ x y))))\""),
                27
        );
    }

    @Test
    public void testNestedLetWithoutShadowing() {
        lisp.evaluate("(define foo 10)");
        lisp.evaluate("(define bar 10)");
        assertEquals( //
                evalAsInt("(let ((x 1) (y (+ foo bar))) (+ x y 1 (let ((a 2) (b 3)) (+ a b))))\""),
                27
        );
    }

    @Test
    public void testLambdaForm() {
        FunctionObject obj = lisp.evaluate("(lambda (x y z) (+ x y z))").asFunction().get();

        LispObject result = obj.function().evaluate(new LispObject[]{
            new IntObject(1),
            new IntObject(1),
            new IntObject(1)
        });

        assertEquals(
                result.asInt().get().intValue(),
                3
        );
    }

    @Test
    public void testDefineLambdaForm() {
        lisp.evaluate("(define foo (lambda (x y z) (+ x y z)))");

        assertEquals(
                lisp.evaluate("(foo 1 2 3)").asInt().get().intValue(),
                6
        );
    }

    @Test
    public void testDefineForm() {
        lisp.evaluate("(define (foo x y z) (+ x y z))");

        assertEquals(
                lisp.evaluate("(foo 1 2 3)").asInt().get().intValue(),
                6
        );
    }

    @Test
    public void testBeginForm() {
        assertEquals(
                evalAsInt("(begin (println 1) (println 2) 3)"),
                3
        );
    }

    @Test
    public void testCondForm() {
        lisp.evaluate("(define (test x)"
                + "(cond ((< x 0) 'negative) ((= x 0) 'zero) (else 'positive)))");

        assertEquals(
                evalAsAtom("(test -1)"),
                evalAsAtom("'negative")
        );

        assertEquals(
                evalAsAtom("(test 1)"),
                evalAsAtom("'positive")
        );

        assertEquals(
                evalAsAtom("(test 0)"),
                evalAsAtom("'zero")
        );
    }

    @Test
    public void testCaseForm() {
        lisp.evaluate("(display (case (mod 35 10)\n"
                + "((2 4 6 8) \"positive and even\")\n"
                + "((1 3 5 7 9) \"positive and odd\")\n"
                + "((-2 -4 -6 -8) \"negative and even\")\n"
                + "((-1 -3 -5 -7 -9) \"negative and odd\")\n"
                + "(else \"zero\")))");
        
        Assert.assertTrue(
            getStdOut().contains("positive and odd")
        );
    }

}
