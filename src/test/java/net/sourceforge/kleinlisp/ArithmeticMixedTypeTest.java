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

import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for arithmetic operations with mixed int/double types and numeric tower
 * behavior.
 */
public class ArithmeticMixedTypeTest extends BaseTestClass {

  // ==================== ADDITION (+) ====================

  @Test
  public void testAddIntOnly() {
    assertEquals("6", lisp.evaluate("(+ 1 2 3)").toString());
    assertEquals("0", lisp.evaluate("(+)").toString());
    assertEquals("5", lisp.evaluate("(+ 5)").toString());
    assertEquals("-3", lisp.evaluate("(+ -1 -2)").toString());
  }

  @Test
  public void testAddDoubleOnly() {
    assertEquals("3.5", lisp.evaluate("(+ 1.0 2.5)").toString());
    assertEquals("0.0", lisp.evaluate("(+ 0.0)").toString());
    assertEquals("-1.5", lisp.evaluate("(+ -0.5 -1.0)").toString());
  }

  @Test
  public void testAddMixedTypes() {
    // The key bug fix: (+ 1 1.2) should return 2.2, not 2
    assertEquals("2.2", lisp.evaluate("(+ 1 1.2)").toString());
    assertEquals("3.5", lisp.evaluate("(+ 1 2.5)").toString());
    assertEquals("6.5", lisp.evaluate("(+ 1 2 3.5)").toString());
    assertEquals("6.5", lisp.evaluate("(+ 1.5 2 3)").toString());
    assertEquals("6.0", lisp.evaluate("(+ 1 2 3.0)").toString());
  }

  @Test
  public void testAddReturnsCorrectType() {
    // Integer addition should return IntObject
    LispObject intResult = lisp.evaluate("(+ 1 2)");
    assertTrue(intResult instanceof IntObject, "Integer addition should return IntObject");

    // Mixed addition should return DoubleObject
    LispObject doubleResult = lisp.evaluate("(+ 1 2.0)");
    assertTrue(doubleResult instanceof DoubleObject, "Mixed addition should return DoubleObject");
  }

  // ==================== SUBTRACTION (-) ====================

  @Test
  public void testSubIntOnly() {
    assertEquals("5", lisp.evaluate("(- 10 3 2)").toString());
    assertEquals("-5", lisp.evaluate("(- 5)").toString()); // Unary negation
    assertEquals("0", lisp.evaluate("(- 5 5)").toString());
  }

  @Test
  public void testSubDoubleOnly() {
    assertEquals("1.5", lisp.evaluate("(- 3.5 2.0)").toString());
    assertEquals("-2.5", lisp.evaluate("(- 2.5)").toString()); // Unary negation
  }

  @Test
  public void testSubMixedTypes() {
    assertEquals("0.5", lisp.evaluate("(- 3 2.5)").toString());
    assertEquals("-0.5", lisp.evaluate("(- 2.5 3)").toString());
    assertEquals("1.5", lisp.evaluate("(- 5.5 2 2)").toString());
    assertEquals("0.5", lisp.evaluate("(- 5 2 2.5)").toString());
  }

  @Test
  public void testSubReturnsCorrectType() {
    LispObject intResult = lisp.evaluate("(- 5 3)");
    assertTrue(intResult instanceof IntObject, "Integer subtraction should return IntObject");

    LispObject doubleResult = lisp.evaluate("(- 5 3.0)");
    assertTrue(
        doubleResult instanceof DoubleObject, "Mixed subtraction should return DoubleObject");

    // Unary negation
    LispObject unaryInt = lisp.evaluate("(- 5)");
    assertTrue(unaryInt instanceof IntObject, "Unary negation of int should return IntObject");

    LispObject unaryDouble = lisp.evaluate("(- 5.0)");
    assertTrue(
        unaryDouble instanceof DoubleObject, "Unary negation of double should return DoubleObject");
  }

  // ==================== MULTIPLICATION (*) ====================

  @Test
  public void testMulIntOnly() {
    assertEquals("24", lisp.evaluate("(* 2 3 4)").toString());
    assertEquals("1", lisp.evaluate("(*)").toString()); // Identity
    assertEquals("5", lisp.evaluate("(* 5)").toString());
    assertEquals("-6", lisp.evaluate("(* -2 3)").toString());
  }

  @Test
  public void testMulDoubleOnly() {
    assertEquals("6.0", lisp.evaluate("(* 2.0 3.0)").toString());
    assertEquals("0.25", lisp.evaluate("(* 0.5 0.5)").toString());
  }

  @Test
  public void testMulMixedTypes() {
    assertEquals("6.0", lisp.evaluate("(* 2 3.0)").toString());
    assertEquals("7.5", lisp.evaluate("(* 3 2.5)").toString());
    assertEquals("12.0", lisp.evaluate("(* 2 3 2.0)").toString());
    assertEquals("15.0", lisp.evaluate("(* 2.5 2 3)").toString());
  }

  @Test
  public void testMulReturnsCorrectType() {
    LispObject intResult = lisp.evaluate("(* 2 3)");
    assertTrue(intResult instanceof IntObject, "Integer multiplication should return IntObject");

    LispObject doubleResult = lisp.evaluate("(* 2 3.0)");
    assertTrue(
        doubleResult instanceof DoubleObject, "Mixed multiplication should return DoubleObject");
  }

  // ==================== DIVISION (/) ====================

  @Test
  public void testDivIntExact() {
    // Exact integer division should return int
    assertEquals("3", lisp.evaluate("(/ 6 2)").toString());
    assertEquals("2", lisp.evaluate("(/ 24 4 3)").toString());
    assertEquals("-3", lisp.evaluate("(/ -6 2)").toString());
  }

  @Test
  public void testDivIntInexact() {
    // Inexact integer division should return double
    assertEquals("0.5", lisp.evaluate("(/ 1 2)").toString());
    String result = lisp.evaluate("(/ 1 3)").toString();
    assertTrue(result.startsWith("0.333"), "1/3 should be approximately 0.333...");
  }

  @Test
  public void testDivDoubleOnly() {
    assertEquals("2.0", lisp.evaluate("(/ 6.0 3.0)").toString());
    assertEquals("0.5", lisp.evaluate("(/ 1.0 2.0)").toString());
  }

  @Test
  public void testDivMixedTypes() {
    assertEquals("2.0", lisp.evaluate("(/ 6 3.0)").toString());
    assertEquals("2.0", lisp.evaluate("(/ 6.0 3)").toString());
    assertEquals("1.5", lisp.evaluate("(/ 6 4.0)").toString());
  }

  @Test
  public void testDivUnary() {
    // (/ n) should return 1/n
    assertEquals("1", lisp.evaluate("(/ 1)").toString());
    assertEquals("-1", lisp.evaluate("(/ -1)").toString());
    assertEquals("0.5", lisp.evaluate("(/ 2)").toString());
    assertEquals("0.25", lisp.evaluate("(/ 4)").toString());
    assertEquals("0.5", lisp.evaluate("(/ 2.0)").toString());
  }

  @Test
  public void testDivByZero() {
    assertThrows(Exception.class, () -> lisp.evaluate("(/ 1 0)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(/ 1 0.0)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(/ 0)"));
  }

  @Test
  public void testDivReturnsCorrectType() {
    // Exact division returns int
    LispObject exactResult = lisp.evaluate("(/ 6 2)");
    assertTrue(exactResult instanceof IntObject, "Exact division should return IntObject");

    // Inexact division returns double
    LispObject inexactResult = lisp.evaluate("(/ 5 2)");
    assertTrue(
        inexactResult instanceof DoubleObject, "Inexact division should return DoubleObject");

    // Mixed types always return double
    LispObject mixedResult = lisp.evaluate("(/ 6 2.0)");
    assertTrue(mixedResult instanceof DoubleObject, "Mixed division should return DoubleObject");
  }

  // ==================== MODULO (mod) ====================

  @Test
  public void testModIntOnly() {
    assertEquals("1", lisp.evaluate("(mod 10 3)").toString());
    assertEquals("0", lisp.evaluate("(mod 9 3)").toString());
    // R7RS modulo: result has same sign as divisor
    assertEquals("2", lisp.evaluate("(mod -10 3)").toString());
    assertEquals("-2", lisp.evaluate("(mod 10 -3)").toString());
  }

  @Test
  public void testModMixedTypes() {
    assertEquals("1.0", lisp.evaluate("(mod 10.0 3)").toString());
    assertEquals("1.0", lisp.evaluate("(mod 10 3.0)").toString());
    // Floating point modulo
    String result = lisp.evaluate("(mod 10.5 3)").toString();
    assertTrue(result.startsWith("1.5"), "10.5 mod 3 should be 1.5");
  }

  @Test
  public void testModByZero() {
    assertThrows(Exception.class, () -> lisp.evaluate("(mod 10 0)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(mod 10 0.0)"));
  }

  // ==================== ABS ====================

  @Test
  public void testAbsInt() {
    assertEquals("5", lisp.evaluate("(abs 5)").toString());
    assertEquals("5", lisp.evaluate("(abs -5)").toString());
    assertEquals("0", lisp.evaluate("(abs 0)").toString());
  }

  @Test
  public void testAbsDouble() {
    assertEquals("5.5", lisp.evaluate("(abs 5.5)").toString());
    assertEquals("5.5", lisp.evaluate("(abs -5.5)").toString());
    assertEquals("0.0", lisp.evaluate("(abs 0.0)").toString());
  }

  @Test
  public void testAbsReturnsCorrectType() {
    LispObject intResult = lisp.evaluate("(abs -5)");
    assertTrue(intResult instanceof IntObject, "abs of int should return IntObject");

    LispObject doubleResult = lisp.evaluate("(abs -5.5)");
    assertTrue(doubleResult instanceof DoubleObject, "abs of double should return DoubleObject");
  }

  // ==================== MIN ====================

  @Test
  public void testMinIntOnly() {
    assertEquals("1", lisp.evaluate("(min 3 1 2)").toString());
    assertEquals("-5", lisp.evaluate("(min -5 0 5)").toString());
    assertEquals("5", lisp.evaluate("(min 5)").toString());
  }

  @Test
  public void testMinDoubleOnly() {
    assertEquals("1.5", lisp.evaluate("(min 2.5 1.5 3.5)").toString());
  }

  @Test
  public void testMinMixedTypes() {
    assertEquals("1.0", lisp.evaluate("(min 2 1.0 3)").toString());
    assertEquals("1.5", lisp.evaluate("(min 2 1.5 3)").toString());
    assertEquals("-0.5", lisp.evaluate("(min 0 -0.5 1)").toString());
  }

  @Test
  public void testMinReturnsCorrectType() {
    LispObject intResult = lisp.evaluate("(min 1 2 3)");
    assertTrue(intResult instanceof IntObject, "min of ints should return IntObject");

    LispObject doubleResult = lisp.evaluate("(min 1 2.0 3)");
    assertTrue(doubleResult instanceof DoubleObject, "min with doubles should return DoubleObject");
  }

  // ==================== MAX ====================

  @Test
  public void testMaxIntOnly() {
    assertEquals("3", lisp.evaluate("(max 1 3 2)").toString());
    assertEquals("5", lisp.evaluate("(max -5 0 5)").toString());
    assertEquals("5", lisp.evaluate("(max 5)").toString());
  }

  @Test
  public void testMaxDoubleOnly() {
    assertEquals("3.5", lisp.evaluate("(max 2.5 1.5 3.5)").toString());
  }

  @Test
  public void testMaxMixedTypes() {
    assertEquals("3.0", lisp.evaluate("(max 2 1.0 3.0)").toString());
    assertEquals("3.5", lisp.evaluate("(max 2 3.5 3)").toString());
    assertEquals("1.5", lisp.evaluate("(max 0 -0.5 1.5)").toString());
  }

  @Test
  public void testMaxReturnsCorrectType() {
    LispObject intResult = lisp.evaluate("(max 1 2 3)");
    assertTrue(intResult instanceof IntObject, "max of ints should return IntObject");

    LispObject doubleResult = lisp.evaluate("(max 1 2.0 3)");
    assertTrue(doubleResult instanceof DoubleObject, "max with doubles should return DoubleObject");
  }

  // ==================== COMPARISONS WITH MIXED TYPES ====================

  @Test
  public void testComparisonsMixedTypes() {
    assertEquals("true", lisp.evaluate("(= 2 2.0)").toString());
    assertEquals("true", lisp.evaluate("(< 1 1.5)").toString());
    assertEquals("true", lisp.evaluate("(< 1.5 2)").toString());
    assertEquals("true", lisp.evaluate("(> 2 1.5)").toString());
    assertEquals("true", lisp.evaluate("(<= 1 1.0)").toString());
    assertEquals("true", lisp.evaluate("(>= 2.0 2)").toString());
    assertEquals("false", lisp.evaluate("(= 2 2.1)").toString());
  }

  // ==================== COMPLEX EXPRESSIONS ====================

  @Test
  public void testComplexExpressionsMixedTypes() {
    // Nested operations with mixed types
    assertEquals("6.5", lisp.evaluate("(+ (* 2 2.5) 1.5)").toString());
    assertEquals("2.5", lisp.evaluate("(/ (+ 3 2.0) 2)").toString());
    assertEquals("10.0", lisp.evaluate("(- (* 3 4.0) 2)").toString());
  }

  @Test
  public void testTypePromotion() {
    // Once a double appears, the result should be double even if value is "integer-like"
    LispObject result = lisp.evaluate("(+ 1 1.0)");
    assertTrue(result instanceof DoubleObject, "Adding int and double should give DoubleObject");
    assertEquals("2.0", result.toString());

    result = lisp.evaluate("(* 2 2.0)");
    assertTrue(
        result instanceof DoubleObject, "Multiplying int and double should give DoubleObject");
    assertEquals("4.0", result.toString());
  }

  // ==================== EDGE CASES ====================

  @Test
  public void testVerySmallNumbers() {
    String result = lisp.evaluate("(+ 0.0000001 0.0000001)").toString();
    assertTrue(
        result.contains("E") || result.startsWith("2.0E") || result.startsWith("0.000000"),
        "Should handle very small numbers");
  }

  @Test
  public void testVeryLargeNumbers() {
    // Integer overflow protection - use doubles for very large numbers
    LispObject result = lisp.evaluate("(* 1000000.0 1000000.0)");
    assertTrue(result instanceof DoubleObject);
  }

  @Test
  public void testNegativeZero() {
    assertEquals("0.0", lisp.evaluate("(+ 0.0 -0.0)").toString());
  }

  // ==================== ERROR HANDLING ====================

  @Test
  public void testWrongArgumentType() {
    assertThrows(Exception.class, () -> lisp.evaluate("(+ 1 \"string\")"));
    assertThrows(Exception.class, () -> lisp.evaluate("(- 1 #t)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(* 1 '(1 2))"));
  }
}
