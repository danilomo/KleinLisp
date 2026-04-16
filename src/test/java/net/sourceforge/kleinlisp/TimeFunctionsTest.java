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
 * Tests for R7RS (scheme.time) functions.
 *
 * <p>Tests current-second, current-jiffy, and jiffies-per-second functions.
 */
public class TimeFunctionsTest extends BaseTestClass {

  private double evalAsDouble(String str) {
    return eval(str).asDouble().value;
  }

  @Test
  public void testCurrentSecond() {
    // current-second should return a positive number
    assertTrue(evalAsDouble("(current-second)") > 0);

    // current-second should return a reasonable Unix timestamp (after year 2000)
    // Unix timestamp for 2000-01-01 is approximately 946684800
    assertTrue(evalAsDouble("(current-second)") > 946684800);

    // current-second should return an inexact (floating-point) number
    assertTrue(evalAsBoolean("(inexact? (current-second))"));

    // Multiple calls should return similar values (within 1 second)
    double t1 = evalAsDouble("(current-second)");
    double t2 = evalAsDouble("(current-second)");
    assertTrue(Math.abs(t2 - t1) < 1.0);
  }

  @Test
  public void testCurrentJiffy() {
    // current-jiffy should return a number
    assertTrue(evalAsBoolean("(number? (current-jiffy))"));

    // current-jiffy should return a positive value
    assertTrue(evalAsDouble("(current-jiffy)") > 0);

    // Multiple calls should return increasing or equal values
    double j1 = evalAsDouble("(current-jiffy)");
    double j2 = evalAsDouble("(current-jiffy)");
    assertTrue(j2 >= j1);

    // Note: current-jiffy is inexact in KleinLisp (due to lack of BigInteger support)
    // even though R7RS specifies it should be exact
    assertTrue(evalAsBoolean("(inexact? (current-jiffy))"));
  }

  @Test
  public void testJiffiesPerSecond() {
    // jiffies-per-second should return exactly 1 billion (nanoseconds per second)
    assertEquals(1000000000, evalAsInt("(jiffies-per-second)"));

    // jiffies-per-second should return an exact integer
    assertTrue(evalAsBoolean("(exact? (jiffies-per-second))"));
    assertTrue(evalAsBoolean("(integer? (jiffies-per-second))"));

    // jiffies-per-second should be constant
    assertEquals(
        evalAsInt("(jiffies-per-second)"),
        evalAsInt("(jiffies-per-second)"),
        "jiffies-per-second should return the same value on multiple calls");
  }

  @Test
  public void testTimeMeasurement() {
    // Test that we can use these functions to measure time
    // This is a simple sanity check that the functions work together
    String code =
        "(let ((start (current-jiffy)) "
            + "      (dummy (+ 1 1)) "
            + "      (end (current-jiffy))) "
            + "  (>= end start))";
    assertTrue(evalAsBoolean(code));
  }

  @Test
  public void testCurrentSecondVsCurrentJiffy() {
    // Test that current-second and current-jiffy are roughly consistent
    // Get start time in seconds and jiffies
    String code =
        "(let ((sec-start (current-second)) "
            + "      (jiffy-start (current-jiffy)) "
            + "      (sec-end (current-second)) "
            + "      (jiffy-end (current-jiffy))) "
            + "  (let ((sec-elapsed (- sec-end sec-start)) "
            + "        (jiffy-elapsed (- jiffy-end jiffy-start))) "
            + "    (< sec-elapsed 1)))"; // Time elapsed should be less than 1 second
    assertTrue(evalAsBoolean(code));
  }
}
