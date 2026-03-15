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

import net.sourceforge.kleinlisp.objects.PVectorObject;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for persistent vector functions. */
public class PersistentVectorTest extends BaseTestClass {

  // ========== Construction Tests ==========

  @Test
  public void testEmptyPVec() {
    LispObject result = lisp.evaluate("(p-vec)");
    assertTrue(result instanceof PVectorObject);
    assertEquals(0, lisp.evaluate("(p-vec-length (p-vec))").asInt().value);
  }

  @Test
  public void testPVecWithElements() {
    lisp.evaluate("(define v (p-vec 1 2 3 4 5))");
    assertEquals(5, lisp.evaluate("(p-vec-length v)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref v 0)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-vec-ref v 2)").asInt().value);
    assertEquals(5, lisp.evaluate("(p-vec-ref v 4)").asInt().value);
  }

  @Test
  public void testPVecWithMixedTypes() {
    lisp.evaluate("(define v (p-vec 1 \"hello\" (list 1 2)))");
    assertEquals(3, lisp.evaluate("(p-vec-length v)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref v 0)").asInt().value);
    assertEquals("hello", lisp.evaluate("(p-vec-ref v 1)").asString().value());
    assertEquals(2, lisp.evaluate("(length (p-vec-ref v 2))").asInt().value);
  }

  // ========== Immutability Tests ==========

  @Test
  public void testPVecAssocImmutability() {
    lisp.evaluate("(define v1 (p-vec 1 2 3))");
    lisp.evaluate("(define v2 (p-vec-assoc v1 1 99))");

    // Original vector unchanged
    assertEquals(2, lisp.evaluate("(p-vec-ref v1 1)").asInt().value);

    // New vector has the change
    assertEquals(99, lisp.evaluate("(p-vec-ref v2 1)").asInt().value);

    // Other elements unchanged in both
    assertEquals(1, lisp.evaluate("(p-vec-ref v1 0)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref v2 0)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-vec-ref v1 2)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-vec-ref v2 2)").asInt().value);
  }

  @Test
  public void testPVecConjImmutability() {
    lisp.evaluate("(define v1 (p-vec 1 2 3))");
    lisp.evaluate("(define v2 (p-vec-conj v1 4))");

    // Original unchanged
    assertEquals(3, lisp.evaluate("(p-vec-length v1)").asInt().value);

    // New vector has additional element
    assertEquals(4, lisp.evaluate("(p-vec-length v2)").asInt().value);
    assertEquals(4, lisp.evaluate("(p-vec-ref v2 3)").asInt().value);
  }

  @Test
  public void testPVecPopImmutability() {
    lisp.evaluate("(define v1 (p-vec 1 2 3))");
    lisp.evaluate("(define v2 (p-vec-pop v1))");

    // Original unchanged
    assertEquals(3, lisp.evaluate("(p-vec-length v1)").asInt().value);

    // New vector has one less element
    assertEquals(2, lisp.evaluate("(p-vec-length v2)").asInt().value);
  }

  // ========== Access Operations ==========

  @Test
  public void testPVecRef() {
    lisp.evaluate("(define v (p-vec 10 20 30 40 50))");
    assertEquals(10, lisp.evaluate("(p-vec-ref v 0)").asInt().value);
    assertEquals(30, lisp.evaluate("(p-vec-ref v 2)").asInt().value);
    assertEquals(50, lisp.evaluate("(p-vec-ref v 4)").asInt().value);
  }

  @Test
  public void testPVecRefOutOfBounds() {
    lisp.evaluate("(define v (p-vec 1 2 3))");
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(p-vec-ref v 5)"));
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(p-vec-ref v -1)"));
  }

  @Test
  public void testPVecPeek() {
    lisp.evaluate("(define v (p-vec 1 2 3 4 5))");
    assertEquals(5, lisp.evaluate("(p-vec-peek v)").asInt().value);
  }

  @Test
  public void testPVecPeekEmpty() {
    lisp.evaluate("(define v (p-vec))");
    assertTrue(lisp.evaluate("(null? (p-vec-peek v))").truthiness());
  }

  // ========== Modification Operations ==========

  @Test
  public void testPVecAssoc() {
    lisp.evaluate("(define v (p-vec 1 2 3))");
    lisp.evaluate("(define v2 (p-vec-assoc v 0 100))");
    assertEquals(100, lisp.evaluate("(p-vec-ref v2 0)").asInt().value);
    assertEquals(2, lisp.evaluate("(p-vec-ref v2 1)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-vec-ref v2 2)").asInt().value);
  }

  @Test
  public void testPVecConjMultiple() {
    lisp.evaluate("(define v (p-vec 1))");
    lisp.evaluate("(define v2 (p-vec-conj v 2 3 4))");
    assertEquals(4, lisp.evaluate("(p-vec-length v2)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref v2 0)").asInt().value);
    assertEquals(2, lisp.evaluate("(p-vec-ref v2 1)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-vec-ref v2 2)").asInt().value);
    assertEquals(4, lisp.evaluate("(p-vec-ref v2 3)").asInt().value);
  }

  @Test
  public void testPVecPop() {
    lisp.evaluate("(define v (p-vec 1 2 3 4))");
    lisp.evaluate("(define v2 (p-vec-pop v))");
    assertEquals(3, lisp.evaluate("(p-vec-length v2)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-vec-peek v2)").asInt().value);
  }

  @Test
  public void testPVecPopEmpty() {
    lisp.evaluate("(define v (p-vec))");
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(p-vec-pop v)"));
  }

  // ========== Subvector Operations ==========

  @Test
  public void testPVecSubvec() {
    lisp.evaluate("(define v (p-vec 0 1 2 3 4 5))");
    lisp.evaluate("(define sub (p-vec-subvec v 2 5))");
    assertEquals(3, lisp.evaluate("(p-vec-length sub)").asInt().value);
    assertEquals(2, lisp.evaluate("(p-vec-ref sub 0)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-vec-ref sub 1)").asInt().value);
    assertEquals(4, lisp.evaluate("(p-vec-ref sub 2)").asInt().value);
  }

  @Test
  public void testPVecSubvecInvalidRange() {
    lisp.evaluate("(define v (p-vec 1 2 3))");
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(p-vec-subvec v 2 1)"));
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(p-vec-subvec v -1 2)"));
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(p-vec-subvec v 0 10)"));
  }

  // ========== Concatenation ==========

  @Test
  public void testPVecConcat() {
    lisp.evaluate("(define v1 (p-vec 1 2 3))");
    lisp.evaluate("(define v2 (p-vec 4 5 6))");
    lisp.evaluate("(define v3 (p-vec-concat v1 v2))");
    assertEquals(6, lisp.evaluate("(p-vec-length v3)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref v3 0)").asInt().value);
    assertEquals(4, lisp.evaluate("(p-vec-ref v3 3)").asInt().value);
    assertEquals(6, lisp.evaluate("(p-vec-ref v3 5)").asInt().value);
  }

  @Test
  public void testPVecConcatMultiple() {
    lisp.evaluate("(define v (p-vec-concat (p-vec 1) (p-vec 2) (p-vec 3)))");
    assertEquals(3, lisp.evaluate("(p-vec-length v)").asInt().value);
  }

  // ========== Conversion Operations ==========

  @Test
  public void testPVecToList() {
    lisp.evaluate("(define v (p-vec 1 2 3))");
    LispObject result = lisp.evaluate("(p-vec->list v)");
    assertEquals(3, result.asList().length());
    assertEquals(1, result.asList().car().asInt().value);
  }

  @Test
  public void testListToPVec() {
    lisp.evaluate("(define v (list->p-vec (list 1 2 3)))");
    assertTrue(lisp.evaluate("(p-vec? v)").truthiness());
    assertEquals(3, lisp.evaluate("(p-vec-length v)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref v 0)").asInt().value);
  }

  @Test
  public void testEmptyListToPVec() {
    lisp.evaluate("(define v (list->p-vec (list)))");
    assertTrue(lisp.evaluate("(p-vec-empty? v)").truthiness());
  }

  // ========== Predicates ==========

  @Test
  public void testIsPVec() {
    assertTrue(lisp.evaluate("(p-vec? (p-vec))").truthiness());
    assertTrue(lisp.evaluate("(p-vec? (p-vec 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(p-vec? (vector 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(p-vec? (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(p-vec? 42)").truthiness());
  }

  @Test
  public void testIsPVecEmpty() {
    assertTrue(lisp.evaluate("(p-vec-empty? (p-vec))").truthiness());
    assertFalse(lisp.evaluate("(p-vec-empty? (p-vec 1))").truthiness());
  }

  // ========== Structural Sharing Tests ==========

  @Test
  public void testStructuralSharingWithChainedOperations() {
    // Create a chain of modifications
    lisp.evaluate("(define v0 (p-vec 1 2 3 4 5))");
    lisp.evaluate("(define v1 (p-vec-assoc v0 0 10))");
    lisp.evaluate("(define v2 (p-vec-conj v1 6))");
    lisp.evaluate("(define v3 (p-vec-pop v2))");

    // All versions exist independently
    assertEquals(5, lisp.evaluate("(p-vec-length v0)").asInt().value);
    assertEquals(5, lisp.evaluate("(p-vec-length v1)").asInt().value);
    assertEquals(6, lisp.evaluate("(p-vec-length v2)").asInt().value);
    assertEquals(5, lisp.evaluate("(p-vec-length v3)").asInt().value);

    // Each has correct values
    assertEquals(1, lisp.evaluate("(p-vec-ref v0 0)").asInt().value);
    assertEquals(10, lisp.evaluate("(p-vec-ref v1 0)").asInt().value);
    assertEquals(6, lisp.evaluate("(p-vec-ref v2 5)").asInt().value);
  }

  // ========== Higher-Order Function Integration ==========

  @Test
  public void testPVecWithMap() {
    lisp.evaluate("(define v (p-vec 1 2 3 4 5))");
    lisp.evaluate("(define result (map (lambda (x) (* x 2)) (p-vec->list v)))");
    assertEquals(2, lisp.evaluate("(car result)").asInt().value);
    assertEquals(10, lisp.evaluate("(car (reverse result))").asInt().value);
  }

  @Test
  public void testPVecBuildFromFold() {
    // Build a vector using fold
    lisp.evaluate(
        "(define v (fold-left (lambda (acc x) (p-vec-conj acc x)) (p-vec) (list 1 2 3)))");
    assertEquals(3, lisp.evaluate("(p-vec-length v)").asInt().value);
  }

  // ========== Edge Cases ==========

  @Test
  public void testPVecWithNil() {
    lisp.evaluate("(define v (p-vec (list) 1 2))");
    assertEquals(3, lisp.evaluate("(p-vec-length v)").asInt().value);
    assertTrue(lisp.evaluate("(null? (p-vec-ref v 0))").truthiness());
  }

  @Test
  public void testPVecToString() {
    String result = lisp.evaluate("(p-vec 1 2 3)").toString();
    assertEquals("[1 2 3]", result);
  }

  @Test
  public void testEmptyPVecToString() {
    String result = lisp.evaluate("(p-vec)").toString();
    assertEquals("[]", result);
  }
}
