/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 */
package net.sourceforge.kleinlisp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for internal definitions (nested define in function bodies). R7RS requires that internal
 * definitions at the beginning of a body be treated as letrec*.
 */
public class InternalDefineTest extends BaseTestClass {

  @Test
  public void testNestedDefineInFunction() {
    // This should work like:
    // (define (outer x)
    //   (letrec* ((inner (lambda (y) (* y 2))))
    //     (+ x (inner x))))
    lisp.evaluate("(define (outer x) (define (inner y) (* y 2)) (+ x (inner x)))");
    assertEquals(15, evalAsInt("(outer 5)")); // 5 + (5 * 2) = 15
  }

  @Test
  public void testNestedDefineVariable() {
    lisp.evaluate("(define (foo) (define x 10) (+ x 5))");
    assertEquals(15, evalAsInt("(foo)"));
  }

  @Test
  public void testMultipleInternalDefines() {
    lisp.evaluate("(define (bar) (define a 1) (define b 2) (define c 3) (+ a b c))");
    assertEquals(6, evalAsInt("(bar)"));
  }

  @Test
  public void testInternalDefineWithMutualRecursion() {
    lisp.evaluate(
        "(define (test-mutual)"
            + "  (define (even? n) (if (= n 0) #t (odd? (- n 1))))"
            + "  (define (odd? n) (if (= n 0) #f (even? (- n 1))))"
            + "  (even? 4))");
    assertTrue(lisp.evaluate("(test-mutual)").truthiness());
  }

  @Test
  public void testWorkaroundWithLetrecStar() {
    // This is the workaround using letrec* directly
    lisp.evaluate("(define (outer2 x) (letrec* ((inner (lambda (y) (* y 2)))) (+ x (inner x))))");
    assertEquals(15, evalAsInt("(outer2 5)")); // 5 + (5 * 2) = 15
  }
}
