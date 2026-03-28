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

/** Tests for R7RS parameters: make-parameter, parameterize, parameter?. */
public class ParameterTest extends BaseTestClass {

  @Test
  public void testMakeParameter() {
    lisp.evaluate("(define radix (make-parameter 10))");
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(parameter? radix)"));
  }

  @Test
  public void testParameterGet() {
    lisp.evaluate("(define radix (make-parameter 10))");
    assertEquals(10, evalAsInt("(radix)"));
  }

  @Test
  public void testParameterSet() {
    lisp.evaluate("(define radix (make-parameter 10))");
    lisp.evaluate("(radix 16)");
    assertEquals(16, evalAsInt("(radix)"));
  }

  @Test
  public void testParameterize() {
    lisp.evaluate("(define radix (make-parameter 10))");
    assertEquals(2, evalAsInt("(parameterize ((radix 2)) (radix))"));
  }

  @Test
  public void testParameterizeRestores() {
    lisp.evaluate("(define radix (make-parameter 10))");
    lisp.evaluate("(parameterize ((radix 2)) (radix))");
    assertEquals(10, evalAsInt("(radix)"));
  }

  @Test
  public void testNestedParameterize() {
    lisp.evaluate("(define radix (make-parameter 10))");
    assertEquals(
        2,
        evalAsInt(
            "(parameterize ((radix 16)) " + "  (parameterize ((radix 2)) " + "    (radix)))"));
  }

  @Test
  public void testNestedParameterizeRestores() {
    lisp.evaluate("(define radix (make-parameter 10))");
    assertEquals(
        16,
        evalAsInt(
            "(parameterize ((radix 16)) "
                + "  (parameterize ((radix 2)) (radix)) "
                + "  (radix))"));
  }

  @Test
  public void testMultipleParameters() {
    lisp.evaluate("(define a (make-parameter 1))");
    lisp.evaluate("(define b (make-parameter 2))");
    assertEquals(30, evalAsInt("(parameterize ((a 10) (b 20)) " + "  (+ (a) (b)))"));
  }

  @Test
  public void testParameterConverter() {
    lisp.evaluate(
        "(define int-param (make-parameter 0 "
            + "  (lambda (x) (if (integer? x) x (error \"not an integer\")))))");
    assertEquals(42, evalAsInt("(begin (int-param 42) (int-param))"));
  }

  @Test
  public void testParameterConverterOnInit() {
    // Converter is applied to initial value
    lisp.evaluate("(define doubled (make-parameter 5 (lambda (x) (* x 2))))");
    assertEquals(10, evalAsInt("(doubled)"));
  }

  @Test
  public void testParameterConverterOnParameterize() {
    lisp.evaluate("(define doubled (make-parameter 5 (lambda (x) (* x 2))))");
    assertEquals(20, evalAsInt("(parameterize ((doubled 10)) (doubled))"));
  }

  @Test
  public void testParameterPredicate() {
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(parameter? (make-parameter 1))"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(parameter? 1)"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(parameter? (lambda () 1))"));
  }

  @Test
  public void testParameterizeWithException() {
    lisp.evaluate("(define p (make-parameter 1))");
    try {
      lisp.evaluate("(parameterize ((p 100)) " + "  (error \"test error\"))");
      fail("Should have thrown");
    } catch (Exception e) {
      // Expected
    }
    // Parameter should still be restored after exception
    assertEquals(1, evalAsInt("(p)"));
  }

  @Test
  public void testParameterInFunction() {
    lisp.evaluate("(define radix (make-parameter 10))");
    lisp.evaluate("(define (get-radix) (radix))");
    assertEquals(10, evalAsInt("(get-radix)"));
    assertEquals(16, evalAsInt("(parameterize ((radix 16)) (get-radix))"));
  }

  @Test
  public void testEmptyParameterize() {
    assertEquals(42, evalAsInt("(parameterize () 42)"));
  }

  @Test
  public void testParameterizeWithMultipleBodyExpressions() {
    lisp.evaluate("(define p (make-parameter 0))");
    assertEquals(
        3,
        evalAsInt("(parameterize ((p 1)) " + "  (p (+ (p) 1)) " + "  (p (+ (p) 1)) " + "  (p))"));
  }

  @Test
  public void testParameterString() {
    String repr = lisp.evaluate("(make-parameter 42)").toString();
    assertTrue(repr.contains("parameter"));
    assertTrue(repr.contains("42"));
  }

  @Test
  public void testParameterDynamicScope() {
    // Test that parameters have dynamic scope, not lexical scope
    lisp.evaluate("(define p (make-parameter 1))");
    lisp.evaluate("(define (inner) (p))");
    lisp.evaluate("(define (outer) (parameterize ((p 2)) (inner)))");

    // inner sees p's dynamic value from outer's parameterize
    assertEquals(2, evalAsInt("(outer)"));
    // but outside of outer, p is still 1
    assertEquals(1, evalAsInt("(p)"));
  }

  @Test
  public void testParameterConverterValidation() {
    // Converter that rejects non-strings
    lisp.evaluate(
        "(define str-param (make-parameter \"default\" "
            + "  (lambda (x) (if (string? x) x (error \"not a string\")))))");

    assertEquals(
        "\"hello\"", lisp.evaluate("(begin (str-param \"hello\") (str-param))").toString());

    // Should fail with non-string
    assertThrows(Exception.class, () -> lisp.evaluate("(str-param 123)"));
  }

  @Test
  public void testParameterSetAndGetInSequence() {
    lisp.evaluate("(define p (make-parameter 0))");
    lisp.evaluate("(p 1)");
    assertEquals(1, evalAsInt("(p)"));
    lisp.evaluate("(p 2)");
    assertEquals(2, evalAsInt("(p)"));
    lisp.evaluate("(p 3)");
    assertEquals(3, evalAsInt("(p)"));
  }

  @Test
  public void testParameterizeDoesNotAffectOuterScope() {
    lisp.evaluate("(define p (make-parameter 0))");
    lisp.evaluate("(p 5)");
    lisp.evaluate("(parameterize ((p 100)) (p))");
    assertEquals(5, evalAsInt("(p)")); // Outer value preserved
  }

  @Test
  public void testParameterWithBooleanValue() {
    lisp.evaluate("(define debug (make-parameter #f))");
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(debug)"));

    assertEquals(
        "\"debugging\"",
        lisp.evaluate("(parameterize ((debug #t)) (if (debug) \"debugging\" \"normal\"))")
            .toString());
  }

  @Test
  public void testParameterWithListValue() {
    lisp.evaluate("(define settings (make-parameter '()))");
    assertEquals("()", lisp.evaluate("(settings)").toString());

    lisp.evaluate("(settings '(a b c))");
    assertEquals("(a b c)", lisp.evaluate("(settings)").toString());
  }

  @Test
  public void testDeepNestedParameterize() {
    lisp.evaluate("(define p (make-parameter 0))");
    assertEquals(
        5,
        evalAsInt(
            "(parameterize ((p 1)) "
                + "  (parameterize ((p 2)) "
                + "    (parameterize ((p 3)) "
                + "      (parameterize ((p 4)) "
                + "        (parameterize ((p 5)) "
                + "          (p))))))"));

    // All levels should be restored
    assertEquals(0, evalAsInt("(p)"));
  }
}
