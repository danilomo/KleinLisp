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
 * Integration tests comparing KleinLisp multiple values behavior with Guile Scheme. These tests
 * ensure that KleinLisp's multiple values implementation is compatible with standard Scheme.
 */
public class MultipleValuesGuileComparisonTest extends BaseTestClass {

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
  public void testSingleValueComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(values 42)").toString();
    String guileCode = "(display (values 42))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCallWithValuesBasicComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (values 1 2)) (lambda (x y) (+ x y)))")
            .toString();
    String guileCode =
        "(display (call-with-values (lambda () (values 1 2)) (lambda (x y) (+ x y))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCallWithValuesSingleValueComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () 100) (lambda (x) (* x 2)))").toString();
    String guileCode = "(display (call-with-values (lambda () 100) (lambda (x) (* x 2))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCallWithValuesThreeValuesComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (values 1 2 3)) (lambda (a b c) (+ a b c)))")
            .toString();
    String guileCode =
        "(display (call-with-values (lambda () (values 1 2 3)) (lambda (a b c) (+ a b c))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCallWithValuesWithPlusComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (values 1 2 3 4 5)) +)").toString();
    String guileCode = "(display (call-with-values (lambda () (values 1 2 3 4 5)) +))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCallWithValuesWithMultiplyComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (values 2 3 4)) *)").toString();
    String guileCode = "(display (call-with-values (lambda () (values 2 3 4)) *))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testNestedCallWithValuesComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(call-with-values "
                    + "  (lambda () (call-with-values "
                    + "               (lambda () (values 10 20)) "
                    + "               (lambda (a b) (values (+ a 1) (+ b 2))))) "
                    + "  (lambda (x y) (+ x y)))")
            .toString();
    String guileCode =
        "(display (call-with-values "
            + "  (lambda () (call-with-values "
            + "               (lambda () (values 10 20)) "
            + "               (lambda (a b) (values (+ a 1) (+ b 2))))) "
            + "  (lambda (x y) (+ x y))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testValuesInConditionalComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(call-with-values "
                    + "  (lambda () (if #t (values 5 6) (values 1 2))) "
                    + "  (lambda (a b) (+ a b)))")
            .toString();
    String guileCode =
        "(display (call-with-values "
            + "  (lambda () (if #t (values 5 6) (values 1 2))) "
            + "  (lambda (a b) (+ a b))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDefinedFunctionReturningValuesComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    lisp.evaluate("(define (div-mod n d) (values (quotient n d) (remainder n d)))");
    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (div-mod 17 5)) (lambda (q r) (list q r)))")
            .toString();
    String guileCode =
        "(define (div-mod n d) (values (quotient n d) (remainder n d)))\n"
            + "(display (call-with-values (lambda () (div-mod 17 5)) (lambda (q r) (list q r))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testExactIntegerSqrtComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(call-with-values (lambda () (exact-integer-sqrt 17)) "
                    + "(lambda (s r) (list s r)))")
            .toString();
    String guileCode =
        "(display (call-with-values (lambda () (exact-integer-sqrt 17)) "
            + "(lambda (s r) (list s r))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testExactIntegerSqrtPerfectSquareComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(call-with-values (lambda () (exact-integer-sqrt 25)) "
                    + "(lambda (s r) (list s r)))")
            .toString();
    String guileCode =
        "(display (call-with-values (lambda () (exact-integer-sqrt 25)) "
            + "(lambda (s r) (list s r))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testValuesConsumerWithRestArgsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate(
                "(call-with-values (lambda () (values 1 2 3 4 5)) "
                    + "(lambda args (length args)))")
            .toString();
    String guileCode =
        "(display (call-with-values (lambda () (values 1 2 3 4 5)) "
            + "(lambda args (length args))))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCallWithValuesNoArgsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult =
        lisp.evaluate("(call-with-values (lambda () (values)) (lambda () 42))").toString();
    String guileCode = "(display (call-with-values (lambda () (values)) (lambda () 42)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }
}
