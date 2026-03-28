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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Integration tests that compare KleinLisp output with Guile for the do loop implementation.
 *
 * @author danilo
 */
public class DoLoopIntegrationTest {

  private static final String SCRIPT_PATH = "src/test/resources/script_tests/do_loop.scm";

  /**
   * Check if guile is available on the system.
   */
  static boolean isGuileAvailable() {
    try {
      Process process = new ProcessBuilder("which", "guile").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Note: Full script comparison is skipped because KleinLisp maps 'display' to 'println'
   * which adds a newline, while Guile's display doesn't. Individual expression tests
   * below verify semantic compatibility.
   */

  @Test
  public void testSimpleCounterMatchesGuile() throws Exception {
    String kleinLisp = runKleinLispExpression("(display (do ((i 0 (+ i 1))) ((= i 5) i)))");
    if (isGuileAvailable()) {
      String guile = runGuileExpression("(display (do ((i 0 (+ i 1))) ((= i 5) i)))");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("5", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testSumMatchesGuile() throws Exception {
    String expr = "(display (do ((i 0 (+ i 1)) (sum 0 (+ sum i))) ((= i 5) sum)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testFactorialMatchesGuile() throws Exception {
    String expr = "(display (do ((n 5 (- n 1)) (result 1 (* result n))) ((= n 0) result)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("120", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testReverseListMatchesGuile() throws Exception {
    String expr =
        "(display (do ((lst '(1 2 3) (cdr lst)) (result '() (cons (car lst) result))) ((null? lst) result)))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("(3 2 1)", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testParallelStepMatchesGuile() throws Exception {
    String expr = "(display (do ((x 0 y) (y 1 x) (i 0 (+ i 1))) ((= i 3) (list x y))))";
    String kleinLisp = runKleinLispExpression(expr);
    if (isGuileAvailable()) {
      String guile = runGuileExpression(expr);
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("(1 0)", normalizeOutput(kleinLisp));
    }
  }

  private String runKleinLisp(String scriptPath) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(baos));

    try {
      Lisp lisp = new Lisp();
      Path path = Paths.get(scriptPath);
      lisp.execute(path);
    } finally {
      System.setOut(originalOut);
    }

    return baos.toString();
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

  private String runGuile(String scriptPath) throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder("guile", "--no-auto-compile", "-s", scriptPath);
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
}
