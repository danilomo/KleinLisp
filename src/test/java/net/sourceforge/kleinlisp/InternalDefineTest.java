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

  @Test
  public void testInternalDefineDoesNotLeakToGlobal() {
    // Create a fresh Lisp instance to ensure clean global environment
    Lisp freshLisp = new Lisp();

    // Define a function with internal define
    freshLisp.evaluate("(define (test-scope) (define local-var 42) local-var)");

    // Call the function - should return 42
    assertEquals(42, freshLisp.evaluate("(test-scope)").asInt().value);

    // The internal variable should NOT exist in global scope
    // Looking up an undefined variable should throw UnboundVariableException
    UnboundVariableException ex =
        assertThrows(UnboundVariableException.class, () -> freshLisp.evaluate("local-var"));
    assertEquals("local-var", ex.getSymbolName());
  }

  @Test
  public void testInternalDefinesCanReferenceEachOther() {
    // Internal defines can reference each other (like letrec*)
    lisp.evaluate(
        "(define (test-ref)"
            + "  (define a 10)"
            + "  (define b (+ a 5))" // b references a
            + "  (define c (+ a b))" // c references both a and b
            + "  c)");
    assertEquals(25, evalAsInt("(test-ref)")); // 10 + (10 + 5) = 25
  }

  @Test
  public void testInternalDefineInLambda() {
    // Test that internal defines also work in anonymous lambdas
    lisp.evaluate("(define my-func (lambda () (define x 100) (define y 200) (+ x y)))");
    assertEquals(300, evalAsInt("(my-func)"));
  }
}
