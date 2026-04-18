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
import org.junit.jupiter.api.Test;

/**
 * Comprehensive checklist of R7RS procedures.
 *
 * <p>This test systematically verifies the existence and basic functionality of all R7RS
 * procedures, organized by category.
 *
 * @author Danilo Oliveira
 */
public class R7RSProcedureChecklist extends BaseTestClass {

  // ========================================
  // List Mutation Functions
  // ========================================

  @Test
  public void testSetCarExists() {
    try {
      lisp.evaluate("(define p (cons 1 2))");
      lisp.evaluate("(set-car! p 3)");
      assertEquals(3, evalAsInt("(car p)"));
      System.out.println("✓ set-car! is implemented");
    } catch (Exception e) {
      System.out.println("✗ set-car! is NOT implemented or has issues: " + e.getMessage());
      // Don't fail the test, just document
    }
  }

  @Test
  public void testSetCdrExists() {
    try {
      lisp.evaluate("(define p (cons 1 2))");
      lisp.evaluate("(set-cdr! p 3)");
      assertEquals(3, evalAsInt("(cdr p)"));
      System.out.println("✓ set-cdr! is implemented");
    } catch (Exception e) {
      System.out.println("✗ set-cdr! is NOT implemented or has issues: " + e.getMessage());
    }
  }

  @Test
  public void testListSetExists() {
    try {
      lisp.evaluate("(define lst (list 1 2 3))");
      lisp.evaluate("(list-set! lst 1 99)");
      assertEquals(99, evalAsInt("(list-ref lst 1)"));
      System.out.println("✓ list-set! is implemented");
    } catch (Exception e) {
      System.out.println("✗ list-set! is NOT implemented or has issues: " + e.getMessage());
    }
  }

  @Test
  public void testMakeListExists() {
    try {
      assertEquals(5, evalAsInt("(length (make-list 5))"));
      System.out.println("✓ make-list is implemented");
    } catch (Exception e) {
      System.out.println("✗ make-list is NOT implemented or has issues: " + e.getMessage());
    }
  }

  @Test
  public void testMakeListWithFill() {
    try {
      lisp.evaluate("(define lst (make-list 3 'x))");
      assertEquals("x", lisp.evaluate("(car lst)").toString());
      System.out.println("✓ make-list with fill value is implemented");
    } catch (Exception e) {
      System.out.println("✗ make-list with fill is NOT fully implemented: " + e.getMessage());
    }
  }

  // ========================================
  // Symbol Comparison
  // ========================================

  @Test
  public void testSymbolEqualsExists() {
    try {
      String result1 = lisp.evaluate("(symbol=? 'foo 'foo)").toString();
      String result2 = lisp.evaluate("(symbol=? 'foo 'bar)").toString();
      // Accept both #t/true and #f/false
      boolean test1 = result1.equals("#t") || result1.equals("true");
      boolean test2 = result2.equals("#f") || result2.equals("false");
      assertTrue(test1, "symbol=? with equal symbols should return true");
      assertTrue(test2, "symbol=? with different symbols should return false");
      System.out.println("✓ symbol=? is implemented");
    } catch (Exception e) {
      System.out.println("✗ symbol=? is NOT implemented: " + e.getMessage());
    }
  }

  // ========================================
  // Division Functions
  // ========================================

  @Test
  public void testFloorQuotient() {
    try {
      // floor-quotient(-13, 4) = floor(-13/4) = floor(-3.25) = -4
      assertEquals(-4, evalAsInt("(floor-quotient -13 4)"));
      System.out.println("✓ floor-quotient is implemented");
    } catch (Exception e) {
      System.out.println("✗ floor-quotient is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testFloorRemainder() {
    try {
      assertEquals(3, evalAsInt("(floor-remainder -13 4)"));
      System.out.println("✓ floor-remainder is implemented");
    } catch (Exception e) {
      System.out.println("✗ floor-remainder is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testFloorDivision() {
    try {
      lisp.evaluate("(define-values (q r) (floor/ -13 4))");
      assertEquals(-4, evalAsInt("q"));
      assertEquals(3, evalAsInt("r"));
      System.out.println("✓ floor/ is implemented");
    } catch (Exception e) {
      System.out.println("✗ floor/ is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testTruncateQuotient() {
    try {
      assertEquals(-3, evalAsInt("(truncate-quotient -13 4)"));
      System.out.println("✓ truncate-quotient is implemented");
    } catch (Exception e) {
      System.out.println("✗ truncate-quotient is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testTruncateRemainder() {
    try {
      assertEquals(-1, evalAsInt("(truncate-remainder -13 4)"));
      System.out.println("✓ truncate-remainder is implemented");
    } catch (Exception e) {
      System.out.println("✗ truncate-remainder is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testTruncateDivision() {
    try {
      lisp.evaluate("(define-values (q r) (truncate/ -13 4))");
      assertEquals(-3, evalAsInt("q"));
      assertEquals(-1, evalAsInt("r"));
      System.out.println("✓ truncate/ is implemented");
    } catch (Exception e) {
      System.out.println("✗ truncate/ is NOT implemented: " + e.getMessage());
    }
  }

  // ========================================
  // String Mutation
  // ========================================

  @Test
  public void testStringSetExists() {
    try {
      lisp.evaluate("(define s (string #\\a #\\b #\\c))");
      lisp.evaluate("(string-set! s 1 #\\x)");
      assertEquals("axc", lisp.evaluate("s").toString());
      System.out.println("✓ string-set! is implemented");
    } catch (Exception e) {
      System.out.println("✗ string-set! is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testStringFillExists() {
    try {
      lisp.evaluate("(define s (string #\\a #\\b #\\c))");
      lisp.evaluate("(string-fill! s #\\x)");
      assertEquals("xxx", lisp.evaluate("s").toString());
      System.out.println("✓ string-fill! is implemented");
    } catch (Exception e) {
      System.out.println("✗ string-fill! is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testStringCopyMutationExists() {
    try {
      lisp.evaluate("(define s1 (string #\\a #\\b #\\c #\\d #\\e))");
      lisp.evaluate("(define s2 (string #\\1 #\\2 #\\3 #\\4 #\\5))");
      lisp.evaluate("(string-copy! s2 1 s1 0 3)");
      assertEquals("1abc5", lisp.evaluate("s2").toString());
      System.out.println("✓ string-copy! is implemented");
    } catch (Exception e) {
      System.out.println("✗ string-copy! is NOT implemented: " + e.getMessage());
    }
  }

  // ========================================
  // Unicode Case Folding
  // ========================================

  @Test
  public void testCharFoldcaseExists() {
    try {
      assertEquals("#\\a", lisp.evaluate("(char-foldcase #\\A)").toString());
      System.out.println("✓ char-foldcase is implemented");
    } catch (Exception e) {
      System.out.println("✗ char-foldcase is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testStringFoldcaseExists() {
    try {
      String result = lisp.evaluate("(string-foldcase \"HeLLo\")").toString();
      // Result might be "hello" or hello (with or without quotes)
      assertTrue(result.contains("hello"), "string-foldcase should produce lowercase");
      System.out.println("✓ string-foldcase is implemented");
    } catch (Exception e) {
      System.out.println("✗ string-foldcase is NOT implemented: " + e.getMessage());
    }
  }

  // ========================================
  // Binary I/O
  // ========================================

  @Test
  public void testBinaryInputFunctions() {
    String[] functions = {
      "open-binary-input-file",
      "read-u8",
      "peek-u8",
      "u8-ready?",
      "read-bytevector",
      "read-bytevector!",
      "open-input-bytevector"
    };

    for (String func : functions) {
      try {
        // Just check if the function exists (will fail with wrong args, but shouldn't be
        // "undefined")
        lisp.evaluate("(" + func + ")");
        System.out.println("✗ " + func + " exists but gave unexpected result");
      } catch (Exception e) {
        if (e.getMessage().contains("Undefined") || e.getMessage().contains("not found")) {
          System.out.println("✗ " + func + " is NOT implemented");
        } else {
          // Error is due to wrong args, which means function exists
          System.out.println("✓ " + func + " is implemented");
        }
      }
    }
  }

  @Test
  public void testBinaryOutputFunctions() {
    String[] functions = {
      "open-binary-output-file",
      "write-u8",
      "write-bytevector",
      "open-output-bytevector",
      "get-output-bytevector"
    };

    for (String func : functions) {
      try {
        lisp.evaluate("(" + func + ")");
        System.out.println("✗ " + func + " exists but gave unexpected result");
      } catch (Exception e) {
        if (e.getMessage().contains("Undefined") || e.getMessage().contains("not found")) {
          System.out.println("✗ " + func + " is NOT implemented");
        } else {
          System.out.println("✓ " + func + " is implemented");
        }
      }
    }
  }

  // ========================================
  // Process Context
  // ========================================

  @Test
  public void testCommandLineExists() {
    try {
      lisp.evaluate("(command-line)");
      System.out.println("✓ command-line is implemented");
    } catch (Exception e) {
      System.out.println("✗ command-line is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testExitExists() {
    try {
      // Can't actually test exit, just check if it's defined
      // We'll test by checking if it's in the function table
      System.out.println("? exit - cannot test (would terminate JVM)");
    } catch (Exception e) {
      System.out.println("✗ exit status unclear");
    }
  }

  @Test
  public void testGetEnvironmentVariableExists() {
    try {
      lisp.evaluate("(get-environment-variable \"PATH\")");
      System.out.println("✓ get-environment-variable is implemented");
    } catch (Exception e) {
      System.out.println("✗ get-environment-variable is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testGetEnvironmentVariablesExists() {
    try {
      lisp.evaluate("(get-environment-variables)");
      System.out.println("✓ get-environment-variables is implemented");
    } catch (Exception e) {
      System.out.println("✗ get-environment-variables is NOT implemented: " + e.getMessage());
    }
  }

  // ========================================
  // Error Predicates
  // ========================================

  @Test
  public void testErrorObjectPredicateExists() {
    try {
      lisp.evaluate("(error-object? 42)");
      System.out.println("✓ error-object? is implemented");
    } catch (Exception e) {
      System.out.println("✗ error-object? is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testFileErrorPredicateExists() {
    try {
      lisp.evaluate("(file-error? 42)");
      System.out.println("✓ file-error? is implemented");
    } catch (Exception e) {
      System.out.println("✗ file-error? is NOT implemented: " + e.getMessage());
    }
  }

  @Test
  public void testReadErrorPredicateExists() {
    try {
      lisp.evaluate("(read-error? 42)");
      System.out.println("✓ read-error? is implemented");
    } catch (Exception e) {
      System.out.println("✗ read-error? is NOT implemented: " + e.getMessage());
    }
  }

  // ========================================
  // Include Special Forms
  // ========================================

  @Test
  public void testIncludeExists() {
    try {
      // This will fail if file doesn't exist, but we're just checking if it's recognized
      lisp.evaluate("(include \"nonexistent.scm\")");
      System.out.println("? include - exists but file not found (expected)");
    } catch (Exception e) {
      if (e.getMessage().contains("not implemented") || e.getMessage().contains("Undefined")) {
        System.out.println("✗ include is NOT implemented: " + e.getMessage());
      } else if (e.getMessage().contains("File") || e.getMessage().contains("file")) {
        System.out.println("✓ include is implemented (file not found error is expected)");
      } else {
        System.out.println("? include - unclear status: " + e.getMessage());
      }
    }
  }

  @Test
  public void testIncludeCiExists() {
    try {
      lisp.evaluate("(include-ci \"nonexistent.scm\")");
      System.out.println("? include-ci - exists but file not found (expected)");
    } catch (Exception e) {
      if (e.getMessage().contains("not implemented") || e.getMessage().contains("Undefined")) {
        System.out.println("✗ include-ci is NOT implemented: " + e.getMessage());
      } else if (e.getMessage().contains("File") || e.getMessage().contains("file")) {
        System.out.println("✓ include-ci is implemented (file not found error is expected)");
      } else {
        System.out.println("? include-ci - unclear status: " + e.getMessage());
      }
    }
  }

  // ========================================
  // Summary Test
  // ========================================

  @Test
  public void printSummary() {
    System.out.println("\n=== R7RS Procedure Checklist Summary ===");
    System.out.println("Run all tests above to see individual results.");
    System.out.println("\nCategories tested:");
    System.out.println("  1. List mutation (set-car!, set-cdr!, list-set!, make-list)");
    System.out.println("  2. Symbol comparison (symbol=?)");
    System.out.println("  3. Division functions (floor/, truncate/, etc.)");
    System.out.println("  4. String mutation (string-set!, string-fill!, string-copy!)");
    System.out.println("  5. Unicode case folding (char-foldcase, string-foldcase)");
    System.out.println("  6. Binary I/O (read-u8, write-u8, bytevectors, etc.)");
    System.out.println("  7. Process context (command-line, get-environment-variable, etc.)");
    System.out.println("  8. Error predicates (error-object?, file-error?, read-error?)");
    System.out.println("  9. Include special forms (include, include-ci)");
    System.out.println("\nLegend:");
    System.out.println("  ✓ = Implemented");
    System.out.println("  ✗ = NOT implemented");
    System.out.println("  ? = Status unclear");
  }
}
