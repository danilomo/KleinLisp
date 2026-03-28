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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that compare KleinLisp output with Guile for the letrec* implementation.
 *
 * @author danilo
 */
public class LetrecStarIntegrationTest {

  /** Check if guile is available on the system. */
  static boolean isGuileAvailable() {
    try {
      Process process = new ProcessBuilder("which", "guile").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  @Test
  public void testSimpleBindingMatchesGuile() throws Exception {
    String expr = "(display (letrec* ((x 1)) x))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("1", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testSequentialDependencyMatchesGuile() throws Exception {
    String expr = "(display (letrec* ((a 1) (b (+ a 1)) (c (+ b 1))) c))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("3", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testMutualRecursionEvenMatchesGuile() throws Exception {
    String expr =
        "(display (letrec* ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1))))) "
            + "(odd? (lambda (n) (if (= n 0) #f (even? (- n 1)))))) "
            + "(even? 10)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeBooleans(normalizeOutput(guile)), normalizeOutput(kleinLisp));
    } else {
      assertEquals("true", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testMutualRecursionOddMatchesGuile() throws Exception {
    String expr =
        "(display (letrec* ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1))))) "
            + "(odd? (lambda (n) (if (= n 0) #f (even? (- n 1)))))) "
            + "(odd? 7)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeBooleans(normalizeOutput(guile)), normalizeOutput(kleinLisp));
    } else {
      assertEquals("true", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testFactorialMatchesGuile() throws Exception {
    String expr =
        "(display (letrec* ((fact (lambda (n) (if (= n 0) 1 (* n (fact (- n 1))))))) (fact 5)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("120", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testLambdaCaptureMatchesGuile() throws Exception {
    String expr = "(display (letrec* ((x 10) (f (lambda (y) (+ x y)))) (f 5)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("15", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testFunctionOnEarlierBindingMatchesGuile() throws Exception {
    String expr =
        "(display (letrec* ((double (lambda (x) (* x 2))) (five 5) (ten (double five))) ten))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testEmptyBindingsMatchesGuile() throws Exception {
    String expr = "(display (letrec* () 42))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("42", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testChainedDependenciesMatchesGuile() throws Exception {
    String expr = "(display (letrec* ((a 1) (b (+ a 2)) (c (+ b 3)) (d (+ c 4))) d))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testListFromBindingsMatchesGuile() throws Exception {
    String expr = "(display (letrec* ((a 1) (b 2) (c 3) (lst (list a b c))) lst))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("(1 2 3)", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testNestedLetrecStarMatchesGuile() throws Exception {
    String expr =
        "(display (letrec* ((x 1)) (letrec* ((y (+ x 1)) (z (+ y 1))) (+ x y z))))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("6", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testFibonacciMatchesGuile() throws Exception {
    String expr =
        "(display (letrec* ((fib (lambda (n) (if (<= n 1) n (+ (fib (- n 1)) (fib (- n 2))))))) (fib 6)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("8", normalizeOutput(kleinLisp));
    }
  }

  private String runKleinLispExpression(String expression) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(baos));

    try {
      Lisp lisp = new Lisp();
      lisp.evaluate(expression);
    } finally {
      System.setOut(originalOut);
    }

    return baos.toString();
  }

  private String runGuileExpression(String expression) throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder("guile", "--no-auto-compile", "-c", expression);
    pb.redirectErrorStream(true);
    Process process = pb.start();

    String output;
    try (InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      output = reader.lines().collect(Collectors.joining("\n"));
    }

    boolean finished = process.waitFor(30, TimeUnit.SECONDS);
    if (!finished) {
      process.destroyForcibly();
      throw new RuntimeException("Guile process timed out");
    }

    return output;
  }

  private String normalizeOutput(String output) {
    return output.trim().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
  }

  /**
   * Normalize boolean representation differences between KleinLisp (true/false) and Guile
   * (#t/#f).
   */
  private String normalizeBooleans(String output) {
    return output.replace("#t", "true").replace("#f", "false");
  }
}
