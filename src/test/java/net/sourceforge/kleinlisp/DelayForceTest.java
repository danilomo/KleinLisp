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

/** Tests for R7RS delay-force special form. */
public class DelayForceTest extends BaseTestClass {

  @Test
  public void testDelayForceCreatesPromise() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(promise? (delay-force (delay 42)))"));
  }

  @Test
  public void testDelayForceBasic() {
    // delay-force should force the result iteratively
    assertEquals(42, evalAsInt("(force (delay-force (delay 42)))"));
  }

  @Test
  public void testDelayForceNestedPromises() {
    // delay-force should iteratively force nested promises
    assertEquals(42, evalAsInt("(force (delay-force (delay (delay 42))))"));
    assertEquals(42, evalAsInt("(force (delay-force (delay (delay (delay 42)))))"));
  }

  @Test
  public void testDelayForceVsDelay() {
    // Regular delay does NOT force nested promises
    assertEquals(
        BooleanObject.TRUE, lisp.evaluate("(promise? (force (delay (delay (delay 42)))))"));

    // delay-force DOES force nested promises iteratively
    assertEquals(42, evalAsInt("(force (delay-force (delay (delay 42))))"));
  }

  @Test
  public void testDelayForceWithComputation() {
    assertEquals(3, evalAsInt("(force (delay-force (delay (+ 1 2))))"));
  }

  @Test
  public void testDelayForceCachesResult() {
    // Side effect should only happen once
    lisp.evaluate("(define count 0)");
    lisp.evaluate("(define p (delay-force (delay (begin (set! count (+ count 1)) count))))");

    assertEquals(1, evalAsInt("(force p)"));
    assertEquals(1, evalAsInt("(force p)")); // Cached
    assertEquals(1, evalAsInt("count")); // Only incremented once
  }

  @Test
  public void testDelayForceWithNonPromiseResult() {
    // If the expression doesn't return a promise, delay-force behaves like delay
    assertEquals(42, evalAsInt("(force (delay-force 42))"));
    assertEquals(3, evalAsInt("(force (delay-force (+ 1 2)))"));
  }

  @Test
  public void testDelayForceStreamFilter() {
    // Classic R7RS example: stream-filter using delay-force
    // Now properly supports let bindings inside delayed expressions
    lisp.evaluate(
        "(define (stream-filter p? s)\n"
            + "  (delay-force\n"
            + "    (if (null? (force s))\n"
            + "        (delay '())\n"
            + "        (let ((h (car (force s)))\n"
            + "              (t (cdr (force s))))\n"
            + "          (if (p? h)\n"
            + "              (delay (cons h (stream-filter p? t)))\n"
            + "              (stream-filter p? t))))))");

    lisp.evaluate("(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))");
    lisp.evaluate("(define naturals (integers-from 0))");
    lisp.evaluate("(define evens (stream-filter even? naturals))");

    // Get first few even numbers
    assertEquals(0, evalAsInt("(car (force evens))"));
    assertEquals(2, evalAsInt("(car (force (cdr (force evens))))"));
    assertEquals(4, evalAsInt("(car (force (cdr (force (cdr (force evens))))))"));
  }

  @Test
  public void testDelayForceInfiniteStream() {
    // Create an infinite stream of ones using delay-force
    lisp.evaluate("(define ones (delay-force (delay (cons 1 ones))))");

    assertEquals(1, evalAsInt("(car (force ones))"));
    assertEquals(1, evalAsInt("(car (force (cdr (force ones))))"));
    assertEquals(1, evalAsInt("(car (force (cdr (force (cdr (force ones))))))"));
  }

  @Test
  public void testDelayForceWithConditional() {
    lisp.evaluate("(define p (delay-force (delay (if #t 1 2))))");
    assertEquals(1, evalAsInt("(force p)"));
  }

  @Test
  public void testDelayForceWithBegin() {
    lisp.evaluate("(define x 0)");
    lisp.evaluate("(define p (delay-force (delay (begin (set! x 1) (set! x (+ x 1)) x))))");

    assertEquals(0, evalAsInt("x"));
    assertEquals(2, evalAsInt("(force p)"));
    assertEquals(2, evalAsInt("x"));

    // Second force returns cached value, no side effects
    assertEquals(2, evalAsInt("(force p)"));
    assertEquals(2, evalAsInt("x"));
  }

  @Test
  public void testDelayForceCapturesEnvironment() {
    assertEquals(10, evalAsInt("(let ((x 10)) (force (delay-force (delay x))))"));
  }

  @Test
  public void testDelayForceCapturesClosureEnvironment() {
    lisp.evaluate("(define (make-delayed-value x) (delay-force (delay (* x 2))))");
    lisp.evaluate("(define p (make-delayed-value 21))");
    assertEquals(42, evalAsInt("(force p)"));
  }

  @Test
  public void testDelayForceErrorNotThrownUntilForced() {
    // Error inside delay-force should not be thrown until forced
    lisp.evaluate("(define p (delay-force (delay (/ 1 0))))");
    // This should succeed - no evaluation yet
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(promise? p)"));

    // Force should throw the error
    assertThrows(Exception.class, () -> lisp.evaluate("(force p)"));
  }

  @Test
  public void testDelayForceLongChain() {
    // Test that delay-force doesn't cause stack overflow on long chains
    // Build a chain of nested delays programmatically
    lisp.evaluate(
        "(define (make-chain n val)\n"
            + "  (if (= n 0)\n"
            + "      val\n"
            + "      (delay (make-chain (- n 1) val))))");

    lisp.evaluate("(define long-chain (delay-force (make-chain 100 42)))");
    assertEquals(42, evalAsInt("(force long-chain)"));
  }

  @Test
  public void testDelayForceWithMap() {
    // Create lazy computation that uses map
    lisp.evaluate(
        "(define lazy-squares (delay-force (delay (map (lambda (x) (* x x)) '(1 2 3 4 5)))))");
    assertEquals("(1 4 9 16 25)", lisp.evaluate("(force lazy-squares)").toString());
  }

  @Test
  public void testDelayForcePromiseString() {
    String unforced = lisp.evaluate("(delay-force (delay 42))").toString();
    assertTrue(unforced.contains("promise"));

    lisp.evaluate("(define p (delay-force (delay 42)))");
    lisp.evaluate("(force p)");
    String forced = lisp.evaluate("p").toString();
    assertTrue(forced.contains("promise"));
    assertTrue(forced.contains("forced"));
  }

  @Test
  public void testDelayForceR7RSExample() {
    // From R7RS specification section 4.2.5
    lisp.evaluate("(define count 0)");
    lisp.evaluate(
        "(define p (delay (begin (set! count (+ count 1))\n"
            + "                                 (if (> count x)\n"
            + "                                     count\n"
            + "                                     (force (delay-force p))))))");
    lisp.evaluate("(define x 5)");

    assertEquals(6, evalAsInt("(force p)"));
    // Promise was forced multiple times until count > x
    assertEquals(6, evalAsInt("count"));

    // Second force should return cached value
    assertEquals(6, evalAsInt("(force p)"));
    assertEquals(6, evalAsInt("count")); // count unchanged
  }

  @Test
  public void testDelayForceWithLetBindings() {
    // Test that let bindings are properly captured in delay-force
    lisp.evaluate("(define p (delay-force (let ((x 10) (y 20)) (delay (+ x y)))))");
    assertEquals(30, evalAsInt("(force p)"));

    // Test nested let bindings
    lisp.evaluate("(define p2 (delay-force (let ((x 5)) (delay (let ((y 10)) (+ x y))))))");
    assertEquals(15, evalAsInt("(force p2)"));
  }

  @Test
  public void testDelayForceStreamMap() {
    // Stream map implementation using delay-force
    lisp.evaluate(
        "(define (stream-map f s)\n"
            + "  (delay-force\n"
            + "    (if (null? (force s))\n"
            + "        (delay '())\n"
            + "        (delay (cons (f (car (force s)))\n"
            + "                     (stream-map f (cdr (force s))))))))");

    lisp.evaluate("(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))");
    lisp.evaluate("(define naturals (integers-from 1))");
    lisp.evaluate("(define squares (stream-map (lambda (x) (* x x)) naturals))");

    assertEquals(1, evalAsInt("(car (force squares))"));
    assertEquals(4, evalAsInt("(car (force (cdr (force squares))))"));
    assertEquals(9, evalAsInt("(car (force (cdr (force (cdr (force squares))))))"));
  }
}
