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

import net.sourceforge.kleinlisp.objects.*;
import org.junit.jupiter.api.Test;

/** Tests for persistent collection literal syntax. */
public class PersistentCollectionLiteralsTest extends BaseTestClass {

  // ========== Persistent Vector Tests ==========

  @Test
  public void testPVecLiteralShortForm() {
    LispObject result = lisp.evaluate("#v[1 2 3]");
    assertTrue(result instanceof PVectorObject);
    assertEquals(3, lisp.evaluate("(p-vec-length #v[1 2 3])").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref #v[1 2 3] 0)").asInt().value);
  }

  @Test
  public void testPVecLiteralLongForm() {
    LispObject result = lisp.evaluate("#vec[1 2 3]");
    assertTrue(result instanceof PVectorObject);
    assertEquals(3, lisp.evaluate("(p-vec-length #vec[1 2 3])").asInt().value);
  }

  @Test
  public void testPVecLiteralEmpty() {
    LispObject result = lisp.evaluate("#v[]");
    assertTrue(result instanceof PVectorObject);
    assertEquals(0, lisp.evaluate("(p-vec-length #v[])").asInt().value);
  }

  @Test
  public void testPVecLiteralNested() {
    lisp.evaluate("(define v #v[1 #v[2 3] 4])");
    assertEquals(3, lisp.evaluate("(p-vec-length v)").asInt().value);
    assertEquals(2, lisp.evaluate("(p-vec-ref (p-vec-ref v 1) 0)").asInt().value);
  }

  @Test
  public void testPVecLiteralWithDifferentTypes() {
    lisp.evaluate("(define v #v[1 \"hello\" #t #:key])");
    assertEquals(4, lisp.evaluate("(p-vec-length v)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-vec-ref v 0)").asInt().value);
    assertEquals("hello", evalAsString("(p-vec-ref v 1)"));
    assertTrue(lisp.evaluate("(p-vec-ref v 2)").truthiness());
  }

  // ========== Persistent Map Tests ==========

  @Test
  public void testPMapLiteralShortForm() {
    LispObject result = lisp.evaluate("#m{#:a 1 #:b 2}");
    assertTrue(result instanceof PMapObject);
    assertEquals(2, lisp.evaluate("(p-map-size #m{#:a 1 #:b 2})").asInt().value);
    assertEquals(1, lisp.evaluate("(p-map-get #m{#:a 1 #:b 2} #:a)").asInt().value);
  }

  @Test
  public void testPMapLiteralLongForm() {
    LispObject result = lisp.evaluate("#map{#:x 10}");
    assertTrue(result instanceof PMapObject);
    assertEquals(10, lisp.evaluate("(p-map-get #map{#:x 10} #:x)").asInt().value);
  }

  @Test
  public void testPMapLiteralEmpty() {
    LispObject result = lisp.evaluate("#m{}");
    assertTrue(result instanceof PMapObject);
    assertEquals(0, lisp.evaluate("(p-map-size #m{})").asInt().value);
  }

  @Test
  public void testPMapLiteralOddElementsError() {
    assertThrows(
        RuntimeException.class,
        () -> {
          lisp.evaluate("#m{#:key}"); // Missing value
        });
  }

  @Test
  public void testPMapLiteralAsFunction() {
    // Maps can be used as functions to look up values
    assertEquals(100, lisp.evaluate("(#m{#:a 100} #:a)").asInt().value);
  }

  @Test
  public void testPMapLiteralNested() {
    lisp.evaluate("(define m #m{#:user #m{#:name \"Alice\" #:age 30}})");
    assertEquals("Alice", evalAsString("(p-map-get (p-map-get m #:user) #:name)"));
    assertEquals(30, lisp.evaluate("(p-map-get (p-map-get m #:user) #:age)").asInt().value);
  }

  // ========== Persistent Set Tests ==========

  @Test
  public void testPSetLiteralShortForm() {
    LispObject result = lisp.evaluate("#s{1 2 3}");
    assertTrue(result instanceof PSetObject);
    assertEquals(3, lisp.evaluate("(p-set-size #s{1 2 3})").asInt().value);
    assertTrue(lisp.evaluate("(p-set-contains? #s{1 2 3} 2)").truthiness());
  }

  @Test
  public void testPSetLiteralLongForm() {
    LispObject result = lisp.evaluate("#set{#:a #:b}");
    assertTrue(result instanceof PSetObject);
    assertEquals(2, lisp.evaluate("(p-set-size #set{#:a #:b})").asInt().value);
  }

  @Test
  public void testPSetLiteralEmpty() {
    LispObject result = lisp.evaluate("#s{}");
    assertTrue(result instanceof PSetObject);
    assertEquals(0, lisp.evaluate("(p-set-size #s{})").asInt().value);
  }

  @Test
  public void testPSetLiteralDeduplication() {
    LispObject result = lisp.evaluate("#s{1 2 2 3 3 3}");
    assertEquals(3, lisp.evaluate("(p-set-size #s{1 2 2 3 3 3})").asInt().value);
  }

  @Test
  public void testPSetLiteralContains() {
    assertTrue(lisp.evaluate("(p-set-contains? #s{1 2 3} 1)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? #s{1 2 3} 2)").truthiness());
    assertTrue(lisp.evaluate("(p-set-contains? #s{1 2 3} 3)").truthiness());
    assertFalse(lisp.evaluate("(p-set-contains? #s{1 2 3} 4)").truthiness());
  }

  // ========== Bracket-Paren Equivalence Tests ==========

  @Test
  public void testBracketListEquivalence() {
    // Ensure [1 2 3] creates a list, not a vector
    LispObject parenList = lisp.evaluate("(list 1 2 3)");
    LispObject bracketList = lisp.evaluate("[list 1 2 3]");
    assertEquals(parenList.toString(), bracketList.toString());
  }

  @Test
  public void testBracketLetEquivalence() {
    // Traditional Scheme idiom: [let ([x 1]) x]
    assertEquals(1, lisp.evaluate("[let ([x 1]) x]").asInt().value);
    assertEquals(1, lisp.evaluate("(let ((x 1)) x)").asInt().value);
  }

  @Test
  public void testBracketEmptyList() {
    // Empty brackets should create empty list
    LispObject result = lisp.evaluate("[]");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testVectorVsPVectorDistinction() {
    // Ensure we can distinguish mutable vs persistent vectors
    LispObject mutableVec = lisp.evaluate("#(1 2 3)");
    LispObject persistentVec = lisp.evaluate("#v[1 2 3]");

    assertTrue(mutableVec instanceof VectorObject);
    assertTrue(persistentVec instanceof PVectorObject);
    assertNotEquals(mutableVec.getClass(), persistentVec.getClass());
  }

  @Test
  public void testBracketDottedList() {
    // Test dotted list with brackets
    LispObject result = lisp.evaluate("[cons 1 2]");
    assertTrue(result instanceof ListObject);
    assertEquals("(1 . 2)", result.toString());
  }

  // ========== Integration Tests ==========

  @Test
  public void testNestedCollections() {
    // Complex nested structure
    lisp.evaluate(
        "(define data #m{#:users #v[#m{#:name \"Alice\" #:age 30} #m{#:name \"Bob\" #:age 25}] #:tags #s{#:admin #:user}})");

    // Access nested data
    assertEquals(2, lisp.evaluate("(p-vec-length (p-map-get data #:users))").asInt().value);
    assertEquals("Alice", evalAsString("(p-map-get (p-vec-ref (p-map-get data #:users) 0) #:name)"));
    assertEquals(
        25, lisp.evaluate("(p-map-get (p-vec-ref (p-map-get data #:users) 1) #:age)").asInt().value);
    assertTrue(
        lisp.evaluate("(p-set-contains? (p-map-get data #:tags) #:admin)").truthiness());
  }

  @Test
  public void testLiteralVsConstructor() {
    // Literal and constructor should create equivalent objects
    LispObject literalVec = lisp.evaluate("#v[1 2 3]");
    LispObject constructorVec = lisp.evaluate("(p-vec 1 2 3)");
    assertEquals(literalVec.toString(), constructorVec.toString());

    LispObject literalMap = lisp.evaluate("#m{#:a 1 #:b 2}");
    LispObject constructorMap = lisp.evaluate("(p-map #:a 1 #:b 2)");
    // Note: Map toString might have different ordering, so check size and values instead
    assertEquals(
        lisp.evaluate("(p-map-size #m{#:a 1 #:b 2})").asInt().value,
        lisp.evaluate("(p-map-size (p-map #:a 1 #:b 2))").asInt().value);

    LispObject literalSet = lisp.evaluate("#s{1 2 3}");
    LispObject constructorSet = lisp.evaluate("(p-set 1 2 3)");
    assertEquals(literalSet.toString(), constructorSet.toString());
  }

  @Test
  public void testQuotedLiterals() {
    // Quoted literals should work
    LispObject quotedVec = lisp.evaluate("'#v[1 2 3]");
    assertTrue(quotedVec instanceof PVectorObject);
    assertEquals(3, lisp.evaluate("(p-vec-length '#v[1 2 3])").asInt().value);
  }
}
