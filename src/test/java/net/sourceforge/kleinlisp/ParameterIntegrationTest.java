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
 * Integration tests that compare KleinLisp output with Guile for the R7RS parameters implementation
 * (make-parameter, parameterize, parameter?).
 *
 * @author danilo
 */
public class ParameterIntegrationTest {

  /** Check if guile is available on the system. */
  static boolean isGuileAvailable() {
    try {
      Process process = new ProcessBuilder("which", "guile").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  /** Check if chicken is available on the system. */
  static boolean isChickenAvailable() {
    try {
      Process process = new ProcessBuilder("which", "csi").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  @Test
  public void testBasicParameterMatchesGuile() throws Exception {
    String expr = "(define p (make-parameter 10)) (display (p)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterSetMatchesGuile() throws Exception {
    String expr = "(define p (make-parameter 10)) (p 20) (display (p)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("20", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterizeMatchesGuile() throws Exception {
    String expr = "(define p (make-parameter 10)) (display (parameterize ((p 20)) (p))) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("20", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterizeRestoresMatchesGuile() throws Exception {
    String expr =
        "(define p (make-parameter 10)) "
            + "(parameterize ((p 20)) (display (p)) (newline)) "
            + "(display (p)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("20\n10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testNestedParameterizeMatchesGuile() throws Exception {
    String expr =
        "(define p (make-parameter 10)) "
            + "(display (parameterize ((p 20)) "
            + "  (parameterize ((p 30)) (p)))) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("30", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testMultipleParametersMatchesGuile() throws Exception {
    String expr =
        "(define a (make-parameter 1)) "
            + "(define b (make-parameter 2)) "
            + "(display (parameterize ((a 10) (b 20)) (+ (a) (b)))) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("30", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterConverterMatchesGuile() throws Exception {
    String expr = "(define p (make-parameter 5 (lambda (x) (* x 2)))) (display (p)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterConverterOnSetMatchesGuile() throws Exception {
    String expr =
        "(define p (make-parameter 0 (lambda (x) (* x 2)))) (p 5) (display (p)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterConverterOnParameterizeMatchesGuile() throws Exception {
    String expr =
        "(define p (make-parameter 0 (lambda (x) (* x 2)))) "
            + "(display (parameterize ((p 5)) (p))) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testEmptyParameterizeMatchesGuile() throws Exception {
    String expr = "(display (parameterize () 42)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("42", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterPredicateMatchesGuile() throws Exception {
    String expr = "(display (parameter? (make-parameter 1))) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("#t", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterPredicateFalseMatchesGuile() throws Exception {
    String expr = "(display (parameter? 42)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("#f", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testDynamicScopeMatchesGuile() throws Exception {
    String expr =
        "(define p (make-parameter 1)) "
            + "(define (inner) (p)) "
            + "(define (outer) (parameterize ((p 2)) (inner))) "
            + "(display (outer)) (newline) "
            + "(display (p)) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("2\n1", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParameterizeWithMultipleBodyExpressionsMatchesGuile() throws Exception {
    String expr =
        "(define p (make-parameter 0)) "
            + "(display (parameterize ((p 1)) "
            + "  (p 2) "
            + "  (p 3) "
            + "  (p))) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("3", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testNestedParameterizeWithRestoreMatchesGuile() throws Exception {
    String expr =
        "(define p (make-parameter 1)) "
            + "(display (parameterize ((p 2)) "
            + "  (parameterize ((p 3)) (p)) "
            + "  (p))) (newline)";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      // After inner parameterize returns, p should be back to 2
      assertEquals("2", normalizeOutput(kleinLisp));
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

  private String runChickenExpression(String expression) throws IOException, InterruptedException {
    // Note: Chicken Scheme needs the srfi-39 import for parameters
    String fullExpr = "(import (chicken base) (srfi-39)) " + expression;
    ProcessBuilder pb = new ProcessBuilder("csi", "-q", "-e", fullExpr);
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
      throw new RuntimeException("Chicken process timed out");
    }

    return output;
  }

  private String normalizeOutput(String output) {
    return output
        .trim()
        .replaceAll("\\r\\n", "\n")
        .replaceAll("\\r", "\n")
        // Collapse multiple newlines (KleinLisp's display is println, adds extra newline)
        .replaceAll("\n+", "\n")
        // Normalize boolean representation: KleinLisp uses true/false, Guile uses #t/#f
        .replace("true", "#t")
        .replace("false", "#f");
  }
}
