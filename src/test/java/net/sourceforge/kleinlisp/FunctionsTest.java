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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author danilo
 */
public class FunctionsTest extends BaseTestClass {

  @Test
  public void testRecursiveFunction() {
    lisp.evaluate(
        "(define (fib n)\n"
            + "  (if (< n 2)\n"
            + "      n\n"
            + "      (+ (fib (- n 1)) (fib (- n 2)))))");

    assertEquals(evalAsInt("(fib 10)"), 55);
  }

  @Test
  public void testClosure() {
    lisp.evaluate(
        "(define new-counter (lambda (i)\n"
            + "    (lambda ()\n"
            + "      (set! i (+ i 1))\n"
            + "      i)))");
    lisp.evaluate("(define c1 (new-counter 0))");
    lisp.evaluate("(define c2 (new-counter 10))");

    assertEquals(evalAsInt("(c1)"), 1);

    assertEquals(evalAsInt("(c1)"), 2);

    assertEquals(evalAsInt("(c2)"), 11);

    assertEquals(evalAsInt("(c2)"), 12);
  }

  @Test
  public void testDefineWithLet() {
    lisp.evaluate("(define (foo a b) (let ((c (+ a b))) (* c c)))");

    assertEquals(evalAsInt("(foo 1 1)"), 4);
  }

  @Test
  public void testMultiExpressionFunction() {
    lisp.evaluate("(define (foo a b) (print 1) (print 2) (+ a b))");

    assertEquals(evalAsInt("(foo 1 1)"), 2);
  }

  @Test
  public void testSetParameter() {
    lisp.evaluate("(define (foo a) (set! a 10) a)");

    assertEquals(evalAsInt("(foo 1)"), 10);
  }
}
