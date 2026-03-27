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
 * Integration tests comparing KleinLisp character type behavior with Guile Scheme. These tests
 * ensure that KleinLisp's character implementation is compatible with standard Scheme.
 */
public class CharacterGuileComparisonTest extends BaseTestClass {

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

  /**
   * Normalize boolean output for comparison. Guile uses #t/#f, KleinLisp uses true/false.
   */
  private String normalizeBoolean(String s) {
    return s.replace("#t", "true").replace("#f", "false");
  }

  @Test
  public void testCharLiteralComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("#\\a").toString();
    // Use write instead of display to get the representation #\a
    String guileCode = "(write #\\a)\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharSpaceComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Guile display of #\space is just the space character, not "#\space"
    // So we use write instead which outputs the representation
    String kleinResult = lisp.evaluate("#\\space").toString();
    String guileCode = "(write #\\space)\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharNewlineComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Use write instead of display for proper representation
    String kleinResult = lisp.evaluate("#\\newline").toString();
    String guileCode = "(write #\\newline)\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharPredicateComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char? #\\a)").toString();
    String guileCode = "(display (char? #\\a))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharPredicateFalseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char? 97)").toString();
    String guileCode = "(display (char? 97))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharEqualComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char=? #\\a #\\a)").toString();
    String guileCode = "(display (char=? #\\a #\\a))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharLessThanComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char<? #\\a #\\b)").toString();
    String guileCode = "(display (char<? #\\a #\\b))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharCiEqualComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-ci=? #\\a #\\A)").toString();
    String guileCode = "(display (char-ci=? #\\a #\\A))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharAlphabeticComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-alphabetic? #\\a)").toString();
    String guileCode = "(display (char-alphabetic? #\\a))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharNumericComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-numeric? #\\5)").toString();
    String guileCode = "(display (char-numeric? #\\5))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharWhitespaceComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-whitespace? #\\space)").toString();
    String guileCode = "(display (char-whitespace? #\\space))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharUpperCaseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-upper-case? #\\A)").toString();
    String guileCode = "(display (char-upper-case? #\\A))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharLowerCaseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-lower-case? #\\a)").toString();
    String guileCode = "(display (char-lower-case? #\\a))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharToIntegerComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char->integer #\\a)").toString();
    String guileCode = "(display (char->integer #\\a))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testIntegerToCharComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(integer->char 65)").toString();
    String guileCode = "(write (integer->char 65))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharUpcaseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-upcase #\\a)").toString();
    String guileCode = "(write (char-upcase #\\a))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharDowncaseComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char-downcase #\\A)").toString();
    String guileCode = "(write (char-downcase #\\A))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testDigitValueViaCharIntegerComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    // Note: digit-value is not available in standard Guile, so we test via char->integer
    // Both should give the same integer value for a digit character
    String kleinResult = lisp.evaluate("(- (char->integer #\\5) (char->integer #\\0))").toString();
    String guileCode = "(display (- (char->integer #\\5) (char->integer #\\0)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testStringRefReturnsCharComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(string-ref \"hello\" 0)").toString();
    String guileCode = "(write (string-ref \"hello\" 0))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharInListComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(list #\\a #\\b #\\c)").toString();
    String guileCode = "(write (list #\\a #\\b #\\c))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharRoundTripComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(integer->char (char->integer #\\Z))").toString();
    String guileCode = "(write (integer->char (char->integer #\\Z)))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCharComparisonChainComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available, skipping comparison test");

    String kleinResult = lisp.evaluate("(char<? #\\a #\\b #\\c)").toString();
    String guileCode = "(display (char<? #\\a #\\b #\\c))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }
}
