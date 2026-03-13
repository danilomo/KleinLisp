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

import net.sourceforge.kleinlisp.objects.ListObject;
import org.junit.jupiter.api.Test;

/** Tests for higher-order functions. */
public class HigherOrderFunctionsTest extends BaseTestClass {

  @Test
  public void testMap() {
    // Map with single list
    LispObject result = lisp.evaluate("(map (lambda (x) (* x 2)) (list 1 2 3 4 5))");
    assertEquals(5, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);

    // Map with increment
    lisp.evaluate("(define (inc x) (+ x 1))");
    result = lisp.evaluate("(map inc (list 1 2 3))");
    assertEquals(3, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);
  }

  @Test
  public void testMapWithEmptyList() {
    LispObject result = lisp.evaluate("(map (lambda (x) x) (list))");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testMapWithTwoLists() {
    LispObject result = lisp.evaluate("(map + (list 1 2 3) (list 10 20 30))");
    assertEquals(3, result.asList().length());
    assertEquals(11, result.asList().car().asInt().value);
  }

  @Test
  public void testFilter() {
    // Filter even numbers
    LispObject result = lisp.evaluate("(filter even? (list 1 2 3 4 5 6))");
    assertEquals(3, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);

    // Filter positive numbers
    result = lisp.evaluate("(filter positive? (list -2 -1 0 1 2))");
    assertEquals(2, result.asList().length());
    assertEquals(1, result.asList().car().asInt().value);
  }

  @Test
  public void testFilterWithEmptyList() {
    LispObject result = lisp.evaluate("(filter even? (list))");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testFilterNoMatch() {
    LispObject result = lisp.evaluate("(filter negative? (list 1 2 3))");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testForEach() {
    // for-each returns void, test side effect via captured stdout
    lisp.evaluate("(for-each println (list 1 2 3))");
    String output = getStdOut();
    assertTrue(output.contains("1"));
    assertTrue(output.contains("2"));
    assertTrue(output.contains("3"));
  }

  @Test
  public void testFoldLeft() {
    // Sum using fold-left
    assertEquals(15, lisp.evaluate("(fold-left + 0 (list 1 2 3 4 5))").asInt().value);

    // Product using fold-left
    assertEquals(120, lisp.evaluate("(fold-left * 1 (list 1 2 3 4 5))").asInt().value);

    // Subtraction (left associative)
    assertEquals(-13, lisp.evaluate("(fold-left - 0 (list 1 2 3 4 5 -2))").asInt().value);
  }

  @Test
  public void testFoldRight() {
    // Sum using fold-right
    assertEquals(15, lisp.evaluate("(fold-right + 0 (list 1 2 3 4 5))").asInt().value);

    // Build a list in order (cons from right)
    LispObject result = lisp.evaluate("(fold-right cons '() (list 1 2 3))");
    assertEquals(1, result.asList().car().asInt().value);
  }

  @Test
  public void testFoldWithEmptyList() {
    assertEquals(0, lisp.evaluate("(fold-left + 0 (list))").asInt().value);
    assertEquals(100, lisp.evaluate("(fold-right + 100 (list))").asInt().value);
  }

  @Test
  public void testApply() {
    // Simple apply
    assertEquals(6, lisp.evaluate("(apply + (list 1 2 3))").asInt().value);

    // Apply with mixed args
    assertEquals(10, lisp.evaluate("(apply + 1 2 (list 3 4))").asInt().value);
  }

  @Test
  public void testApplyWithEmptyList() {
    assertEquals(0, lisp.evaluate("(apply + (list))").asInt().value);
  }

  @Test
  public void testCompose() {
    lisp.evaluate("(define (double x) (* x 2))");
    lisp.evaluate("(define (inc x) (+ x 1))");
    lisp.evaluate("(define double-then-inc (compose inc double))");

    // (inc (double 5)) = (inc 10) = 11
    assertEquals(11, lisp.evaluate("(double-then-inc 5)").asInt().value);
  }

  @Test
  public void testIdentity() {
    assertEquals(42, lisp.evaluate("(identity 42)").asInt().value);
    assertEquals("hello", lisp.evaluate("(identity \"hello\")").asString().value());
  }

  @Test
  public void testNegate() {
    lisp.evaluate("(define not-even? (negate even?))");
    assertTrue(lisp.evaluate("(not-even? 3)").truthiness());
    assertFalse(lisp.evaluate("(not-even? 4)").truthiness());
  }

  @Test
  public void testAny() {
    assertTrue(lisp.evaluate("(any even? (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(any even? (list 1 3 5))").truthiness());
    assertFalse(lisp.evaluate("(any even? (list))").truthiness());
  }

  @Test
  public void testAll() {
    assertTrue(lisp.evaluate("(all positive? (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(all positive? (list 1 -1 3))").truthiness());
    assertTrue(lisp.evaluate("(all positive? (list))").truthiness()); // vacuously true
  }

  @Test
  public void testEvery() {
    // every is alias for all
    assertTrue(lisp.evaluate("(every even? (list 2 4 6))").truthiness());
    assertFalse(lisp.evaluate("(every even? (list 2 3 4))").truthiness());
  }

  @Test
  public void testReduce() {
    // reduce is alias for fold-left
    assertEquals(15, lisp.evaluate("(reduce + 0 (list 1 2 3 4 5))").asInt().value);
  }
}
