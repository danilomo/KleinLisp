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
 * Tests for the closure parameter capture bug. See docs/CLOSURE-PARAMETER-CAPTURE-BUG.md for full
 * description.
 *
 * <p>When a lambda captures a function parameter and is passed to a higher-order function like
 * `any`, `map`, or `filter`, the captured parameter value must be correctly accessible inside the
 * lambda.
 */
public class ClosureParameterCaptureTest extends BaseTestClass {

  @Test
  public void testParameterCaptureInAny() {
    lisp.evaluate(
        "(define (find-match target items) " + "  (any (lambda (x) (= x target)) items))");

    assertTrue(lisp.evaluate("(find-match 2 (list 1 2 3))").truthiness());
    assertTrue(lisp.evaluate("(find-match 3 (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(find-match 5 (list 1 2 3))").truthiness());
  }

  @Test
  public void testParameterCaptureInAnyWithStrings() {
    lisp.evaluate(
        "(define (has-children? sid) "
            + "  (any (lambda (span) (string=? (car span) sid)) "
            + "       (list (list \"a\") (list \"b\") (list \"c\"))))");

    assertTrue(lisp.evaluate("(has-children? \"a\")").truthiness());
    assertTrue(lisp.evaluate("(has-children? \"b\")").truthiness());
    assertFalse(lisp.evaluate("(has-children? \"d\")").truthiness());
  }

  @Test
  public void testParameterCaptureInMap() {
    lisp.evaluate("(define (scale factor items) " + "  (map (lambda (x) (* x factor)) items))");

    ListObject result = lisp.evaluate("(scale 2 (list 1 2 3))").asList();
    assertEquals(2, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
    assertEquals(6, result.cdr().cdr().car().asInt().value);

    // Test with different factor
    result = lisp.evaluate("(scale 10 (list 1 2 3))").asList();
    assertEquals(10, result.car().asInt().value);
    assertEquals(20, result.cdr().car().asInt().value);
    assertEquals(30, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testParameterCaptureInFilter() {
    lisp.evaluate(
        "(define (filter-gt threshold items) " + "  (filter (lambda (x) (> x threshold)) items))");

    ListObject result = lisp.evaluate("(filter-gt 2 (list 1 2 3 4 5))").asList();
    assertEquals(3, result.length());
    assertEquals(3, result.car().asInt().value);
    assertEquals(4, result.cdr().car().asInt().value);
    assertEquals(5, result.cdr().cdr().car().asInt().value);

    // Test with different threshold
    result = lisp.evaluate("(filter-gt 3 (list 1 2 3 4 5))").asList();
    assertEquals(2, result.length());
    assertEquals(4, result.car().asInt().value);
    assertEquals(5, result.cdr().car().asInt().value);
  }

  @Test
  public void testParameterCaptureInFoldLeft() {
    lisp.evaluate(
        "(define (sum-with-offset offset items) "
            + "  (fold-left (lambda (acc x) (+ acc x offset)) 0 items))");

    // With offset 0: 0 + (1+0) + (2+0) + (3+0) = 6
    assertEquals(6, lisp.evaluate("(sum-with-offset 0 (list 1 2 3))").asInt().value);

    // With offset 10: 0 + (1+10) + (2+10) + (3+10) = 36
    assertEquals(36, lisp.evaluate("(sum-with-offset 10 (list 1 2 3))").asInt().value);
  }

  @Test
  public void testParameterCaptureInForEach() {
    lisp.evaluate(
        "(define (print-with-prefix prefix items)   (for-each (lambda (x) (println (string-append"
            + " prefix (number->string x)))) items))");

    lisp.evaluate("(print-with-prefix \"Value: \" (list 1 2 3))");
    String output = getStdOut();
    assertTrue(output.contains("\"Value: 1\""));
    assertTrue(output.contains("\"Value: 2\""));
    assertTrue(output.contains("\"Value: 3\""));
  }

  @Test
  public void testParameterCaptureWithMultipleParameters() {
    lisp.evaluate(
        "(define (in-range? lo hi items) "
            + "  (filter (lambda (x) (and (>= x lo) (<= x hi))) items))");

    ListObject result = lisp.evaluate("(in-range? 2 4 (list 1 2 3 4 5))").asList();
    assertEquals(3, result.length());
    assertEquals(2, result.car().asInt().value);
    assertEquals(3, result.cdr().car().asInt().value);
    assertEquals(4, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testParameterCaptureInFind() {
    lisp.evaluate(
        "(define (find-greater-than threshold items) "
            + "  (find (lambda (x) (> x threshold)) items))");

    assertEquals(3, lisp.evaluate("(find-greater-than 2 (list 1 2 3 4 5))").asInt().value);
    assertEquals(4, lisp.evaluate("(find-greater-than 3 (list 1 2 3 4 5))").asInt().value);
  }

  @Test
  public void testParameterCaptureInAll() {
    lisp.evaluate(
        "(define (all-greater-than threshold items) "
            + "  (all (lambda (x) (> x threshold)) items))");

    assertFalse(lisp.evaluate("(all-greater-than 2 (list 1 2 3 4 5))").truthiness());
    assertTrue(lisp.evaluate("(all-greater-than 0 (list 1 2 3 4 5))").truthiness());
    assertFalse(lisp.evaluate("(all-greater-than 3 (list 1 2 3 4 5))").truthiness());
  }

  @Test
  public void testNestedClosuresWithParameterCapture() {
    lisp.evaluate("(define (make-adder n) " + "  (lambda (x) (+ x n)))");
    lisp.evaluate("(define (apply-to-all fn items) " + "  (map fn items))");

    lisp.evaluate("(define add5 (make-adder 5))");
    ListObject result = lisp.evaluate("(apply-to-all add5 (list 1 2 3))").asList();
    assertEquals(6, result.car().asInt().value);
    assertEquals(7, result.cdr().car().asInt().value);
    assertEquals(8, result.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testRepeatedCallsWithDifferentArguments() {
    lisp.evaluate("(define (contains? target items) " + "  (any (lambda (x) (= x target)) items))");

    // Call multiple times with different targets
    assertTrue(lisp.evaluate("(contains? 1 (list 1 2 3))").truthiness());
    assertTrue(lisp.evaluate("(contains? 2 (list 1 2 3))").truthiness());
    assertTrue(lisp.evaluate("(contains? 3 (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(contains? 4 (list 1 2 3))").truthiness());

    // Call again with the first target to ensure no stale value issues
    assertTrue(lisp.evaluate("(contains? 1 (list 1 2 3))").truthiness());
  }
}
