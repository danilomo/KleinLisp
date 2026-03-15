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

import net.sourceforge.kleinlisp.objects.PMapObject;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for persistent map functions. */
public class PersistentMapTest extends BaseTestClass {

  // ========== Construction Tests ==========

  @Test
  public void testEmptyPMap() {
    LispObject result = lisp.evaluate("(p-map)");
    assertTrue(result instanceof PMapObject);
    assertEquals(0, lisp.evaluate("(p-map-size (p-map))").asInt().value);
  }

  @Test
  public void testPMapWithKeyValuePairs() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2 #:c 3))");
    assertEquals(3, lisp.evaluate("(p-map-size m)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-map-get m #:a)").asInt().value);
    assertEquals(2, lisp.evaluate("(p-map-get m #:b)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-map-get m #:c)").asInt().value);
  }

  @Test
  public void testPMapWithIntegerKeys() {
    lisp.evaluate("(define m (p-map 1 \"one\" 2 \"two\" 3 \"three\"))");
    assertEquals(3, lisp.evaluate("(p-map-size m)").asInt().value);
    assertEquals("one", lisp.evaluate("(p-map-get m 1)").asString().value());
    assertEquals("two", lisp.evaluate("(p-map-get m 2)").asString().value());
  }

  @Test
  public void testPMapWithStringKeys() {
    lisp.evaluate("(define m (p-map \"name\" \"Alice\" \"age\" 30))");
    assertEquals(2, lisp.evaluate("(p-map-size m)").asInt().value);
    assertEquals("Alice", lisp.evaluate("(p-map-get m \"name\")").asString().value());
    assertEquals(30, lisp.evaluate("(p-map-get m \"age\")").asInt().value);
  }

  @Test
  public void testPMapOddArguments() {
    assertThrows(LispArgumentError.class, () -> lisp.evaluate("(p-map #:a 1 #:b)"));
  }

  // ========== Immutability Tests ==========

  @Test
  public void testPMapAssocImmutability() {
    lisp.evaluate("(define m1 (p-map #:a 1 #:b 2))");
    lisp.evaluate("(define m2 (p-map-assoc m1 #:c 3))");

    // Original map unchanged
    assertEquals(2, lisp.evaluate("(p-map-size m1)").asInt().value);
    assertFalse(lisp.evaluate("(p-map-contains? m1 #:c)").truthiness());

    // New map has the addition
    assertEquals(3, lisp.evaluate("(p-map-size m2)").asInt().value);
    assertTrue(lisp.evaluate("(p-map-contains? m2 #:c)").truthiness());
  }

  @Test
  public void testPMapDissocImmutability() {
    lisp.evaluate("(define m1 (p-map #:a 1 #:b 2 #:c 3))");
    lisp.evaluate("(define m2 (p-map-dissoc m1 #:b))");

    // Original map unchanged
    assertEquals(3, lisp.evaluate("(p-map-size m1)").asInt().value);
    assertTrue(lisp.evaluate("(p-map-contains? m1 #:b)").truthiness());

    // New map has the removal
    assertEquals(2, lisp.evaluate("(p-map-size m2)").asInt().value);
    assertFalse(lisp.evaluate("(p-map-contains? m2 #:b)").truthiness());
  }

  // ========== Access Operations ==========

  @Test
  public void testPMapGet() {
    lisp.evaluate("(define m (p-map #:x 10 #:y 20 #:z 30))");
    assertEquals(10, lisp.evaluate("(p-map-get m #:x)").asInt().value);
    assertEquals(20, lisp.evaluate("(p-map-get m #:y)").asInt().value);
    assertEquals(30, lisp.evaluate("(p-map-get m #:z)").asInt().value);
  }

  @Test
  public void testPMapGetMissingKey() {
    lisp.evaluate("(define m (p-map #:a 1))");
    // Should return NIL for missing keys
    assertTrue(lisp.evaluate("(null? (p-map-get m #:missing))").truthiness());
  }

  @Test
  public void testPMapGetWithDefault() {
    lisp.evaluate("(define m (p-map #:a 1))");
    assertEquals(1, lisp.evaluate("(p-map-get m #:a 0)").asInt().value);
    assertEquals(42, lisp.evaluate("(p-map-get m #:missing 42)").asInt().value);
  }

  @Test
  public void testPMapContains() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2))");
    assertTrue(lisp.evaluate("(p-map-contains? m #:a)").truthiness());
    assertTrue(lisp.evaluate("(p-map-contains? m #:b)").truthiness());
    assertFalse(lisp.evaluate("(p-map-contains? m #:c)").truthiness());
  }

  // ========== Modification Operations ==========

  @Test
  public void testPMapAssoc() {
    lisp.evaluate("(define m (p-map #:a 1))");
    lisp.evaluate("(define m2 (p-map-assoc m #:b 2))");
    assertEquals(2, lisp.evaluate("(p-map-size m2)").asInt().value);
    assertEquals(2, lisp.evaluate("(p-map-get m2 #:b)").asInt().value);
  }

  @Test
  public void testPMapAssocOverwrite() {
    lisp.evaluate("(define m (p-map #:a 1))");
    lisp.evaluate("(define m2 (p-map-assoc m #:a 100))");
    assertEquals(1, lisp.evaluate("(p-map-size m2)").asInt().value);
    assertEquals(100, lisp.evaluate("(p-map-get m2 #:a)").asInt().value);
  }

  @Test
  public void testPMapAssocMultiple() {
    lisp.evaluate("(define m (p-map #:a 1))");
    lisp.evaluate("(define m2 (p-map-assoc m #:b 2 #:c 3 #:d 4))");
    assertEquals(4, lisp.evaluate("(p-map-size m2)").asInt().value);
  }

  @Test
  public void testPMapDissoc() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2 #:c 3))");
    lisp.evaluate("(define m2 (p-map-dissoc m #:b))");
    assertEquals(2, lisp.evaluate("(p-map-size m2)").asInt().value);
    assertFalse(lisp.evaluate("(p-map-contains? m2 #:b)").truthiness());
  }

  @Test
  public void testPMapDissocMultiple() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2 #:c 3 #:d 4))");
    lisp.evaluate("(define m2 (p-map-dissoc m #:a #:c))");
    assertEquals(2, lisp.evaluate("(p-map-size m2)").asInt().value);
    assertTrue(lisp.evaluate("(p-map-contains? m2 #:b)").truthiness());
    assertTrue(lisp.evaluate("(p-map-contains? m2 #:d)").truthiness());
  }

  @Test
  public void testPMapDissocMissingKey() {
    lisp.evaluate("(define m (p-map #:a 1))");
    lisp.evaluate("(define m2 (p-map-dissoc m #:nonexistent))");
    assertEquals(1, lisp.evaluate("(p-map-size m2)").asInt().value);
  }

  // ========== Collection Access ==========

  @Test
  public void testPMapKeys() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2 #:c 3))");
    LispObject keys = lisp.evaluate("(p-map-keys m)");
    assertEquals(3, keys.asList().length());
  }

  @Test
  public void testPMapVals() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2 #:c 3))");
    LispObject vals = lisp.evaluate("(p-map-vals m)");
    assertEquals(3, vals.asList().length());
  }

  @Test
  public void testPMapEntries() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2))");
    LispObject entries = lisp.evaluate("(p-map-entries m)");
    assertEquals(2, entries.asList().length());
    // Each entry is a list of (key value)
    assertEquals(2, entries.asList().car().asList().length());
  }

  @Test
  public void testEmptyPMapKeys() {
    assertTrue(lisp.evaluate("(null? (p-map-keys (p-map)))").truthiness());
  }

  // ========== Merge Operations ==========

  @Test
  public void testPMapMerge() {
    lisp.evaluate("(define m1 (p-map #:a 1 #:b 2))");
    lisp.evaluate("(define m2 (p-map #:c 3 #:d 4))");
    lisp.evaluate("(define m3 (p-map-merge m1 m2))");
    assertEquals(4, lisp.evaluate("(p-map-size m3)").asInt().value);
  }

  @Test
  public void testPMapMergeOverwrite() {
    lisp.evaluate("(define m1 (p-map #:a 1 #:b 2))");
    lisp.evaluate("(define m2 (p-map #:b 99 #:c 3))");
    lisp.evaluate("(define m3 (p-map-merge m1 m2))");
    assertEquals(3, lisp.evaluate("(p-map-size m3)").asInt().value);
    // Later map takes precedence
    assertEquals(99, lisp.evaluate("(p-map-get m3 #:b)").asInt().value);
  }

  @Test
  public void testPMapMergeMultiple() {
    lisp.evaluate("(define m (p-map-merge (p-map #:a 1) (p-map #:b 2) (p-map #:c 3)))");
    assertEquals(3, lisp.evaluate("(p-map-size m)").asInt().value);
  }

  // ========== Conversion Operations ==========

  @Test
  public void testPMapToList() {
    lisp.evaluate("(define m (p-map #:a 1 #:b 2))");
    LispObject list = lisp.evaluate("(p-map->list m)");
    // Flat list: (key1 val1 key2 val2 ...)
    assertEquals(4, list.asList().length());
  }

  @Test
  public void testListToPMapFlat() {
    lisp.evaluate("(define m (list->p-map (list #:a 1 #:b 2 #:c 3)))");
    assertTrue(lisp.evaluate("(p-map? m)").truthiness());
    assertEquals(3, lisp.evaluate("(p-map-size m)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-map-get m #:a)").asInt().value);
  }

  @Test
  public void testListToPMapAlist() {
    lisp.evaluate("(define m (list->p-map (list (list #:a 1) (list #:b 2))))");
    assertTrue(lisp.evaluate("(p-map? m)").truthiness());
    assertEquals(2, lisp.evaluate("(p-map-size m)").asInt().value);
    assertEquals(1, lisp.evaluate("(p-map-get m #:a)").asInt().value);
  }

  @Test
  public void testEmptyListToPMap() {
    lisp.evaluate("(define m (list->p-map (list)))");
    assertTrue(lisp.evaluate("(p-map-empty? m)").truthiness());
  }

  // ========== Predicates ==========

  @Test
  public void testIsPMap() {
    assertTrue(lisp.evaluate("(p-map? (p-map))").truthiness());
    assertTrue(lisp.evaluate("(p-map? (p-map #:a 1))").truthiness());
    assertFalse(lisp.evaluate("(p-map? (list 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(p-map? 42)").truthiness());
  }

  @Test
  public void testIsPMapEmpty() {
    assertTrue(lisp.evaluate("(p-map-empty? (p-map))").truthiness());
    assertFalse(lisp.evaluate("(p-map-empty? (p-map #:a 1))").truthiness());
  }

  // ========== Structural Sharing Tests ==========

  @Test
  public void testStructuralSharingWithChainedOperations() {
    lisp.evaluate("(define m0 (p-map #:a 1 #:b 2 #:c 3))");
    lisp.evaluate("(define m1 (p-map-assoc m0 #:d 4))");
    lisp.evaluate("(define m2 (p-map-dissoc m1 #:a))");
    lisp.evaluate("(define m3 (p-map-assoc m2 #:b 200))");

    // All versions exist independently
    assertEquals(3, lisp.evaluate("(p-map-size m0)").asInt().value);
    assertEquals(4, lisp.evaluate("(p-map-size m1)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-map-size m2)").asInt().value);
    assertEquals(3, lisp.evaluate("(p-map-size m3)").asInt().value);

    // Each has correct values
    assertEquals(1, lisp.evaluate("(p-map-get m0 #:a)").asInt().value);
    assertTrue(lisp.evaluate("(p-map-contains? m1 #:a)").truthiness());
    assertFalse(lisp.evaluate("(p-map-contains? m2 #:a)").truthiness());
    assertEquals(2, lisp.evaluate("(p-map-get m0 #:b)").asInt().value);
    assertEquals(200, lisp.evaluate("(p-map-get m3 #:b)").asInt().value);
  }

  // ========== Use with Complex Values ==========

  @Test
  public void testPMapWithListValues() {
    lisp.evaluate("(define m (p-map #:items (list 1 2 3) #:name \"test\"))");
    assertEquals(3, lisp.evaluate("(length (p-map-get m #:items))").asInt().value);
  }

  @Test
  public void testPMapWithNestedMaps() {
    lisp.evaluate("(define inner (p-map #:x 10 #:y 20))");
    lisp.evaluate("(define outer (p-map #:position inner #:name \"point\"))");
    assertEquals(10, lisp.evaluate("(p-map-get (p-map-get outer #:position) #:x)").asInt().value);
  }

  // ========== Edge Cases ==========

  @Test
  public void testPMapWithNilValue() {
    lisp.evaluate("(define m (p-map #:empty (list)))");
    assertTrue(lisp.evaluate("(null? (p-map-get m #:empty))").truthiness());
    assertTrue(lisp.evaluate("(p-map-contains? m #:empty)").truthiness());
  }

  @Test
  public void testPMapToString() {
    String result = lisp.evaluate("(p-map #:a 1)").toString();
    assertTrue(result.startsWith("{") && result.endsWith("}"));
  }
}
