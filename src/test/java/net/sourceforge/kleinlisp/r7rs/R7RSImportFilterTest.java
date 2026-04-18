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

import net.sourceforge.kleinlisp.BaseTestClass;
import net.sourceforge.kleinlisp.Library;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispRuntimeException;
import org.junit.jupiter.api.Test;

/**
 * Tests for R7RS import filter behavior.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Basic import statements (with and without r7rs mode)
 *   <li>Import filters: only, except, prefix, rename
 *   <li>Nested filter combinations
 *   <li>Validation behavior in r7rs mode
 *   <li>Error handling for invalid imports
 * </ul>
 *
 * <p>Note: KleinLisp uses a backwards-compatible hybrid approach where:
 *
 * <ul>
 *   <li>Default mode: All functions globally available, imports accepted as no-ops
 *   <li>R7RS mode: Import validation enabled, but functions still globally available
 * </ul>
 *
 * @author Danilo Oliveira
 */
public class R7RSImportFilterTest extends BaseTestClass {

  // ========================================
  // Basic Import Tests
  // ========================================

  @Test
  public void testBasicImportDefaultMode() {
    // In default mode, imports are no-ops
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (scheme base))");
        });
  }

  @Test
  public void testBasicImportR7RSMode() {
    // In R7RS mode, imports are validated
    Lisp r7rsLisp = new Lisp(true);
    assertDoesNotThrow(
        () -> {
          r7rsLisp.evaluate("(import (scheme base))");
        });
  }

  @Test
  public void testImportNonexistentLibraryDefaultMode() {
    // In default mode, nonexistent libraries don't throw errors
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (nonexistent library))");
        });
  }

  @Test
  public void testImportNonexistentLibraryR7RSMode() {
    // In R7RS mode, nonexistent libraries throw errors
    Lisp r7rsLisp = new Lisp(true);
    assertThrows(
        LispRuntimeException.class,
        () -> {
          r7rsLisp.evaluate("(import (nonexistent library))");
        });
  }

  @Test
  public void testMultipleImports() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (scheme base) (scheme write) (scheme read))");
        });
  }

  @Test
  public void testFunctionsAvailableWithoutImport() {
    // In backwards-compatible mode, all functions are always available
    // even without importing
    assertEquals(6, evalAsInt("(+ 1 2 3)"));
    assertEquals(3, evalAsInt("(- 10 7)"));
  }

  // ========================================
  // Only Filter Tests
  // ========================================

  @Test
  public void testOnlyFilterParsing() {
    // The only filter should parse correctly
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (only (scheme base) + - *))");
        });
  }

  @Test
  public void testOnlyFilterFunctionsStillGloballyAvailable() {
    // Even with only filter, all functions remain globally available
    // in backwards-compatible mode
    lisp.evaluate("(import (only (scheme base) +))");

    // + should work (in the only list)
    assertEquals(6, evalAsInt("(+ 1 2 3)"));

    // But - should also work (not in the only list, but globally available)
    assertEquals(5, evalAsInt("(- 10 5)"));
  }

  @Test
  public void testOnlyFilterWithNonexistentSymbol() {
    // TODO: Once export sets are populated, this should validate
    // For now, it just parses
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (only (scheme base) nonexistent-function))");
        });
  }

  @Test
  public void testNestedOnlyFilters() {
    // Nested only filters should parse
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (only (only (scheme base) + - * /) + -))");
        });
  }

  // ========================================
  // Except Filter Tests
  // ========================================

  @Test
  public void testExceptFilterParsing() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (except (scheme base) set!))");
        });
  }

  @Test
  public void testExceptFilterFunctionsStillGloballyAvailable() {
    // Even with except filter, excluded functions remain globally available
    lisp.evaluate("(import (except (scheme base) set!))");

    // set! should still work (backwards-compatible mode)
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(define x 10)");
          lisp.evaluate("(set! x 20)");
        });
  }

  @Test
  public void testExceptFilterMultipleSymbols() {
    assertDoesNotThrow(
        () -> {
          // Note: 'lambda' is a special form so we use different symbols
          lisp.evaluate("(import (except (scheme base) set! car cdr))");
        });
  }

  // ========================================
  // Prefix Filter Tests
  // ========================================

  @Test
  public void testPrefixFilterParsing() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (prefix (scheme base) base:))");
        });
  }

  @Test
  public void testPrefixFilterOriginalNamesStillAvailable() {
    // In backwards-compatible mode, prefixed imports don't create aliases
    // Original functions remain globally available
    lisp.evaluate("(import (prefix (scheme base) base:))");

    // Original name still works
    assertEquals(6, evalAsInt("(+ 1 2 3)"));

    // Prefixed name doesn't exist (not implemented in backwards-compatible mode)
    // This is a known limitation
  }

  @Test
  public void testPrefixFilterRequiresTwoArgs() {
    assertThrows(
        Exception.class,
        () -> {
          lisp.evaluate("(import (prefix (scheme base)))");
        });
  }

  // ========================================
  // Rename Filter Tests
  // ========================================

  @Test
  public void testRenameFilterParsing() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (rename (scheme base) (+ add) (- subtract)))");
        });
  }

  @Test
  public void testRenameFilterOriginalNamesStillAvailable() {
    // In backwards-compatible mode, renamed imports don't create aliases
    // Original functions remain globally available
    lisp.evaluate("(import (rename (scheme base) (+ add)))");

    // Original name still works
    assertEquals(6, evalAsInt("(+ 1 2 3)"));

    // Renamed name doesn't exist (not implemented in backwards-compatible mode)
    // This is a known limitation
  }

  @Test
  public void testRenameFilterMultiplePairs() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (rename (scheme base) (+ add) (- sub) (* mul) (/ div)))");
        });
  }

  // ========================================
  // Combined Filter Tests
  // ========================================

  @Test
  public void testOnlyWithPrefix() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (prefix (only (scheme base) + -) math:))");
        });
  }

  @Test
  public void testExceptWithRename() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (rename (except (scheme base) set!) (+ add)))");
        });
  }

  @Test
  public void testComplexNestedFilters() {
    assertDoesNotThrow(
        () -> {
          lisp.evaluate("(import (prefix (only (except (scheme base) set!) + - *) math:))");
        });
  }

  // ========================================
  // Standard Library Existence Tests
  // ========================================

  @Test
  public void testStandardLibrariesExist() {
    Lisp r7rsLisp = new Lisp(true);

    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme base))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme case-lambda))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme char))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme cxr))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme eval))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme file))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme inexact))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme lazy))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme load))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme process-context))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme read))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme repl))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme time))"));
    assertDoesNotThrow(() -> r7rsLisp.evaluate("(import (scheme write))"));
  }

  // ========================================
  // Export Set Tests (once populated)
  // ========================================

  @Test
  public void testLibraryExportSetsExist() {
    // Check if standard libraries have exports defined
    Library schemeBase =
        lisp.environment().getLibraryRegistry().get(java.util.Arrays.asList("scheme", "base"));

    assertNotNull(schemeBase, "scheme base library should be registered");

    // Currently exports are empty - this is a known issue
    // Once fixed, we should verify exports are populated
    assertNotNull(schemeBase.getExports(), "Export set should not be null");

    // TODO: After Phase 3.1, verify exports are populated:
    // assertTrue(schemeBase.getExports().contains("+"));
    // assertTrue(schemeBase.getExports().contains("define"));
    // etc.
  }

  // ========================================
  // Error Handling Tests
  // ========================================

  @Test
  public void testImportRequiresAtLeastOneArg() {
    assertThrows(
        Exception.class,
        () -> {
          lisp.evaluate("(import)");
        });
  }

  @Test
  public void testOnlyRequiresAtLeastTwoArgs() {
    assertThrows(
        Exception.class,
        () -> {
          lisp.evaluate("(import (only (scheme base)))");
        });
  }

  @Test
  public void testExceptRequiresAtLeastTwoArgs() {
    assertThrows(
        Exception.class,
        () -> {
          lisp.evaluate("(import (except (scheme base)))");
        });
  }

  @Test
  public void testRenameRequiresAtLeastTwoArgs() {
    assertThrows(
        Exception.class,
        () -> {
          lisp.evaluate("(import (rename (scheme base)))");
        });
  }

  @Test
  public void testInvalidImportSetThrowsError() {
    assertThrows(
        LispRuntimeException.class,
        () -> {
          lisp.evaluate("(import 42)");
        });
  }

  @Test
  public void testEmptyLibraryNameThrowsError() {
    // Empty library name causes exception during parsing/evaluation
    assertThrows(
        Exception.class,
        () -> {
          lisp.evaluate("(import ())");
        });
  }
}
