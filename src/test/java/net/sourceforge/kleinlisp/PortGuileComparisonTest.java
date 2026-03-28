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
 * Integration tests comparing KleinLisp port behavior with standard Scheme implementations. Uses
 * Guile for most tests, but falls back to Chicken Scheme when Guile deviates from R7RS. Note: Guile
 * 2.x needs (use-modules (ice-9 rdelim)) for read-line and does not have eof-object.
 */
public class PortGuileComparisonTest extends BaseTestClass {

  private static boolean guileAvailable = false;
  private static boolean chickenAvailable = false;

  @BeforeAll
  public static void checkSchemesAvailable() {
    try {
      Process process = Runtime.getRuntime().exec(new String[] {"guile", "--version"});
      int exitCode = process.waitFor();
      guileAvailable = (exitCode == 0);
    } catch (Exception e) {
      guileAvailable = false;
    }

    try {
      Process process = Runtime.getRuntime().exec(new String[] {"csi", "-version"});
      int exitCode = process.waitFor();
      chickenAvailable = (exitCode == 0);
    } catch (Exception e) {
      chickenAvailable = false;
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
      BufferedReader errReader =
          new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String output = reader.lines().collect(Collectors.joining("\n"));
      process.waitFor();
      return output.trim();
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  private String runChicken(String code) throws Exception {
    Path tempFile = Files.createTempFile("chicken_test", ".scm");
    Files.writeString(tempFile, code);

    try {
      Process process = Runtime.getRuntime().exec(new String[] {"csi", "-s", tempFile.toString()});
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String output = reader.lines().collect(Collectors.joining("\n"));
      process.waitFor();
      return output.trim();
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  /** Normalize boolean output for comparison. Guile uses #t/#f, KleinLisp uses true/false. */
  private String normalizeBoolean(String s) {
    return s.replace("#t", "true").replace("#f", "false");
  }

  // Port predicates

  @Test
  public void testPortPredicateOnStringOutputPort() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-output-string))");
    String kleinResult = lisp.evaluate("(port? sp)").toString();
    String guileCode = "(define sp (open-output-string)) (display (port? sp))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testInputPortPredicate() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"hello\"))");
    String kleinResult = lisp.evaluate("(input-port? sp)").toString();
    String guileCode = "(define sp (open-input-string \"hello\")) (display (input-port? sp))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testOutputPortPredicate() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-output-string))");
    String kleinResult = lisp.evaluate("(output-port? sp)").toString();
    String guileCode = "(define sp (open-output-string)) (display (output-port? sp))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testOutputPortOnInputPortIsFalse() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"hello\"))");
    String kleinResult = lisp.evaluate("(output-port? sp)").toString();
    String guileCode = "(define sp (open-input-string \"hello\")) (display (output-port? sp))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  // String ports

  @Test
  public void testOpenOutputStringAndGetOutputString() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"hello\" sp)");
    String kleinResult = lisp.evaluate("(get-output-string sp)").toString();

    String guileCode =
        "(define sp (open-output-string))\n"
            + "(display \"hello\" sp)\n"
            + "(write (get-output-string sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testReadCharFromStringPort() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"a\"))");
    String kleinResult = lisp.evaluate("(read-char sp)").toString();

    String guileCode = "(define sp (open-input-string \"a\"))\n" + "(write (read-char sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testPeekCharFromStringPort() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"ab\"))");
    String kleinPeek1 = lisp.evaluate("(peek-char sp)").toString();
    String kleinPeek2 = lisp.evaluate("(peek-char sp)").toString();

    // Peek should return same char twice
    assertEquals(kleinPeek1, kleinPeek2);

    String guileCode =
        "(define sp (open-input-string \"ab\"))\n"
            + "(write (peek-char sp))\n"
            + "(display \" \")\n"
            + "(write (peek-char sp))\n";
    String guileOutput = runGuile(guileCode);
    String[] guileParts = guileOutput.split(" ");

    assertEquals(guileParts[0], kleinPeek1);
    assertEquals(guileParts[1], kleinPeek2);
  }

  // read-line tests - use Guile with ice-9 rdelim module
  @Test
  public void testReadLineFromStringPort() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"hello\"))");
    String kleinResult = lisp.evaluate("(read-line sp)").toString();

    // Guile needs ice-9 rdelim for read-line
    String guileCode =
        "(use-modules (ice-9 rdelim))\n"
            + "(define sp (open-input-string \"hello\"))\n"
            + "(write (read-line sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // EOF object tests - use Chicken since Guile 2.x doesn't have eof-object
  @Test
  public void testEofObjectPredicateWithChicken() throws Exception {
    assumeTrue(chickenAvailable, "Chicken is not available");

    String kleinResult = lisp.evaluate("(eof-object? (eof-object))").toString();
    String chickenCode = "(display (eof-object? #!eof))\n";
    String chickenOutput = normalizeBoolean(runChicken(chickenCode));

    assertEquals(chickenOutput, kleinResult);
  }

  @Test
  public void testEofObjectPredicateFalse() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(eof-object? #f)").toString();
    String guileCode = "(display (eof-object? #f))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testReadCharReturnsEofAtEnd() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"\"))");
    String kleinResult = lisp.evaluate("(eof-object? (read-char sp))").toString();

    String guileCode =
        "(define sp (open-input-string \"\"))\n" + "(display (eof-object? (read-char sp)))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testReadLineReturnsEofAtEndWithChicken() throws Exception {
    assumeTrue(chickenAvailable, "Chicken is not available");

    lisp.evaluate("(define sp (open-input-string \"\"))");
    String kleinResult = lisp.evaluate("(eof-object? (read-line sp))").toString();

    // Chicken needs (chicken io) for read-line
    String chickenCode =
        "(import (chicken io))\n"
            + "(define sp (open-input-string \"\"))\n"
            + "(display (eof-object? (read-line sp)))\n";
    String chickenOutput = normalizeBoolean(runChicken(chickenCode));

    assertEquals(chickenOutput, kleinResult);
  }

  // Write operations

  @Test
  public void testWriteCharToStringPort() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-char #\\X sp)");
    String kleinResult = lisp.evaluate("(get-output-string sp)").toString();

    String guileCode =
        "(define sp (open-output-string))\n"
            + "(write-char #\\X sp)\n"
            + "(write (get-output-string sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testWriteStringMultiple() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-string \"Hello\" sp)");
    lisp.evaluate("(write-string \" World\" sp)");
    String kleinResult = lisp.evaluate("(get-output-string sp)").toString();

    String guileCode =
        "(define sp (open-output-string))\n"
            + "(display \"Hello\" sp)\n"
            + "(display \" World\" sp)\n"
            + "(write (get-output-string sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Current ports

  @Test
  public void testCurrentInputPortIsInputPort() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(input-port? (current-input-port))").toString();
    String guileCode = "(display (input-port? (current-input-port)))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testCurrentOutputPortIsOutputPort() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    String kleinResult = lisp.evaluate("(output-port? (current-output-port))").toString();
    String guileCode = "(display (output-port? (current-output-port)))\n";
    String guileOutput = normalizeBoolean(runGuile(guileCode));

    assertEquals(guileOutput, kleinResult);
  }

  // Character comparisons with Guile

  @Test
  public void testReadCharSequenceComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"abc\"))");
    String k1 = lisp.evaluate("(read-char sp)").toString();
    String k2 = lisp.evaluate("(read-char sp)").toString();
    String k3 = lisp.evaluate("(read-char sp)").toString();

    String guileCode =
        "(define sp (open-input-string \"abc\"))\n"
            + "(write (read-char sp)) (display \" \")\n"
            + "(write (read-char sp)) (display \" \")\n"
            + "(write (read-char sp))\n";
    String guileOutput = runGuile(guileCode);
    String[] parts = guileOutput.split(" ");

    assertEquals(parts[0], k1);
    assertEquals(parts[1], k2);
    assertEquals(parts[2], k3);
  }

  @Test
  public void testWriteCharSequenceComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-output-string))");
    lisp.evaluate("(write-char #\\a sp)");
    lisp.evaluate("(write-char #\\b sp)");
    lisp.evaluate("(write-char #\\c sp)");
    String kleinResult = lisp.evaluate("(get-output-string sp)").toString();

    String guileCode =
        "(define sp (open-output-string))\n"
            + "(write-char #\\a sp)\n"
            + "(write-char #\\b sp)\n"
            + "(write-char #\\c sp)\n"
            + "(write (get-output-string sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Special characters

  @Test
  public void testReadCharSpaceComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \" \"))");
    String kleinResult = lisp.evaluate("(read-char sp)").toString();

    String guileCode = "(define sp (open-input-string \" \"))\n" + "(write (read-char sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testReadCharNewlineComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"\\n\"))");
    String kleinResult = lisp.evaluate("(read-char sp)").toString();

    String guileCode = "(define sp (open-input-string \"\\n\"))\n" + "(write (read-char sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testReadCharTabComparesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"\\t\"))");
    String kleinResult = lisp.evaluate("(read-char sp)").toString();

    String guileCode = "(define sp (open-input-string \"\\t\"))\n" + "(write (read-char sp))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  // Using Chicken for R7RS compliance tests where Guile differs

  @Test
  public void testTextualPortPredicateWithChicken() throws Exception {
    assumeTrue(chickenAvailable, "Chicken is not available");

    lisp.evaluate("(define sp (open-output-string))");
    String kleinResult = lisp.evaluate("(textual-port? sp)").toString();

    // Chicken has textual-port? built-in (at least as true for string ports)
    String chickenCode =
        "(define sp (open-output-string))\n"
            + "(display (if (port? sp) \"true\" \"false\"))\n"; // Use port? as proxy
    String chickenOutput = runChicken(chickenCode);

    // Both should be true
    assertEquals("true", kleinResult);
    assertEquals("true", chickenOutput);
  }

  // Complex operations

  @Test
  public void testReadWriteRoundTrip() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define in (open-input-string \"test\"))");
    lisp.evaluate("(define out (open-output-string))");
    lisp.evaluate(
        "(define (copy-loop c)"
            + "  (if (eof-object? c)"
            + "      #t"
            + "      (begin (write-char c out) (copy-loop (read-char in)))))");
    lisp.evaluate("(copy-loop (read-char in))");
    String kleinResult = lisp.evaluate("(get-output-string out)").toString();

    String guileCode =
        "(define in (open-input-string \"test\"))\n"
            + "(define out (open-output-string))\n"
            + "(define (copy-loop c)\n"
            + "  (if (eof-object? c)\n"
            + "      #t\n"
            + "      (begin (write-char c out) (copy-loop (read-char in)))))\n"
            + "(copy-loop (read-char in))\n"
            + "(write (get-output-string out))\n";
    String guileOutput = runGuile(guileCode);

    assertEquals(guileOutput, kleinResult);
  }

  @Test
  public void testReadLineMultipleLinesWithGuile() throws Exception {
    assumeTrue(guileAvailable, "Guile is not available");

    lisp.evaluate("(define sp (open-input-string \"line1\\nline2\\nline3\"))");
    String k1 = lisp.evaluate("(read-line sp)").toString();
    String k2 = lisp.evaluate("(read-line sp)").toString();
    String k3 = lisp.evaluate("(read-line sp)").toString();

    // Guile needs ice-9 rdelim for read-line
    String guileCode =
        "(use-modules (ice-9 rdelim))\n"
            + "(define sp (open-input-string \"line1\\nline2\\nline3\"))\n"
            + "(write (read-line sp)) (display \" \")\n"
            + "(write (read-line sp)) (display \" \")\n"
            + "(write (read-line sp))\n";
    String guileOutput = runGuile(guileCode);
    String[] parts = guileOutput.split(" ");

    assertEquals(parts[0], k1);
    assertEquals(parts[1], k2);
    assertEquals(parts[2], k3);
  }
}
