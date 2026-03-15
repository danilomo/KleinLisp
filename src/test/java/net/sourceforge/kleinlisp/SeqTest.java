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

/** Tests for sequence abstraction (seq, first, rest, next, seqable?). */
public class SeqTest extends BaseTestClass {

  // --- first tests ---

  @Test
  public void testFirstOnList() {
    assertEquals(1, lisp.evaluate("(first '(1 2 3))").asInt().value);
    assertEquals("a", lisp.evaluate("(first '(a b c))").asAtom().toString());
  }

  @Test
  public void testFirstOnVector() {
    assertEquals(1, lisp.evaluate("(first (p-vec 1 2 3))").asInt().value);
  }

  @Test
  public void testFirstOnSet() {
    // Sets don't have a specific order, so we just check that first returns an element
    LispObject result = lisp.evaluate("(first (p-set 1 2 3))");
    assertTrue(result.asInt() != null);
    int value = result.asInt().value;
    assertTrue(value == 1 || value == 2 || value == 3);
  }

  @Test
  public void testFirstOnEmptyList() {
    assertEquals(ListObject.NIL, lisp.evaluate("(first '())"));
  }

  @Test
  public void testFirstOnEmptyVector() {
    assertEquals(ListObject.NIL, lisp.evaluate("(first (p-vec))"));
  }

  @Test
  public void testFirstOnEmptySet() {
    assertEquals(ListObject.NIL, lisp.evaluate("(first (p-set))"));
  }

  // --- rest tests ---

  @Test
  public void testRestOnList() {
    LispObject result = lisp.evaluate("(rest '(1 2 3))");
    assertEquals(2, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);
  }

  @Test
  public void testRestOnVector() {
    LispObject result = lisp.evaluate("(rest (p-vec 1 2 3))");
    assertEquals(2, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);
  }

  @Test
  public void testRestOnSet() {
    // Sets don't have a specific order, but rest should return a list with 2 elements
    LispObject result = lisp.evaluate("(rest (p-set 1 2 3))");
    assertEquals(2, result.asList().length());
  }

  @Test
  public void testRestOnEmptyList() {
    assertEquals(ListObject.NIL, lisp.evaluate("(rest '())"));
  }

  @Test
  public void testRestOnSingleElementList() {
    assertEquals(ListObject.NIL, lisp.evaluate("(rest '(1))"));
  }

  @Test
  public void testRestOnSingleElementVector() {
    assertEquals(ListObject.NIL, lisp.evaluate("(rest (p-vec 1))"));
  }

  // --- next tests ---

  @Test
  public void testNextOnList() {
    LispObject result = lisp.evaluate("(next '(1 2 3))");
    assertEquals(2, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);
  }

  @Test
  public void testNextOnEmptyList() {
    assertEquals(ListObject.NIL, lisp.evaluate("(next '())"));
  }

  @Test
  public void testNextOnSingleElementList() {
    // next returns nil when there's nothing left, unlike rest which returns ()
    assertEquals(ListObject.NIL, lisp.evaluate("(next '(1))"));
  }

  // --- seq tests ---

  @Test
  public void testSeqOnList() {
    LispObject result = lisp.evaluate("(seq '(1 2 3))");
    assertEquals(3, result.asList().length());
  }

  @Test
  public void testSeqOnVector() {
    LispObject result = lisp.evaluate("(seq (p-vec 1 2 3))");
    assertEquals(3, result.asList().length());
    assertEquals(1, result.asList().car().asInt().value);
  }

  @Test
  public void testSeqOnSet() {
    LispObject result = lisp.evaluate("(seq (p-set 1 2 3))");
    assertEquals(3, result.asList().length());
  }

  @Test
  public void testSeqOnEmptyList() {
    assertEquals(ListObject.NIL, lisp.evaluate("(seq '())"));
  }

  @Test
  public void testSeqOnEmptyVector() {
    assertEquals(ListObject.NIL, lisp.evaluate("(seq (p-vec))"));
  }

  @Test
  public void testSeqOnEmptySet() {
    assertEquals(ListObject.NIL, lisp.evaluate("(seq (p-set))"));
  }

  // --- seqable? tests ---

  @Test
  public void testSeqableOnList() {
    assertTrue(lisp.evaluate("(seqable? '(1 2 3))").truthiness());
  }

  @Test
  public void testSeqableOnVector() {
    assertTrue(lisp.evaluate("(seqable? (p-vec 1 2 3))").truthiness());
  }

  @Test
  public void testSeqableOnSet() {
    assertTrue(lisp.evaluate("(seqable? (p-set 1 2 3))").truthiness());
  }

  @Test
  public void testSeqableOnEmptyList() {
    assertTrue(lisp.evaluate("(seqable? '())").truthiness());
  }

  @Test
  public void testSeqableOnNumber() {
    assertFalse(lisp.evaluate("(seqable? 42)").truthiness());
  }

  @Test
  public void testSeqableOnString() {
    // Strings are not seqable in this implementation
    assertFalse(lisp.evaluate("(seqable? \"hello\")").truthiness());
  }

  // --- HOFs with vectors ---

  @Test
  public void testMapOnVector() {
    lisp.evaluate("(define (inc x) (+ x 1))");
    LispObject result = lisp.evaluate("(map inc (p-vec 1 2 3))");
    assertEquals(3, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);
    assertEquals(3, result.asList().cdr().car().asInt().value);
    assertEquals(4, result.asList().cdr().cdr().car().asInt().value);
  }

  @Test
  public void testMapOnVectorWithLambda() {
    LispObject result = lisp.evaluate("(map (lambda (x) (* x 2)) (p-vec 1 2 3))");
    assertEquals(3, result.asList().length());
    assertEquals(2, result.asList().car().asInt().value);
    assertEquals(4, result.asList().cdr().car().asInt().value);
    assertEquals(6, result.asList().cdr().cdr().car().asInt().value);
  }

  @Test
  public void testFilterOnVector() {
    LispObject result = lisp.evaluate("(filter odd? (p-vec 1 2 3 4))");
    assertEquals(2, result.asList().length());
    assertEquals(1, result.asList().car().asInt().value);
    assertEquals(3, result.asList().cdr().car().asInt().value);
  }

  @Test
  public void testFilterOnVectorNoMatch() {
    LispObject result = lisp.evaluate("(filter negative? (p-vec 1 2 3))");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testReduceOnVector() {
    assertEquals(6, lisp.evaluate("(reduce + 0 (p-vec 1 2 3))").asInt().value);
  }

  @Test
  public void testFoldLeftOnVector() {
    assertEquals(10, lisp.evaluate("(fold-left + 0 (p-vec 1 2 3 4))").asInt().value);
  }

  @Test
  public void testFoldRightOnVector() {
    // Test order: fold-right cons '() (p-vec 1 2 3) should give (1 2 3)
    LispObject result = lisp.evaluate("(fold-right cons '() (p-vec 1 2 3))");
    assertEquals(1, result.asList().car().asInt().value);
  }

  // --- HOFs with sets ---

  @Test
  public void testMapOnSet() {
    LispObject result = lisp.evaluate("(map (lambda (x) (* x 2)) (p-set 1 2 3))");
    // Result is a list containing doubled values, order not guaranteed
    assertEquals(3, result.asList().length());
  }

  @Test
  public void testFilterOnSet() {
    LispObject result = lisp.evaluate("(filter odd? (p-set 1 2 3 4))");
    // Result is a list containing odd values, order not guaranteed
    assertEquals(2, result.asList().length());
  }

  @Test
  public void testReduceOnSet() {
    // Sum of 1+2+3 = 6, regardless of order
    assertEquals(6, lisp.evaluate("(reduce + 0 (p-set 1 2 3))").asInt().value);
  }

  // --- any/all/find with vectors and sets ---

  @Test
  public void testAnyOnVector() {
    assertTrue(lisp.evaluate("(any even? (p-vec 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(any even? (p-vec 1 3 5))").truthiness());
  }

  @Test
  public void testAllOnVector() {
    assertTrue(lisp.evaluate("(all positive? (p-vec 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(all positive? (p-vec 1 -1 3))").truthiness());
  }

  @Test
  public void testFindOnVector() {
    assertEquals(2, lisp.evaluate("(find even? (p-vec 1 2 3 4))").asInt().value);
    assertFalse(lisp.evaluate("(find even? (p-vec 1 3 5))").truthiness());
  }

  @Test
  public void testAnyOnSet() {
    assertTrue(lisp.evaluate("(any even? (p-set 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(any even? (p-set 1 3 5))").truthiness());
  }

  @Test
  public void testAllOnSet() {
    assertTrue(lisp.evaluate("(all positive? (p-set 1 2 3))").truthiness());
    assertFalse(lisp.evaluate("(all positive? (p-set 1 -1 3))").truthiness());
  }

  // --- for-each with vectors ---

  @Test
  public void testForEachOnVector() {
    lisp.evaluate("(for-each println (p-vec 1 2 3))");
    String output = getStdOut();
    assertTrue(output.contains("1"));
    assertTrue(output.contains("2"));
    assertTrue(output.contains("3"));
  }

  // --- multi-collection map with mixed types ---

  @Test
  public void testMapWithListAndVector() {
    LispObject result = lisp.evaluate("(map + '(1 2 3) (p-vec 10 20 30))");
    assertEquals(3, result.asList().length());
    assertEquals(11, result.asList().car().asInt().value);
    assertEquals(22, result.asList().cdr().car().asInt().value);
    assertEquals(33, result.asList().cdr().cdr().car().asInt().value);
  }

  // --- edge cases ---

  @Test
  public void testEmptyVectorOperations() {
    assertEquals(ListObject.NIL, lisp.evaluate("(map (lambda (x) (+ x 1)) (p-vec))"));
    assertEquals(ListObject.NIL, lisp.evaluate("(filter even? (p-vec))"));
    assertEquals(0, lisp.evaluate("(reduce + 0 (p-vec))").asInt().value);
  }

  @Test
  public void testEmptySetOperations() {
    assertEquals(ListObject.NIL, lisp.evaluate("(map (lambda (x) (+ x 1)) (p-set))"));
    assertEquals(ListObject.NIL, lisp.evaluate("(filter even? (p-set))"));
    assertEquals(0, lisp.evaluate("(reduce + 0 (p-set))").asInt().value);
  }
}
