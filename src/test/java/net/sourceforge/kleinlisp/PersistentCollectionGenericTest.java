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
 * Tests for generic persistent collection functions and callable collections. These tests verify
 * the Clojure-like polymorphic API.
 */
public class PersistentCollectionGenericTest extends BaseTestClass {

  // ========== Generic `get` function ==========

  @Test
  public void testGetOnPMap() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2))");
    assertEquals(1, lisp.evaluate("(get m #:a)").asInt().value);
    assertEquals(2, lisp.evaluate("(get m #:b)").asInt().value);
  }

  @Test
  public void testGetOnPMapWithDefault() {
    lisp.evaluate("(define m (p-map #:a 1))");
    assertEquals(1, lisp.evaluate("(get m #:a 99)").asInt().value);
    assertEquals(99, lisp.evaluate("(get m #:missing 99)").asInt().value);
  }

  @Test
  public void testGetOnPVec() {
    lisp.evaluate("(define v (p-vec 10 20 30))");
    assertEquals(10, lisp.evaluate("(get v 0)").asInt().value);
    assertEquals(20, lisp.evaluate("(get v 1)").asInt().value);
    assertEquals(30, lisp.evaluate("(get v 2)").asInt().value);
  }

  @Test
  public void testGetOnPVecWithDefault() {
    lisp.evaluate("(define v (p-vec 10 20))");
    assertEquals(10, lisp.evaluate("(get v 0 99)").asInt().value);
    assertEquals(99, lisp.evaluate("(get v 5 99)").asInt().value);
  }

  @Test
  public void testGetOnPSet() {
    lisp.evaluate("(define s (p-set 1 2 3))");
    assertEquals(2, lisp.evaluate("(get s 2)").asInt().value);
    assertTrue(lisp.evaluate("(null? (get s 99))").truthiness());
  }

  @Test
  public void testGetOnPSetWithDefault() {
    lisp.evaluate("(define s (p-set 1 2 3))");
    assertEquals(2, lisp.evaluate("(get s 2 #:not-found)").asInt().value);
    assertEquals("#:not-found", lisp.evaluate("(get s 99 #:not-found)").toString());
  }

  // ========== Generic `assoc` function ==========

  @Test
  public void testAssocOnPMap() {
    lisp.evaluate("(define m (p-map #:a 1))");
    lisp.evaluate("(define m2 (assoc m #:b 2))");
    assertEquals(2, lisp.evaluate("(get m2 #:b)").asInt().value);
    // Original unchanged
    assertTrue(lisp.evaluate("(null? (get m #:b))").truthiness());
  }

  @Test
  public void testAssocOnPMapMultiple() {
    lisp.evaluate("(define m (assoc (p-map) #:a 1 #:b 2 #:c 3))");
    assertEquals(3, lisp.evaluate("(count m)").asInt().value);
  }

  @Test
  public void testAssocOnPVec() {
    lisp.evaluate("(define v (p-vec 1 2 3))");
    lisp.evaluate("(define v2 (assoc v 1 99))");
    assertEquals(99, lisp.evaluate("(get v2 1)").asInt().value);
    // Original unchanged
    assertEquals(2, lisp.evaluate("(get v 1)").asInt().value);
  }

  @Test
  public void testAssocOnPVecAppend() {
    // Assoc at length position appends (Clojure behavior)
    lisp.evaluate("(define v (p-vec 1 2))");
    lisp.evaluate("(define v2 (assoc v 2 3))");
    assertEquals(3, lisp.evaluate("(count v2)").asInt().value);
    assertEquals(3, lisp.evaluate("(get v2 2)").asInt().value);
  }

  // ========== Generic `conj` function ==========

  @Test
  public void testConjOnPVec() {
    lisp.evaluate("(define v (conj (p-vec 1 2) 3 4 5))");
    assertEquals(5, lisp.evaluate("(count v)").asInt().value);
    assertEquals(5, lisp.evaluate("(get v 4)").asInt().value);
  }

  @Test
  public void testConjOnPSet() {
    lisp.evaluate("(define s (conj (p-set 1) 2 3 2))");
    assertEquals(3, lisp.evaluate("(count s)").asInt().value);
  }

  @Test
  public void testConjOnPMap() {
    // For maps, conj takes [key value] pairs
    lisp.evaluate("(define m (conj (p-map) (list #:a 1) (list #:b 2)))");
    assertEquals(2, lisp.evaluate("(count m)").asInt().value);
    assertEquals(1, lisp.evaluate("(get m #:a)").asInt().value);
  }

  // ========== Generic `dissoc` function ==========

  @Test
  public void testDissocOnPMap() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2 #:c 3))");
    lisp.evaluate("(define m2 (dissoc m #:b))");
    assertEquals(2, lisp.evaluate("(count m2)").asInt().value);
    assertFalse(lisp.evaluate("(contains? m2 #:b)").truthiness());
  }

  @Test
  public void testDissocOnPMapMultiple() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2 #:c 3 #:d 4))");
    lisp.evaluate("(define m2 (dissoc m #:a #:c))");
    assertEquals(2, lisp.evaluate("(count m2)").asInt().value);
  }

  @Test
  public void testDissocOnPSet() {
    lisp.evaluate("(define s (p-set 1 2 3 4))");
    lisp.evaluate("(define s2 (dissoc s 2 4))");
    assertEquals(2, lisp.evaluate("(count s2)").asInt().value);
  }

  // ========== Generic `contains?` function ==========

  @Test
  public void testContainsOnPMap() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2))");
    assertTrue(lisp.evaluate("(contains? m #:a)").truthiness());
    assertFalse(lisp.evaluate("(contains? m #:c)").truthiness());
  }

  @Test
  public void testContainsOnPSet() {
    lisp.evaluate("(define s (p-set 1 2 3))");
    assertTrue(lisp.evaluate("(contains? s 2)").truthiness());
    assertFalse(lisp.evaluate("(contains? s 5)").truthiness());
  }

  @Test
  public void testContainsOnPVec() {
    // For vectors, contains? checks if index is valid
    lisp.evaluate("(define v (p-vec 10 20 30))");
    assertTrue(lisp.evaluate("(contains? v 0)").truthiness());
    assertTrue(lisp.evaluate("(contains? v 2)").truthiness());
    assertFalse(lisp.evaluate("(contains? v 3)").truthiness());
    assertFalse(lisp.evaluate("(contains? v -1)").truthiness());
  }

  // ========== Generic `count` function ==========

  @Test
  public void testCountOnPVec() {
    assertEquals(5, lisp.evaluate("(count (p-vec 1 2 3 4 5))").asInt().value);
    assertEquals(0, lisp.evaluate("(count (p-vec))").asInt().value);
  }

  @Test
  public void testCountOnPMap() {
    assertEquals(3, lisp.evaluate("(count (p-map #:a 1 #:b 2 #:c 3))").asInt().value);
  }

  @Test
  public void testCountOnPSet() {
    assertEquals(3, lisp.evaluate("(count (p-set 1 2 3))").asInt().value);
  }

  @Test
  public void testCountOnList() {
    // count also works on regular lists
    assertEquals(4, lisp.evaluate("(count (list 1 2 3 4))").asInt().value);
    assertEquals(0, lisp.evaluate("(count (list))").asInt().value);
  }

  // ========== Generic `empty?` function ==========

  @Test
  public void testEmptyOnCollections() {
    assertTrue(lisp.evaluate("(empty? (p-vec))").truthiness());
    assertFalse(lisp.evaluate("(empty? (p-vec 1))").truthiness());
    assertTrue(lisp.evaluate("(empty? (p-map))").truthiness());
    assertFalse(lisp.evaluate("(empty? (p-map #:a 1))").truthiness());
    assertTrue(lisp.evaluate("(empty? (p-set))").truthiness());
    assertFalse(lisp.evaluate("(empty? (p-set 1))").truthiness());
    assertTrue(lisp.evaluate("(empty? (list))").truthiness());
    assertFalse(lisp.evaluate("(empty? (list 1))").truthiness());
  }

  // ========== Generic `into` function ==========

  @Test
  public void testIntoVec() {
    lisp.evaluate("(define v (into (p-vec) (list 1 2 3)))");
    assertEquals(3, lisp.evaluate("(count v)").asInt().value);
    assertTrue(lisp.evaluate("(p-vec? v)").truthiness());
  }

  @Test
  public void testIntoSet() {
    lisp.evaluate("(define s (into (p-set) (list 1 2 2 3 3)))");
    assertEquals(3, lisp.evaluate("(count s)").asInt().value);
    assertTrue(lisp.evaluate("(p-set? s)").truthiness());
  }

  @Test
  public void testIntoMap() {
    lisp.evaluate("(define m (into (p-map) (list (list #:a 1) (list #:b 2))))");
    assertEquals(2, lisp.evaluate("(count m)").asInt().value);
    assertEquals(1, lisp.evaluate("(get m #:a)").asInt().value);
  }

  @Test
  public void testIntoFromPVec() {
    lisp.evaluate("(define s (into (p-set) (p-vec 1 2 3 2 1)))");
    assertEquals(3, lisp.evaluate("(count s)").asInt().value);
  }

  // ========== Callable Collections ==========

  @Test
  public void testCallableVector() {
    lisp.evaluate("(define v (p-vec 10 20 30))");
    assertEquals(10, lisp.evaluate("(v 0)").asInt().value);
    assertEquals(20, lisp.evaluate("(v 1)").asInt().value);
    assertEquals(30, lisp.evaluate("(v 2)").asInt().value);
  }

  @Test
  public void testCallableVectorWithDefault() {
    lisp.evaluate("(define v (p-vec 10 20))");
    assertEquals(10, lisp.evaluate("(v 0 99)").asInt().value);
    assertEquals(99, lisp.evaluate("(v 5 99)").asInt().value);
  }

  @Test
  public void testCallableMap() {
    lisp.evaluate("(define m (p-map #:name \"Alice\" #:age 30))");
    assertEquals("Alice", lisp.evaluate("(m #:name)").asString().value());
    assertEquals(30, lisp.evaluate("(m #:age)").asInt().value);
  }

  @Test
  public void testCallableMapWithDefault() {
    lisp.evaluate("(define m (p-map #:a 1))");
    assertEquals(1, lisp.evaluate("(m #:a 0)").asInt().value);
    assertEquals(0, lisp.evaluate("(m #:b 0)").asInt().value);
  }

  @Test
  public void testCallableSet() {
    lisp.evaluate("(define s (p-set 1 2 3))");
    assertEquals(2, lisp.evaluate("(s 2)").asInt().value);
    // R7RS: Sets return #f for not-found, not nil. Use not to check falsiness.
    assertTrue(lisp.evaluate("(not (s 99))").truthiness());
  }

  @Test
  public void testCallableSetWithDefault() {
    lisp.evaluate("(define s (p-set 1 2 3))");
    assertEquals(2, lisp.evaluate("(s 2 #:not-found)").asInt().value);
    assertEquals("#:not-found", lisp.evaluate("(s 99 #:not-found)").toString());
  }

  // ========== Callable Keywords ==========

  @Test
  public void testCallableKeyword() {
    lisp.evaluate("(define m (p-map #:name \"Bob\" #:age 25))");
    assertEquals("Bob", lisp.evaluate("(#:name m)").asString().value());
    assertEquals(25, lisp.evaluate("(#:age m)").asInt().value);
  }

  @Test
  public void testCallableKeywordWithDefault() {
    lisp.evaluate("(define m (p-map #:a 1))");
    assertEquals(1, lisp.evaluate("(#:a m 0)").asInt().value);
    assertEquals(0, lisp.evaluate("(#:b m 0)").asInt().value);
  }

  // ========== `persistent?` predicate ==========

  @Test
  public void testPersistentPredicate() {
    assertTrue(lisp.evaluate("(persistent? (p-vec 1 2 3))").truthiness());
    assertTrue(lisp.evaluate("(persistent? (p-map #:a 1))").truthiness());
    assertTrue(lisp.evaluate("(persistent? (p-set 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(persistent? (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(persistent? (vector 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(persistent? 42)").truthiness());
  }

  // ========== Practical Use Cases ==========

  @Test
  public void testNestedMapAccess() {
    lisp.evaluate(
        "(define person (p-map #:name \"Alice\" #:address (p-map #:city \"NYC\" #:zip 10001)))");
    assertEquals("NYC", lisp.evaluate("(#:city (#:address person))").asString().value());
    assertEquals("NYC", lisp.evaluate("(get (get person #:address) #:city)").asString().value());
  }

  @Test
  public void testBuildingCollectionsWithFold() {
    lisp.evaluate("(define nums (list 1 2 3 4 5))");
    lisp.evaluate("(define doubled (fold-left (lambda (acc x) (conj acc (* x 2))) (p-vec) nums))");
    assertEquals(5, lisp.evaluate("(count doubled)").asInt().value);
    assertEquals(2, lisp.evaluate("(doubled 0)").asInt().value);
    assertEquals(10, lisp.evaluate("(doubled 4)").asInt().value);
  }

  @Test
  public void testFilteringWithSets() {
    // Use the set directly as the predicate - sets are callable
    lisp.evaluate("(define allowed (p-set 2 4 6 8 10))");
    lisp.evaluate("(define result (filter allowed (list 1 2 3 4 5 6 7 8)))");
    assertEquals(4, lisp.evaluate("(length result)").asInt().value);
  }

  @Test
  public void testMapAsLookupFunction() {
    lisp.evaluate("(define grades (p-map \"Alice\" 95 \"Bob\" 87 \"Charlie\" 92))");
    lisp.evaluate("(define student-grades (map grades (list \"Alice\" \"Charlie\")))");
    assertEquals(95, lisp.evaluate("(car student-grades)").asInt().value);
    assertEquals(92, lisp.evaluate("(cadr student-grades)").asInt().value);
  }
}
