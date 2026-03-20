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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests comparing KleinLisp behavior with Guile Scheme. These tests ensure that
 * KleinLisp's let/let* bindings work correctly by comparing with a reference Scheme implementation.
 */
public class LetBindingGuileComparisonTest extends BaseTestClass {

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
    // Create a temporary file with the Guile code
    Path tempFile = Files.createTempFile("guile_test", ".scm");
    Files.writeString(tempFile, "(use-modules (srfi srfi-1))\n" + code);

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

  @Test
  public void testMapWithLetComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate("(define (double x) (let ((result (* x 2))) result))");
    ListObject kleinResult = lisp.evaluate("(map double (list 1 2 3 4 5))").asList();

    // Test Guile
    String guileCode =
        "(define (double x) (let ((result (* x 2))) result))\n"
            + "(display (map double '(1 2 3 4 5)))\n";
    String guileOutput = runGuile(guileCode);

    // Both should produce (2 4 6 8 10)
    assertEquals("(2 4 6 8 10)", guileOutput);
    assertEquals(2, kleinResult.car().asInt().value);
    assertEquals(4, kleinResult.cdr().car().asInt().value);
    assertEquals(6, kleinResult.cdr().cdr().car().asInt().value);
    assertEquals(8, kleinResult.cdr().cdr().cdr().car().asInt().value);
    assertEquals(10, kleinResult.cdr().cdr().cdr().cdr().car().asInt().value);
  }

  @Test
  public void testLetStarComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate("(define (triple-star x) (let* ((a x) (b (* a 2)) (c (* b x))) c))");
    ListObject kleinResult = lisp.evaluate("(map triple-star (list 1 2 3))").asList();

    // Test Guile: for x=1: a=1, b=2, c=2; for x=2: a=2, b=4, c=8; for x=3: a=3, b=6, c=18
    String guileCode =
        "(define (triple-star x) (let* ((a x) (b (* a 2)) (c (* b x))) c))\n"
            + "(display (map triple-star '(1 2 3)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals("(2 8 18)", guileOutput);
    assertEquals(2, kleinResult.car().asInt().value);
    assertEquals(8, kleinResult.cdr().car().asInt().value);
    assertEquals(18, kleinResult.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testFoldLeftWithLetComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp: sum with let
    lisp.evaluate("(define (sum-let acc x) (let ((v x)) (+ acc v)))");
    int kleinResult = lisp.evaluate("(fold-left sum-let 0 (list 1 2 3 4 5))").asInt().value;

    // Test Guile
    String guileCode =
        "(define (sum-let acc x) (let ((v x)) (+ acc v)))\n"
            + "(display (fold sum-let 0 '(1 2 3 4 5)))\n";
    String guileOutput = runGuile(guileCode);

    // Guile's fold has different argument order, result should be 15
    assertEquals(15, kleinResult);
    assertTrue(
        guileOutput.contains("15") || guileOutput.contains("1 2 3 4 5"),
        "Guile output should contain sum result");
  }

  @Test
  public void testNestedLetComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate(
        "(define (nested x)"
            + "  (let ((outer x))"
            + "    (let ((inner (* outer 3)))"
            + "      (+ outer inner))))");
    ListObject kleinResult = lisp.evaluate("(map nested (list 1 2 3))").asList();

    // Test Guile: for x=1: outer=1, inner=3, result=4; x=2: 2+6=8; x=3: 3+9=12
    String guileCode =
        "(define (nested x)"
            + "  (let ((outer x))"
            + "    (let ((inner (* outer 3)))"
            + "      (+ outer inner))))\n"
            + "(display (map nested '(1 2 3)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals("(4 8 12)", guileOutput);
    assertEquals(4, kleinResult.car().asInt().value);
    assertEquals(8, kleinResult.cdr().car().asInt().value);
    assertEquals(12, kleinResult.cdr().cdr().car().asInt().value);
  }
}
