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

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for the let binding stale values bug. See docs/BUG-LET-BINDING-STALE-VALUES.md for full
 * description.
 */
public class LetBindingBugTest extends BaseTestClass {

  @Test
  public void testLetInMapReturnsFreshValues() {
    // Test case 2 from bug doc: Using map
    // (define (double x) (let ((result (* x 2))) result))
    // (map double (list 1 2 3 4 5))
    // Expected: (2 4 6 8 10)
    // Bug produces: (2 2 2 2 2)
    lisp.evaluate("(define (double x) (let ((result (* x 2))) result))");
    ListObject result = lisp.evaluate("(map double (list 1 2 3 4 5))").asList();

    assertEquals(2, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
    assertEquals(6, result.cdr().cdr().car().asInt().value);
    assertEquals(8, result.cdr().cdr().cdr().car().asInt().value);
    assertEquals(10, result.cdr().cdr().cdr().cdr().car().asInt().value);
  }

  @Test
  public void testLetWithSimpleParameterCapture() {
    // Simplest case: let binding just captures parameter
    lisp.evaluate("(define (identity-let x) (let ((captured x)) captured))");
    ListObject result = lisp.evaluate("(map identity-let (list 1 2 3))").asList();

    assertEquals(1, result.car().asInt().value);
    assertEquals(2, result.cdr().car().asInt().value);
    assertEquals(3, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testLetStarInMapReturnsFreshValues() {
    // Test case 5 from bug doc: let* has same bug
    lisp.evaluate("(define (double-star x) (let* ((a x) (b (* a 2))) b))");
    ListObject result = lisp.evaluate("(map double-star (list 1 2 3))").asList();

    assertEquals(2, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
    assertEquals(6, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testLetInFoldLeftReturnsCorrectAccumulation() {
    // Test case 1: fold-left with let
    lisp.evaluate("(define (sum-with-let acc x) (let ((v x)) (+ acc v)))");
    int result = lisp.evaluate("(fold-left sum-with-let 0 (list 1 2 3))").asInt().value;

    assertEquals(6, result); // 0 + 1 + 2 + 3 = 6
  }

  @Test
  public void testNestedLetInMap() {
    // Nested let should also work correctly
    lisp.evaluate(
        "(define (nested-double x)"
            + "  (let ((outer x))"
            + "    (let ((inner (* outer 2)))"
            + "      inner)))");
    ListObject result = lisp.evaluate("(map nested-double (list 1 2 3))").asList();

    assertEquals(2, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
    assertEquals(6, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testLetWithMultipleBindingsInMap() {
    // Multiple bindings in single let
    lisp.evaluate("(define (multi-binding x)" + "  (let ((a x) (b (* x 2)))" + "    (+ a b)))");
    ListObject result = lisp.evaluate("(map multi-binding (list 1 2 3))").asList();

    // 1 + 2 = 3, 2 + 4 = 6, 3 + 6 = 9
    assertEquals(3, result.car().asInt().value);
    assertEquals(6, result.cdr().car().asInt().value);
    assertEquals(9, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testDirectParameterAccessWorksCorrectly() {
    // This test should pass even with the bug - confirms parameter access works
    lisp.evaluate("(define (direct-double x) (* x 2))");
    ListObject result = lisp.evaluate("(map direct-double (list 1 2 3 4 5))").asList();

    assertEquals(2, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
    assertEquals(6, result.cdr().cdr().car().asInt().value);
    assertEquals(8, result.cdr().cdr().cdr().car().asInt().value);
    assertEquals(10, result.cdr().cdr().cdr().cdr().car().asInt().value);
  }
}
