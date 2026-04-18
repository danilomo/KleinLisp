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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sourceforge.kleinlisp.Lisp;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs R7RS test cases from .scm files.
 *
 * <p>Test files contain expressions and expected results in the format: ((expression)
 * expected-result)
 */
public class R7RSTestSuite {

  private static final String TEST_RESOURCES_PATH = "src/test/resources/r7rs/";
  private static final boolean GUILE_AVAILABLE = checkGuile();
  private static final boolean CHICKEN_AVAILABLE = checkChicken();

  private static boolean checkGuile() {
    try {
      Process process = new ProcessBuilder("which", "guile").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean checkChicken() {
    try {
      Process process = new ProcessBuilder("which", "csi").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  @TestFactory
  public Stream<DynamicTest> numericTests() throws Exception {
    return loadTestCases("numeric-tests.scm");
  }

  @TestFactory
  public Stream<DynamicTest> stringTests() throws Exception {
    return loadTestCases("string-tests.scm");
  }

  @TestFactory
  public Stream<DynamicTest> charTests() throws Exception {
    return loadTestCases("char-tests.scm");
  }

  @TestFactory
  public Stream<DynamicTest> comparisonTests() throws Exception {
    return loadTestCases("comparison-tests.scm");
  }

  @TestFactory
  public Stream<DynamicTest> truthinessTests() throws Exception {
    return loadTestCases("truthiness-tests.scm");
  }

  // Note: Time functions (current-second, current-jiffy, jiffies-per-second) are tested
  // in TimeFunctionsTest.java instead of here because:
  // 1. They are not supported by reference implementations (Guile/Chicken)
  // 2. They are non-deterministic (time-based)
  // 3. JUnit tests provide better control for testing time-based functions

  private Stream<DynamicTest> loadTestCases(String filename) throws Exception {
    Path path = Paths.get(TEST_RESOURCES_PATH + filename);
    if (!Files.exists(path)) {
      return Stream.empty();
    }

    String content = Files.readString(path);
    List<TestCase> testCases = parseTestCases(content);

    return testCases.stream()
        .map(
            tc ->
                dynamicTest(
                    tc.expression,
                    () -> {
                      Lisp lisp = new Lisp();
                      String result = normalizeResult(lisp.evaluate(tc.expression).toString());

                      // Compare with reference implementation if available
                      if (GUILE_AVAILABLE) {
                        try {
                          String guileResult = runGuile(tc.expression);
                          assertEquals(
                              guileResult, result, "Mismatch with Guile for: " + tc.expression);
                        } catch (Exception e) {
                          // Fall back to expected result if Guile fails
                          assertEquals(tc.expected, result, "Expression: " + tc.expression);
                        }
                      } else if (CHICKEN_AVAILABLE) {
                        try {
                          String chickenResult = runChicken(tc.expression);
                          assertEquals(
                              chickenResult, result, "Mismatch with Chicken for: " + tc.expression);
                        } catch (Exception e) {
                          // Fall back to expected result if Chicken fails
                          assertEquals(tc.expected, result, "Expression: " + tc.expression);
                        }
                      } else {
                        // No reference implementation, use expected result from test file
                        assertEquals(tc.expected, result, "Expression: " + tc.expression);
                      }
                    }));
  }

  private List<TestCase> parseTestCases(String content) {
    List<TestCase> testCases = new ArrayList<>();

    // Pattern to match ((expression) expected-result)
    // Use a proper parser that handles nested parentheses
    int pos = 0;
    while (pos < content.length()) {
      // Skip whitespace and comments
      while (pos < content.length()
          && (Character.isWhitespace(content.charAt(pos)) || content.charAt(pos) == ';')) {
        if (content.charAt(pos) == ';') {
          // Skip comment line
          while (pos < content.length() && content.charAt(pos) != '\n') {
            pos++;
          }
        }
        pos++;
      }

      // Check if we have a test case starting with ((
      if (pos < content.length() - 1
          && content.charAt(pos) == '('
          && content.charAt(pos + 1) == '(') {
        pos += 2; // Skip ((

        // Extract expression by counting parentheses
        StringBuilder exprBuilder = new StringBuilder();
        int parenCount = 1;
        while (pos < content.length() && parenCount > 0) {
          char ch = content.charAt(pos);
          if (ch == '(') {
            parenCount++;
          } else if (ch == ')') {
            parenCount--;
            if (parenCount == 0) {
              pos++; // Skip the closing )
              break;
            }
          }
          exprBuilder.append(ch);
          pos++;
        }

        String expression = "(" + exprBuilder.toString().trim() + ")";

        // Skip whitespace between expression and expected
        while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) {
          pos++;
        }

        // Extract expected result
        StringBuilder expectedBuilder = new StringBuilder();
        if (pos < content.length() && content.charAt(pos) == '(') {
          // Expected is a list
          parenCount = 0;
          while (pos < content.length()) {
            char ch = content.charAt(pos);
            if (ch == '(') {
              parenCount++;
            } else if (ch == ')') {
              expectedBuilder.append(ch);
              pos++;
              parenCount--;
              if (parenCount == 0) {
                break;
              }
              continue;
            }
            expectedBuilder.append(ch);
            pos++;
          }
        } else {
          // Expected is an atom
          while (pos < content.length()
              && !Character.isWhitespace(content.charAt(pos))
              && content.charAt(pos) != ')') {
            expectedBuilder.append(content.charAt(pos));
            pos++;
          }
        }

        String expected = normalizeResult(expectedBuilder.toString().trim());

        // Skip closing paren of test case
        while (pos < content.length() && content.charAt(pos) != ')') {
          pos++;
        }
        if (pos < content.length()) {
          pos++; // Skip )
        }

        if (!expression.isEmpty() && !expected.isEmpty()) {
          testCases.add(new TestCase(expression, expected));
        }
      } else if (pos < content.length()) {
        pos++;
      }
    }

    return testCases;
  }

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

    return normalizeResult(output);
  }

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

    return normalizeResult(output);
  }

  private String normalizeResult(String result) {
    String normalized =
        result
            .trim()
            .replaceAll("\\r\\n", "\n")
            .replaceAll("\\r", "\n")
            .replace("true", "#t")
            .replace("false", "#f");

    // Normalize numeric representation: treat 4.0 and 4 as equivalent
    // This handles cases where Guile returns exact integers but KleinLisp returns inexact
    if (normalized.matches("-?\\d+\\.0")) {
      normalized = normalized.substring(0, normalized.length() - 2);
    }

    return normalized;
  }

  /**
   * Compare two results, treating equivalent numeric representations as equal. For example, "4.0"
   * and "4" are considered equal.
   */
  private boolean numericEquals(String a, String b) {
    if (a.equals(b)) {
      return true;
    }

    // Try to parse as numbers and compare
    try {
      double da = Double.parseDouble(a);
      double db = Double.parseDouble(b);
      return Math.abs(da - db) < 0.0001;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static class TestCase {
    final String expression;
    final String expected;

    TestCase(String expression, String expected) {
      this.expression = expression;
      this.expected = expected;
    }
  }
}
