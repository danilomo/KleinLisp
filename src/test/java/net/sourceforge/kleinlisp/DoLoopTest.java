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

/**
 * Tests for the R7RS do loop implementation.
 *
 * @author danilo
 */
public class DoLoopTest extends BaseTestClass {

  @Test
  public void testSimpleCounter() {
    assertEquals(5, evalAsInt("(do ((i 0 (+ i 1))) ((= i 5) i))"));
  }

  @Test
  public void testSumLoop() {
    // Sum of 0+1+2+3+4 = 10
    assertEquals(10, evalAsInt("(do ((i 0 (+ i 1)) (sum 0 (+ sum i))) ((= i 5) sum))"));
  }

  @Test
  public void testWithBody() {
    // Body sets a variable as side effect
    lisp.evaluate("(define result '())");
    lisp.evaluate("(do ((i 0 (+ i 1))) ((= i 3)) (set! result (cons i result)))");
    assertEquals("(2 1 0)", lisp.evaluate("result").toString());
  }

  @Test
  public void testNoStepExpression() {
    // Variable without step keeps its value
    assertEquals(10, evalAsInt("(do ((x 10) (i 0 (+ i 1))) ((= i 3) x))"));
  }

  @Test
  public void testMultipleResults() {
    // Multiple result expressions, last one is returned
    assertEquals(6, evalAsInt("(do ((i 1 (+ i 1))) ((= i 4) (+ i 1) (+ i 2)))"));
  }

  @Test
  public void testEmptyBody() {
    assertEquals(5, evalAsInt("(do ((i 0 (+ i 1))) ((= i 5) i))"));
  }

  @Test
  public void testParallelStep() {
    // Step expressions should see values from before the step
    // This swaps x and y each iteration
    // i=0: x=0, y=1 -> step -> x=1, y=0
    // i=1: x=1, y=0 -> step -> x=0, y=1
    // i=2: x=0, y=1 -> step -> x=1, y=0
    // i=3: exit with (1 0)
    assertEquals("(1 0)", lisp.evaluate("(do ((x 0 y) (y 1 x) (i 0 (+ i 1))) ((= i 3) (list x y)))").toString());
  }

  @Test
  public void testBuildVector() {
    assertEquals(
        "#(0 1 2 3 4)",
        lisp.evaluate(
                "(do ((vec (make-vector 5)) (i 0 (+ i 1))) ((= i 5) vec) (vector-set! vec i i))")
            .toString());
  }

  @Test
  public void testReverseList() {
    assertEquals(
        "(3 2 1)",
        lisp.evaluate(
                "(do ((lst '(1 2 3) (cdr lst)) (result '() (cons (car lst) result))) ((null? lst) result))")
            .toString());
  }

  @Test
  public void testFactorial() {
    assertEquals(120, evalAsInt("(do ((n 5 (- n 1)) (result 1 (* result n))) ((= n 0) result))"));
  }

  @Test
  public void testImmediateExit() {
    // Test is true immediately
    assertEquals("done", lisp.evaluate("(do ((i 0)) (#t 'done))").toString());
  }

  @Test
  public void testNestedDo() {
    // Outer loop runs 3 times, inner loop adds 2 each time -> 6
    assertEquals(
        6,
        evalAsInt(
            "(do ((i 0 (+ i 1)) (sum 0)) "
                + "    ((= i 3) sum) "
                + "  (set! sum (+ sum "
                + "    (do ((j 0 (+ j 1)) (inner 0 (+ inner 1))) "
                + "        ((= j 2) inner)))))"));
  }

  @Test
  public void testNoResultExpressions() {
    // When test clause has no result expressions, void is returned
    LispObject result = lisp.evaluate("(do ((i 0 (+ i 1))) ((= i 3)))");
    // Void object should be returned
    assertNotNull(result);
  }

  @Test
  public void testCountDown() {
    assertEquals(0, evalAsInt("(do ((i 10 (- i 1))) ((= i 0) i))"));
  }

  @Test
  public void testGreaterThanComparison() {
    assertEquals(5, evalAsInt("(do ((i 0 (+ i 1))) ((>= i 5) i))"));
  }

  @Test
  public void testAccessOuterScope() {
    lisp.evaluate("(define multiplier 10)");
    assertEquals(
        100,
        evalAsInt("(do ((i 0 (+ i 1)) (sum 0 (+ sum multiplier))) ((= i 10) sum))"));
  }

  @Test
  public void testSetBangInBody() {
    lisp.evaluate("(define total 0)");
    lisp.evaluate("(do ((i 1 (+ i 1))) ((> i 5)) (set! total (+ total i)))");
    assertEquals(15, evalAsInt("total")); // 1+2+3+4+5 = 15
  }
}
