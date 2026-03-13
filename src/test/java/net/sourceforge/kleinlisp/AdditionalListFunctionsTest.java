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

/** Tests for additional list functions. */
public class AdditionalListFunctionsTest extends BaseTestClass {

  @Test
  public void testAppend() {
    LispObject result = lisp.evaluate("(append (list 1 2) (list 3 4))");
    assertEquals(4, result.asList().length());
    assertEquals(1, result.asList().car().asInt().value);

    // Multiple lists
    result = lisp.evaluate("(append (list 1) (list 2) (list 3))");
    assertEquals(3, result.asList().length());
  }

  @Test
  public void testAppendWithEmptyLists() {
    LispObject result = lisp.evaluate("(append (list) (list 1 2))");
    assertEquals(2, result.asList().length());

    result = lisp.evaluate("(append (list 1 2) (list))");
    assertEquals(2, result.asList().length());

    result = lisp.evaluate("(append (list) (list))");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testMember() {
    LispObject result = lisp.evaluate("(member 3 (list 1 2 3 4 5))");
    assertTrue(result.truthiness());
    assertEquals(3, result.asList().car().asInt().value);
    assertEquals(3, result.asList().length());

    // Not found returns #f
    assertFalse(lisp.evaluate("(member 6 (list 1 2 3 4 5))").truthiness());
  }

  @Test
  public void testMemq() {
    // memq uses eq? (identity)
    assertTrue(lisp.evaluate("(memq 'a (list 'a 'b 'c))").truthiness());
    assertFalse(lisp.evaluate("(memq 'd (list 'a 'b 'c))").truthiness());
  }

  @Test
  public void testAssoc() {
    lisp.evaluate("(define alist (list (list 'a 1) (list 'b 2) (list 'c 3)))");

    LispObject result = lisp.evaluate("(assoc 'b alist)");
    assertTrue(result.truthiness());
    assertEquals(2, result.asList().cdr().car().asInt().value);

    // Not found returns #f
    assertFalse(lisp.evaluate("(assoc 'd alist)").truthiness());
  }

  @Test
  public void testAssocWithNumbers() {
    lisp.evaluate("(define alist (list (list 1 \"one\") (list 2 \"two\") (list 3 \"three\")))");

    LispObject result = lisp.evaluate("(assoc 2 alist)");
    assertTrue(result.truthiness());
    assertEquals("two", result.asList().cdr().car().asString().value());
  }

  @Test
  public void testAssq() {
    // assq uses eq? (identity)
    lisp.evaluate("(define alist (list (list 'a 1) (list 'b 2)))");
    assertTrue(lisp.evaluate("(assq 'a alist)").truthiness());
    assertFalse(lisp.evaluate("(assq 'c alist)").truthiness());
  }

  @Test
  public void testListRef() {
    assertEquals(1, lisp.evaluate("(list-ref (list 1 2 3 4 5) 0)").asInt().value);
    assertEquals(3, lisp.evaluate("(list-ref (list 1 2 3 4 5) 2)").asInt().value);
    assertEquals(5, lisp.evaluate("(list-ref (list 1 2 3 4 5) 4)").asInt().value);
  }

  @Test
  public void testListTail() {
    LispObject result = lisp.evaluate("(list-tail (list 1 2 3 4 5) 2)");
    assertEquals(3, result.asList().length());
    assertEquals(3, result.asList().car().asInt().value);

    result = lisp.evaluate("(list-tail (list 1 2 3) 0)");
    assertEquals(3, result.asList().length());

    result = lisp.evaluate("(list-tail (list 1 2 3) 3)");
    assertEquals(ListObject.NIL, result);
  }

  @Test
  public void testCadrCaddr() {
    assertEquals(2, lisp.evaluate("(cadr (list 1 2 3))").asInt().value);
    assertEquals(3, lisp.evaluate("(caddr (list 1 2 3))").asInt().value);
    assertEquals(4, lisp.evaluate("(cadddr (list 1 2 3 4))").asInt().value);
  }

  @Test
  public void testCddr() {
    LispObject result = lisp.evaluate("(cddr (list 1 2 3 4))");
    assertEquals(2, result.asList().length());
    assertEquals(3, result.asList().car().asInt().value);
  }

  @Test
  public void testCdddr() {
    LispObject result = lisp.evaluate("(cdddr (list 1 2 3 4 5))");
    assertEquals(2, result.asList().length());
    assertEquals(4, result.asList().car().asInt().value);
  }

  @Test
  public void testLast() {
    assertEquals(5, lisp.evaluate("(last (list 1 2 3 4 5))").asInt().value);
    assertEquals(1, lisp.evaluate("(last (list 1))").asInt().value);
  }

  @Test
  public void testLastPair() {
    LispObject result = lisp.evaluate("(last-pair (list 1 2 3))");
    assertEquals(1, result.asList().length());
    assertEquals(3, result.asList().car().asInt().value);
  }

  @Test
  public void testButlast() {
    LispObject result = lisp.evaluate("(butlast (list 1 2 3 4 5))");
    assertEquals(4, result.asList().length());
    assertEquals(4, result.asList().cdr().cdr().cdr().car().asInt().value);
  }

  @Test
  public void testTake() {
    LispObject result = lisp.evaluate("(take (list 1 2 3 4 5) 3)");
    assertEquals(3, result.asList().length());
    assertEquals(1, result.asList().car().asInt().value);
    assertEquals(3, result.asList().cdr().cdr().car().asInt().value);
  }

  @Test
  public void testDrop() {
    LispObject result = lisp.evaluate("(drop (list 1 2 3 4 5) 2)");
    assertEquals(3, result.asList().length());
    assertEquals(3, result.asList().car().asInt().value);
  }

  @Test
  public void testIota() {
    LispObject result = lisp.evaluate("(iota 5)");
    assertEquals(5, result.asList().length());
    assertEquals(0, result.asList().car().asInt().value);
    assertEquals(4, lisp.evaluate("(last (iota 5))").asInt().value);

    // With start
    result = lisp.evaluate("(iota 3 10)");
    assertEquals(3, result.asList().length());
    assertEquals(10, result.asList().car().asInt().value);

    // With start and step
    result = lisp.evaluate("(iota 4 0 2)");
    assertEquals(4, result.asList().length());
    assertEquals(0, result.asList().car().asInt().value);
    assertEquals(6, lisp.evaluate("(last (iota 4 0 2))").asInt().value);
  }

  @Test
  public void testReverse() {
    LispObject result = lisp.evaluate("(reverse (list 1 2 3 4 5))");
    assertEquals(5, result.asList().length());
    assertEquals(5, result.asList().car().asInt().value);
    assertEquals(1, lisp.evaluate("(last (reverse (list 1 2 3 4 5)))").asInt().value);
  }

  @Test
  public void testReverseEmpty() {
    LispObject result = lisp.evaluate("(reverse (list))");
    assertEquals(ListObject.NIL, result);
  }
}
