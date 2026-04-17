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

import org.junit.jupiter.api.Test;

/**
 * Tests for R7RS library system (import and define-library).
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Import statements in both R7RS and default modes
 *   <li>Define-library for user libraries
 *   <li>Import filters (only, except, prefix, rename)
 *   <li>Library registry functionality
 * </ul>
 *
 * @author Danilo Oliveira
 */
public class LibrarySystemTest extends BaseTestClass {

  // ========== Import Tests - Default Mode ==========

  @Test
  public void testImportInDefaultModeIsNoOp() {
    // In default mode, import should be a no-op (backwards compatible)
    lisp.evaluate("(import (scheme base))");
    // Should succeed without error
    int result = evalAsInt("(+ 1 2)");
    assertEquals(3, result);
  }

  @Test
  public void testImportMultipleLibrariesDefaultMode() {
    lisp.evaluate("(import (scheme base) (scheme write) (scheme file))");
    // All functions should still be available
    assertEquals(3, evalAsInt("(+ 1 2)"));
  }

  @Test
  public void testImportWithOnlyFilterDefaultMode() {
    // Should work as no-op in default mode
    lisp.evaluate("(import (only (scheme base) + - *))");
    assertEquals(6, evalAsInt("(+ 1 2 3)"));
  }

  @Test
  public void testImportWithExceptFilterDefaultMode() {
    // Should work as no-op in default mode
    lisp.evaluate("(import (except (scheme base) +))");
    // Even though + was "excepted", it should still be available in default mode
    assertEquals(3, evalAsInt("(+ 1 2)"));
  }

  @Test
  public void testImportWithPrefixFilterDefaultMode() {
    // Should work as no-op in default mode
    lisp.evaluate("(import (prefix (scheme base) sb:))");
    // Original names should still work
    assertEquals(3, evalAsInt("(+ 1 2)"));
  }

  @Test
  public void testImportWithRenameFilterDefaultMode() {
    // Should work as no-op in default mode
    lisp.evaluate("(import (rename (scheme base) (+ add) (- subtract)))");
    // Original names should still work
    assertEquals(3, evalAsInt("(+ 1 2)"));
  }

  // ========== Import Tests - R7RS Mode ==========

  @Test
  public void testImportInR7RSModeValidatesLibrary() {
    Lisp r7rsLisp = new Lisp(true);
    // Should succeed for known library
    r7rsLisp.evaluate("(import (scheme base))");
    int result = r7rsLisp.evaluate("(+ 1 2)").asInt().value;
    assertEquals(3, result);
  }

  @Test
  public void testImportInR7RSModeRejectsUnknownLibrary() {
    Lisp r7rsLisp = new Lisp(true);
    // Should fail for unknown library
    assertThrows(
        LispRuntimeException.class,
        () -> r7rsLisp.evaluate("(import (nonexistent library))"),
        "Should reject unknown library in R7RS mode");
  }

  @Test
  public void testImportMultipleStandardLibrariesR7RSMode() {
    Lisp r7rsLisp = new Lisp(true);
    r7rsLisp.evaluate("(import (scheme base) (scheme write) (scheme char))");
    // Functions should be available
    assertEquals(3, r7rsLisp.evaluate("(+ 1 2)").asInt().value);
  }

  @Test
  public void testImportWithOnlyFilterR7RSMode() {
    Lisp r7rsLisp = new Lisp(true);
    // Should validate the nested library exists
    r7rsLisp.evaluate("(import (only (scheme base) + -))");
    assertEquals(3, r7rsLisp.evaluate("(+ 1 2)").asInt().value);
  }

  @Test
  public void testImportWithExceptFilterR7RSMode() {
    Lisp r7rsLisp = new Lisp(true);
    r7rsLisp.evaluate("(import (except (scheme base) vector-length))");
    assertEquals(3, r7rsLisp.evaluate("(+ 1 2)").asInt().value);
  }

  @Test
  public void testImportWithPrefixFilterR7RSMode() {
    Lisp r7rsLisp = new Lisp(true);
    r7rsLisp.evaluate("(import (prefix (scheme base) base:))");
    assertEquals(3, r7rsLisp.evaluate("(+ 1 2)").asInt().value);
  }

  @Test
  public void testImportWithRenameFilterR7RSMode() {
    Lisp r7rsLisp = new Lisp(true);
    r7rsLisp.evaluate("(import (rename (scheme base) (cons pair)))");
    assertEquals(3, r7rsLisp.evaluate("(+ 1 2)").asInt().value);
  }

  @Test
  public void testImportNestedFiltersR7RSMode() {
    Lisp r7rsLisp = new Lisp(true);
    // Test nested filters
    r7rsLisp.evaluate("(import (only (except (scheme base) map) + -))");
    assertEquals(3, r7rsLisp.evaluate("(+ 1 2)").asInt().value);
  }

  // ========== Define-Library Tests ==========

  @Test
  public void testDefineLibraryBasic() {
    lisp.evaluate(
        "(define-library (mylib utils)"
            + "  (export double triple)"
            + "  (import (scheme base))"
            + "  (begin"
            + "    (define (double x) (* x 2))"
            + "    (define (triple x) (* x 3))))");

    // Functions should be available globally (backwards-compatible mode)
    assertEquals(10, evalAsInt("(double 5)"));
    assertEquals(15, evalAsInt("(triple 5)"));
  }

  @Test
  public void testDefineLibraryWithMultipleExports() {
    lisp.evaluate(
        "(define-library (math helpers)"
            + "  (export square cube add-one)"
            + "  (import (scheme base))"
            + "  (begin"
            + "    (define (square x) (* x x))"
            + "    (define (cube x) (* x x x))"
            + "    (define (add-one x) (+ x 1))))");

    assertEquals(25, evalAsInt("(square 5)"));
    assertEquals(125, evalAsInt("(cube 5)"));
    assertEquals(6, evalAsInt("(add-one 5)"));
  }

  @Test
  public void testDefineLibraryWithNoExports() {
    // Library with no exports
    lisp.evaluate(
        "(define-library (mylib private)"
            + "  (import (scheme base))"
            + "  (begin"
            + "    (define secret 42)))");

    // In backwards-compatible mode, even "private" functions are global
    assertEquals(42, evalAsInt("secret"));
  }

  @Test
  public void testDefineLibraryWithNestedDefinitions() {
    lisp.evaluate(
        "(define-library (mylib nested)"
            + "  (export factorial)"
            + "  (import (scheme base))"
            + "  (begin"
            + "    (define (factorial n)"
            + "      (if (<= n 1)"
            + "          1"
            + "          (* n (factorial (- n 1)))))))");

    assertEquals(120, evalAsInt("(factorial 5)"));
  }

  @Test
  public void testDefineLibraryCanBeImported() {
    // Define a library
    lisp.evaluate(
        "(define-library (mylib test)"
            + "  (export helper)"
            + "  (import (scheme base))"
            + "  (begin"
            + "    (define (helper x) (+ x 10))))");

    // Should be able to import it
    lisp.evaluate("(import (mylib test))");

    // Function should be available
    assertEquals(15, evalAsInt("(helper 5)"));
  }

  @Test
  public void testDefineLibraryInR7RSModeValidatesImports() {
    Lisp r7rsLisp = new Lisp(true);

    // Should succeed with valid import
    r7rsLisp.evaluate(
        "(define-library (mylib valid)"
            + "  (export func)"
            + "  (import (scheme base))"
            + "  (begin"
            + "    (define (func x) x)))");

    // Should fail with invalid import
    assertThrows(
        LispRuntimeException.class,
        () ->
            r7rsLisp.evaluate(
                "(define-library (mylib invalid)"
                    + "  (export func)"
                    + "  (import (nonexistent lib))"
                    + "  (begin"
                    + "    (define (func x) x)))"),
        "Should reject unknown library in imports");
  }

  @Test
  public void testDefineLibraryWithMultipleBeginBlocks() {
    lisp.evaluate(
        "(define-library (mylib multi)"
            + "  (export a b)"
            + "  (import (scheme base))"
            + "  (begin (define a 1))"
            + "  (begin (define b 2)))");

    assertEquals(1, evalAsInt("a"));
    assertEquals(2, evalAsInt("b"));
  }

  @Test
  public void testDefineLibraryWithComplexName() {
    lisp.evaluate(
        "(define-library (company project module 1)"
            + "  (export version)"
            + "  (import (scheme base))"
            + "  (begin (define version 1)))");

    // Should be registered
    assertTrue(
        lisp.environment()
            .getLibraryRegistry()
            .exists(java.util.Arrays.asList("company", "project", "module", 1)));
  }

  // ========== Library Registry Tests ==========

  @Test
  public void testLibraryRegistryContainsStandardLibraries() {
    LibraryRegistry registry = lisp.environment().getLibraryRegistry();

    assertTrue(registry.exists(java.util.Arrays.asList("scheme", "base")));
    assertTrue(registry.exists(java.util.Arrays.asList("scheme", "write")));
    assertTrue(registry.exists(java.util.Arrays.asList("scheme", "char")));
    assertTrue(registry.exists(java.util.Arrays.asList("scheme", "file")));
    assertTrue(registry.exists(java.util.Arrays.asList("scheme", "lazy")));
  }

  @Test
  public void testLibraryRegistryCanRegisterUserLibraries() {
    LibraryRegistry registry = lisp.environment().getLibraryRegistry();

    java.util.List<Object> libName = java.util.Arrays.asList("user", "library");
    assertFalse(registry.exists(libName), "Library should not exist initially");

    // Define the library
    lisp.evaluate(
        "(define-library (user library)"
            + "  (export foo)"
            + "  (import (scheme base))"
            + "  (begin (define foo 42)))");

    assertTrue(registry.exists(libName), "Library should exist after definition");
  }

  @Test
  public void testLibraryRegistryGet() {
    LibraryRegistry registry = lisp.environment().getLibraryRegistry();

    java.util.List<Object> libName = java.util.Arrays.asList("scheme", "base");
    Library lib = registry.get(libName);

    assertNotNull(lib, "Should retrieve standard library");
    assertEquals("(scheme base)", lib.getNameString());
  }

  // ========== Error Handling Tests ==========

  @Test
  public void testImportWithInvalidSyntax() {
    assertThrows(
        LispException.class,
        () -> lisp.evaluate("(import)"),
        "Import requires at least one argument");
  }

  @Test
  public void testDefineLibraryWithInvalidSyntax() {
    assertThrows(
        LispException.class,
        () -> lisp.evaluate("(define-library)"),
        "Define-library requires at least name and one declaration");
  }

  @Test
  public void testDefineLibraryWithNonListName() {
    assertThrows(
        LispRuntimeException.class,
        () ->
            lisp.evaluate(
                "(define-library mylib"
                    + "  (export foo)"
                    + "  (import (scheme base))"
                    + "  (begin (define foo 1)))"),
        "Library name must be a list");
  }

  @Test
  public void testDefineLibraryWithInvalidDeclaration() {
    assertThrows(
        LispRuntimeException.class,
        () ->
            lisp.evaluate(
                "(define-library (mylib test)"
                    + "  (unknown-declaration foo)"
                    + "  (import (scheme base))"
                    + "  (begin (define foo 1)))"),
        "Should reject unknown declaration");
  }

  @Test
  public void testImportWithNonListLibraryName() {
    Lisp r7rsLisp = new Lisp(true);
    assertThrows(
        LispRuntimeException.class,
        () -> r7rsLisp.evaluate("(import scheme-base)"),
        "Import requires library names to be lists");
  }

  // ========== Integration Tests ==========

  @Test
  public void testDefineAndImportUserLibrary() {
    // Define a user library
    lisp.evaluate(
        "(define-library (calculator ops)"
            + "  (export add subtract multiply)"
            + "  (import (scheme base))"
            + "  (begin"
            + "    (define (add a b) (+ a b))"
            + "    (define (subtract a b) (- a b))"
            + "    (define (multiply a b) (* a b))))");

    // Import it (in backwards-compatible mode, this is a no-op)
    lisp.evaluate("(import (calculator ops))");

    // Use the functions
    assertEquals(8, evalAsInt("(add 3 5)"));
    assertEquals(2, evalAsInt("(subtract 5 3)"));
    assertEquals(15, evalAsInt("(multiply 3 5)"));
  }

  @Test
  public void testMultipleUserLibraries() {
    // Define first library
    lisp.evaluate(
        "(define-library (lib1)"
            + "  (export func1)"
            + "  (import (scheme base))"
            + "  (begin (define (func1 x) (* x 2))))");

    // Define second library
    lisp.evaluate(
        "(define-library (lib2)"
            + "  (export func2)"
            + "  (import (scheme base))"
            + "  (begin (define (func2 x) (* x 3))))");

    // Use both
    assertEquals(10, evalAsInt("(func1 5)"));
    assertEquals(15, evalAsInt("(func2 5)"));
  }

  @Test
  public void testStandardAndUserLibrariesCoexist() {
    // Define a user library
    lisp.evaluate(
        "(define-library (user lib)"
            + "  (export custom-+)"
            + "  (import (scheme base))"
            + "  (begin (define (custom-+ a b c) (+ a b c))))");

    // Standard library functions should still work
    assertEquals(3, evalAsInt("(+ 1 2)"));
    // User library function should work
    assertEquals(6, evalAsInt("(custom-+ 1 2 3)"));
  }
}
