/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 */
package net.sourceforge.kleinlisp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LetBindingDebugTest extends BaseTestClass {

  @Test
  public void testSimpleLetBinding() {
    // Basic let binding works
    assertEquals(5, lisp.evaluate("(let ((x 5)) x)").asInt().value);
  }

  @Test
  public void testLetBindingWithLambdaNotCalled() {
    // Let binding with lambda that captures it
    lisp.evaluate("(define f (let ((x 42)) (lambda () x)))");
    // Now call the lambda
    assertEquals(42, lisp.evaluate("(f)").asInt().value);
  }

  @Test
  public void testMakeAdderDirectCall() {
    // Direct call, not through map
    lisp.evaluate("(define (make-adder item) (let ((x item)) (lambda () x)))");
    lisp.evaluate("(define adder (make-adder 10))");
    LispObject result = lisp.evaluate("(adder)");
    assertNotNull(result, "adder() returned null");
    assertEquals(10, result.asInt().value);
  }

  @Test
  public void testLetBindingFromParameterSimple() {
    // Let binding that captures function parameter
    lisp.evaluate("(define (test-param p) (let ((x p)) x))");
    assertEquals(42, lisp.evaluate("(test-param 42)").asInt().value);
  }

  @Test
  public void testLetBindingFromParameterWithLambda() {
    // Let binding that captures function parameter, used in lambda
    lisp.evaluate("(define (test-param p) (let ((x p)) (lambda () x)))");
    // Don't call yet, just check that the function was defined
    assertNotNull(lisp.evaluate("test-param"));
    // Now call it
    lisp.evaluate("(define f (test-param 42))");
    // Check that f is a function
    assertNotNull(lisp.evaluate("f"));
    // Now call f
    LispObject result = lisp.evaluate("(f)");
    assertNotNull(result, "f() returned null");
    assertEquals(42, result.asInt().value);
  }

  @Test
  public void testLambdaInsideLetReturnsValue() {
    // Simplest test: lambda inside let that returns captured literal
    lisp.evaluate("(define f (let ((x 5)) (lambda () x)))");
    LispObject result = lisp.evaluate("(f)");
    assertNotNull(result, "f() returned null");
    assertEquals(5, result.asInt().value);
  }

  @Test
  public void testFunctionThatMakesClosureOverLet() {
    // Function that creates a closure over a let binding
    lisp.evaluate("(define (make-fn val) (let ((x val)) (lambda () x)))");
    lisp.evaluate("(define f1 (make-fn 100))");
    LispObject r1 = lisp.evaluate("(f1)");
    assertNotNull(r1, "f1() returned null");
    assertEquals(100, r1.asInt().value);
  }

  @Test
  public void testTwoMakeAdderDirectCalls() {
    // Two direct calls
    lisp.evaluate("(define (make-adder item) (let ((x item)) (lambda () x)))");
    lisp.evaluate("(define adder1 (make-adder 10))");
    lisp.evaluate("(define adder2 (make-adder 20))");
    assertEquals(10, lisp.evaluate("(adder1)").asInt().value);
    assertEquals(20, lisp.evaluate("(adder2)").asInt().value);
  }

  @Test
  public void testMapSimple() {
    // Map with simple lambda
    lisp.evaluate("(define nums (map (lambda (x) (* x 2)) (list 1 2 3)))");
    assertEquals(2, lisp.evaluate("(car nums)").asInt().value);
    assertEquals(4, lisp.evaluate("(cadr nums)").asInt().value);
    assertEquals(6, lisp.evaluate("(caddr nums)").asInt().value);
  }

  @Test
  public void testMapWithNamedFunction() {
    // Map with named function that has no let
    lisp.evaluate("(define (double x) (* x 2))");
    lisp.evaluate("(define nums (map double (list 1 2 3)))");
    assertEquals(2, lisp.evaluate("(car nums)").asInt().value);
    assertEquals(4, lisp.evaluate("(cadr nums)").asInt().value);
    assertEquals(6, lisp.evaluate("(caddr nums)").asInt().value);
  }

  @Test
  public void testMapWithMakeAdder() {
    // Map with make-adder
    lisp.evaluate("(define (make-adder item) (let ((x item)) (lambda () x)))");
    lisp.evaluate("(define adders (map make-adder (list 1 2 3)))");

    // Now call each adder
    assertEquals(1, lisp.evaluate("((car adders))").asInt().value);
    assertEquals(2, lisp.evaluate("((cadr adders))").asInt().value);
    assertEquals(3, lisp.evaluate("((caddr adders))").asInt().value);
  }
}
