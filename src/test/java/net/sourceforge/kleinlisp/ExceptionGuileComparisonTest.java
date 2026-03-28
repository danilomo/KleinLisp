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
 * Integration tests comparing KleinLisp exception handling behavior with Guile Scheme. These tests
 * ensure that KleinLisp's exception implementation is compatible with standard Scheme.
 */
public class ExceptionGuileComparisonTest extends BaseTestClass {

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
    // Prepend the rnrs exceptions import for guard/raise support
    String fullCode = "(use-modules (rnrs exceptions) (rnrs conditions))\n" + code;
    Path tempFile = Files.createTempFile("guile_test", ".scm");
    Files.writeString(tempFile, fullCode);

    try {
      Process process =
          Runtime.getRuntime()
              .exec(new String[] {"guile", "--no-auto-compile", tempFile.toString()});
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String output = reader.lines().collect(Collectors.joining("\n"));
      process.waitFor();
      // Normalize Guile output to match KleinLisp output
      return normalizeOutput(output.trim());
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /**
   * Normalizes Guile output to match KleinLisp output format.
   * - #t -> true
   * - #f -> false
   */
  private String normalizeOutput(String output) {
    return output.replace("#t", "true").replace("#f", "false");
  }

  @Test
  public void testGuardWithElseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(guard (e (else 'caught)) (raise 'error))").toString();
    String guileCode = "(display (guard (e (else 'caught)) (raise 'error)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithConditionComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(guard (e ((number? e) 'number) ((string? e) 'string) (else 'other)) "
                    + "(raise 42))")
            .toString();
    String guileCode =
        "(display (guard (e ((number? e) 'number) ((string? e) 'string) (else 'other)) "
            + "(raise 42)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardNoExceptionComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(guard (e (else 'caught)) (+ 1 2))").toString();
    String guileCode = "(display (guard (e (else 'caught)) (+ 1 2)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithMultipleBodyExprsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(guard (e (else 'caught)) 1 2 3)").toString();
    String guileCode = "(display (guard (e (else 'caught)) 1 2 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testNestedGuardComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(guard (outer (else 'outer)) "
                    + "(guard (inner (else 'inner)) "
                    + "(raise 'error)))")
            .toString();
    String guileCode =
        "(display (guard (outer (else 'outer)) "
            + "(guard (inner (else 'inner)) "
            + "(raise 'error))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardReraiseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(guard (outer (else 'outer)) "
                    + "(guard (inner ((string? inner) 'string)) "
                    + "(raise 42)))")
            .toString();
    String guileCode =
        "(display (guard (outer (else 'outer)) "
            + "(guard (inner ((string? inner) 'string)) "
            + "(raise 42))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardExpressionValueComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(guard (e ((= e 42))) (raise 42))").toString();
    String guileCode = "(display (guard (e ((= e 42))) (raise 42)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithHandlerBodyComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(guard (e (else (+ e 10))) (raise 5))").toString();
    String guileCode = "(display (guard (e (else (+ e 10))) (raise 5)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithSymbolRaisedComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(guard (e ((symbol? e) e)) (raise 'my-error))").toString();
    String guileCode = "(display (guard (e ((symbol? e) e)) (raise 'my-error)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithListRaisedComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(guard (e ((list? e) (car e))) (raise '(1 2 3)))").toString();
    String guileCode = "(display (guard (e ((list? e) (car e))) (raise '(1 2 3))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Note: The following tests use Guile's condition system (R6RS) which differs from
  // R7RS error-object functions. We test condition? instead of error-object? since
  // Guile's error procedure creates conditions, not R7RS error objects.

  @Test
  public void testErrorObjectPredicateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // KleinLisp uses R7RS error-object?, Guile uses R6RS condition?
    // Both should return true when error is called
    String kleinResult =
        lisp.evaluate("(guard (e (else (error-object? e))) (error \"test\"))").toString();
    // Use condition? for Guile which is equivalent for error objects
    String guileCode = "(display (guard (e (else (condition? e))) (error \"test\")))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Note: testErrorObjectMessageComparesWithGuile and testErrorObjectIrritantsComparesWithGuile
  // are skipped because Guile's R6RS condition system stores messages and irritants differently
  // from R7RS error objects. Guile uses format strings for messages ("~A") and has a different
  // irritants structure. These R7RS error-object functions are tested in ExceptionTest.java.

  @Test
  public void testRaiseNonErrorObjectComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // raise with a non-error value should not be an error object (KleinLisp)
    // or a condition (Guile)
    String kleinResult =
        lisp.evaluate("(guard (e (else (error-object? e))) (raise 42))").toString();
    // In Guile, raise with non-condition also produces false for condition?
    String guileCode = "(display (guard (e (else (condition? e))) (raise 42)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithDefineComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (safe-op x) (guard (e (else 0)) (/ 100 x)))");
    String kleinResult = lisp.evaluate("(safe-op 5)").toString();
    String guileCode =
        "(define (safe-op x) (guard (e (else 0)) (/ 100 x)))\n"
            + "(display (safe-op 5))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithLetComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(let ((x 10)) "
                    + "(guard (e (else (+ x e))) "
                    + "(raise 5)))")
            .toString();
    String guileCode =
        "(display (let ((x 10)) "
            + "(guard (e (else (+ x e))) "
            + "(raise 5))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testGuardWithMultipleHandlerExprsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(guard (e (else 1 2 3)) "
                    + "(raise 'error))")
            .toString();
    String guileCode =
        "(display (guard (e (else 1 2 3)) "
            + "(raise 'error)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }
}
