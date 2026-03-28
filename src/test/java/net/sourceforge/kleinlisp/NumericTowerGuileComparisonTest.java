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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests comparing KleinLisp numeric tower behavior with Guile Scheme. These tests
 * ensure that KleinLisp's numeric implementation is compatible with standard Scheme.
 */
public class NumericTowerGuileComparisonTest extends BaseTestClass {

  private static boolean guileAvailable = false;

  @BeforeAll
  public static void checkGuileAvailable() {
    try {
      Process process = Runtime.getRuntime().exec(new String[] {"guile", "--version"});
      int exitCode = process.waitFor();
      guileAvailable = (exitCode == 0);
    } catch (Exception e) {
      guileAvailable = false;
    }
  }

  private String runGuile(String code) throws Exception {
    Path tempFile = Files.createTempFile("guile_test", ".scm");
    Files.writeString(tempFile, code);

    try {
      Process process =
          Runtime.getRuntime()
              .exec(new String[] {"guile", "--no-auto-compile", tempFile.toString()});
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String output = reader.lines().collect(Collectors.joining("\n"));
      process.waitFor();
      return output.trim();
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /** Normalize boolean output for comparison. Guile uses #t/#f, KleinLisp uses true/false. */
  private String normalizeBoolean(String s) {
    return s.replace("#t", "true").replace("#f", "false");
  }

  /** Normalize numeric output for comparison. */
  private String normalizeNumeric(String s) {
    // Handle Guile's exact->inexact representation differences if needed
    return s.trim();
  }

  // Rounding operations
  @Test
  public void testFloorComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(floor 3.7)").toString();
    String guileCode = "(display (floor 3.7))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testFloorNegativeComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(floor -3.2)").toString();
    String guileCode = "(display (floor -3.2))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCeilingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(ceiling 3.2)").toString();
    String guileCode = "(display (ceiling 3.2))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCeilingNegativeComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(ceiling -3.7)").toString();
    String guileCode = "(display (ceiling -3.7))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testTruncateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(truncate 3.7)").toString();
    String guileCode = "(display (truncate 3.7))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testTruncateNegativeComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(truncate -3.7)").toString();
    String guileCode = "(display (truncate -3.7))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testRoundComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(round 3.5)").toString();
    String guileCode = "(display (round 3.5))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  // Integer division
  @Test
  public void testQuotientComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(quotient 10 3)").toString();
    String guileCode = "(display (quotient 10 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuotientNegativeComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(quotient -10 3)").toString();
    String guileCode = "(display (quotient -10 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testRemainderComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(remainder 10 3)").toString();
    String guileCode = "(display (remainder 10 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testRemainderNegativeComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(remainder -10 3)").toString();
    String guileCode = "(display (remainder -10 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testModuloComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(modulo 10 3)").toString();
    String guileCode = "(display (modulo 10 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testModuloNegativeComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(modulo -10 3)").toString();
    String guileCode = "(display (modulo -10 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // GCD and LCM
  @Test
  public void testGcdComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(gcd 12 18)").toString();
    String guileCode = "(display (gcd 12 18))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGcdMultipleArgsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(gcd 12 8 20)").toString();
    String guileCode = "(display (gcd 12 8 20))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLcmComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(lcm 12 18)").toString();
    String guileCode = "(display (lcm 12 18))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLcmMultipleArgsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(lcm 4 6)").toString();
    String guileCode = "(display (lcm 4 6))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Exactness predicates
  @Test
  public void testExactPredicateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(exact? 5)").toString();
    String guileCode = "(display (exact? 5))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testInexactPredicateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(inexact? 5.0)").toString();
    String guileCode = "(display (inexact? 5.0))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  // Transcendental functions
  // Note: Guile preserves exactness (sqrt of exact 16 returns 4), while KleinLisp
  // always returns inexact results for transcendental functions. Using inexact inputs
  // ensures consistent behavior.
  @Test
  public void testSqrtComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(sqrt 16.0)").toString();
    String guileCode = "(display (sqrt 16.0))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testExptComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(expt 2 10)").toString();
    String guileCode = "(display (expt 2 10))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testExpComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(exp 0)").toString();
    String guileCode = "(display (exp 0))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLogComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(log 1)").toString();
    String guileCode = "(display (log 1))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testSinComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Using inexact input to ensure consistent behavior
    String kleinResult = lisp.evaluate("(sin 0.0)").toString();
    String guileCode = "(display (sin 0.0))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCosComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Using inexact input to ensure consistent behavior
    String kleinResult = lisp.evaluate("(cos 0.0)").toString();
    String guileCode = "(display (cos 0.0))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testTanComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Using inexact input to ensure consistent behavior
    String kleinResult = lisp.evaluate("(tan 0.0)").toString();
    String guileCode = "(display (tan 0.0))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  // Numeric predicates
  @Test
  public void testIntegerPredicateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(integer? 5)").toString();
    String guileCode = "(display (integer? 5))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testIntegerPredicateDoubleComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(integer? 5.0)").toString();
    String guileCode = "(display (integer? 5.0))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testIntegerPredicateFalseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(integer? 5.5)").toString();
    String guileCode = "(display (integer? 5.5))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testRealPredicateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(real? 5)").toString();
    String guileCode = "(display (real? 5))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testRealPredicateDoubleComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(real? 5.5)").toString();
    String guileCode = "(display (real? 5.5))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  // Note: Guile doesn't have a built-in 'square' function, so we define it inline
  @Test
  public void testSquareComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(square 5)").toString();
    String guileCode = "(define (square x) (* x x)) (display (square 5))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testSquareDoubleComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(square 2.5)").toString();
    String guileCode = "(define (square x) (* x x)) (display (square 2.5))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testExactIntegerSqrtComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Both Guile and KleinLisp now return multiple values
    // We use call-with-values to convert to list for comparison
    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (exact-integer-sqrt 11)) list)").toString();
    String guileCode =
        "(call-with-values (lambda () (exact-integer-sqrt 11)) (lambda (s r) (display (list s"
            + " r))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testExactIntegerSqrtPerfectSquareComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (exact-integer-sqrt 16)) list)").toString();
    String guileCode =
        "(call-with-values (lambda () (exact-integer-sqrt 16)) (lambda (s r) (display (list s"
            + " r))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Combined operations
  @Test
  public void testCombinedFloorCeilingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(floor (ceiling 3.2))").toString();
    String guileCode = "(display (floor (ceiling 3.2)))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCombinedSqrtSquareComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(sqrt (square 5.0))").toString();
    String guileCode = "(define (square x) (* x x)) (display (sqrt (square 5.0)))\n";
    String guileOutput = normalizeNumeric(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGcdLcmRelationComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // a * b = gcd(a,b) * lcm(a,b)
    String kleinResult = lisp.evaluate("(= (* 12 18) (* (gcd 12 18) (lcm 12 18)))").toString();
    String guileCode = "(display (= (* 12 18) (* (gcd 12 18) (lcm 12 18))))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }
}
