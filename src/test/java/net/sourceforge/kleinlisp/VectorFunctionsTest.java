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

import net.sourceforge.kleinlisp.objects.VectorObject;
import org.junit.jupiter.api.Test;

/** Tests for vector functions. */
public class VectorFunctionsTest extends BaseTestClass {

  @Test
  public void testMakeVector() {
    LispObject result = lisp.evaluate("(make-vector 5)");
    assertTrue(result instanceof VectorObject);
    assertEquals(5, lisp.evaluate("(vector-length (make-vector 5))").asInt().value);
  }

  @Test
  public void testMakeVectorWithFill() {
    lisp.evaluate("(define v (make-vector 3 42))");
    assertEquals(42, lisp.evaluate("(vector-ref v 0)").asInt().value);
    assertEquals(42, lisp.evaluate("(vector-ref v 1)").asInt().value);
    assertEquals(42, lisp.evaluate("(vector-ref v 2)").asInt().value);
  }

  @Test
  public void testVector() {
    lisp.evaluate("(define v (vector 1 2 3 4 5))");
    assertEquals(5, lisp.evaluate("(vector-length v)").asInt().value);
    assertEquals(1, lisp.evaluate("(vector-ref v 0)").asInt().value);
    assertEquals(3, lisp.evaluate("(vector-ref v 2)").asInt().value);
    assertEquals(5, lisp.evaluate("(vector-ref v 4)").asInt().value);
  }

  @Test
  public void testVectorRef() {
    lisp.evaluate("(define v (vector 10 20 30))");
    assertEquals(10, lisp.evaluate("(vector-ref v 0)").asInt().value);
    assertEquals(20, lisp.evaluate("(vector-ref v 1)").asInt().value);
    assertEquals(30, lisp.evaluate("(vector-ref v 2)").asInt().value);
  }

  @Test
  public void testVectorSet() {
    lisp.evaluate("(define v (vector 1 2 3))");
    lisp.evaluate("(vector-set! v 1 99)");
    assertEquals(99, lisp.evaluate("(vector-ref v 1)").asInt().value);
    assertEquals(1, lisp.evaluate("(vector-ref v 0)").asInt().value); // unchanged
    assertEquals(3, lisp.evaluate("(vector-ref v 2)").asInt().value); // unchanged
  }

  @Test
  public void testVectorLength() {
    assertEquals(0, lisp.evaluate("(vector-length (vector))").asInt().value);
    assertEquals(3, lisp.evaluate("(vector-length (vector 1 2 3))").asInt().value);
    assertEquals(5, lisp.evaluate("(vector-length (make-vector 5))").asInt().value);
  }

  @Test
  public void testVectorToList() {
    LispObject result = lisp.evaluate("(vector->list (vector 1 2 3))");
    assertEquals(3, result.asList().length());
    assertEquals(1, result.asList().car().asInt().value);
  }

  @Test
  public void testListToVector() {
    lisp.evaluate("(define v (list->vector (list 1 2 3)))");
    assertTrue(lisp.evaluate("(vector? v)").truthiness());
    assertEquals(3, lisp.evaluate("(vector-length v)").asInt().value);
    assertEquals(1, lisp.evaluate("(vector-ref v 0)").asInt().value);
  }

  @Test
  public void testVectorFill() {
    lisp.evaluate("(define v (vector 1 2 3))");
    lisp.evaluate("(vector-fill! v 0)");
    assertEquals(0, lisp.evaluate("(vector-ref v 0)").asInt().value);
    assertEquals(0, lisp.evaluate("(vector-ref v 1)").asInt().value);
    assertEquals(0, lisp.evaluate("(vector-ref v 2)").asInt().value);
  }

  @Test
  public void testVectorCopy() {
    lisp.evaluate("(define v1 (vector 1 2 3))");
    lisp.evaluate("(define v2 (vector-copy v1))");

    // v2 should have same values
    assertEquals(1, lisp.evaluate("(vector-ref v2 0)").asInt().value);
    assertEquals(2, lisp.evaluate("(vector-ref v2 1)").asInt().value);
    assertEquals(3, lisp.evaluate("(vector-ref v2 2)").asInt().value);

    // Modifying v1 should not affect v2
    lisp.evaluate("(vector-set! v1 0 99)");
    assertEquals(1, lisp.evaluate("(vector-ref v2 0)").asInt().value);
  }

  @Test
  public void testVectorWithDifferentTypes() {
    lisp.evaluate("(define v (vector 1 \"hello\" (list 1 2)))");
    assertEquals(3, lisp.evaluate("(vector-length v)").asInt().value);
    assertEquals(1, lisp.evaluate("(vector-ref v 0)").asInt().value);
    assertEquals("hello", lisp.evaluate("(vector-ref v 1)").asString().value());
    assertEquals(2, lisp.evaluate("(length (vector-ref v 2))").asInt().value);
  }

  @Test
  public void testEmptyVector() {
    lisp.evaluate("(define v (vector))");
    assertEquals(0, lisp.evaluate("(vector-length v)").asInt().value);
    assertTrue(lisp.evaluate("(null? (vector->list v))").truthiness());
  }

  @Test
  public void testEmptyListToVector() {
    lisp.evaluate("(define v (list->vector (list)))");
    assertEquals(0, lisp.evaluate("(vector-length v)").asInt().value);
  }
}
