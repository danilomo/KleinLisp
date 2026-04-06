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

/** Tests for WrongTypeToApplyException and type error messages - Guile-compatible error formats. */
public class WrongTypeTest extends BaseTestClass {

  @Test
  void testWrongTypeToApplyInteger() {
    WrongTypeToApplyException ex =
        assertThrows(WrongTypeToApplyException.class, () -> lisp.evaluate("(42 1 2)"));
    assertEquals("Wrong type to apply: 42", ex.getMessage());
  }

  @Test
  void testWrongTypeToApplyString() {
    WrongTypeToApplyException ex =
        assertThrows(WrongTypeToApplyException.class, () -> lisp.evaluate("(\"hello\" 1 2)"));
    assertTrue(ex.getMessage().startsWith("Wrong type to apply:"));
  }

  @Test
  void testWrongTypeToApplyList() {
    WrongTypeToApplyException ex =
        assertThrows(WrongTypeToApplyException.class, () -> lisp.evaluate("('(1 2 3) 0)"));
    assertTrue(ex.getMessage().startsWith("Wrong type to apply:"));
  }

  @Test
  void testWrongTypeToApplyBoolean() {
    WrongTypeToApplyException ex =
        assertThrows(WrongTypeToApplyException.class, () -> lisp.evaluate("(#t 1)"));
    assertTrue(ex.getMessage().startsWith("Wrong type to apply:"));
  }

  @Test
  void testCarOnNonPair() {
    LispArgumentError ex = assertThrows(LispArgumentError.class, () -> lisp.evaluate("(car 42)"));
    assertTrue(ex.getMessage().contains("Wrong type argument"));
    assertTrue(ex.getMessage().contains("expecting pair"));
    assertTrue(ex.getMessage().contains("42"));
  }

  @Test
  void testCarOnNil() {
    LispArgumentError ex = assertThrows(LispArgumentError.class, () -> lisp.evaluate("(car '())"));
    assertTrue(ex.getMessage().contains("Wrong type argument"));
    assertTrue(ex.getMessage().contains("expecting pair"));
  }

  @Test
  void testCdrOnNonPair() {
    LispArgumentError ex =
        assertThrows(LispArgumentError.class, () -> lisp.evaluate("(cdr \"hello\")"));
    assertTrue(ex.getMessage().contains("Wrong type argument"));
    assertTrue(ex.getMessage().contains("expecting pair"));
  }

  @Test
  void testListRefOutOfRange() {
    LispArgumentError ex =
        assertThrows(LispArgumentError.class, () -> lisp.evaluate("(list-ref '(1 2 3) 10)"));
    assertTrue(ex.getMessage().contains("out of range"));
    assertTrue(ex.getMessage().contains("10"));
  }

  @Test
  void testListRefWrongTypeList() {
    LispArgumentError ex =
        assertThrows(LispArgumentError.class, () -> lisp.evaluate("(list-ref 42 0)"));
    assertTrue(ex.getMessage().contains("Wrong type argument"));
    assertTrue(ex.getMessage().contains("expecting list"));
  }

  @Test
  void testListRefWrongTypeIndex() {
    LispArgumentError ex =
        assertThrows(LispArgumentError.class, () -> lisp.evaluate("(list-ref '(1 2 3) \"zero\")"));
    assertTrue(ex.getMessage().contains("Wrong type argument"));
    assertTrue(ex.getMessage().contains("expecting integer"));
  }

  @Test
  void testListTailOutOfRange() {
    LispArgumentError ex =
        assertThrows(LispArgumentError.class, () -> lisp.evaluate("(list-tail '(1 2) 5)"));
    assertTrue(ex.getMessage().contains("out of range"));
  }

  @Test
  void testValidFunctionCall() {
    // Make sure valid function calls still work
    assertEquals(6, evalAsInt("(+ 1 2 3)"));
    assertEquals(2, evalAsInt("(car '(2 3 4))"));
    assertEquals(5, evalAsInt("(list-ref '(3 4 5 6) 2)"));
  }
}
