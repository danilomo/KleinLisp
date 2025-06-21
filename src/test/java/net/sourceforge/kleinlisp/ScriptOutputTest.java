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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Parameterized test suite that runs Scheme scripts and compares their output against expected
 * results.
 */
public class ScriptOutputTest extends BaseTestClass {

  private static final String TEST_RESOURCES_PATH = "src/test/resources/script_tests";

  /**
   * Provides test script names by finding all .scm files that have corresponding .out files.
   */
  static Stream<String> provideTestScripts() throws IOException {
    Path resourcesDir = Paths.get(TEST_RESOURCES_PATH);

    if (!Files.exists(resourcesDir)) {
      return Stream.empty();
    }

    return Files.list(resourcesDir)
        .filter(path -> path.toString().endsWith(".scm"))
        .map(path -> path.getFileName().toString())
        .map(filename -> filename.substring(0, filename.length() - 4)) // Remove .scm extension
        .filter(basename -> Files.exists(resourcesDir.resolve(basename + ".out")))
        .sorted();
  }

  @ParameterizedTest(name = "Testing script: {0}")
  @MethodSource("provideTestScripts")
  public void testScriptOutput(String scriptName) throws IOException {
    Path scriptPath = Paths.get(TEST_RESOURCES_PATH, scriptName + ".scm");
    Path expectedOutputPath = Paths.get(TEST_RESOURCES_PATH, scriptName + ".out");

    // Execute the script
    lisp.execute(scriptPath);

    // Get actual output and normalize line endings
    String actualOutput = getStdOut().trim().replace("\r\n", "\n");

    // Read expected output and normalize line endings
    List<String> expectedLines = Files.readAllLines(expectedOutputPath);
    String expectedOutput = String.join("\n", expectedLines);

    assertEquals(
        expectedOutput,
        actualOutput,
        String.format(
            "Script %s.scm produced unexpected output.\nExpected:\n%s\nActual:\n%s",
            scriptName, expectedOutput, actualOutput));
  }
}
