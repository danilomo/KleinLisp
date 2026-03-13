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

/** Tests for type predicate functions. */
public class TypePredicatesTest extends BaseTestClass {

  @Test
  public void testStringPredicate() {
    assertTrue(lisp.evaluate("(string? \"hello\")").truthiness());
    assertFalse(lisp.evaluate("(string? 42)").truthiness());
    assertFalse(lisp.evaluate("(string? (list 1 2 3))").truthiness());
  }

  @Test
  public void testNumberPredicate() {
    assertTrue(lisp.evaluate("(number? 42)").truthiness());
    assertTrue(lisp.evaluate("(number? -10)").truthiness());
    assertFalse(lisp.evaluate("(number? \"hello\")").truthiness());
    assertFalse(lisp.evaluate("(number? (list 1 2))").truthiness());
  }

  @Test
  public void testIntegerPredicate() {
    assertTrue(lisp.evaluate("(integer? 42)").truthiness());
    assertTrue(lisp.evaluate("(integer? -10)").truthiness());
    assertTrue(lisp.evaluate("(integer? 0)").truthiness());
    assertFalse(lisp.evaluate("(integer? \"42\")").truthiness());
  }

  @Test
  public void testPairPredicate() {
    assertTrue(lisp.evaluate("(pair? (list 1 2 3))").truthiness());
    assertTrue(lisp.evaluate("(pair? (cons 1 2))").truthiness());
    assertFalse(lisp.evaluate("(pair? (list))").truthiness());
    assertFalse(lisp.evaluate("(pair? 42)").truthiness());
  }

  @Test
  public void testListPredicate() {
    assertTrue(lisp.evaluate("(list? (list 1 2 3))").truthiness());
    assertTrue(lisp.evaluate("(list? (list))").truthiness());
    assertFalse(lisp.evaluate("(list? 42)").truthiness());
    assertFalse(lisp.evaluate("(list? \"hello\")").truthiness());
  }

  @Test
  public void testSymbolPredicate() {
    assertTrue(lisp.evaluate("(symbol? 'hello)").truthiness());
    assertTrue(lisp.evaluate("(symbol? 'x)").truthiness());
    assertFalse(lisp.evaluate("(symbol? \"hello\")").truthiness());
    assertFalse(lisp.evaluate("(symbol? 42)").truthiness());
  }

  @Test
  public void testBooleanPredicate() {
    // Test with comparison results which are BooleanObjects
    assertTrue(lisp.evaluate("(boolean? (< 1 2))").truthiness());
    assertTrue(lisp.evaluate("(boolean? (> 1 2))").truthiness());
    assertFalse(lisp.evaluate("(boolean? 0)").truthiness());
    assertFalse(lisp.evaluate("(boolean? \"true\")").truthiness());
  }

  @Test
  public void testProcedurePredicate() {
    assertTrue(lisp.evaluate("(procedure? (lambda (x) x))").truthiness());
    assertTrue(lisp.evaluate("(procedure? +)").truthiness());
    assertFalse(lisp.evaluate("(procedure? 42)").truthiness());
    assertFalse(lisp.evaluate("(procedure? \"hello\")").truthiness());
  }

  @Test
  public void testNullPredicate() {
    assertTrue(lisp.evaluate("(null? (list))").truthiness());
    assertTrue(lisp.evaluate("(null? '())").truthiness());
    assertFalse(lisp.evaluate("(null? (list 1))").truthiness());
    assertFalse(lisp.evaluate("(null? 0)").truthiness());
  }

  @Test
  public void testVectorPredicate() {
    assertTrue(lisp.evaluate("(vector? (make-vector 3))").truthiness());
    assertTrue(lisp.evaluate("(vector? (vector 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(vector? (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(vector? \"hello\")").truthiness());
  }

  @Test
  public void testZeroPredicate() {
    assertTrue(lisp.evaluate("(zero? 0)").truthiness());
    assertFalse(lisp.evaluate("(zero? 1)").truthiness());
    assertFalse(lisp.evaluate("(zero? -1)").truthiness());
  }

  @Test
  public void testPositivePredicate() {
    assertTrue(lisp.evaluate("(positive? 1)").truthiness());
    assertTrue(lisp.evaluate("(positive? 100)").truthiness());
    assertFalse(lisp.evaluate("(positive? 0)").truthiness());
    assertFalse(lisp.evaluate("(positive? -1)").truthiness());
  }

  @Test
  public void testNegativePredicate() {
    assertTrue(lisp.evaluate("(negative? -1)").truthiness());
    assertTrue(lisp.evaluate("(negative? -100)").truthiness());
    assertFalse(lisp.evaluate("(negative? 0)").truthiness());
    assertFalse(lisp.evaluate("(negative? 1)").truthiness());
  }

  @Test
  public void testOddPredicate() {
    assertTrue(lisp.evaluate("(odd? 1)").truthiness());
    assertTrue(lisp.evaluate("(odd? 3)").truthiness());
    assertTrue(lisp.evaluate("(odd? -1)").truthiness());
    assertFalse(lisp.evaluate("(odd? 0)").truthiness());
    assertFalse(lisp.evaluate("(odd? 2)").truthiness());
  }

  @Test
  public void testEvenPredicate() {
    assertTrue(lisp.evaluate("(even? 0)").truthiness());
    assertTrue(lisp.evaluate("(even? 2)").truthiness());
    assertTrue(lisp.evaluate("(even? -2)").truthiness());
    assertFalse(lisp.evaluate("(even? 1)").truthiness());
    assertFalse(lisp.evaluate("(even? 3)").truthiness());
  }
}
