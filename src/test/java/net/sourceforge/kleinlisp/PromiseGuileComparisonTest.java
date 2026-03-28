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
 * Integration tests comparing KleinLisp promise behavior with Guile Scheme. These tests ensure that
 * KleinLisp's promise implementation is compatible with standard Scheme.
 *
 * <p>Note: Some R7RS features (like make-promise on values, force on non-promises) have different
 * semantics in Guile. These tests only cover behaviors that are compatible between R7RS and Guile.
 */
public class PromiseGuileComparisonTest extends BaseTestClass {

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

  /**
   * Normalize boolean output for comparison. KleinLisp outputs "true"/"false" while Guile outputs
   * "#t"/"#f".
   */
  private String normalizeBoolean(String value) {
    return value.replace("#t", "true").replace("#f", "false");
  }

  @Test
  public void testBasicDelayForceComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(force (delay (+ 1 2)))").toString();
    String guileCode = "(display (force (delay (+ 1 2))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testPromisePredicateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(promise? (delay 42))").toString();
    String guileCode = "(display (promise? (delay 42)))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testPromisePredicateOnNonPromiseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(promise? 42)").toString();
    String guileCode = "(display (promise? 42))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  // Note: force on non-promise has different behavior in R7RS vs Guile
  // R7RS: returns value unchanged
  // Guile: throws an error
  // Skipping this test for Guile comparison

  @Test
  public void testNestedDelayComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // R7RS: force does NOT automatically force nested promises
    // To get the final value, force must be called for each nesting level
    String kleinResult =
        lisp.evaluate("(force (force (force (delay (delay (delay 42))))))").toString();
    String guileCode = "(display (force (force (force (delay (delay (delay 42)))))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDelayWithLetComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let ((x 10)) (force (delay x)))").toString();
    String guileCode = "(display (let ((x 10)) (force (delay x))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDelayWithComputationComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(force (delay (* 6 7)))").toString();
    String guileCode = "(display (force (delay (* 6 7))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDelayWithConditionalComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(force (delay (if (< 1 2) 'yes 'no)))").toString();
    String guileCode = "(display (force (delay (if (< 1 2) 'yes 'no))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Note: make-promise has different semantics in R7RS vs Guile
  // R7RS: (make-promise value) creates an already-forced promise
  // Guile: (make-promise thunk) creates a promise from a thunk
  // Skipping make-promise tests for Guile comparison

  @Test
  public void testDelayMemoizationComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test that delay memoizes - counter should only increment once
    lisp.evaluate("(define count 0)");
    lisp.evaluate("(define p (delay (begin (set! count (+ count 1)) count)))");
    lisp.evaluate("(force p)");
    lisp.evaluate("(force p)");
    lisp.evaluate("(force p)");
    String kleinResult = lisp.evaluate("count").toString();

    String guileCode =
        "(define count 0)\n"
            + "(define p (delay (begin (set! count (+ count 1)) count)))\n"
            + "(force p)\n"
            + "(force p)\n"
            + "(force p)\n"
            + "(display count)\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDelayInFunctionComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (lazy-add a b) (delay (+ a b)))");
    String kleinResult = lisp.evaluate("(force (lazy-add 3 4))").toString();

    String guileCode =
        "(define (lazy-add a b) (delay (+ a b)))\n" + "(display (force (lazy-add 3 4)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLazyStreamHeadComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))");
    lisp.evaluate("(define naturals (integers-from 0))");
    String kleinResult = lisp.evaluate("(car (force naturals))").toString();

    String guileCode =
        "(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))\n"
            + "(define naturals (integers-from 0))\n"
            + "(display (car (force naturals)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLazyStreamSecondElementComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))");
    lisp.evaluate("(define naturals (integers-from 0))");
    String kleinResult = lisp.evaluate("(car (force (cdr (force naturals))))").toString();

    String guileCode =
        "(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))\n"
            + "(define naturals (integers-from 0))\n"
            + "(display (car (force (cdr (force naturals)))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLazyStreamThirdElementComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))");
    lisp.evaluate("(define naturals (integers-from 0))");
    String kleinResult =
        lisp.evaluate("(car (force (cdr (force (cdr (force naturals))))))").toString();

    String guileCode =
        "(define (integers-from n) (delay (cons n (integers-from (+ n 1)))))\n"
            + "(define naturals (integers-from 0))\n"
            + "(display (car (force (cdr (force (cdr (force naturals)))))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testFibonacciStreamComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (fib-gen a b) (delay (cons a (fib-gen b (+ a b)))))");
    lisp.evaluate("(define fibs (fib-gen 0 1))");

    // Get 5th fibonacci number (0, 1, 1, 2, 3)
    String kleinResult =
        lisp.evaluate(
                "(car (force (cdr (force (cdr (force (cdr (force (cdr (force fibs))))))))))")
            .toString();

    String guileCode =
        "(define (fib-gen a b) (delay (cons a (fib-gen b (+ a b)))))\n"
            + "(define fibs (fib-gen 0 1))\n"
            + "(display (car (force (cdr (force (cdr (force (cdr (force (cdr (force fibs)))))))))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDelayWithBeginComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define x 0)");
    lisp.evaluate("(define p (delay (begin (set! x 1) (set! x (+ x 1)) x)))");
    lisp.evaluate("(force p)");
    String kleinResult = lisp.evaluate("x").toString();

    String guileCode =
        "(define x 0)\n"
            + "(define p (delay (begin (set! x 1) (set! x (+ x 1)) x)))\n"
            + "(force p)\n"
            + "(display x)\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDelayWithListOperationsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(force (delay (cons 1 (cons 2 (cons 3 '())))))").toString();
    String guileCode = "(display (force (delay (cons 1 (cons 2 (cons 3 '()))))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDelayWithMapComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(force (delay (map (lambda (x) (* x x)) '(1 2 3))))").toString();
    String guileCode = "(display (force (delay (map (lambda (x) (* x x)) '(1 2 3)))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testForceMultipleTimesComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define p (delay (+ 10 20)))");
    lisp.evaluate("(force p)");
    lisp.evaluate("(force p)");
    String kleinResult = lisp.evaluate("(force p)").toString();

    String guileCode =
        "(define p (delay (+ 10 20)))\n"
            + "(force p)\n"
            + "(force p)\n"
            + "(display (force p))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }
}
