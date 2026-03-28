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

/** Tests for R7RS let-values, let*-values, and define-values. */
public class LetValuesTest extends BaseTestClass {

  // let-values tests

  @Test
  public void testLetValuesSingle() {
    assertEquals("1", lisp.evaluate("(let-values (((a) (values 1))) a)").toString());
  }

  @Test
  public void testLetValuesMultiple() {
    assertEquals(
        "(1 2)", lisp.evaluate("(let-values (((a b) (values 1 2))) (list a b))").toString());
  }

  @Test
  public void testLetValuesMultipleBindings() {
    assertEquals(
        "(1 2 3 4 5)",
        lisp.evaluate(
                "(let-values (((a b) (values 1 2)) "
                    + "             ((c d e) (values 3 4 5))) "
                    + "  (list a b c d e))")
            .toString());
  }

  @Test
  public void testLetValuesFromFunction() {
    lisp.evaluate("(define (div-mod n d) (values (quotient n d) (remainder n d)))");
    assertEquals(
        "(3 1)", lisp.evaluate("(let-values (((q r) (div-mod 10 3))) (list q r))").toString());
  }

  @Test
  public void testLetValuesParallel() {
    // Both expressions should see original 'a' value
    lisp.evaluate("(define a 10)");
    assertEquals(
        "(1 10)",
        lisp.evaluate(
                "(let-values (((a) (values 1)) "
                    + "             ((b) (values a))) " // Should see outer 'a' = 10
                    + "  (list a b))")
            .toString());
  }

  // let*-values tests

  @Test
  public void testLetStarValuesSingle() {
    assertEquals("1", lisp.evaluate("(let*-values (((a) (values 1))) a)").toString());
  }

  @Test
  public void testLetStarValuesSequential() {
    assertEquals(
        "3",
        lisp.evaluate(
                "(let*-values (((a b) (values 1 2)) "
                    + "              ((c) (values (+ a b)))) " // Can reference a, b
                    + "  c)")
            .toString());
  }

  @Test
  public void testLetStarValuesChain() {
    assertEquals(
        "(1 2 3 6)",
        lisp.evaluate(
                "(let*-values (((a) (values 1)) "
                    + "              ((b) (values (+ a 1))) "
                    + "              ((c) (values (+ b 1))) "
                    + "              ((sum) (values (+ a b c)))) "
                    + "  (list a b c sum))")
            .toString());
  }

  @Test
  public void testLetStarValuesMultipleVarsPerBinding() {
    assertEquals(
        "(1 2 3 5)",
        lisp.evaluate(
                "(let*-values (((a b) (values 1 2)) "
                    + "              ((c d) (values (+ a b) (+ a b 2)))) "
                    + "  (list a b c d))")
            .toString());
  }

  // define-values tests

  @Test
  public void testDefineValues() {
    lisp.evaluate("(define-values (x y) (values 10 20))");
    assertEquals("10", lisp.evaluate("x").toString());
    assertEquals("20", lisp.evaluate("y").toString());
  }

  @Test
  public void testDefineValuesFromFunction() {
    lisp.evaluate("(define (min-max a b) (if (< a b) (values a b) (values b a)))");
    lisp.evaluate("(define-values (lo hi) (min-max 5 3))");
    assertEquals("3", lisp.evaluate("lo").toString());
    assertEquals("5", lisp.evaluate("hi").toString());
  }

  @Test
  public void testDefineValuesThreeValues() {
    lisp.evaluate("(define-values (a b c) (values 1 2 3))");
    assertEquals("6", lisp.evaluate("(+ a b c)").toString());
  }

  @Test
  public void testDefineValuesSingleValue() {
    lisp.evaluate("(define-values (x) (values 42))");
    assertEquals("42", lisp.evaluate("x").toString());
  }

  @Test
  public void testDefineValuesSingleNonValues() {
    // Single value without using (values ...)
    lisp.evaluate("(define-values (x) 42)");
    assertEquals("42", lisp.evaluate("x").toString());
  }

  // Error cases

  @Test
  public void testLetValuesWrongCount() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(let-values (((a b c) (values 1 2))) a)");
        });
  }

  @Test
  public void testLetValuesTooFewValues() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(let-values (((a b) (values 1))) a)");
        });
  }

  @Test
  public void testDefineValuesWrongCount() {
    assertThrows(
        LispArgumentError.class,
        () -> {
          lisp.evaluate("(define-values (a b c) (values 1 2))");
        });
  }

  // Empty bindings

  @Test
  public void testLetValuesEmptyBindings() {
    assertEquals("42", lisp.evaluate("(let-values () 42)").toString());
  }

  @Test
  public void testLetStarValuesEmptyBindings() {
    assertEquals("42", lisp.evaluate("(let*-values () 42)").toString());
  }

  // Single value (not values object)

  @Test
  public void testLetValuesSingleNonValues() {
    assertEquals("42", lisp.evaluate("(let-values (((a) 42)) a)").toString());
  }

  // Nested let-values

  @Test
  public void testNestedLetValues() {
    assertEquals(
        "6",
        lisp.evaluate(
                "(let-values (((a b) (values 1 2))) "
                    + "  (let-values (((c) (values (+ a b)))) "
                    + "    (+ a b c)))")
            .toString());
  }

  // Multiple body expressions

  @Test
  public void testLetValuesMultipleBody() {
    assertEquals(
        "3",
        lisp.evaluate(
                "(let-values (((a b) (values 1 2))) "
                    + "  (+ a 0) " // Evaluated but discarded
                    + "  (+ a b))") // Returned
            .toString());
  }

  @Test
  public void testLetStarValuesMultipleBody() {
    assertEquals(
        "6",
        lisp.evaluate(
                "(let*-values (((a) (values 1)) "
                    + "              ((b) (values 2))) "
                    + "  (define c 3) "
                    + "  (+ a b c))")
            .toString());
  }

  // Zero formals

  @Test
  public void testLetValuesZeroFormals() {
    assertEquals("42", lisp.evaluate("(let-values ((() (values))) 42)").toString());
  }

  // Using exact-integer-sqrt which returns multiple values

  @Test
  public void testLetValuesWithExactIntegerSqrt() {
    assertEquals(
        "(3 2)",
        lisp.evaluate("(let-values (((s r) (exact-integer-sqrt 11))) (list s r))").toString());
  }

  @Test
  public void testLetStarValuesWithExactIntegerSqrt() {
    assertEquals(
        "5",
        lisp.evaluate(
                "(let*-values (((s r) (exact-integer-sqrt 11)) "
                    + "              ((sum) (values (+ s r)))) "
                    + "  sum)")
            .toString());
  }

  // Shadowing behavior

  @Test
  public void testLetValuesShadowing() {
    lisp.evaluate("(define x 100)");
    assertEquals("1", lisp.evaluate("(let-values (((x) (values 1))) x)").toString());
    // Original x should be unchanged
    assertEquals("100", lisp.evaluate("x").toString());
  }

  @Test
  public void testLetStarValuesShadowing() {
    lisp.evaluate("(define x 100)");
    assertEquals(
        "2",
        lisp.evaluate(
                "(let*-values (((x) (values 1)) "
                    + "              ((y) (values (+ x 1)))) " // Uses shadowed x = 1
                    + "  y)")
            .toString());
    // Original x should be unchanged
    assertEquals("100", lisp.evaluate("x").toString());
  }
}
