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
import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests comparing KleinLisp closure parameter capture behavior with Guile Scheme. These
 * tests ensure that KleinLisp correctly captures parameters in closures passed to higher-order
 * functions like map, filter, any, etc.
 *
 * <p>See docs/CLOSURE-PARAMETER-CAPTURE-BUG.md for the bug description.
 */
public class ClosureParameterCaptureGuileComparisonTest extends BaseTestClass {

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
  public void testClosureInMapComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate("(define (scale factor items) (map (lambda (x) (* x factor)) items))");
    ListObject kleinResult = lisp.evaluate("(scale 2 (list 1 2 3))").asList();

    // Test Guile
    String guileCode =
        "(define (scale factor items) (map (lambda (x) (* x factor)) items))\n"
            + "(display (scale 2 '(1 2 3)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals("(2 4 6)", guileOutput);
    assertEquals(2, kleinResult.car().asInt().value);
    assertEquals(4, kleinResult.cdr().car().asInt().value);
    assertEquals(6, kleinResult.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testClosureInFilterComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate(
        "(define (filter-gt threshold items) (filter (lambda (x) (> x threshold)) items))");
    ListObject kleinResult = lisp.evaluate("(filter-gt 2 (list 1 2 3 4 5))").asList();

    // Test Guile
    String guileCode =
        "(define (filter-gt threshold items) (filter (lambda (x) (> x threshold)) items))\n"
            + "(display (filter-gt 2 '(1 2 3 4 5)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals("(3 4 5)", guileOutput);
    assertEquals(3, kleinResult.car().asInt().value);
    assertEquals(4, kleinResult.cdr().car().asInt().value);
    assertEquals(5, kleinResult.cdr().cdr().car().asInt().value);
  }

  @Test
  public void testClosureInAnyComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate("(define (contains? target items) (any (lambda (x) (= x target)) items))");
    boolean kleinResult1 = lisp.evaluate("(contains? 2 (list 1 2 3))").truthiness();
    boolean kleinResult2 = lisp.evaluate("(contains? 5 (list 1 2 3))").truthiness();

    // Test Guile
    String guileCode1 =
        "(define (contains? target items) (any (lambda (x) (= x target)) items))\n"
            + "(display (contains? 2 '(1 2 3)))\n";
    String guileCode2 =
        "(define (contains? target items) (any (lambda (x) (= x target)) items))\n"
            + "(display (contains? 5 '(1 2 3)))\n";

    String guileOutput1 = runGuile(guileCode1);
    String guileOutput2 = runGuile(guileCode2);

    assertEquals("#t", guileOutput1);
    assertEquals(true, kleinResult1);
    assertEquals("#f", guileOutput2);
    assertEquals(false, kleinResult2);
  }

  @Test
  public void testClosureWithStringComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate(
        "(define (has-children? sid) "
            + "  (any (lambda (span) (string=? (car span) sid)) "
            + "       (list (list \"a\") (list \"b\") (list \"c\"))))");
    boolean kleinResult1 = lisp.evaluate("(has-children? \"a\")").truthiness();
    boolean kleinResult2 = lisp.evaluate("(has-children? \"b\")").truthiness();
    boolean kleinResult3 = lisp.evaluate("(has-children? \"d\")").truthiness();

    // Test Guile
    String guileCode1 =
        "(define (has-children? sid) "
            + "  (any (lambda (span) (string=? (car span) sid)) "
            + "       (list (list \"a\") (list \"b\") (list \"c\"))))\n"
            + "(display (has-children? \"a\"))\n";
    String guileCode2 =
        "(define (has-children? sid) "
            + "  (any (lambda (span) (string=? (car span) sid)) "
            + "       (list (list \"a\") (list \"b\") (list \"c\"))))\n"
            + "(display (has-children? \"b\"))\n";
    String guileCode3 =
        "(define (has-children? sid) "
            + "  (any (lambda (span) (string=? (car span) sid)) "
            + "       (list (list \"a\") (list \"b\") (list \"c\"))))\n"
            + "(display (has-children? \"d\"))\n";

    String guileOutput1 = runGuile(guileCode1);
    String guileOutput2 = runGuile(guileCode2);
    String guileOutput3 = runGuile(guileCode3);

    assertEquals("#t", guileOutput1);
    assertEquals(true, kleinResult1);
    assertEquals("#t", guileOutput2);
    assertEquals(true, kleinResult2);
    assertEquals("#f", guileOutput3);
    assertEquals(false, kleinResult3);
  }

  @Test
  public void testClosureInFoldLeftComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp
    lisp.evaluate(
        "(define (sum-with-offset offset items) "
            + "  (fold-left (lambda (acc x) (+ acc x offset)) 0 items))");
    int kleinResult = lisp.evaluate("(sum-with-offset 10 (list 1 2 3))").asInt().value;

    // Test Guile - note: Guile's fold has (proc elem acc) instead of (proc acc elem)
    String guileCode =
        "(define (sum-with-offset offset items) "
            + "  (fold (lambda (x acc) (+ acc x offset)) 0 items))\n"
            + "(display (sum-with-offset 10 '(1 2 3)))\n";
    String guileOutput = runGuile(guileCode);

    // With offset 10: 0 + (1+10) + (2+10) + (3+10) = 36
    assertEquals("36", guileOutput);
    assertEquals(36, kleinResult);
  }

  @Test
  public void testLetBindingInMapCallbackComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp - let binding captured in closure inside map callback
    lisp.evaluate("(define (make-adder item) (let ((x item)) (lambda () x)))");
    lisp.evaluate("(define adders (map make-adder (list 1 2 3)))");

    int klein1 = lisp.evaluate("((car adders))").asInt().value;
    int klein2 = lisp.evaluate("((cadr adders))").asInt().value;
    int klein3 = lisp.evaluate("((caddr adders))").asInt().value;

    // Test Guile
    String guileCode =
        "(define (make-adder item) (let ((x item)) (lambda () x)))\n"
            + "(define adders (map make-adder '(1 2 3)))\n"
            + "(display ((car adders)))\n"
            + "(newline)\n"
            + "(display ((cadr adders)))\n"
            + "(newline)\n"
            + "(display ((caddr adders)))\n";
    String guileOutput = runGuile(guileCode);

    String[] lines = guileOutput.split("\n");
    assertEquals("1", lines[0]);
    assertEquals("2", lines[1]);
    assertEquals("3", lines[2]);

    // Verify KleinLisp matches Guile
    assertEquals(1, klein1);
    assertEquals(2, klein2);
    assertEquals(3, klein3);
  }

  @Test
  public void testNestedLetBindingComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp - nested let bindings
    lisp.evaluate(
        "(define (make-nested-adder item) "
            + "  (let ((x item)) "
            + "    (let ((y (* x 2))) "
            + "      (lambda () (+ x y)))))");
    lisp.evaluate("(define adders (map make-nested-adder (list 1 2 3)))");

    int klein1 = lisp.evaluate("((car adders))").asInt().value;
    int klein2 = lisp.evaluate("((cadr adders))").asInt().value;
    int klein3 = lisp.evaluate("((caddr adders))").asInt().value;

    // Test Guile
    String guileCode =
        "(define (make-nested-adder item) "
            + "  (let ((x item)) "
            + "    (let ((y (* x 2))) "
            + "      (lambda () (+ x y)))))\n"
            + "(define adders (map make-nested-adder '(1 2 3)))\n"
            + "(display ((car adders)))\n"
            + "(newline)\n"
            + "(display ((cadr adders)))\n"
            + "(newline)\n"
            + "(display ((caddr adders)))\n";
    String guileOutput = runGuile(guileCode);

    String[] lines = guileOutput.split("\n");
    assertEquals("3", lines[0]); // 1 + 2 = 3
    assertEquals("6", lines[1]); // 2 + 4 = 6
    assertEquals("9", lines[2]); // 3 + 6 = 9

    // Verify KleinLisp matches Guile
    assertEquals(3, klein1);
    assertEquals(6, klein2);
    assertEquals(9, klein3);
  }

  @Test
  public void testRepeatedCallsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Test KleinLisp - call with different arguments to ensure no stale values
    lisp.evaluate("(define (scale factor items) (map (lambda (x) (* x factor)) items))");

    ListObject kleinResult1 = lisp.evaluate("(scale 2 (list 1 2 3))").asList();
    ListObject kleinResult2 = lisp.evaluate("(scale 10 (list 1 2 3))").asList();
    ListObject kleinResult3 = lisp.evaluate("(scale 2 (list 1 2 3))").asList();

    // Test Guile
    String guileCode =
        "(define (scale factor items) (map (lambda (x) (* x factor)) items))\n"
            + "(display (scale 2 '(1 2 3)))\n"
            + "(newline)\n"
            + "(display (scale 10 '(1 2 3)))\n"
            + "(newline)\n"
            + "(display (scale 2 '(1 2 3)))\n";
    String guileOutput = runGuile(guileCode);

    String[] lines = guileOutput.split("\n");
    assertEquals("(2 4 6)", lines[0]);
    assertEquals("(10 20 30)", lines[1]);
    assertEquals("(2 4 6)", lines[2]);

    // Verify KleinLisp results match
    assertEquals(2, kleinResult1.car().asInt().value);
    assertEquals(10, kleinResult2.car().asInt().value);
    assertEquals(2, kleinResult3.car().asInt().value);
  }
}
