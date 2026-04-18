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

import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispRuntimeException;
import org.junit.jupiter.api.Test;

/**
 * Tests for import filter validation in R7RS mode.
 *
 * <p>These tests verify that import filters properly validate symbol existence against library
 * exports when R7RS mode is enabled.
 *
 * @author Danilo Oliveira
 */
public class R7RSImportValidationTest {

  // ========================================
  // Only Filter Validation Tests
  // ========================================

  @Test
  public void testOnlyFilterValidatesSymbolsExist() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid import - symbols exist
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (only (scheme base) + - *))");
        });
  }

  @Test
  public void testOnlyFilterRejectsNonexistentSymbol() {
    Lisp r7rsLisp = new Lisp(true);

    // Invalid import - symbol doesn't exist
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              r7rsLisp.evaluate("(import (only (scheme base) nonexistent-function))");
            });

    assertTrue(ex.getMessage().contains("nonexistent-function"));
    assertTrue(ex.getMessage().contains("not exported"));
  }

  @Test
  public void testOnlyFilterRejectsMultipleNonexistentSymbols() {
    Lisp r7rsLisp = new Lisp(true);

    // Invalid import - first nonexistent symbol should fail
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (scheme base) + - nonexistent1 nonexistent2))");
        });
  }

  @Test
  public void testOnlyFilterDefaultModeDoesNotValidate() {
    Lisp defaultLisp = new Lisp(false);

    // In default mode, nonexistent symbols don't cause errors
    assertDoesNotThrow(
        () -> {
          defaultLisp.evaluate("(import (only (scheme base) nonexistent-function))");
        });
  }

  // ========================================
  // Except Filter Validation Tests
  // ========================================

  @Test
  public void testExceptFilterValidatesSymbolsExist() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid import - excluded symbols exist
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (except (scheme base) + -))");
        });
  }

  @Test
  public void testExceptFilterRejectsNonexistentSymbol() {
    Lisp r7rsLisp = new Lisp(true);

    // Invalid import - trying to exclude symbol that doesn't exist
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              r7rsLisp.evaluate("(import (except (scheme base) nonexistent-function))");
            });

    assertTrue(ex.getMessage().contains("nonexistent-function"));
    assertTrue(ex.getMessage().contains("cannot exclude"));
  }

  @Test
  public void testExceptFilterDefaultModeDoesNotValidate() {
    Lisp defaultLisp = new Lisp(false);

    // In default mode, nonexistent symbols don't cause errors
    assertDoesNotThrow(
        () -> {
          defaultLisp.evaluate("(import (except (scheme base) nonexistent-function))");
        });
  }

  // ========================================
  // Rename Filter Validation Tests
  // ========================================

  @Test
  public void testRenameFilterValidatesSymbolsExist() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid import - renamed symbols exist
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (rename (scheme base) (+ add) (- subtract)))");
        });
  }

  @Test
  public void testRenameFilterRejectsNonexistentSymbol() {
    Lisp r7rsLisp = new Lisp(true);

    // Invalid import - trying to rename symbol that doesn't exist
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              r7rsLisp.evaluate("(import (rename (scheme base) (nonexistent-function foo)))");
            });

    assertTrue(ex.getMessage().contains("nonexistent-function"));
    assertTrue(ex.getMessage().contains("cannot rename"));
  }

  @Test
  public void testRenameFilterDefaultModeDoesNotValidate() {
    Lisp defaultLisp = new Lisp(false);

    // In default mode, nonexistent symbols don't cause errors
    assertDoesNotThrow(
        () -> {
          defaultLisp.evaluate("(import (rename (scheme base) (nonexistent-function foo)))");
        });
  }

  // ========================================
  // Nested Filter Validation Tests
  // ========================================

  @Test
  public void testNestedOnlyFiltersValidate() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid nested import
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (only (only (scheme base) + - * /) + -))");
        });

    // Invalid nested import - inner filter has nonexistent symbol
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (only (scheme base) + - nonexistent) +))");
        });

    // Invalid nested import - outer filter has valid symbol from inner, but doesn't exist in
    // library
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (only (scheme base) + -) nonexistent))");
        });
  }

  @Test
  public void testOnlyAfterExceptValidates() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid: select from what remains after except
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (only (except (scheme base) cons) +))");
        });

    // Invalid: try to select nonexistent symbol
    // Note: Current implementation validates against original library, not filtered result
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (except (scheme base) cons) nonexistent))");
        });
  }

  @Test
  public void testRenameAfterOnlyValidates() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid: rename symbols that were selected with only
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (rename (only (scheme base) + -) (+ add)))");
        });

    // Invalid: try to rename symbol that doesn't exist
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (rename (only (scheme base) + -) (nonexistent foo)))");
        });
  }

  // ========================================
  // Different Library Tests
  // ========================================

  @Test
  public void testValidationWorksForSchemeWrite() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid imports
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (only (scheme write) write display))");
        });

    // Invalid import
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (scheme write) nonexistent-write-function))");
        });
  }

  @Test
  public void testValidationWorksForSchemeChar() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid imports
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (only (scheme char) char-upcase char-downcase))");
        });

    // Invalid import
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (scheme char) nonexistent-char-function))");
        });
  }

  @Test
  public void testValidationWorksForSchemeCxr() {
    Lisp r7rsLisp = new Lisp(true);

    // Valid imports (scheme cxr has 3 and 4 letter combinations)
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (only (scheme cxr) caaar caadr cadar caddr))");
        });

    // Invalid import
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (scheme cxr) nonexistent-cxr))");
        });
  }

  // ========================================
  // Error Message Quality Tests
  // ========================================

  @Test
  public void testErrorMessageIncludesLibraryName() {
    Lisp r7rsLisp = new Lisp(true);

    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              r7rsLisp.evaluate("(import (only (scheme base) nonexistent))");
            });

    assertTrue(
        ex.getMessage().contains("(scheme base)"), "Error message should include library name");
  }

  @Test
  public void testErrorMessageIncludesSymbolName() {
    Lisp r7rsLisp = new Lisp(true);

    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> {
              r7rsLisp.evaluate("(import (only (scheme base) my-nonexistent-symbol))");
            });

    assertTrue(
        ex.getMessage().contains("my-nonexistent-symbol"),
        "Error message should include symbol name");
  }

  // ========================================
  // Edge Cases
  // ========================================

  @Test
  public void testValidationWithEmptyOnlyListFails() {
    Lisp r7rsLisp = new Lisp(true);

    // Only requires at least one identifier
    assertThrows(
        Exception.class,
        () -> {
          r7rsLisp.evaluate("(import (only (scheme base)))");
        });
  }

  @Test
  public void testValidationWithEmptyExceptListFails() {
    Lisp r7rsLisp = new Lisp(true);

    // Except requires at least one identifier
    assertThrows(
        Exception.class,
        () -> {
          r7rsLisp.evaluate("(import (except (scheme base)))");
        });
  }

  @Test
  public void testValidationWithMixedValidAndInvalid() {
    Lisp r7rsLisp = new Lisp(true);

    // First valid symbol, then invalid - should fail on invalid
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (only (scheme base) + - nonexistent *))");
        });
  }

  // ========================================
  // Integration Test
  // ========================================

  @Test
  public void testCompleteR7RSWorkflow() {
    Lisp r7rsLisp = new Lisp(true);

    // Import base library with validation
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (scheme base))");
        });

    // Import with only filter
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (only (scheme write) write display))");
        });

    // Import with except filter
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (except (scheme char) digit-value))");
        });

    // Functions should still be available (backwards-compatible mode)
    Object result = r7rsLisp.evaluate("(+ 1 2 3)");
    assertEquals("6", result.toString());
  }
}
