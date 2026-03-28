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
 * Integration tests comparing KleinLisp additional procedures with Guile Scheme. These tests ensure
 * R7RS compatibility for spec 13 procedures.
 */
public class AdditionalProceduresIntegrationTest extends BaseTestClass {

  private static boolean guileAvailable = false;

  @BeforeAll
  public static void checkSchemeAvailable() {
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

  private String normalizeBoolean(String s) {
    return s.replace("#t", "true").replace("#f", "false");
  }

  // ========== String Procedures ==========

  @Test
  public void testMakeStringComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(string-length (make-string 5 #\\a))").toString();
    String guileCode = "(display (string-length (make-string 5 #\\a)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testMakeStringContentComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(make-string 3 #\\x)").asString().value();
    String guileCode = "(display (make-string 3 #\\x))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringFromCharsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(string #\\a #\\b #\\c)").asString().value();
    String guileCode = "(display (string #\\a #\\b #\\c))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringToListComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(string->list \"abc\")").toString();
    String guileCode = "(write (string->list \"abc\"))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testListToStringComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(list->string '(#\\h #\\i))").asString().value();
    String guileCode = "(display (list->string '(#\\h #\\i)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringCopyComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(string-copy \"hello\")").asString().value();
    String guileCode = "(display (string-copy \"hello\"))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringMapComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(string-map char-upcase \"abc\")").asString().value();
    String guileCode = "(display (string-map char-upcase \"abc\"))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringMapLambdaComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(string-map char-downcase \"ABC\")").asString().value();
    String guileCode = "(display (string-map char-downcase \"ABC\"))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // ========== List Procedures ==========

  @Test
  public void testListCopyComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(list-copy '(1 2 3))").toString();
    // Guile has list-copy in SRFI-1
    String guileCode = "(use-modules (srfi srfi-1))\n" + "(write (list-copy '(1 2 3)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testListCopyEmptyComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(list-copy '())").toString();
    String guileCode = "(use-modules (srfi srfi-1))\n" + "(write (list-copy '()))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // ========== Round-trip tests ==========

  @Test
  public void testStringListRoundTripComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult =
        lisp.evaluate("(list->string (string->list \"hello\"))").asString().value();
    String guileCode = "(display (list->string (string->list \"hello\")))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharacterManipulationComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    // Test integer->char and char->integer round trip
    String kleinResult = lisp.evaluate("(char->integer (integer->char 65))").toString();
    String guileCode = "(display (char->integer (integer->char 65)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // ========== Complex scenarios ==========

  @Test
  public void testComplexStringManipulationComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    // Test substring via string-copy with indices
    String kleinResult = lisp.evaluate("(string-copy \"hello world\" 0 5)").asString().value();
    String guileCode = "(display (substring \"hello world\" 0 5))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringForEachComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    // Test that string-for-each processes each character
    lisp.evaluate("(define count 0)");
    lisp.evaluate("(string-for-each (lambda (c) (set! count (+ count 1))) \"hello\")");
    String kleinResult = lisp.evaluate("count").toString();

    String guileCode =
        "(define count 0)\n"
            + "(string-for-each (lambda (c) (set! count (+ count 1))) \"hello\")\n"
            + "(display count)\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringToListWithIndicesComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    // Test string->list with start and end indices
    String kleinResult = lisp.evaluate("(length (string->list \"hello\" 1 4))").toString();
    String guileCode = "(display (length (string->list \"hello\" 1 4)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testEmptyStringOperationsComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    // Test operations on empty strings
    String kleinResult = lisp.evaluate("(string-length (string))").toString();
    String guileCode = "(display (string-length (string)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringMapPreservesLengthComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult =
        lisp.evaluate("(string-length (string-map char-upcase \"hello\"))").toString();
    String guileCode = "(display (string-length (string-map char-upcase \"hello\")))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }
}
