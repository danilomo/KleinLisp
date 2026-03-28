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
 * Integration tests that compare KleinLisp bytevector output with Guile's (rnrs bytevectors)
 * module.
 *
 * <p>Note: Guile 2.2.x uses (rnrs bytevectors) module which differs slightly from R7RS: - Uses
 * `u8-list->bytevector` instead of `bytevector` - Prints `#vu8(...)` instead of `#u8(...)`
 *
 * <p>These tests compare semantic behavior (lengths, byte values) rather than exact output strings.
 */
public class BytevectorIntegrationTest {

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
  public void testMakeBytevectorLengthMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression("(display (bytevector-length (make-bytevector 5))) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(display (bytevector-length (make-bytevector 5))) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("5", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testMakeBytevectorWithFillMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (make-bytevector 3 255)) "
                + "(display (bytevector-u8-ref bv 0)) (newline) "
                + "(display (bytevector-u8-ref bv 1)) (newline) "
                + "(display (bytevector-u8-ref bv 2)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (make-bytevector 3 255)) "
                  + "(display (bytevector-u8-ref bv 0)) (newline) "
                  + "(display (bytevector-u8-ref bv 1)) (newline) "
                  + "(display (bytevector-u8-ref bv 2)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("255\n255\n255", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testBytevectorU8RefMatchesGuile() throws Exception {
    // Use u8-list->bytevector for Guile, bytevector for KleinLisp
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (bytevector 10 20 30)) "
                + "(display (bytevector-u8-ref bv 0)) (newline) "
                + "(display (bytevector-u8-ref bv 1)) (newline) "
                + "(display (bytevector-u8-ref bv 2)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (u8-list->bytevector '(10 20 30))) "
                  + "(display (bytevector-u8-ref bv 0)) (newline) "
                  + "(display (bytevector-u8-ref bv 1)) (newline) "
                  + "(display (bytevector-u8-ref bv 2)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("10\n20\n30", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testBytevectorU8SetMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (make-bytevector 3 0)) "
                + "(bytevector-u8-set! bv 1 255) "
                + "(display (bytevector-u8-ref bv 0)) (newline) "
                + "(display (bytevector-u8-ref bv 1)) (newline) "
                + "(display (bytevector-u8-ref bv 2)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (make-bytevector 3 0)) "
                  + "(bytevector-u8-set! bv 1 255) "
                  + "(display (bytevector-u8-ref bv 0)) (newline) "
                  + "(display (bytevector-u8-ref bv 1)) (newline) "
                  + "(display (bytevector-u8-ref bv 2)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("0\n255\n0", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testBytevectorCopyMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (bytevector 1 2 3 4 5)) "
                + "(define copy (bytevector-copy bv)) "
                + "(display (bytevector-length copy)) (newline) "
                + "(display (bytevector-u8-ref copy 2)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (u8-list->bytevector '(1 2 3 4 5))) "
                  + "(define copy (bytevector-copy bv)) "
                  + "(display (bytevector-length copy)) (newline) "
                  + "(display (bytevector-u8-ref copy 2)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("5\n3", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testBytevectorCopyIndependentMatchesGuile() throws Exception {
    // Test that copy is independent - modifying copy doesn't affect original
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (bytevector 1 2 3)) "
                + "(define copy (bytevector-copy bv)) "
                + "(bytevector-u8-set! copy 0 99) "
                + "(display (bytevector-u8-ref bv 0)) (newline) "
                + "(display (bytevector-u8-ref copy 0)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (u8-list->bytevector '(1 2 3))) "
                  + "(define copy (bytevector-copy bv)) "
                  + "(bytevector-u8-set! copy 0 99) "
                  + "(display (bytevector-u8-ref bv 0)) (newline) "
                  + "(display (bytevector-u8-ref copy 0)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("1\n99", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testUtf8StringConversionMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (string->utf8 \"hello\")) "
                + "(display (bytevector-length bv)) (newline) "
                + "(display (bytevector-u8-ref bv 0)) (newline) "
                + "(display (bytevector-u8-ref bv 4)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (string->utf8 \"hello\")) "
                  + "(display (bytevector-length bv)) (newline) "
                  + "(display (bytevector-u8-ref bv 0)) (newline) "
                  + "(display (bytevector-u8-ref bv 4)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      // h=104, e=101, l=108, l=108, o=111
      assertEquals("5\n104\n111", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testUtf8ToStringMatchesGuile() throws Exception {
    // Test that utf8->string produces the correct string by checking string equality
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (bytevector 104 101 108 108 111)) "
                + "(display (string=? (utf8->string bv) \"hello\"))");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (u8-list->bytevector '(104 101 108 108 111))) "
                  + "(display (string=? (utf8->string bv) \"hello\"))");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("#t", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testUtf8RoundTripMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression(
            "(define original \"Hello, World!\") "
                + "(define bv (string->utf8 original)) "
                + "(define result (utf8->string bv)) "
                + "(display (string=? original result))");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define original \"Hello, World!\") "
                  + "(define bv (string->utf8 original)) "
                  + "(define result (utf8->string bv)) "
                  + "(display (string=? original result))");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("#t", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testUtf8UnicodeMatchesGuile() throws Exception {
    // Test UTF-8 encoding of e-acute (U+00E9) - should be 2 bytes
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (string->utf8 \"\u00e9\")) "
                + "(display (bytevector-length bv)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (string->utf8 \"\u00e9\")) "
                  + "(display (bytevector-length bv)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("2", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testBytevectorPredicateMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression(
            "(display (bytevector? (make-bytevector 3))) (newline) "
                + "(display (bytevector? \"hello\")) (newline) "
                + "(display (bytevector? 42)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(display (bytevector? (make-bytevector 3))) (newline) "
                  + "(display (bytevector? \"hello\")) (newline) "
                  + "(display (bytevector? 42)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("#t\n#f\n#f", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testBytevectorEmptyMatchesGuile() throws Exception {
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (make-bytevector 0)) " + "(display (bytevector-length bv)) (newline)");
    if (isGuileAvailable()) {
      String guile =
          runGuileExpression(
              "(use-modules (rnrs bytevectors)) "
                  + "(define bv (make-bytevector 0)) "
                  + "(display (bytevector-length bv)) (newline)");
      assertEquals(normalizeOutput(guile), normalizeOutput(kleinLisp));
    } else {
      assertEquals("0", normalizeOutput(kleinLisp));
    }
  }

  @Test
  public void testBytevectorCopyPartialMatchesGuile() throws Exception {
    // R7RS bytevector-copy with start/end indices
    String kleinLisp =
        runKleinLispExpression(
            "(define bv (bytevector 1 2 3 4 5)) "
                + "(define partial (bytevector-copy bv 1 3)) "
                + "(display (bytevector-length partial)) (newline) "
                + "(display (bytevector-u8-ref partial 0)) (newline) "
                + "(display (bytevector-u8-ref partial 1)) (newline)");
    // Note: Guile's (rnrs bytevectors) bytevector-copy doesn't support start/end,
    // so we use a workaround with bytevector-copy! or just verify KleinLisp behavior
    assertEquals("2\n2\n3", normalizeOutput(kleinLisp));
  }

  @Test
  public void testBytevectorAppendBehavior() throws Exception {
    // R7RS bytevector-append - Guile doesn't have this in (rnrs bytevectors)
    String kleinLisp =
        runKleinLispExpression(
            "(define a (bytevector 1 2)) "
                + "(define b (bytevector 3 4)) "
                + "(define c (bytevector-append a b)) "
                + "(display (bytevector-length c)) (newline) "
                + "(display (bytevector-u8-ref c 0)) (newline) "
                + "(display (bytevector-u8-ref c 2)) (newline)");
    assertEquals("4\n1\n3", normalizeOutput(kleinLisp));
  }

  @Test
  public void testBytevectorCopyMutateBehavior() throws Exception {
    // R7RS bytevector-copy! behavior
    String kleinLisp =
        runKleinLispExpression(
            "(define src (bytevector 1 2 3)) "
                + "(define dst (make-bytevector 5 0)) "
                + "(bytevector-copy! dst 1 src) "
                + "(display (bytevector-u8-ref dst 0)) (newline) "
                + "(display (bytevector-u8-ref dst 1)) (newline) "
                + "(display (bytevector-u8-ref dst 2)) (newline) "
                + "(display (bytevector-u8-ref dst 3)) (newline) "
                + "(display (bytevector-u8-ref dst 4)) (newline)");
    assertEquals("0\n1\n2\n3\n0", normalizeOutput(kleinLisp));
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
    return output
        .trim()
        .replaceAll("\\r\\n", "\n")
        .replaceAll("\\r", "\n")
        // Collapse multiple newlines
        .replaceAll("\n+", "\n")
        // Normalize boolean representation
        .replace("true", "#t")
        .replace("false", "#f");
  }
}
