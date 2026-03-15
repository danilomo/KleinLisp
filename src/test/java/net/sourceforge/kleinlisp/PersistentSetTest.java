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

import net.sourceforge.kleinlisp.objects.PSetObject;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for persistent set functions. */
public class PersistentSetTest extends BaseTestClass {

  // ========== Construction Tests ==========

  @Test
  public void testEmptyPSet() {
    LispObject result = lisp.evaluate("(p-set)");
    assertTrue(result instanceof PSetObject);
    assertEquals(0, lisp.evaluate("(p-set-size (p-set))").asInt().value);
  }

  @Test
  public void testPSetWithElements() {
    lisp.evaluate("(define s (p-set 1 2 3 4 5))");
    assertEquals(5, lisp.evaluate("(p-set-size s)").asInt().value);
  }

  @Test
  public void testPSetDeduplication() {
    // Sets should not have duplicates
    lisp.evaluate("(define s (p-set 1 2 2 3 3 3))");
    assertEquals(3, lisp.evaluate("(p-set-size s)").asInt().value);
  }

  @Test
  public void testPSetWithMixedTypes() {
    lisp.evaluate("(define s (p-set 1 \"hello\" #:keyword))");
    assertEquals(3, lisp.evaluate("(p-set-size s)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s 1)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s \"hello\")").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s #:keyword)").truthiness());
  }

  // ========== Immutability Tests ==========

  @Test
  public void testPSetConjImmutability() {
    lisp.evaluate("(define s1 (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set-conj s1 4))");

    // Original set unchanged
    assertEquals(3, lisp.evaluate("(p-set-size s1)").asInt().value);
    assertFalse(lisp.evaluate("(p-set-contains? s1 4)").truthiness());

    // New set has the addition
    assertEquals(4, lisp.evaluate("(p-set-size s2)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s2 4)").truthiness());
  }

  @Test
  public void testPSetDisjImmutability() {
    lisp.evaluate("(define s1 (p-set 1 2 3 4))");
    lisp.evaluate("(define s2 (p-set-disj s1 2))");

    // Original set unchanged
    assertEquals(4, lisp.evaluate("(p-set-size s1)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s1 2)").truthiness());

    // New set has the removal
    assertEquals(3, lisp.evaluate("(p-set-size s2)").asInt().value);
    assertFalse(lisp.evaluate("(p-set-contains? s2 2)").truthiness());
  }

  // ========== Contains Tests ==========

  @Test
  public void testPSetContains() {
    lisp.evaluate("(define s (p-set 1 2 3 4 5))");
    assertTrue(lisp.evaluate("(p-set-contains? s 1)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s 3)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s 5)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? s 0)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? s 6)").truthiness());
  }

  @Test
  public void testPSetContainsWithKeywords() {
    lisp.evaluate("(define s (p-set #:a #:b #:c))");
    assertTrue(lisp.evaluate("(p-set-contains? s #:a)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s #:b)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? s #:d)").truthiness());
  }

  // ========== Modification Operations ==========

  @Test
  public void testPSetConj() {
    lisp.evaluate("(define s (p-set 1 2))");
    lisp.evaluate("(define s2 (p-set-conj s 3))");
    assertEquals(3, lisp.evaluate("(p-set-size s2)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s2 3)").truthiness());
  }

  @Test
  public void testPSetConjExisting() {
    // Adding an existing element should not change the set
    lisp.evaluate("(define s (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set-conj s 2))");
    assertEquals(3, lisp.evaluate("(p-set-size s2)").asInt().value);
  }

  @Test
  public void testPSetConjMultiple() {
    lisp.evaluate("(define s (p-set 1))");
    lisp.evaluate("(define s2 (p-set-conj s 2 3 4 5))");
    assertEquals(5, lisp.evaluate("(p-set-size s2)").asInt().value);
  }

  @Test
  public void testPSetDisj() {
    lisp.evaluate("(define s (p-set 1 2 3 4 5))");
    lisp.evaluate("(define s2 (p-set-disj s 3))");
    assertEquals(4, lisp.evaluate("(p-set-size s2)").asInt().value);
    assertFalse(lisp.evaluate("(p-set-contains? s2 3)").truthiness());
  }

  @Test
  public void testPSetDisjNonExisting() {
    // Removing a non-existing element should not change the set
    lisp.evaluate("(define s (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set-disj s 99))");
    assertEquals(3, lisp.evaluate("(p-set-size s2)").asInt().value);
  }

  @Test
  public void testPSetDisjMultiple() {
    lisp.evaluate("(define s (p-set 1 2 3 4 5))");
    lisp.evaluate("(define s2 (p-set-disj s 2 4))");
    assertEquals(3, lisp.evaluate("(p-set-size s2)").asInt().value);
    assertFalse(lisp.evaluate("(p-set-contains? s2 2)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? s2 4)").truthiness());
  }

  // ========== Set Operations ==========

  @Test
  public void testPSetUnion() {
    lisp.evaluate("(define s1 (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set 3 4 5))");
    lisp.evaluate("(define s3 (p-set-union s1 s2))");
    assertEquals(5, lisp.evaluate("(p-set-size s3)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s3 1)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s3 3)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s3 5)").truthiness());
  }

  @Test
  public void testPSetUnionMultiple() {
    lisp.evaluate("(define s (p-set-union (p-set 1 2) (p-set 3 4) (p-set 5 6)))");
    assertEquals(6, lisp.evaluate("(p-set-size s)").asInt().value);
  }

  @Test
  public void testPSetIntersection() {
    lisp.evaluate("(define s1 (p-set 1 2 3 4))");
    lisp.evaluate("(define s2 (p-set 3 4 5 6))");
    lisp.evaluate("(define s3 (p-set-intersection s1 s2))");
    assertEquals(2, lisp.evaluate("(p-set-size s3)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s3 3)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s3 4)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? s3 1)").truthiness());
  }

  @Test
  public void testPSetIntersectionEmpty() {
    lisp.evaluate("(define s1 (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set 4 5 6))");
    lisp.evaluate("(define s3 (p-set-intersection s1 s2))");
    assertEquals(0, lisp.evaluate("(p-set-size s3)").asInt().value);
  }

  @Test
  public void testPSetIntersectionMultiple() {
    lisp.evaluate(
        "(define s (p-set-intersection (p-set 1 2 3 4) (p-set 2 3 4 5) (p-set 3 4 5 6)))");
    assertEquals(2, lisp.evaluate("(p-set-size s)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s 3)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s 4)").truthiness());
  }

  @Test
  public void testPSetDifference() {
    lisp.evaluate("(define s1 (p-set 1 2 3 4 5))");
    lisp.evaluate("(define s2 (p-set 3 4 5 6 7))");
    lisp.evaluate("(define s3 (p-set-difference s1 s2))");
    assertEquals(2, lisp.evaluate("(p-set-size s3)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? s3 1)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s3 2)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? s3 3)").truthiness());
  }

  @Test
  public void testPSetDifferenceEmpty() {
    lisp.evaluate("(define s1 (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set 1 2 3 4 5))");
    lisp.evaluate("(define s3 (p-set-difference s1 s2))");
    assertEquals(0, lisp.evaluate("(p-set-size s3)").asInt().value);
  }

  // ========== Subset/Superset Tests ==========

  @Test
  public void testPSetSubset() {
    lisp.evaluate("(define s1 (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set 1 2 3 4 5))");
    assertTrue(lisp.evaluate("(p-set-subset? s1 s2)").truthiness());
    assertFalse(lisp.evaluate("(p-set-subset? s2 s1)").truthiness());
  }

  @Test
  public void testPSetSubsetEqual() {
    lisp.evaluate("(define s1 (p-set 1 2 3))");
    lisp.evaluate("(define s2 (p-set 1 2 3))");
    assertTrue(lisp.evaluate("(p-set-subset? s1 s2)").truthiness());
    assertTrue(lisp.evaluate("(p-set-subset? s2 s1)").truthiness());
  }

  @Test
  public void testPSetSubsetEmpty() {
    lisp.evaluate("(define empty (p-set))");
    lisp.evaluate("(define s (p-set 1 2 3))");
    assertTrue(lisp.evaluate("(p-set-subset? empty s)").truthiness());
    assertTrue(lisp.evaluate("(p-set-subset? empty empty)").truthiness());
  }

  @Test
  public void testPSetSuperset() {
    lisp.evaluate("(define s1 (p-set 1 2 3 4 5))");
    lisp.evaluate("(define s2 (p-set 1 2 3))");
    assertTrue(lisp.evaluate("(p-set-superset? s1 s2)").truthiness());
    assertFalse(lisp.evaluate("(p-set-superset? s2 s1)").truthiness());
  }

  // ========== Conversion Operations ==========

  @Test
  public void testPSetToList() {
    lisp.evaluate("(define s (p-set 1 2 3))");
    LispObject list = lisp.evaluate("(p-set->list s)");
    assertEquals(3, list.asList().length());
  }

  @Test
  public void testListToPSet() {
    lisp.evaluate("(define s (list->p-set (list 1 2 2 3 3 3)))");
    assertTrue(lisp.evaluate("(p-set? s)").truthiness());
    assertEquals(3, lisp.evaluate("(p-set-size s)").asInt().value);
  }

  @Test
  public void testEmptyListToPSet() {
    lisp.evaluate("(define s (list->p-set (list)))");
    assertTrue(lisp.evaluate("(p-set-empty? s)").truthiness());
  }

  // ========== Predicates ==========

  @Test
  public void testIsPSet() {
    assertTrue(lisp.evaluate("(p-set? (p-set))").truthiness());
    assertTrue(lisp.evaluate("(p-set? (p-set 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(p-set? (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(p-set? (p-vec 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(p-set? 42)").truthiness());
  }

  @Test
  public void testIsPSetEmpty() {
    assertTrue(lisp.evaluate("(p-set-empty? (p-set))").truthiness());
    assertFalse(lisp.evaluate("(p-set-empty? (p-set 1))").truthiness());
  }

  // ========== Structural Sharing Tests ==========

  @Test
  public void testStructuralSharingWithChainedOperations() {
    lisp.evaluate("(define s0 (p-set 1 2 3 4 5))");
    lisp.evaluate("(define s1 (p-set-conj s0 6))");
    lisp.evaluate("(define s2 (p-set-disj s1 1))");
    lisp.evaluate("(define s3 (p-set-conj s2 7 8))");

    // All versions exist independently
    assertEquals(5, lisp.evaluate("(p-set-size s0)").asInt().value);
    assertEquals(6, lisp.evaluate("(p-set-size s1)").asInt().value);
    assertEquals(5, lisp.evaluate("(p-set-size s2)").asInt().value);
    assertEquals(7, lisp.evaluate("(p-set-size s3)").asInt().value);

    // Each has correct membership
    assertTrue(lisp.evaluate("(p-set-contains? s0 1)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s1 1)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? s2 1)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? s3 8)").truthiness());
  }

  // ========== Higher-Order Function Integration ==========

  @Test
  public void testPSetWithFilter() {
    lisp.evaluate("(define s (p-set 1 2 3 4 5 6))");
    lisp.evaluate("(define evens (list->p-set (filter even? (p-set->list s))))");
    assertEquals(3, lisp.evaluate("(p-set-size evens)").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? evens 2)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? evens 4)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? evens 6)").truthiness());
  }

  @Test
  public void testPSetBuildFromFold() {
    lisp.evaluate(
        "(define s (fold-left (lambda (acc x) (p-set-conj acc x)) (p-set) (list 1 2 3 2 1)))");
    assertEquals(3, lisp.evaluate("(p-set-size s)").asInt().value);
  }

  // ========== Edge Cases ==========

  @Test
  public void testPSetToString() {
    String result = lisp.evaluate("(p-set 1 2 3)").toString();
    assertTrue(result.startsWith("#{") && result.endsWith("}"));
  }

  @Test
  public void testEmptyPSetToString() {
    String result = lisp.evaluate("(p-set)").toString();
    assertEquals("#{}", result);
  }

  @Test
  public void testPSetUnionEmpty() {
    lisp.evaluate("(define s (p-set-union (p-set) (p-set)))");
    assertTrue(lisp.evaluate("(p-set-empty? s)").truthiness());
  }

  @Test
  public void testPSetWithNestedStructures() {
    // Sets can contain lists and other structures
    lisp.evaluate("(define s (p-set (list 1 2) (list 3 4)))");
    assertEquals(2, lisp.evaluate("(p-set-size s)").asInt().value);
  }
}
