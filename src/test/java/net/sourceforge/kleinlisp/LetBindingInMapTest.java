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

import static org.junit.jupiter.api.Assertions.*;

import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for let binding capture in closures inside map callbacks. This is the remaining bug
 * documented in docs/CLOSURE-PARAMETER-CAPTURE-BUG.md
 */
public class LetBindingInMapTest extends BaseTestClass {

  @Test
  public void testLetBindingCapturedInClosureInsideMapCallback() {
    // This is the core bug: let binding captured in closure inside map callback
    lisp.evaluate("(define (make-adder item) " + "  (let ((x item)) " + "    (lambda () x)))");
    lisp.evaluate("(define adders (map make-adder (list 1 2 3)))");

    // Each adder should return its captured value
    assertEquals(1, lisp.evaluate("((car adders))").asInt().value);
    assertEquals(2, lisp.evaluate("((cadr adders))").asInt().value);
    assertEquals(3, lisp.evaluate("((caddr adders))").asInt().value);
  }

  @Test
  public void testLetBindingWithMultipleBindings() {
    // Multiple let bindings captured in closure
    lisp.evaluate(
        "(define (make-pair-adder item) "
            + "  (let ((x item) (y (* item 10))) "
            + "    (lambda () (+ x y))))");
    lisp.evaluate("(define adders (map make-pair-adder (list 1 2 3)))");

    // 1 + 10 = 11, 2 + 20 = 22, 3 + 30 = 33
    assertEquals(11, lisp.evaluate("((car adders))").asInt().value);
    assertEquals(22, lisp.evaluate("((cadr adders))").asInt().value);
    assertEquals(33, lisp.evaluate("((caddr adders))").asInt().value);
  }

  @Test
  public void testNestedLetBindings() {
    // Nested let bindings
    lisp.evaluate(
        "(define (make-nested-adder item) "
            + "  (let ((x item)) "
            + "    (let ((y (* x 2))) "
            + "      (lambda () (+ x y)))))");
    lisp.evaluate("(define adders (map make-nested-adder (list 1 2 3)))");

    // 1 + 2 = 3, 2 + 4 = 6, 3 + 6 = 9
    assertEquals(3, lisp.evaluate("((car adders))").asInt().value);
    assertEquals(6, lisp.evaluate("((cadr adders))").asInt().value);
    assertEquals(9, lisp.evaluate("((caddr adders))").asInt().value);
  }

  @Test
  public void testLetBindingWithExternalVariable() {
    // Let binding that also captures external variable
    lisp.evaluate("(define multiplier 10)");
    lisp.evaluate(
        "(define (make-scaled-adder item) "
            + "  (let ((x item)) "
            + "    (lambda () (* x multiplier))))");
    lisp.evaluate("(define adders (map make-scaled-adder (list 1 2 3)))");

    assertEquals(10, lisp.evaluate("((car adders))").asInt().value);
    assertEquals(20, lisp.evaluate("((cadr adders))").asInt().value);
    assertEquals(30, lisp.evaluate("((caddr adders))").asInt().value);
  }

  @Test
  public void testLetBindingInFilterCallback() {
    // Let binding in filter callback
    lisp.evaluate(
        "(define (make-threshold-filter threshold) "
            + "  (let ((t threshold)) "
            + "    (lambda (x) (> x t))))");
    lisp.evaluate("(define gt2 (make-threshold-filter 2))");
    lisp.evaluate("(define gt3 (make-threshold-filter 3))");

    ListObject result1 = lisp.evaluate("(filter gt2 (list 1 2 3 4 5))").asList();
    assertEquals(3, result1.length());
    assertEquals(3, result1.car().asInt().value);

    ListObject result2 = lisp.evaluate("(filter gt3 (list 1 2 3 4 5))").asList();
    assertEquals(2, result2.length());
    assertEquals(4, result2.car().asInt().value);
  }

  @Test
  public void testLetBindingWithStringCapture() {
    // Let binding with string values
    lisp.evaluate(
        "(define (make-greeter name) "
            + "  (let ((n name)) "
            + "    (lambda () (string-append \"Hello, \" n))))");
    lisp.evaluate("(define greeters (map make-greeter (list \"Alice\" \"Bob\" \"Carol\")))");

    assertEquals("Hello, Alice", lisp.evaluate("((car greeters))").asString().value());
    assertEquals("Hello, Bob", lisp.evaluate("((cadr greeters))").asString().value());
    assertEquals("Hello, Carol", lisp.evaluate("((caddr greeters))").asString().value());
  }

  @Test
  public void testLetBindingComputedFromParameter() {
    // Let binding computed from function parameter
    lisp.evaluate(
        "(define (make-square-adder n) " + "  (let ((sq (* n n))) " + "    (lambda () sq)))");
    lisp.evaluate("(define adders (map make-square-adder (list 2 3 4)))");

    assertEquals(4, lisp.evaluate("((car adders))").asInt().value);
    assertEquals(9, lisp.evaluate("((cadr adders))").asInt().value);
    assertEquals(16, lisp.evaluate("((caddr adders))").asInt().value);
  }
}
