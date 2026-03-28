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

/** Tests for the R7RS numeric tower implementation. */
public class NumericTowerTest extends BaseTestClass {

  // Rounding
  @Test
  public void testFloor() {
    assertEquals("3.0", lisp.evaluate("(floor 3.7)").toString());
    assertEquals("-4.0", lisp.evaluate("(floor -3.2)").toString());
    assertEquals("5", lisp.evaluate("(floor 5)").toString());
  }

  @Test
  public void testCeiling() {
    assertEquals("4.0", lisp.evaluate("(ceiling 3.2)").toString());
    assertEquals("-3.0", lisp.evaluate("(ceiling -3.7)").toString());
    assertEquals("5", lisp.evaluate("(ceiling 5)").toString());
  }

  @Test
  public void testTruncate() {
    assertEquals("3.0", lisp.evaluate("(truncate 3.7)").toString());
    assertEquals("-3.0", lisp.evaluate("(truncate -3.7)").toString());
    assertEquals("5", lisp.evaluate("(truncate 5)").toString());
  }

  @Test
  public void testRound() {
    assertEquals("4.0", lisp.evaluate("(round 3.5)").toString()); // banker's rounding
    assertEquals("4.0", lisp.evaluate("(round 4.5)").toString()); // banker's rounding to even
    assertEquals("3.0", lisp.evaluate("(round 3.2)").toString());
    assertEquals("4.0", lisp.evaluate("(round 3.7)").toString());
    assertEquals("5", lisp.evaluate("(round 5)").toString());
  }

  // Integer division
  @Test
  public void testQuotient() {
    assertEquals("3", lisp.evaluate("(quotient 10 3)").toString());
    assertEquals("-3", lisp.evaluate("(quotient -10 3)").toString());
    assertEquals("-3", lisp.evaluate("(quotient 10 -3)").toString());
    assertEquals("3", lisp.evaluate("(quotient -10 -3)").toString());
  }

  @Test
  public void testRemainder() {
    assertEquals("1", lisp.evaluate("(remainder 10 3)").toString());
    assertEquals("-1", lisp.evaluate("(remainder -10 3)").toString());
    assertEquals("1", lisp.evaluate("(remainder 10 -3)").toString());
    assertEquals("-1", lisp.evaluate("(remainder -10 -3)").toString());
  }

  @Test
  public void testModulo() {
    assertEquals("1", lisp.evaluate("(modulo 10 3)").toString());
    assertEquals("2", lisp.evaluate("(modulo -10 3)").toString()); // same sign as divisor
    assertEquals("-2", lisp.evaluate("(modulo 10 -3)").toString()); // same sign as divisor
    assertEquals("-1", lisp.evaluate("(modulo -10 -3)").toString());
  }

  @Test
  public void testDivisionByZero() {
    // ArithmeticException may be wrapped in a RuntimeException
    assertThrows(Exception.class, () -> lisp.evaluate("(quotient 10 0)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(remainder 10 0)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(modulo 10 0)"));
  }

  // GCD and LCM
  @Test
  public void testGcd() {
    assertEquals("6", lisp.evaluate("(gcd 12 18)").toString());
    assertEquals("4", lisp.evaluate("(gcd 12 8 20)").toString());
    assertEquals("0", lisp.evaluate("(gcd)").toString());
    assertEquals("5", lisp.evaluate("(gcd 5)").toString());
    assertEquals("6", lisp.evaluate("(gcd -12 18)").toString()); // handles negatives
    assertEquals("1", lisp.evaluate("(gcd 7 11)").toString()); // coprime
  }

  @Test
  public void testLcm() {
    assertEquals("36", lisp.evaluate("(lcm 12 18)").toString());
    assertEquals("1", lisp.evaluate("(lcm)").toString());
    assertEquals("60", lisp.evaluate("(lcm 12 15 20)").toString());
    assertEquals("12", lisp.evaluate("(lcm 4 6)").toString());
    assertEquals("0", lisp.evaluate("(lcm 0 5)").toString());
  }

  // Exactness
  @Test
  public void testExactPredicate() {
    assertEquals("true", lisp.evaluate("(exact? 5)").toString());
    assertEquals("false", lisp.evaluate("(exact? 5.0)").toString());
  }

  @Test
  public void testInexactPredicate() {
    assertEquals("true", lisp.evaluate("(inexact? 5.0)").toString());
    assertEquals("false", lisp.evaluate("(inexact? 5)").toString());
  }

  @Test
  public void testExactConversion() {
    assertEquals("5", lisp.evaluate("(exact 5.0)").toString());
    assertEquals("5", lisp.evaluate("(exact 5)").toString());
    assertEquals("-3", lisp.evaluate("(exact -3.0)").toString());
  }

  @Test
  public void testExactConversionError() {
    assertThrows(Exception.class, () -> lisp.evaluate("(exact 5.5)"));
  }

  @Test
  public void testInexactConversion() {
    assertEquals("5.0", lisp.evaluate("(inexact 5)").toString());
    assertEquals("5.0", lisp.evaluate("(inexact 5.0)").toString());
    assertEquals("-3.0", lisp.evaluate("(inexact -3)").toString());
  }

  // Transcendental functions
  @Test
  public void testSqrt() {
    assertEquals("4.0", lisp.evaluate("(sqrt 16)").toString());
    assertEquals("3.0", lisp.evaluate("(sqrt 9)").toString());
    assertEquals("0.0", lisp.evaluate("(sqrt 0)").toString());
  }

  @Test
  public void testSqrtNegativeError() {
    assertThrows(Exception.class, () -> lisp.evaluate("(sqrt -1)"));
  }

  @Test
  public void testExpt() {
    assertEquals("8", lisp.evaluate("(expt 2 3)").toString());
    assertEquals("8.0", lisp.evaluate("(expt 2.0 3)").toString());
    assertEquals("1", lisp.evaluate("(expt 5 0)").toString());
    assertEquals("0.5", lisp.evaluate("(expt 2 -1)").toString());
    assertEquals("1024", lisp.evaluate("(expt 2 10)").toString());
  }

  @Test
  public void testExp() {
    assertTrue(lisp.evaluate("(exp 0)").toString().startsWith("1.0"));
    assertTrue(Double.parseDouble(lisp.evaluate("(exp 1)").toString()) > 2.7);
    assertTrue(Double.parseDouble(lisp.evaluate("(exp 1)").toString()) < 2.8);
  }

  @Test
  public void testLog() {
    assertTrue(lisp.evaluate("(log 1)").toString().startsWith("0.0"));
    // log base 10 of 100 = 2
    String result = lisp.evaluate("(log 100 10)").toString();
    assertTrue(result.startsWith("2.0"));
    // natural log of e^1 = 1
    assertTrue(Double.parseDouble(lisp.evaluate("(log 2.718281828)").toString()) < 1.01);
    assertTrue(Double.parseDouble(lisp.evaluate("(log 2.718281828)").toString()) > 0.99);
  }

  @Test
  public void testLogErrors() {
    assertThrows(Exception.class, () -> lisp.evaluate("(log 0)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(log -1)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(log 10 1)")); // base 1 invalid
  }

  @Test
  public void testTrigonometric() {
    assertTrue(lisp.evaluate("(sin 0)").toString().startsWith("0.0"));
    assertTrue(lisp.evaluate("(cos 0)").toString().startsWith("1.0"));
    assertTrue(lisp.evaluate("(tan 0)").toString().startsWith("0.0"));

    // sin(pi/2) should be close to 1
    double sinResult = Double.parseDouble(lisp.evaluate("(sin 1.5707963267948966)").toString());
    assertTrue(Math.abs(sinResult - 1.0) < 0.0001);
  }

  @Test
  public void testInverseTrigonometric() {
    // asin(0) = 0
    assertTrue(lisp.evaluate("(asin 0)").toString().startsWith("0.0"));
    // acos(1) = 0
    assertTrue(lisp.evaluate("(acos 1)").toString().startsWith("0.0"));
    // atan(0) = 0
    assertTrue(lisp.evaluate("(atan 0)").toString().startsWith("0.0"));
    // atan2(1, 1) = pi/4
    double atanResult = Double.parseDouble(lisp.evaluate("(atan 1 1)").toString());
    assertTrue(Math.abs(atanResult - Math.PI / 4) < 0.0001);
  }

  @Test
  public void testSquare() {
    assertEquals("25", lisp.evaluate("(square 5)").toString());
    assertEquals("6.25", lisp.evaluate("(square 2.5)").toString());
    assertEquals("0", lisp.evaluate("(square 0)").toString());
    assertEquals("9", lisp.evaluate("(square -3)").toString());
  }

  @Test
  public void testExactIntegerSqrt() {
    // exact-integer-sqrt returns multiple values (R7RS), use call-with-values to convert to list
    assertEquals(
        "(3 2)",
        lisp.evaluate("(call-with-values (lambda () (exact-integer-sqrt 11)) list)").toString());
    assertEquals(
        "(4 0)",
        lisp.evaluate("(call-with-values (lambda () (exact-integer-sqrt 16)) list)").toString());
    assertEquals(
        "(0 0)",
        lisp.evaluate("(call-with-values (lambda () (exact-integer-sqrt 0)) list)").toString());
    assertEquals(
        "(5 0)",
        lisp.evaluate("(call-with-values (lambda () (exact-integer-sqrt 25)) list)").toString());
    assertEquals(
        "(5 1)",
        lisp.evaluate("(call-with-values (lambda () (exact-integer-sqrt 26)) list)").toString());
  }

  @Test
  public void testExactIntegerSqrtNegativeError() {
    assertThrows(Exception.class, () -> lisp.evaluate("(exact-integer-sqrt -1)"));
  }

  // Rational (simplified)
  @Test
  public void testNumerator() {
    assertEquals("5", lisp.evaluate("(numerator 5)").toString());
    assertEquals("-7", lisp.evaluate("(numerator -7)").toString());
    assertEquals("0", lisp.evaluate("(numerator 0)").toString());
  }

  @Test
  public void testDenominator() {
    assertEquals("1", lisp.evaluate("(denominator 5)").toString());
    assertEquals("1", lisp.evaluate("(denominator -7)").toString());
  }

  @Test
  public void testRationalize() {
    assertEquals("3", lisp.evaluate("(rationalize 3.2 0.1)").toString());
    assertEquals("4", lisp.evaluate("(rationalize 3.7 0.1)").toString());
  }

  // Type predicates
  @Test
  public void testIntegerPredicate() {
    assertEquals("true", lisp.evaluate("(integer? 5)").toString());
    assertEquals("true", lisp.evaluate("(integer? 5.0)").toString()); // integer-valued double
    assertEquals("false", lisp.evaluate("(integer? 5.5)").toString());
    assertEquals("true", lisp.evaluate("(integer? -3)").toString());
    assertEquals("true", lisp.evaluate("(integer? 0)").toString());
  }

  @Test
  public void testRationalPredicate() {
    assertEquals("true", lisp.evaluate("(rational? 5)").toString());
    assertEquals("false", lisp.evaluate("(rational? 5.0)").toString()); // simplified: only ints
    assertEquals("false", lisp.evaluate("(rational? 5.5)").toString());
  }

  @Test
  public void testRealPredicate() {
    assertEquals("true", lisp.evaluate("(real? 5)").toString());
    assertEquals("true", lisp.evaluate("(real? 5.5)").toString());
    assertEquals("false", lisp.evaluate("(real? \"hello\")").toString());
  }

  @Test
  public void testComplexPredicate() {
    assertEquals("true", lisp.evaluate("(complex? 5)").toString());
    assertEquals("true", lisp.evaluate("(complex? 5.5)").toString());
  }

  @Test
  public void testFinitePredicate() {
    assertEquals("true", lisp.evaluate("(finite? 5)").toString());
    assertEquals("true", lisp.evaluate("(finite? 5.5)").toString());
  }

  @Test
  public void testNanPredicate() {
    assertEquals("false", lisp.evaluate("(nan? 5)").toString());
    assertEquals("false", lisp.evaluate("(nan? 5.5)").toString());
  }

  // Combined operations
  @Test
  public void testCombinedOperations() {
    // sqrt(square(x)) = abs(x)
    assertEquals("5.0", lisp.evaluate("(sqrt (square 5))").toString());
    assertEquals("5.0", lisp.evaluate("(sqrt (square -5))").toString());

    // floor(ceiling(x)) = ceiling(x) for any x
    assertEquals("4.0", lisp.evaluate("(floor (ceiling 3.2))").toString());

    // gcd and lcm relation: a * b = gcd(a,b) * lcm(a,b)
    int a = 12, b = 18;
    int gcd = evalAsInt("(gcd 12 18)");
    int lcm = evalAsInt("(lcm 12 18)");
    assertEquals(a * b, gcd * lcm);
  }

  @Test
  public void testExactnessPreservation() {
    // floor of int stays int
    assertEquals("5", lisp.evaluate("(floor 5)").toString());

    // exact->inexact->exact round trip (for integer values)
    assertEquals("5", lisp.evaluate("(exact (inexact 5))").toString());
  }
}
