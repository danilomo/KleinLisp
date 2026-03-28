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
package net.sourceforge.kleinlisp.r7rs;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.Lisp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Integration tests comparing KleinLisp output with Guile (and Chicken as fallback for R7RS
 * compliance).
 *
 * <p>These tests require Guile or Chicken to be installed on the system. When Guile deviates from
 * R7RS, Chicken is used as the reference implementation.
 */
public class GuileComparisonTest {

  private static boolean guileAvailable = false;
  private static boolean chickenAvailable = false;
  private Lisp lisp;

  @BeforeAll
  public static void checkAvailability() {
    guileAvailable = checkCommand("guile", "--version");
    chickenAvailable = checkCommand("csi", "-version");
  }

  private static boolean checkCommand(String... args) {
    try {
      ProcessBuilder pb = new ProcessBuilder(args);
      pb.redirectErrorStream(true);
      Process p = pb.start();
      boolean finished = p.waitFor(5, TimeUnit.SECONDS);
      return finished && p.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  @BeforeEach
  public void setUp() {
    lisp = new Lisp();
  }

  static boolean isGuileAvailable() {
    return guileAvailable;
  }

  static boolean isChickenAvailable() {
    return chickenAvailable;
  }

  static boolean isAnyReferenceAvailable() {
    return guileAvailable || chickenAvailable;
  }

  /** Run an expression in Guile and return the output. */
  private String runGuile(String expression) throws IOException, InterruptedException {
    String code = String.format("(write %s)(newline)", expression);
    ProcessBuilder pb = new ProcessBuilder("guile", "--no-auto-compile", "-c", code);
    pb.redirectErrorStream(true);
    Process p = pb.start();

    String output;
    try (InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      output = reader.lines().collect(Collectors.joining("\n"));
    }

    boolean finished = p.waitFor(10, TimeUnit.SECONDS);
    if (!finished) {
      p.destroyForcibly();
      throw new RuntimeException("Guile process timed out");
    }

    return normalizeOutput(output);
  }

  /** Run an expression in Chicken Scheme and return the output. */
  private String runChicken(String expression) throws IOException, InterruptedException {
    String code = String.format("(write %s)(newline)", expression);
    ProcessBuilder pb = new ProcessBuilder("csi", "-q", "-e", code);
    pb.redirectErrorStream(true);
    Process p = pb.start();

    String output;
    try (InputStream is = p.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      output = reader.lines().collect(Collectors.joining("\n"));
    }

    boolean finished = p.waitFor(10, TimeUnit.SECONDS);
    if (!finished) {
      p.destroyForcibly();
      throw new RuntimeException("Chicken process timed out");
    }

    return normalizeOutput(output);
  }

  /** Run an expression with display output in KleinLisp. */
  private String runKleinLispDisplay(String expression) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(baos));

    try {
      lisp.evaluate(expression);
    } finally {
      System.setOut(originalOut);
    }

    return normalizeOutput(baos.toString());
  }

  /** Run an expression in KleinLisp and return the result. */
  private String runKleinLisp(String expression) {
    return normalizeOutput(lisp.evaluate(expression).toString());
  }

  /** Assert that KleinLisp produces the same output as Guile. */
  private void assertR7RSCompliant(String expression) throws Exception {
    String kleinResult = runKleinLisp(expression);
    if (guileAvailable) {
      String guileResult = runGuile(expression);
      assertEquals(guileResult, kleinResult, "Expression: " + expression);
    } else if (chickenAvailable) {
      String chickenResult = runChicken(expression);
      assertEquals(chickenResult, kleinResult, "Expression: " + expression);
    }
  }

  /** Assert that KleinLisp produces the same output as Chicken (for R7RS-specific behavior). */
  private void assertR7RSCompliantChicken(String expression) throws Exception {
    String kleinResult = runKleinLisp(expression);
    if (chickenAvailable) {
      String chickenResult = runChicken(expression);
      assertEquals(chickenResult, kleinResult, "Expression: " + expression);
    } else if (guileAvailable) {
      String guileResult = runGuile(expression);
      assertEquals(guileResult, kleinResult, "Expression: " + expression);
    }
  }

  /** Assert KleinLisp output matches expected, without running external Scheme. */
  private void assertKleinLispResult(String expression, String expected) {
    assertEquals(normalizeOutput(expected), runKleinLisp(expression), "Expression: " + expression);
  }

  private String normalizeOutput(String output) {
    return output
        .trim()
        .replaceAll("\\r\\n", "\n")
        .replaceAll("\\r", "\n")
        // Normalize boolean representation
        .replace("true", "#t")
        .replace("false", "#f");
  }

  // ========== NUMERIC TESTS ==========

  @Test
  @EnabledIf("isGuileAvailable")
  public void testBasicArithmetic() throws Exception {
    assertR7RSCompliant("(+ 1 2 3)");
    assertR7RSCompliant("(* 2 3 4)");
    assertR7RSCompliant("(- 10 3)");
    assertR7RSCompliant("(/ 20 4)");
  }

  @Test
  public void testFloorCeiling() {
    assertKleinLispResult("(floor 3.7)", "3.0");
    assertKleinLispResult("(ceiling 3.2)", "4.0");
    assertKleinLispResult("(truncate -3.7)", "-3.0");
    assertKleinLispResult("(round 3.5)", "4.0");
  }

  @Test
  public void testGcdLcm() {
    assertKleinLispResult("(gcd 12 18)", "6");
    assertKleinLispResult("(lcm 4 6)", "12");
    assertKleinLispResult("(gcd)", "0");
    assertKleinLispResult("(lcm)", "1");
  }

  @Test
  public void testQuotientRemainder() {
    assertKleinLispResult("(quotient 13 4)", "3");
    assertKleinLispResult("(remainder 13 4)", "1");
    assertKleinLispResult("(modulo -13 4)", "3");
  }

  @Test
  public void testTranscendental() {
    assertKleinLispResult("(sqrt 16)", "4.0");
    assertKleinLispResult("(expt 2 10)", "1024");
  }

  // ========== LIST TESTS ==========

  @Test
  @EnabledIf("isGuileAvailable")
  public void testListOperations() throws Exception {
    assertR7RSCompliant("(car '(1 2 3))");
    assertR7RSCompliant("(cdr '(1 2 3))");
    assertR7RSCompliant("(cons 1 '(2 3))");
    assertR7RSCompliant("(length '(1 2 3 4 5))");
  }

  @Test
  @EnabledIf("isGuileAvailable")
  public void testListPredicates() throws Exception {
    assertR7RSCompliant("(null? '())");
    assertR7RSCompliant("(pair? '(1 . 2))");
    assertR7RSCompliant("(list? '(1 2 3))");
  }

  @Test
  public void testListCopy() {
    assertKleinLispResult("(list-copy '(1 2 3))", "(1 2 3)");
  }

  // ========== STRING TESTS ==========

  @Test
  @EnabledIf("isGuileAvailable")
  public void testStringOperations() throws Exception {
    assertR7RSCompliant("(string-length \"hello\")");
    assertR7RSCompliant("(string-append \"hello\" \" \" \"world\")");
    assertR7RSCompliant("(substring \"hello\" 1 4)");
  }

  @Test
  public void testStringConversions() {
    assertKleinLispResult("(string->list \"abc\")", "(#\\a #\\b #\\c)");
    assertKleinLispResult("(list->string '(#\\h #\\i))", "\"hi\"");
  }

  @Test
  public void testMakeString() {
    assertKleinLispResult("(string-length (make-string 5))", "5");
    assertKleinLispResult("(make-string 3 #\\x)", "\"xxx\"");
  }

  // ========== CHARACTER TESTS ==========

  @Test
  public void testCharPredicates() {
    assertKleinLispResult("(char? #\\a)", "#t");
    assertKleinLispResult("(char-alphabetic? #\\a)", "#t");
    assertKleinLispResult("(char-numeric? #\\5)", "#t");
    assertKleinLispResult("(char-whitespace? #\\space)", "#t");
  }

  @Test
  public void testCharComparisons() {
    assertKleinLispResult("(char=? #\\a #\\a)", "#t");
    assertKleinLispResult("(char<? #\\a #\\b)", "#t");
    assertKleinLispResult("(char-ci=? #\\A #\\a)", "#t");
  }

  @Test
  public void testCharConversions() {
    assertKleinLispResult("(char->integer #\\A)", "65");
    assertKleinLispResult("(integer->char 97)", "#\\a");
    assertKleinLispResult("(char-upcase #\\a)", "#\\A");
    assertKleinLispResult("(char-downcase #\\A)", "#\\a");
  }

  // ========== VECTOR TESTS ==========

  @Test
  @EnabledIf("isGuileAvailable")
  public void testVectorOperations() throws Exception {
    assertR7RSCompliant("(vector-length (vector 1 2 3))");
    assertR7RSCompliant("(vector-ref (vector 'a 'b 'c) 1)");
  }

  @Test
  public void testVectorAppend() {
    assertKleinLispResult("(vector-append (vector 1 2) (vector 3 4))", "#(1 2 3 4)");
  }

  // ========== QUASIQUOTE TESTS ==========

  @Test
  public void testQuasiquote() {
    assertKleinLispResult("`(1 2 3)", "(1 2 3)");
    assertKleinLispResult("`(1 ,(+ 1 1) 3)", "(1 2 3)");
    assertKleinLispResult("`(a ,@(list 1 2 3) b)", "(a 1 2 3 b)");
  }

  @Test
  public void testNestedQuasiquote() {
    // Nested quasiquotes should preserve inner quasiquote structure
    String result = runKleinLisp("`(a `(b ,(+ 1 2)))");
    assertTrue(result.contains("quasiquote"));
  }

  // ========== CONTROL FLOW TESTS ==========

  @Test
  public void testDoLoop() {
    assertKleinLispResult("(do ((i 0 (+ i 1)) (sum 0 (+ sum i))) ((= i 5) sum))", "10");
  }

  @Test
  public void testLetrecStar() {
    assertKleinLispResult("(letrec* ((a 1) (b (+ a 1))) b)", "2");
  }

  // ========== MULTIPLE VALUES TESTS ==========

  @Test
  public void testCallWithValues() {
    assertKleinLispResult("(call-with-values (lambda () (values 1 2)) +)", "3");
  }

  @Test
  public void testLetValues() {
    assertKleinLispResult("(let-values (((a b) (values 1 2))) (+ a b))", "3");
  }

  // ========== PROMISE TESTS ==========

  @Test
  public void testDelayForce() {
    assertKleinLispResult("(force (delay (+ 1 2)))", "3");
    assertKleinLispResult("(promise? (delay 1))", "#t");
  }

  // ========== EXCEPTION TESTS ==========

  @Test
  public void testGuardRaise() {
    assertKleinLispResult("(guard (e (else 'caught)) (raise 'error))", "caught");
  }

  @Test
  public void testErrorObject() {
    assertKleinLispResult(
        "(guard (e ((error-object? e) (error-object-message e))) (error \"test\"))", "\"test\"");
  }

  // ========== PARAMETER TESTS ==========

  @Test
  public void testParameters() {
    lisp.evaluate("(define p (make-parameter 10))");
    assertKleinLispResult("(p)", "10");
    assertKleinLispResult("(parameterize ((p 20)) (p))", "20");
    assertKleinLispResult("(p)", "10"); // Restored
  }

  // ========== PORT TESTS ==========

  @Test
  public void testStringPorts() {
    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"hello\" sp)");
    assertKleinLispResult("(get-output-string sp)", "\"hello\"");
  }

  @Test
  public void testEof() {
    assertKleinLispResult("(eof-object? (eof-object))", "#t");
  }

  // ========== BYTEVECTOR TESTS ==========

  @Test
  public void testBytevector() {
    assertKleinLispResult("(bytevector 1 2 3)", "#u8(1 2 3)");
    assertKleinLispResult("(bytevector-length (bytevector 1 2 3))", "3");
  }

  @Test
  public void testUtf8Conversion() {
    assertKleinLispResult("(utf8->string (bytevector 104 105))", "\"hi\"");
  }

  // ========== BOOLEAN TESTS ==========

  @Test
  public void testBooleanEquals() {
    assertKleinLispResult("(boolean=? #t #t)", "#t");
    assertKleinLispResult("(boolean=? #t #f)", "#f");
  }

  // ========== SYSTEM TESTS ==========

  @Test
  public void testFeatures() {
    String features = runKleinLisp("(features)");
    assertTrue(features.contains("r7rs"));
  }
}
