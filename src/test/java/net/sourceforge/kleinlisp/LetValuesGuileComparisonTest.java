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
 * Integration tests comparing KleinLisp let-values, let*-values, and define-values behavior with
 * Guile Scheme. These tests ensure that KleinLisp's implementation is compatible with standard
 * Scheme.
 */
public class LetValuesGuileComparisonTest extends BaseTestClass {

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

  // SRFI-11 provides let-values and let*-values for Guile
  private static final String SRFI_11_IMPORT = "(import (srfi srfi-11))\n";

  private String runGuile(String code) throws Exception {
    Path tempFile = Files.createTempFile("guile_test", ".scm");
    // Prepend SRFI-11 import for let-values and let*-values support
    Files.writeString(tempFile, SRFI_11_IMPORT + code);

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

  // let-values comparison tests

  @Test
  public void testLetValuesSingleComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let-values (((a) (values 1))) a)").toString();
    String guileCode = "(display (let-values (((a) (values 1))) a))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetValuesMultipleComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let-values (((a b) (values 1 2))) (list a b))").toString();
    String guileCode = "(display (let-values (((a b) (values 1 2))) (list a b)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetValuesMultipleBindingsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(let-values (((a b) (values 1 2)) "
                    + "             ((c d e) (values 3 4 5))) "
                    + "  (list a b c d e))")
            .toString();
    String guileCode =
        "(display (let-values (((a b) (values 1 2)) "
            + "             ((c d e) (values 3 4 5))) "
            + "  (list a b c d e)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetValuesWithFunctionComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (div-mod n d) (values (quotient n d) (remainder n d)))");
    String kleinResult =
        lisp.evaluate("(let-values (((q r) (div-mod 17 5))) (list q r))").toString();
    String guileCode =
        "(define (div-mod n d) (values (quotient n d) (remainder n d)))\n"
            + "(display (let-values (((q r) (div-mod 17 5))) (list q r)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetValuesParallelBindingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // In let-values, all expressions are evaluated in the outer environment
    lisp.evaluate("(define x 10)");
    String kleinResult =
        lisp.evaluate(
                "(let-values (((x) (values 1)) "
                    + "             ((y) (values x))) " // Should see outer x = 10
                    + "  (list x y))")
            .toString();
    String guileCode =
        "(define x 10)\n"
            + "(display (let-values (((x) (values 1)) "
            + "             ((y) (values x))) "
            + "  (list x y)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // let*-values comparison tests

  @Test
  public void testLetStarValuesSingleComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let*-values (((a) (values 1))) a)").toString();
    String guileCode = "(display (let*-values (((a) (values 1))) a))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetStarValuesSequentialComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(let*-values (((a b) (values 1 2)) "
                    + "              ((c) (values (+ a b)))) "
                    + "  c)")
            .toString();
    String guileCode =
        "(display (let*-values (((a b) (values 1 2)) "
            + "              ((c) (values (+ a b)))) "
            + "  c))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetStarValuesChainComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(let*-values (((a) (values 1)) "
                    + "              ((b) (values (+ a 1))) "
                    + "              ((c) (values (+ b 1))) "
                    + "              ((sum) (values (+ a b c)))) "
                    + "  (list a b c sum))")
            .toString();
    String guileCode =
        "(display (let*-values (((a) (values 1)) "
            + "              ((b) (values (+ a 1))) "
            + "              ((c) (values (+ b 1))) "
            + "              ((sum) (values (+ a b c)))) "
            + "  (list a b c sum)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetStarValuesShadowingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // In let*-values, later bindings can reference earlier bindings
    String kleinResult =
        lisp.evaluate(
                "(let*-values (((x) (values 1)) "
                    + "              ((y) (values (+ x 1)))) " // Uses inner x = 1
                    + "  y)")
            .toString();
    String guileCode =
        "(display (let*-values (((x) (values 1)) "
            + "              ((y) (values (+ x 1)))) "
            + "  y))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // define-values comparison tests

  @Test
  public void testDefineValuesBasicComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define-values (x y) (values 10 20))");
    String kleinResult = lisp.evaluate("(list x y)").toString();
    String guileCode = "(define-values (x y) (values 10 20))\n" + "(display (list x y))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDefineValuesThreeComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define-values (a b c) (values 1 2 3))");
    String kleinResult = lisp.evaluate("(+ a b c)").toString();
    String guileCode = "(define-values (a b c) (values 1 2 3))\n" + "(display (+ a b c))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDefineValuesWithFunctionComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (min-max a b) (if (< a b) (values a b) (values b a)))");
    lisp.evaluate("(define-values (lo hi) (min-max 5 3))");
    String kleinResult = lisp.evaluate("(list lo hi)").toString();
    String guileCode =
        "(define (min-max a b) (if (< a b) (values a b) (values b a)))\n"
            + "(define-values (lo hi) (min-max 5 3))\n"
            + "(display (list lo hi))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Nested let-values comparison

  @Test
  public void testNestedLetValuesComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(let-values (((a b) (values 1 2))) "
                    + "  (let-values (((c) (values (+ a b)))) "
                    + "    (+ a b c)))")
            .toString();
    String guileCode =
        "(display (let-values (((a b) (values 1 2))) "
            + "  (let-values (((c) (values (+ a b)))) "
            + "    (+ a b c))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Empty bindings comparison

  @Test
  public void testLetValuesEmptyBindingsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let-values () 42)").toString();
    String guileCode = "(display (let-values () 42))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testLetStarValuesEmptyBindingsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let*-values () 42)").toString();
    String guileCode = "(display (let*-values () 42))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // exact-integer-sqrt comparison

  @Test
  public void testLetValuesWithExactIntegerSqrtComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(let-values (((s r) (exact-integer-sqrt 17))) (list s r))").toString();
    String guileCode = "(display (let-values (((s r) (exact-integer-sqrt 17))) (list s r)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Multiple body expressions comparison

  @Test
  public void testLetValuesMultipleBodyComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(let-values (((a b) (values 1 2))) "
                    + "  (+ a 0) " // Evaluated but discarded
                    + "  (+ a b))") // Returned
            .toString();
    String guileCode =
        "(display (let-values (((a b) (values 1 2))) " + "  (+ a 0) " + "  (+ a b)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Single non-values comparison

  @Test
  public void testLetValuesSingleNonValuesComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let-values (((a) 42)) a)").toString();
    String guileCode = "(display (let-values (((a) 42)) a))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Zero formals comparison

  @Test
  public void testLetValuesZeroFormalsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(let-values ((() (values))) 42)").toString();
    String guileCode = "(display (let-values ((() (values))) 42))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Complex expression comparison

  @Test
  public void testLetValuesComplexExpressionComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(let-values (((q r) (call-with-values "
                    + "                     (lambda () (values (quotient 20 3) (remainder 20 3))) "
                    + "                     (lambda (a b) (values a b))))) "
                    + "  (list q r))")
            .toString();
    String guileCode =
        "(display (let-values (((q r) (call-with-values "
            + "                     (lambda () (values (quotient 20 3) (remainder 20 3))) "
            + "                     (lambda (a b) (values a b))))) "
            + "  (list q r)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }
}
