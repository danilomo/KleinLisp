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

/** Tests for UnboundVariableException - Guile-compatible error messages for undefined symbols. */
public class UnboundVariableTest extends BaseTestClass {

  @Test
  void testUnboundVariable() {
    UnboundVariableException ex =
        assertThrows(UnboundVariableException.class, () -> lisp.evaluate("undefined-xyz"));
    assertEquals("Unbound variable: undefined-xyz", ex.getMessage());
    assertEquals("undefined-xyz", ex.getSymbolName());
  }

  @Test
  void testUnboundInFunctionPosition() {
    UnboundVariableException ex =
        assertThrows(UnboundVariableException.class, () -> lisp.evaluate("(nonexistent 1 2)"));
    assertEquals("Unbound variable: nonexistent", ex.getMessage());
  }

  @Test
  void testUnboundInArgument() {
    UnboundVariableException ex =
        assertThrows(UnboundVariableException.class, () -> lisp.evaluate("(+ 1 undefined-var)"));
    assertEquals("Unbound variable: undefined-var", ex.getMessage());
  }

  @Test
  void testUnboundInNestedExpression() {
    UnboundVariableException ex =
        assertThrows(
            UnboundVariableException.class, () -> lisp.evaluate("(+ 1 (+ 2 missing-symbol))"));
    assertEquals("Unbound variable: missing-symbol", ex.getMessage());
  }

  @Test
  void testDefinedVariableDoesNotThrow() {
    lisp.evaluate("(define x 42)");
    assertEquals(42, evalAsInt("x"));
  }

  @Test
  void testUnboundAfterShadowedScope() {
    // Define a variable, use it in a lambda, then access undefined
    lisp.evaluate("(define f (lambda (x) x))");
    UnboundVariableException ex =
        assertThrows(UnboundVariableException.class, () -> lisp.evaluate("y"));
    assertEquals("Unbound variable: y", ex.getMessage());
  }
}
