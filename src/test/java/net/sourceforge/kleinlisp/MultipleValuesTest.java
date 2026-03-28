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

/** Tests for R7RS multiple values support: values and call-with-values. */
public class MultipleValuesTest extends BaseTestClass {

  @Test
  public void testSingleValue() {
    assertEquals("5", lisp.evaluate("(values 5)").toString());
  }

  @Test
  public void testMultipleValues() {
    String result = lisp.evaluate("(values 1 2 3)").toString();
    // Multiple values display on separate lines
    assertEquals("1\n2\n3", result);
  }

  @Test
  public void testCallWithValues() {
    assertEquals(
        "3",
        lisp.evaluate("(call-with-values (lambda () (values 1 2)) (lambda (x y) (+ x y)))")
            .toString());
  }

  @Test
  public void testCallWithValuesSingleValue() {
    assertEquals(
        "10", lisp.evaluate("(call-with-values (lambda () 10) (lambda (x) x))").toString());
  }

  @Test
  public void testCallWithValuesNoValues() {
    // (values) with no args - consumer gets no args
    assertEquals(
        "0", lisp.evaluate("(call-with-values (lambda () (values)) (lambda () 0))").toString());
  }

  @Test
  public void testValuesInArithmetic() {
    // When used in single-value context, first value is used
    assertEquals("6", lisp.evaluate("(+ (values 1 2 3) 5)").toString());
  }

  @Test
  public void testValuesFromLet() {
    // (values 1 2 3) -> consumer (lambda (a b c) c) returns c=3
    assertEquals(
        "3",
        lisp.evaluate(
                "(call-with-values "
                    + "  (lambda () (let ((x 1) (y 2)) (values x y (+ x y)))) "
                    + "  (lambda (a b c) c))")
            .toString());
  }

  @Test
  public void testNestedCallWithValues() {
    // Inner: (values 1 2 3) -> (values (+ 1 2) (+ 2 3)) = (values 3 5)
    // Outer: (+ 3 5 10) = 18
    assertEquals(
        "18",
        lisp.evaluate(
                "(call-with-values "
                    + "  (lambda () (call-with-values "
                    + "               (lambda () (values 1 2 3)) "
                    + "               (lambda (a b c) (values (+ a b) (+ b c))))) "
                    + "  (lambda (x y) (+ x y 10)))")
            .toString());
  }

  @Test
  public void testValuesInFunction() {
    // (quotient 20 3) = 6, (remainder 20 3) = 2, (+ 6 2) = 8
    lisp.evaluate("(define (div-and-mod n d) (values (quotient n d) (remainder n d)))");
    assertEquals(
        "8",
        lisp.evaluate(
                "(call-with-values (lambda () (div-and-mod 20 3)) "
                    + "                  (lambda (q r) (+ q r)))")
            .toString());
  }

  @Test
  public void testValuesPredicate() {
    assertEquals("true", lisp.evaluate("(values? (values 1 2))").toString());
    assertEquals("false", lisp.evaluate("(values? 5)").toString());
    // Single value doesn't create ValuesObject
    assertEquals("false", lisp.evaluate("(values? (values 5))").toString());
  }

  @Test
  public void testExactIntegerSqrtWithCallWithValues() {
    // exact-integer-sqrt returns multiple values: (s, r) where s*s + r = n
    assertEquals(
        "5",
        lisp.evaluate(
                "(call-with-values (lambda () (exact-integer-sqrt 11)) "
                    + "                  (lambda (s r) (+ s r)))")
            .toString());
  }

  @Test
  public void testExactIntegerSqrtAsList() {
    // Use call-with-values to extract values and create a list
    assertEquals(
        "(3 2)",
        lisp.evaluate(
                "(call-with-values (lambda () (exact-integer-sqrt 11)) "
                    + "                  (lambda (s r) (list s r)))")
            .toString());
  }

  @Test
  public void testCallWithValuesWithApply() {
    // Use + with apply-like semantics via call-with-values
    assertEquals(
        "6",
        lisp.evaluate("(call-with-values (lambda () (values 1 2 3)) " + "                  +)")
            .toString());
  }

  @Test
  public void testValuesAsLastExpression() {
    // Values as the last expression in a lambda
    lisp.evaluate("(define (get-pair) (values 10 20))");
    assertEquals(
        "30",
        lisp.evaluate("(call-with-values get-pair " + "                  (lambda (a b) (+ a b)))")
            .toString());
  }

  @Test
  public void testValuesWithConditional() {
    // Values can be returned from conditional expressions
    assertEquals(
        "30",
        lisp.evaluate(
                "(call-with-values "
                    + "  (lambda () (if #t (values 10 20) (values 1 2))) "
                    + "  (lambda (a b) (+ a b)))")
            .toString());
  }

  @Test
  public void testEmptyValues() {
    // (values) with no arguments
    String result = lisp.evaluate("(values)").toString();
    assertEquals("", result);
  }
}
