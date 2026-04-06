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
 * Tests for special form validation errors - Guile-compatible error formats. Format: "<form>: bad
 * <form> in form <full-form>"
 */
public class SpecialFormErrorTest extends BaseTestClass {

  @Test
  void testBadLetNoBindings() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(let)"));
    assertTrue(ex.getMessage().contains("let: bad let"));
  }

  @Test
  void testBadLetNonListBindings() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(let 42 1)"));
    assertTrue(ex.getMessage().contains("let: bad let"));
  }

  @Test
  void testBadLetInvalidBinding() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(let ((42 1)) x)"));
    assertTrue(ex.getMessage().contains("let: bad let"));
  }

  @Test
  void testBadLetStarNoBindings() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(let*)"));
    assertTrue(ex.getMessage().contains("let*: bad let*"));
  }

  @Test
  void testBadLetStarNonListBindings() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(let* 42 1)"));
    assertTrue(ex.getMessage().contains("let*: bad let*"));
  }

  @Test
  void testBadLetrecNoBindings() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(letrec)"));
    assertTrue(ex.getMessage().contains("letrec: bad letrec"));
  }

  @Test
  void testBadLetrecNonListBindings() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(letrec 42 1)"));
    assertTrue(ex.getMessage().contains("letrec: bad letrec"));
  }

  @Test
  void testBadLambdaNoParams() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(lambda)"));
    assertTrue(ex.getMessage().contains("lambda: bad lambda"));
  }

  @Test
  void testBadLambdaNoBody() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(lambda ())"));
    assertTrue(ex.getMessage().contains("lambda: bad lambda"));
  }

  @Test
  void testBadDefineNoValue() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(define)"));
    assertTrue(ex.getMessage().contains("define: bad define"));
  }

  @Test
  void testBadDefineOnlyName() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(define x)"));
    assertTrue(ex.getMessage().contains("define: bad define"));
  }

  @Test
  void testBadIfNoCondition() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(if)"));
    assertTrue(ex.getMessage().contains("if: bad if"));
  }

  @Test
  void testBadIfOnlyCondition() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(if #t)"));
    assertTrue(ex.getMessage().contains("if: bad if"));
  }

  @Test
  void testBadIfTooManyArgs() {
    LispException ex = assertThrows(LispException.class, () -> lisp.evaluate("(if #t 1 2 3)"));
    assertTrue(ex.getMessage().contains("if: bad if"));
  }

  @Test
  void testValidLet() {
    assertEquals(3, evalAsInt("(let ((x 1) (y 2)) (+ x y))"));
  }

  @Test
  void testValidLetStar() {
    assertEquals(3, evalAsInt("(let* ((x 1) (y (+ x 1))) (+ x y))"));
  }

  @Test
  void testValidLetrec() {
    assertEquals(
        120,
        evalAsInt("(letrec ((fact (lambda (n) (if (= n 0) 1 (* n (fact (- n 1))))))) (fact 5))"));
  }

  @Test
  void testValidLambda() {
    assertEquals(5, evalAsInt("((lambda (x y) (+ x y)) 2 3)"));
  }

  @Test
  void testValidDefine() {
    lisp.evaluate("(define x 42)");
    assertEquals(42, evalAsInt("x"));
  }

  @Test
  void testValidIf() {
    assertEquals(1, evalAsInt("(if #t 1 2)"));
    assertEquals(2, evalAsInt("(if #f 1 2)"));
    assertEquals(1, evalAsInt("(if #t 1)")); // No else branch
  }
}
