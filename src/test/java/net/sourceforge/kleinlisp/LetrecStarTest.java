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
 * Tests for the R7RS letrec* implementation.
 *
 * @author danilo
 */
public class LetrecStarTest extends BaseTestClass {

  @Test
  public void testSimpleBinding() {
    assertEquals(1, evalAsInt("(letrec* ((x 1)) x)"));
  }

  @Test
  public void testSequentialDependency() {
    // b can reference a because a is evaluated first
    assertEquals(3, evalAsInt("(letrec* ((a 1) (b (+ a 1)) (c (+ b 1))) c)"));
  }

  @Test
  public void testMutualRecursionEven() {
    assertEquals(
        "true",
        lisp.evaluate(
                "(letrec* ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1))))) "
                    + "          (odd? (lambda (n) (if (= n 0) #f (even? (- n 1)))))) "
                    + "  (even? 10))")
            .toString());
  }

  @Test
  public void testMutualRecursionOdd() {
    assertEquals(
        "true",
        lisp.evaluate(
                "(letrec* ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1))))) "
                    + "          (odd? (lambda (n) (if (= n 0) #f (even? (- n 1)))))) "
                    + "  (odd? 7))")
            .toString());
  }

  @Test
  public void testMutualRecursionFalse() {
    assertEquals(
        "false",
        lisp.evaluate(
                "(letrec* ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1))))) "
                    + "          (odd? (lambda (n) (if (= n 0) #f (even? (- n 1)))))) "
                    + "  (odd? 4))")
            .toString());
  }

  @Test
  public void testMultipleBodyExpressions() {
    assertEquals(3, evalAsInt("(letrec* ((x 1) (y 2)) (+ x 0) (+ x y))"));
  }

  @Test
  public void testNestedLetrecStar() {
    assertEquals(
        6,
        evalAsInt("(letrec* ((x 1)) (letrec* ((y (+ x 1)) (z (+ y 1))) (+ x y z)))"));
  }

  @Test
  public void testShadowing() {
    lisp.evaluate("(define x 100)");
    assertEquals(1, evalAsInt("(letrec* ((x 1)) x)"));
    assertEquals(100, evalAsInt("x"));
  }

  @Test
  public void testRecursiveFunction() {
    assertEquals(
        120,
        evalAsInt(
            "(letrec* ((fact (lambda (n) "
                + "                  (if (= n 0) "
                + "                      1 "
                + "                      (* n (fact (- n 1))))))) "
                + "  (fact 5))"));
  }

  @Test
  public void testLambdaCapture() {
    assertEquals(15, evalAsInt("(letrec* ((x 10) (f (lambda (y) (+ x y)))) (f 5))"));
  }

  @Test
  public void testSequentialWithFunctions() {
    assertEquals(
        10,
        evalAsInt(
            "(letrec* ((double (lambda (x) (* x 2))) "
                + "          (five 5) "
                + "          (ten (double five))) "
                + "  ten)"));
  }

  @Test
  public void testEmptyBindings() {
    assertEquals(42, evalAsInt("(letrec* () 42)"));
  }

  @Test
  public void testDifferenceFromLetrecWithNonLambda() {
    // In letrec*, later bindings can use earlier non-lambda values
    assertEquals(2, evalAsInt("(letrec* ((a 1) (b (+ a 1))) b)"));
  }

  @Test
  public void testChainedDependencies() {
    // a -> b -> c -> d chain
    assertEquals(10, evalAsInt("(letrec* ((a 1) (b (+ a 2)) (c (+ b 3)) (d (+ c 4))) d)"));
  }

  @Test
  public void testAccessOuterScope() {
    lisp.evaluate("(define multiplier 10)");
    assertEquals(20, evalAsInt("(letrec* ((x 2) (y (* x multiplier))) y)"));
  }

  @Test
  public void testListInBinding() {
    assertEquals(
        "(1 2 3)",
        lisp.evaluate("(letrec* ((a 1) (b 2) (c 3) (lst (list a b c))) lst)").toString());
  }

  @Test
  public void testLetrecStarInFunction() {
    lisp.evaluate(
        "(define (my-func n) "
            + "  (letrec* ((fact (lambda (x) (if (= x 0) 1 (* x (fact (- x 1))))))) "
            + "    (fact n)))");
    assertEquals(120, evalAsInt("(my-func 5)"));
    assertEquals(1, evalAsInt("(my-func 0)"));
  }

  @Test
  public void testComplexMutualRecursion() {
    // Fibonacci using mutual recursion pattern
    assertEquals(
        8,
        evalAsInt(
            "(letrec* ((fib (lambda (n) "
                + "                 (if (<= n 1) "
                + "                     n "
                + "                     (+ (fib (- n 1)) (fib (- n 2))))))) "
                + "  (fib 6))"));
  }

  @Test
  public void testSumWithFold() {
    assertEquals(
        15,
        evalAsInt(
            "(letrec* ((fold (lambda (f init lst) "
                + "                  (if (null? lst) "
                + "                      init "
                + "                      (fold f (f init (car lst)) (cdr lst))))) "
                + "          (sum (lambda (lst) (fold + 0 lst)))) "
                + "  (sum '(1 2 3 4 5)))"));
  }

  @Test
  public void testSingleExpressionBody() {
    assertEquals(3, evalAsInt("(letrec* ((x 1) (y 2)) (+ x y))"));
  }

  @Test
  public void testBooleanResult() {
    assertEquals("true", lisp.evaluate("(letrec* ((x #t)) x)").toString());
    assertEquals("false", lisp.evaluate("(letrec* ((x #f)) x)").toString());
  }
}
