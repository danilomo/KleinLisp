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

import org.junit.jupiter.api.Test;

/** Tests for and/or special forms with short-circuit evaluation. */
public class AndOrFormsTest extends BaseTestClass {

  @Test
  public void testAndWithAllTrue() {
    // Use expressions that evaluate to truthy values
    assertTrue(lisp.evaluate("(and 1 2 3)").truthiness());
    assertEquals(3, lisp.evaluate("(and 1 2 3)").asInt().value); // Returns last value
  }

  @Test
  public void testAndWithFalse() {
    // 0 and empty list are falsy, use (< 1 0) which is false
    assertFalse(lisp.evaluate("(and 1 (< 1 0) 3)").truthiness());
    assertFalse(lisp.evaluate("(and (< 1 0))").truthiness());
  }

  @Test
  public void testAndWithNoArgs() {
    assertTrue(lisp.evaluate("(and)").truthiness()); // (and) returns true
  }

  @Test
  public void testAndReturnsLastValue() {
    assertEquals(42, lisp.evaluate("(and 1 2 42)").asInt().value);
    assertEquals("hello", lisp.evaluate("(and 1 \"hello\")").asString().value());
  }

  @Test
  public void testAndShortCircuit() {
    // If short-circuit works, the second expression should not be evaluated
    // We test this by checking that a side-effect doesn't happen
    lisp.evaluate("(define counter 0)");
    lisp.evaluate("(define (inc-counter) (set! counter (+ counter 1)))");
    // (< 1 0) is false, so (inc-counter) should not be called
    lisp.evaluate("(and (< 1 0) (inc-counter))");
    assertEquals(0, lisp.evaluate("counter").asInt().value);
  }

  @Test
  public void testOrWithAllFalse() {
    assertFalse(lisp.evaluate("(or (< 1 0) (< 2 1) (< 3 2))").truthiness());
  }

  @Test
  public void testOrWithSomeTrue() {
    assertTrue(lisp.evaluate("(or (< 1 0) (> 2 1) (< 3 2))").truthiness());
    assertTrue(lisp.evaluate("(or (> 1 0))").truthiness());
  }

  @Test
  public void testOrWithNoArgs() {
    assertFalse(lisp.evaluate("(or)").truthiness()); // (or) returns false
  }

  @Test
  public void testOrReturnsFirstTrueValue() {
    assertEquals(1, lisp.evaluate("(or 1 2 3)").asInt().value);
    assertEquals(42, lisp.evaluate("(or (< 1 0) 42 99)").asInt().value);
  }

  @Test
  public void testOrShortCircuit() {
    // If short-circuit works, the second expression should not be evaluated
    lisp.evaluate("(define counter 0)");
    lisp.evaluate("(define (inc-counter) (set! counter (+ counter 1)))");
    // (> 1 0) is true, so (inc-counter) should not be called
    lisp.evaluate("(or (> 1 0) (inc-counter))");
    assertEquals(0, lisp.evaluate("counter").asInt().value);
  }

  @Test
  public void testNestedAndOr() {
    // (or false true) = true, (or true false) = true, (and true true) = true
    assertTrue(lisp.evaluate("(and (or (< 1 0) (> 1 0)) (or (> 1 0) (< 1 0)))").truthiness());
    // (and true false) = false, (and false true) = false, (or false false) = false
    assertFalse(lisp.evaluate("(or (and (> 1 0) (< 1 0)) (and (< 1 0) (> 1 0)))").truthiness());
    // (and true true) = true, (and false false) = false, (or true false) = true
    assertTrue(lisp.evaluate("(or (and (> 1 0) (> 2 1)) (and (< 1 0) (< 2 1)))").truthiness());
  }

  @Test
  public void testAndOrWithExpressions() {
    assertTrue(lisp.evaluate("(and (> 5 3) (< 2 4))").truthiness());
    assertFalse(lisp.evaluate("(and (> 5 3) (< 5 3))").truthiness());
    assertTrue(lisp.evaluate("(or (> 1 5) (< 1 5))").truthiness());
  }

  @Test
  public void testAndOrWithFunctionCalls() {
    lisp.evaluate("(define (is-positive x) (> x 0))");
    lisp.evaluate("(define (is-even x) (= (mod x 2) 0))");

    assertTrue(lisp.evaluate("(and (is-positive 5) (is-even 4))").truthiness());
    assertFalse(lisp.evaluate("(and (is-positive 5) (is-even 5))").truthiness());
    assertTrue(lisp.evaluate("(or (is-positive -5) (is-even 4))").truthiness());
  }

  @Test
  public void testAndOrCombinedWithIf() {
    // Common pattern: (if (and cond1 cond2) then else)
    assertEquals(1, lisp.evaluate("(if (and (> 5 3) (< 2 4)) 1 0)").asInt().value);
    assertEquals(0, lisp.evaluate("(if (and (> 5 3) (> 2 4)) 1 0)").asInt().value);

    // (if (or cond1 cond2) then else)
    assertEquals(1, lisp.evaluate("(if (or (> 1 5) (< 1 5)) 1 0)").asInt().value);
    assertEquals(0, lisp.evaluate("(if (or (> 1 5) (< 1 0)) 1 0)").asInt().value);
  }
}
