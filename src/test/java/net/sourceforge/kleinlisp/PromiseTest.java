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

import net.sourceforge.kleinlisp.objects.BooleanObject;
import org.junit.jupiter.api.Test;

/** Tests for R7RS promises: delay, force, promise?, make-promise. */
public class PromiseTest extends BaseTestClass {

  @Test
  public void testDelayCreatesPromise() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(promise? (delay 42))"));
  }

  @Test
  public void testForceEvaluates() {
    assertEquals(42, evalAsInt("(force (delay 42))"));
  }

  @Test
  public void testForceComputation() {
    assertEquals(3, evalAsInt("(force (delay (+ 1 2)))"));
  }

  @Test
  public void testForceCachesResult() {
    // Side effect should only happen once
    lisp.evaluate("(define count 0)");
    lisp.evaluate("(define p (delay (begin (set! count (+ count 1)) count)))");

    assertEquals(1, evalAsInt("(force p)"));
    assertEquals(1, evalAsInt("(force p)")); // Cached
    assertEquals(1, evalAsInt("count")); // Only incremented once
  }

  @Test
  public void testForceOnNonPromise() {
    // R7RS: force on non-promise returns value unchanged
    assertEquals(42, evalAsInt("(force 42)"));
    assertEquals("\"hello\"", lisp.evaluate("(force \"hello\")").toString());
  }

  @Test
  public void testPromisePredicate() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(promise? (delay 1))"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(promise? 1)"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(promise? (lambda () 1))"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(promise? '(1 2 3))"));
  }

  @Test
  public void testMakePromise() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(promise? (make-promise 42))"));
    assertEquals(42, evalAsInt("(force (make-promise 42))"));
  }

  @Test
  public void testMakePromiseOnPromise() {
    // make-promise on a promise returns the same promise
    lisp.evaluate("(define p (delay 42))");
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(eq? p (make-promise p))"));
  }

  @Test
  public void testNestedDelay() {
    // R7RS: force does NOT automatically force nested promises
    // (force (delay (delay 42))) returns a promise, not 42
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(promise? (force (delay (delay 42))))"));
    // To get the final value, you need to force multiple times
    assertEquals(42, evalAsInt("(force (force (force (delay (delay (delay 42))))))"));
  }

  @Test
  public void testDelayWithNestedPromiseComputation() {
    // Nested delay returning another promise
    lisp.evaluate("(define inner (delay (* 6 7)))");
    lisp.evaluate("(define outer (delay (force inner)))");
    assertEquals(42, evalAsInt("(force outer)"));
  }

  @Test
  public void testDelayCapturesEnvironment() {
    assertEquals(10, evalAsInt("(let ((x 10)) (force (delay x)))"));
  }

  @Test
  public void testDelayCapturesClosureEnvironment() {
    lisp.evaluate("(define (make-delayed-value x) (delay (* x 2)))");
    lisp.evaluate("(define p (make-delayed-value 21))");
    assertEquals(42, evalAsInt("(force p)"));
  }

  @Test
  public void testLazyStream() {
    // Simple lazy stream implementation
    lisp.evaluate("(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))");
    lisp.evaluate("(define naturals (integers-from 0))");

    assertEquals(0, evalAsInt("(car (force naturals))"));
    assertEquals(1, evalAsInt("(car (force (cdr (force naturals))))"));
    assertEquals(2, evalAsInt("(car (force (cdr (force (cdr (force naturals))))))"));
  }

  @Test
  public void testDelayWithSideEffects() {
    lisp.evaluate("(define side-effect-happened #f)");
    lisp.evaluate("(define p (delay (begin (set! side-effect-happened #t) 42)))");

    // Side effect should not happen until forced
    assertEquals(BooleanObject.FALSE, lisp.evaluate("side-effect-happened"));

    lisp.evaluate("(force p)");
    assertEquals(BooleanObject.TRUE, lisp.evaluate("side-effect-happened"));
  }

  @Test
  public void testDelayInFunction() {
    lisp.evaluate("(define (lazy-add a b) (delay (+ a b)))");
    assertEquals(7, evalAsInt("(force (lazy-add 3 4))"));
  }

  @Test
  public void testDelayWithConditional() {
    lisp.evaluate("(define p (delay (if #t 1 2)))");
    assertEquals(1, evalAsInt("(force p)"));
  }

  @Test
  public void testDelayWithBegin() {
    lisp.evaluate("(define x 0)");
    lisp.evaluate("(define p (delay (begin (set! x 1) (set! x (+ x 1)) x)))");

    assertEquals(0, evalAsInt("x"));
    assertEquals(2, evalAsInt("(force p)"));
    assertEquals(2, evalAsInt("x"));

    // Second force returns cached value, no side effects
    assertEquals(2, evalAsInt("(force p)"));
    assertEquals(2, evalAsInt("x"));
  }

  @Test
  public void testPromiseString() {
    String unforced = lisp.evaluate("(delay 42)").toString();
    assertTrue(unforced.contains("promise"));

    lisp.evaluate("(define p (delay 42))");
    lisp.evaluate("(force p)");
    String forced = lisp.evaluate("p").toString();
    assertTrue(forced.contains("promise"));
    assertTrue(forced.contains("forced"));
  }

  @Test
  public void testDelayWithMapAndFilter() {
    // Create lazy computation that uses map
    lisp.evaluate("(define lazy-squares (delay (map (lambda (x) (* x x)) '(1 2 3 4 5))))");
    assertEquals(
        "(1 4 9 16 25)", lisp.evaluate("(force lazy-squares)").toString().replace(" ", " "));
  }

  @Test
  public void testForcedPromiseEquality() {
    lisp.evaluate("(define p1 (delay 42))");
    lisp.evaluate("(define p2 (delay (+ 40 2)))");

    // Before forcing, can't compare with eq?
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(eq? p1 p2)"));

    // After forcing, values are equal
    lisp.evaluate("(force p1)");
    lisp.evaluate("(force p2)");
    assertEquals(42, evalAsInt("(force p1)"));
    assertEquals(42, evalAsInt("(force p2)"));
  }

  @Test
  public void testMakePromiseWithComputation() {
    // make-promise wraps already-computed value
    assertEquals(15, evalAsInt("(force (make-promise (+ 5 10)))"));
  }

  @Test
  public void testDelayWithRecursiveStructure() {
    // Define a lazy fibonacci stream
    lisp.evaluate(
        "(define (fib-gen a b) " + "  (delay (cons a (fib-gen b (+ a b)))))");
    lisp.evaluate("(define fibs (fib-gen 0 1))");

    // First few fibonacci numbers
    assertEquals(0, evalAsInt("(car (force fibs))"));
    assertEquals(1, evalAsInt("(car (force (cdr (force fibs))))"));
    assertEquals(1, evalAsInt("(car (force (cdr (force (cdr (force fibs))))))"));
    assertEquals(2, evalAsInt("(car (force (cdr (force (cdr (force (cdr (force fibs))))))))"));
  }

  @Test
  public void testDelayErrorNotThrownUntilForced() {
    // Error inside delay should not be thrown until forced
    lisp.evaluate("(define p (delay (/ 1 0)))");
    // This should succeed - no evaluation yet
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(promise? p)"));

    // Force should throw the error
    assertThrows(Exception.class, () -> lisp.evaluate("(force p)"));
  }
}
