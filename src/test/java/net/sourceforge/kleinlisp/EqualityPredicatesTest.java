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

/** Tests for equality predicate functions. */
public class EqualityPredicatesTest extends BaseTestClass {

  @Test
  public void testEqWithSymbols() {
    // Same symbol should be eq?
    assertTrue(lisp.evaluate("(eq? 'foo 'foo)").truthiness());
    assertFalse(lisp.evaluate("(eq? 'foo 'bar)").truthiness());
  }

  @Test
  public void testEqWithBooleans() {
    // Booleans from comparisons
    assertTrue(lisp.evaluate("(eq? (> 1 0) (> 2 1))").truthiness()); // Both true
    assertTrue(lisp.evaluate("(eq? (< 1 0) (< 2 0))").truthiness()); // Both false
    assertFalse(lisp.evaluate("(eq? (> 1 0) (< 1 0))").truthiness()); // true vs false
  }

  @Test
  public void testEqWithNil() {
    assertTrue(lisp.evaluate("(eq? '() '())").truthiness());
  }

  @Test
  public void testEqWithIntegers() {
    // eqv? should be used for numeric equality, eq? tests identity
    // Small integers may or may not be cached depending on implementation
    assertTrue(lisp.evaluate("(eqv? 42 42)").truthiness());
  }

  @Test
  public void testEqvWithNumbers() {
    assertTrue(lisp.evaluate("(eqv? 42 42)").truthiness());
    assertTrue(lisp.evaluate("(eqv? -10 -10)").truthiness());
    assertFalse(lisp.evaluate("(eqv? 42 43)").truthiness());
  }

  @Test
  public void testEqvWithSymbols() {
    assertTrue(lisp.evaluate("(eqv? 'foo 'foo)").truthiness());
    assertFalse(lisp.evaluate("(eqv? 'foo 'bar)").truthiness());
  }

  @Test
  public void testEqvWithBooleans() {
    assertTrue(lisp.evaluate("(eqv? (> 1 0) (> 2 1))").truthiness()); // Both true
    assertTrue(lisp.evaluate("(eqv? (< 1 0) (< 2 0))").truthiness()); // Both false
    assertFalse(lisp.evaluate("(eqv? (> 1 0) (< 1 0))").truthiness());
  }

  @Test
  public void testEqualWithStrings() {
    assertTrue(lisp.evaluate("(equal? \"hello\" \"hello\")").truthiness());
    assertFalse(lisp.evaluate("(equal? \"hello\" \"world\")").truthiness());
  }

  @Test
  public void testEqualWithLists() {
    assertTrue(lisp.evaluate("(equal? (list 1 2 3) (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(equal? (list 1 2 3) (list 1 2 4))").truthiness());
    assertFalse(lisp.evaluate("(equal? (list 1 2 3) (list 1 2))").truthiness());
  }

  @Test
  public void testEqualWithNestedLists() {
    assertTrue(lisp.evaluate("(equal? (list 1 (list 2 3)) (list 1 (list 2 3)))").truthiness());
    assertFalse(lisp.evaluate("(equal? (list 1 (list 2 3)) (list 1 (list 2 4)))").truthiness());
  }

  @Test
  public void testEqualWithNumbers() {
    assertTrue(lisp.evaluate("(equal? 42 42)").truthiness());
    assertFalse(lisp.evaluate("(equal? 42 43)").truthiness());
  }

  @Test
  public void testEqualWithSymbols() {
    assertTrue(lisp.evaluate("(equal? 'foo 'foo)").truthiness());
    assertFalse(lisp.evaluate("(equal? 'foo 'bar)").truthiness());
  }

  @Test
  public void testEqualWithEmptyLists() {
    assertTrue(lisp.evaluate("(equal? '() '())").truthiness());
    assertTrue(lisp.evaluate("(equal? (list) (list))").truthiness());
  }

  @Test
  public void testEqualWithVectors() {
    assertTrue(lisp.evaluate("(equal? (vector 1 2 3) (vector 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(equal? (vector 1 2 3) (vector 1 2 4))").truthiness());
    assertFalse(lisp.evaluate("(equal? (vector 1 2 3) (vector 1 2))").truthiness());
  }

  @Test
  public void testEqualWithMixedTypes() {
    assertFalse(lisp.evaluate("(equal? 1 \"1\")").truthiness());
    assertFalse(lisp.evaluate("(equal? (list 1) (vector 1))").truthiness());
  }
}
