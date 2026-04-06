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
package net.sourceforge.kleinlisp.chicken;

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
 * Runs test cases extracted from Chicken Scheme test suite.
 *
 * <p>These tests are adapted from the Chicken Scheme project's test files, particularly:
 *
 * <ul>
 *   <li>r4rstest.scm - R4RS compliance tests by Aubrey Jaffer
 *   <li>r5rs_pitfalls.scm - R5RS edge case tests
 *   <li>library-tests.scm - Library function tests
 * </ul>
 *
 * <p>Test files contain expressions and expected results in the format: ((expression)
 * expected-result)
 */
public class ChickenSchemeTestSuite {

  private static final String TEST_RESOURCES_PATH = "src/test/resources/chicken/";
  private static final boolean CHICKEN_AVAILABLE = checkChicken();

  private static boolean checkChicken() {
    try {
      Process process = new ProcessBuilder("which", "csi").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  @TestFactory
  public Stream<DynamicTest> r4rsBasicTests() throws Exception {
    return loadTestCases("r4rs-basic-tests.scm");
  }

  @TestFactory
  public Stream<DynamicTest> r5rsPitfallsTests() throws Exception {
    return loadTestCases("r5rs-pitfalls-tests.scm");
  }

  @TestFactory
  public Stream<DynamicTest> libraryTests() throws Exception {
    return loadTestCases("library-tests.scm");
  }

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
                      String result;
                      try {
                        result = normalizeResult(lisp.evaluate(tc.expression).toString());
                      } catch (Exception e) {
                        fail(
                            "Exception evaluating: "
                                + tc.expression
                                + "\nExpected: "
                                + tc.expected
                                + "\nException: "
                                + e.getMessage());
                        return;
                      }

                      // Compare with Chicken reference implementation if available
                      if (CHICKEN_AVAILABLE) {
                        try {
                          String chickenResult = runChicken(tc.expression);
                          assertEquals(
                              chickenResult, result, "Mismatch with Chicken for: " + tc.expression);
                        } catch (Exception e) {
                          // Fall back to expected result if Chicken fails
                          assertResultEquals(tc.expected, result, tc.expression);
                        }
                      } else {
                        // No reference implementation, use expected result from test file
                        assertResultEquals(tc.expected, result, tc.expression);
                      }
                    }));
  }

  private void assertResultEquals(String expected, String actual, String expression) {
    // Check for exact match first
    if (expected.equals(actual)) {
      return;
    }

    // Try numeric comparison for floating point tolerance
    if (numericEquals(expected, actual)) {
      return;
    }

    fail("Expression: " + expression + "\nExpected: " + expected + "\nActual: " + actual);
  }

  private List<TestCase> parseTestCases(String content) {
    List<TestCase> testCases = new ArrayList<>();
    String[] lines = content.split("\n");

    for (String line : lines) {
      line = line.trim();

      // Skip comments and empty lines
      if (line.isEmpty() || line.startsWith(";")) {
        continue;
      }

      // Parse test case format: ((expression) expected-result)
      TestCase tc = parseTestLine(line);
      if (tc != null) {
        testCases.add(tc);
      }
    }

    return testCases;
  }

  private TestCase parseTestLine(String line) {
    if (!line.startsWith("((") || !line.endsWith(")")) {
      return null;
    }

    // Find the matching close paren for the expression
    int depth = 0;
    int exprEnd = -1;
    for (int i = 1; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '(') {
        depth++;
      } else if (c == ')') {
        depth--;
        if (depth == 0) {
          exprEnd = i;
          break;
        }
      }
    }

    if (exprEnd == -1) {
      return null;
    }

    String expression = line.substring(1, exprEnd + 1);
    String expected = line.substring(exprEnd + 1, line.length() - 1).trim();
    expected = normalizeResult(expected);

    return new TestCase(expression, expected);
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
