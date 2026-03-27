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
 * Integration tests comparing KleinLisp quasiquote behavior with Guile Scheme. These tests ensure
 * that KleinLisp's quasiquote implementation works correctly by comparing with a reference Scheme
 * implementation.
 */
public class QuasiquoteGuileComparisonTest extends BaseTestClass {

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

  @Test
  public void testSimpleQuasiquoteComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("`(1 2 3)").toString();

    // Test Guile
    String guileCode = "(display `(1 2 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testUnquoteComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("`(1 ,(+ 1 1) 3)").toString();

    // Test Guile
    String guileCode = "(display `(1 ,(+ 1 1) 3))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testUnquoteSplicingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("`(1 ,@(list 2 3) 4)").toString();

    // Test Guile
    String guileCode = "(display `(1 ,@(list 2 3) 4))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testMixedUnquoteAndSplicingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("`(a ,(+ 1 2) ,@(list 4 5) b)").toString();

    // Test Guile
    String guileCode = "(display `(a ,(+ 1 2) ,@(list 4 5) b))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testUnquoteSplicingEmptyListComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("`(1 ,@(list) 4)").toString();

    // Test Guile
    String guileCode = "(display `(1 ,@(list) 4))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuasiquoteWithVariableComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate("(define x 42)");
    String kleinResult = lisp.evaluate("`(a ,x b)").toString();

    // Test Guile
    String guileCode = "(define x 42)\n(display `(a ,x b))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuasiquoteWithLetComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("(let ((x 10)) `(1 ,x 3))").toString();

    // Test Guile
    String guileCode = "(display (let ((x 10)) `(1 ,x 3)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuasiquoteNestedListComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("`(a (b ,(+ 1 2) c) d)").toString();

    // Test Guile
    String guileCode = "(display `(a (b ,(+ 1 2) c) d))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuasiquoteMultipleSplicingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    String kleinResult = lisp.evaluate("`(a ,@(list 1 2) b ,@(list 3 4) c)").toString();

    // Test Guile
    String guileCode = "(display `(a ,@(list 1 2) b ,@(list 3 4) c))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuasiquoteCodeGenerationComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate("(define (make-adder n) `(lambda (x) (+ x ,n)))");
    String kleinResult = lisp.evaluate("(make-adder 5)").toString();

    // Test Guile
    String guileCode =
        "(define (make-adder n) `(lambda (x) (+ x ,n)))\n" + "(display (make-adder 5))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuasiquoteWithMapComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp - generate list of quoted forms
    lisp.evaluate("(define (wrap x) `(item ,x))");
    String kleinResult = lisp.evaluate("(map wrap (list 1 2 3))").toString();

    // Test Guile
    String guileCode =
        "(use-modules (srfi srfi-1))\n"
            + "(define (wrap x) `(item ,x))\n"
            + "(display (map wrap '(1 2 3)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testQuasiquoteDeepNestingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate("(define x 5)");
    String kleinResult = lisp.evaluate("`((a ,x) (b ,x))").toString();

    // Test Guile
    String guileCode = "(define x 5)\n" + "(display `((a ,x) (b ,x)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }
}
