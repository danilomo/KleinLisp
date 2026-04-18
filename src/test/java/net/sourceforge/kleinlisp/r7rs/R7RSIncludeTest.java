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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.kleinlisp.BaseTestClass;
import net.sourceforge.kleinlisp.LispRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for R7RS include and include-ci special forms.
 *
 * @author Danilo Oliveira
 */
public class R7RSIncludeTest extends BaseTestClass {

  @TempDir Path tempDir;

  // ========================================
  // Basic Include Tests
  // ========================================

  @Test
  public void testIncludeBasic() throws IOException {
    // Create a helper file
    Path helperFile = tempDir.resolve("helper.scm");
    Files.writeString(helperFile, "(define (double x) (* x 2))");

    // Include and use the helper
    lisp.evaluate("(include \"" + helperFile + "\")");
    assertEquals(10, evalAsInt("(double 5)"));
  }

  @Test
  public void testIncludeMultipleDefinitions() throws IOException {
    // Create a file with multiple definitions
    Path helperFile = tempDir.resolve("helpers.scm");
    Files.writeString(
        helperFile,
        "(define (double x) (* x 2))\n"
            + "(define (triple x) (* x 3))\n"
            + "(define (quadruple x) (* x 4))");

    // Include the file
    lisp.evaluate("(include \"" + helperFile + "\")");

    // All definitions should be available
    assertEquals(10, evalAsInt("(double 5)"));
    assertEquals(15, evalAsInt("(triple 5)"));
    assertEquals(20, evalAsInt("(quadruple 5)"));
  }

  @Test
  public void testIncludeMultipleFiles() throws IOException {
    // Create two helper files
    Path helper1 = tempDir.resolve("helper1.scm");
    Path helper2 = tempDir.resolve("helper2.scm");

    Files.writeString(helper1, "(define (add1 x) (+ x 1))");
    Files.writeString(helper2, "(define (add2 x) (+ x 2))");

    // Include both files
    lisp.evaluate("(include \"" + helper1 + "\" \"" + helper2 + "\")");

    // Both should be available
    assertEquals(6, evalAsInt("(add1 5)"));
    assertEquals(7, evalAsInt("(add2 5)"));
  }

  @Test
  public void testIncludeExpressionsEvaluated() throws IOException {
    // Create a file with side effects
    Path helperFile = tempDir.resolve("sideeffects.scm");
    Files.writeString(helperFile, "(define x 10)\n" + "(set! x (* x 2))");

    // Include the file
    lisp.evaluate("(include \"" + helperFile + "\")");

    // Side effects should have occurred
    assertEquals(20, evalAsInt("x"));
  }

  // ========================================
  // Relative Path Tests
  // ========================================

  @Test
  public void testIncludeRelativePath() throws IOException {
    // Create subdirectory with helper
    Path subdir = tempDir.resolve("subdir");
    Files.createDirectories(subdir);
    Path helperFile = subdir.resolve("helper.scm");
    Files.writeString(helperFile, "(define (negate x) (- x))");

    // Create main file that includes with relative path
    Path mainFile = subdir.resolve("main.scm");
    Files.writeString(mainFile, "(include \"helper.scm\")\n" + "(negate 42)");

    // Execute the main file - it should find helper.scm relative to main.scm
    lisp.execute(mainFile);

    // The included function should be available
    assertEquals(-42, evalAsInt("(negate 42)"));
  }

  // ========================================
  // Error Handling Tests
  // ========================================

  @Test
  public void testIncludeNonexistentFile() {
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              lisp.evaluate("(include \"/nonexistent/file.scm\")");
            });

    assertTrue(ex.getMessage().contains("file not found"));
  }

  @Test
  public void testIncludeRequiresStringArgument() {
    assertThrows(
        LispRuntimeException.class,
        () -> {
          lisp.evaluate("(include 42)");
        });
  }

  @Test
  public void testIncludeRequiresAtLeastOneArgument() {
    assertThrows(
        Exception.class,
        () -> {
          lisp.evaluate("(include)");
        });
  }

  // ========================================
  // Include-CI Tests
  // ========================================

  @Test
  public void testIncludeCiBasic() throws IOException {
    // Create a helper file
    Path helperFile = tempDir.resolve("helper-ci.scm");
    Files.writeString(helperFile, "(define (square x) (* x x))");

    // Include with include-ci
    lisp.evaluate("(include-ci \"" + helperFile + "\")");
    assertEquals(25, evalAsInt("(square 5)"));
  }

  @Test
  public void testIncludeCiNonexistentFile() {
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              lisp.evaluate("(include-ci \"/nonexistent/file.scm\")");
            });

    assertTrue(ex.getMessage().contains("file not found"));
  }

  // ========================================
  // Nested Include Tests
  // ========================================

  @Test
  public void testNestedIncludes() throws IOException {
    // Create a base helper
    Path base = tempDir.resolve("base.scm");
    Files.writeString(base, "(define base-value 100)");

    // Create a middle helper that includes base
    Path middle = tempDir.resolve("middle.scm");
    Files.writeString(
        middle, "(include \"base.scm\")\n" + "(define middle-value (* base-value 2))");

    // Create main file that includes middle
    Path main = tempDir.resolve("main.scm");
    Files.writeString(
        main, "(include \"middle.scm\")\n" + "(define main-value (* middle-value 2))");

    // Execute main - should transitively include everything
    lisp.execute(main);

    // All values should be defined
    assertEquals(100, evalAsInt("base-value"));
    assertEquals(200, evalAsInt("middle-value"));
    assertEquals(400, evalAsInt("main-value"));
  }

  // ========================================
  // Integration Tests
  // ========================================

  @Test
  public void testIncludeWithLibrarySystem() throws IOException {
    // Create a helper that uses imported functions
    Path helperFile = tempDir.resolve("helper-lib.scm");
    Files.writeString(
        helperFile, "(import (scheme base))\n" + "(define (add-all . nums) (apply + nums))");

    // Include the helper
    lisp.evaluate("(include \"" + helperFile + "\")");

    // Should work
    assertEquals(15, evalAsInt("(add-all 1 2 3 4 5)"));
  }

  @Test
  public void testIncludePreservesEnvironment() throws IOException {
    // Define something before include
    lisp.evaluate("(define before-value 42)");

    // Create and include a helper
    Path helperFile = tempDir.resolve("helper-env.scm");
    Files.writeString(helperFile, "(define included-value 99)");
    lisp.evaluate("(include \"" + helperFile + "\")");

    // Define something after include
    lisp.evaluate("(define after-value 77)");

    // All should be available
    assertEquals(42, evalAsInt("before-value"));
    assertEquals(99, evalAsInt("included-value"));
    assertEquals(77, evalAsInt("after-value"));
  }
}
