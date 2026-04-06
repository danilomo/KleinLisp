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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for the load and load-relative functions, including path resolution and cycle detection.
 *
 * @author Danilo Oliveira
 */
public class LoadFunctionTest extends BaseTestClass {

  private Path getTestResourcePath(String filename) {
    return Paths.get("src/test/resources/load-test", filename).toAbsolutePath();
  }

  // ==================== Standard load tests (relative to CWD) ====================

  @Test
  public void testLoadSimpleFile() {
    Path path = getTestResourcePath("simple.scm");
    lisp.evaluate("(load \"" + path + "\")");
    assertEquals(42, evalAsInt("simple-loaded-value"));
  }

  @Test
  public void testLoadRelativeToCwd() {
    // load with relative path should resolve relative to CWD
    // Use the path relative to the project root (which is the CWD during tests)
    lisp.evaluate("(load \"src/test/resources/load-test/simple.scm\")");
    assertEquals(42, evalAsInt("simple-loaded-value"));
  }

  // ==================== load-relative tests (relative to source file) ====================

  @Test
  public void testLoadRelativeWithRelativePath() {
    // Load main.scm which uses load-relative to load helper.scm
    Path path = getTestResourcePath("main.scm");
    lisp.evaluate("(load \"" + path + "\")");

    // helper.scm defines helper-value = 100 and helper-add
    assertEquals(100, evalAsInt("helper-value"));

    // main.scm defines main-value = (helper-add 50) = 50 + 100 = 150
    assertEquals(150, evalAsInt("main-value"));
  }

  @Test
  public void testLoadRelativeFromNestedDirectory() {
    // Load nested.scm which uses load-relative to load ../helper.scm
    Path path = getTestResourcePath("subdir/nested.scm");
    lisp.evaluate("(load \"" + path + "\")");

    // helper.scm defines helper-value = 100
    assertEquals(100, evalAsInt("helper-value"));

    // nested.scm defines nested-value = (helper-add 25) = 25 + 100 = 125
    assertEquals(125, evalAsInt("nested-value"));
  }

  @Test
  public void testCyclicalLoadDetection() {
    Path path = getTestResourcePath("cycle-a.scm");

    LispRuntimeException ex =
        assertThrows(LispRuntimeException.class, () -> lisp.evaluate("(load \"" + path + "\")"));

    assertTrue(
        ex.getMessage().contains("Cyclical load detected"),
        "Error message should mention cyclical load: " + ex.getMessage());
  }

  @Test
  public void testSelfLoadDetection() {
    Path path = getTestResourcePath("self-load.scm");

    LispRuntimeException ex =
        assertThrows(LispRuntimeException.class, () -> lisp.evaluate("(load \"" + path + "\")"));

    assertTrue(
        ex.getMessage().contains("Cyclical load detected"),
        "Error message should mention cyclical load: " + ex.getMessage());
  }

  @Test
  public void testLoadNonexistentFile() {
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> lisp.evaluate("(load \"/nonexistent/file/that/does/not/exist.scm\")"));

    assertTrue(
        ex.getMessage().contains("file not found"),
        "Error message should mention file not found: " + ex.getMessage());
  }

  @Test
  public void testCurrentLoadPathnameOutsideLoad() {
    // When not loading any file, current-load-pathname should return #f
    LispObject result = lisp.evaluate("(current-load-pathname)");
    assertEquals(BooleanObject.FALSE, result);
  }

  @Test
  public void testCurrentLoadPathnameInsideLoad() {
    // Load a file that captures current-load-pathname
    Path path = getTestResourcePath("pathname-test.scm");
    lisp.evaluate("(load \"" + path + "\")");

    // captured-pathname should be the absolute path of pathname-test.scm
    LispObject result = lisp.evaluate("captured-pathname");
    StringObject str = result.asString();
    assertNotNull(str, "captured-pathname should be a string");
    assertTrue(
        str.value().endsWith("pathname-test.scm"),
        "captured-pathname should end with pathname-test.scm: " + str.value());
    assertTrue(
        new File(str.value()).isAbsolute(),
        "captured-pathname should be an absolute path: " + str.value());
  }

  @Test
  public void testLoadRequiresStringArgument() {
    LispArgumentError ex = assertThrows(LispArgumentError.class, () -> lisp.evaluate("(load 123)"));
    assertTrue(
        ex.getMessage().contains("string filename"),
        "Error message should mention string filename: " + ex.getMessage());
  }

  @Test
  public void testLoadRequiresArgument() {
    LispArgumentError ex = assertThrows(LispArgumentError.class, () -> lisp.evaluate("(load)"));
    assertTrue(
        ex.getMessage().contains("requires a filename"),
        "Error message should mention requires filename: " + ex.getMessage());
  }

  @Test
  public void testSameFileCanBeLoadedTwice() {
    // Loading the same file twice should work (not a cycle - the first load completes)
    Path path = getTestResourcePath("simple.scm");
    lisp.evaluate("(load \"" + path + "\")");
    assertEquals(42, evalAsInt("simple-loaded-value"));

    // Modify and reload should work
    lisp.evaluate("(define simple-loaded-value 0)");
    assertEquals(0, evalAsInt("simple-loaded-value"));

    lisp.evaluate("(load \"" + path + "\")");
    assertEquals(42, evalAsInt("simple-loaded-value"));
  }
}
