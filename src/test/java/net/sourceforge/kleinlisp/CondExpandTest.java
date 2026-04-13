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
 * Tests for R7RS cond-expand and syntax-error special forms.
 *
 * @author Danilo Oliveira
 */
public class CondExpandTest extends BaseTestClass {

  // cond-expand basic feature tests

  @Test
  public void testCondExpandR7rsFeature() {
    assertEquals("r7rs", lisp.evaluate("(cond-expand (r7rs 'r7rs) (else 'other))").toString());
  }

  @Test
  public void testCondExpandKleinlispFeature() {
    assertEquals(
        "kleinlisp",
        lisp.evaluate("(cond-expand (kleinlisp 'kleinlisp) (else 'other))").toString());
  }

  @Test
  public void testCondExpandJavaFeature() {
    assertEquals("java", lisp.evaluate("(cond-expand (java 'java) (else 'other))").toString());
  }

  @Test
  public void testCondExpandElseClause() {
    assertEquals(
        "else-branch",
        lisp.evaluate("(cond-expand (nonexistent-feature 'nope) (else 'else-branch))").toString());
  }

  @Test
  public void testCondExpandNoMatch() {
    assertThrows(Exception.class, () -> lisp.evaluate("(cond-expand (nonexistent-feature 'nope))"));
  }

  // cond-expand with multiple expressions in body

  @Test
  public void testCondExpandMultipleExpressionsInBody() {
    lisp.evaluate("(define test-var 0)");
    lisp.evaluate("(cond-expand (r7rs (set! test-var 1) (set! test-var (+ test-var 1))))");
    assertEquals(2, evalAsInt("test-var"));
  }

  // cond-expand with and/or/not

  @Test
  public void testCondExpandAnd() {
    assertEquals(
        "both",
        lisp.evaluate("(cond-expand ((and r7rs kleinlisp) 'both) (else 'not-both))").toString());
  }

  @Test
  public void testCondExpandAndFailure() {
    assertEquals(
        "not-both",
        lisp.evaluate("(cond-expand ((and r7rs nonexistent) 'both) (else 'not-both))").toString());
  }

  @Test
  public void testCondExpandOr() {
    assertEquals(
        "one-of",
        lisp.evaluate("(cond-expand ((or nonexistent r7rs) 'one-of) (else 'none))").toString());
  }

  @Test
  public void testCondExpandOrFailure() {
    assertEquals(
        "none",
        lisp.evaluate("(cond-expand ((or nonexistent1 nonexistent2) 'one-of) (else 'none))")
            .toString());
  }

  @Test
  public void testCondExpandNot() {
    assertEquals(
        "not-present",
        lisp.evaluate("(cond-expand ((not nonexistent) 'not-present) (else 'present))").toString());
  }

  @Test
  public void testCondExpandNotFailure() {
    assertEquals(
        "present",
        lisp.evaluate("(cond-expand ((not r7rs) 'not-present) (else 'present))").toString());
  }

  // cond-expand nested conditions

  @Test
  public void testCondExpandNestedAndOr() {
    assertEquals(
        "matched",
        lisp.evaluate(
                "(cond-expand ((and r7rs (or guile kleinlisp)) 'matched) (else 'not-matched))")
            .toString());
  }

  @Test
  public void testCondExpandNestedNotAnd() {
    assertEquals(
        "matched",
        lisp.evaluate("(cond-expand ((and (not guile) kleinlisp) 'matched) (else 'not-matched))")
            .toString());
  }

  // cond-expand with library

  @Test
  public void testCondExpandLibrary() {
    assertEquals(
        "has-lib",
        lisp.evaluate("(cond-expand ((library (scheme base)) 'has-lib) (else 'no-lib))")
            .toString());
  }

  // cond-expand empty and/or

  @Test
  public void testCondExpandEmptyAnd() {
    assertEquals(
        "empty-and", lisp.evaluate("(cond-expand ((and) 'empty-and) (else 'no))").toString());
  }

  @Test
  public void testCondExpandEmptyOr() {
    assertEquals(
        "fallback", lisp.evaluate("(cond-expand ((or) 'empty-or) (else 'fallback))").toString());
  }

  // cond-expand first matching clause wins

  @Test
  public void testCondExpandFirstMatchWins() {
    assertEquals(
        "first",
        lisp.evaluate("(cond-expand (r7rs 'first) (kleinlisp 'second) (else 'third))").toString());
  }

  // syntax-error tests

  @Test
  public void testSyntaxErrorThrows() {
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class, () -> lisp.evaluate("(syntax-error \"test error\")"));
    assertTrue(ex.getMessage().contains("test error"));
  }

  @Test
  public void testSyntaxErrorWithIrritants() {
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> lisp.evaluate("(syntax-error \"invalid form\" '(a b c))"));
    assertTrue(ex.getMessage().contains("invalid form"));
    assertTrue(ex.getMessage().contains("(a b c)"));
  }

  @Test
  public void testSyntaxErrorWithMultipleIrritants() {
    LispRuntimeException ex =
        assertThrows(
            LispRuntimeException.class,
            () -> lisp.evaluate("(syntax-error \"error\" 'foo 42 \"bar\")"));
    assertTrue(ex.getMessage().contains("foo"));
    assertTrue(ex.getMessage().contains("42"));
  }

  @Test
  public void testSyntaxErrorWithSymbolMessage() {
    LispRuntimeException ex =
        assertThrows(LispRuntimeException.class, () -> lisp.evaluate("(syntax-error 'bad-syntax)"));
    assertTrue(ex.getMessage().contains("bad-syntax"));
  }
}
