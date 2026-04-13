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
 * Tests for R7RS case-lambda special form.
 *
 * @author Danilo Oliveira
 */
public class CaseLambdaTest extends BaseTestClass {

  // case-lambda tests

  @Test
  public void testCaseLambdaZeroArgs() {
    assertEquals(0, evalAsInt("((case-lambda (() 0) ((x) x) ((x y) (+ x y))))"));
  }

  @Test
  public void testCaseLambdaOneArg() {
    assertEquals(5, evalAsInt("((case-lambda (() 0) ((x) x) ((x y) (+ x y))) 5)"));
  }

  @Test
  public void testCaseLambdaTwoArgs() {
    assertEquals(7, evalAsInt("((case-lambda (() 0) ((x) x) ((x y) (+ x y))) 3 4)"));
  }

  @Test
  public void testCaseLambdaWithRest() {
    assertEquals(
        "(1 2 3)",
        lisp.evaluate("((case-lambda ((x) x) ((x . rest) (cons x rest))) 1 2 3)").toString());
  }

  @Test
  public void testCaseLambdaRestMatchesSingle() {
    assertEquals(1, evalAsInt("((case-lambda ((x) x) ((x . rest) (cons x rest))) 1)"));
  }

  @Test
  public void testCaseLambdaRestOnlyWithArgs() {
    assertEquals(
        "(1 2 3)", lisp.evaluate("((case-lambda (() 'none) (args args)) 1 2 3)").toString());
  }

  @Test
  public void testCaseLambdaRestOnlyEmpty() {
    assertEquals("none", lisp.evaluate("((case-lambda (() 'none) (args args)))").toString());
  }

  @Test
  public void testCaseLambdaNoMatchingClause() {
    assertThrows(Exception.class, () -> evalAsInt("((case-lambda (() 0) ((x) x)) 1 2 3)"));
  }

  @Test
  public void testCaseLambdaDefineFunction() {
    // Define a function using case-lambda
    lisp.evaluate("(define add (case-lambda (() 0) ((x) x) ((x y) (+ x y)) ((x y z) (+ x y z))))");
    assertEquals(0, evalAsInt("(add)"));
    assertEquals(5, evalAsInt("(add 5)"));
    assertEquals(7, evalAsInt("(add 3 4)"));
    assertEquals(15, evalAsInt("(add 5 5 5)"));
  }

  // when tests (built-in macro)

  @Test
  public void testWhenWithTrueCondition() {
    assertEquals(42, evalAsInt("(when #t 42)"));
  }

  @Test
  public void testWhenWithFalseCondition() {
    assertEquals("()", lisp.evaluate("(when #f 42)").toString());
  }
}
