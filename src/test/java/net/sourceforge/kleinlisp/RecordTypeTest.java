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

import net.sourceforge.kleinlisp.objects.BooleanObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for R7RS define-record-type special form.
 *
 * @author Danilo Oliveira
 */
public class RecordTypeTest extends BaseTestClass {

  @Test
  public void testBasicRecordCreation() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    String result = lisp.evaluate("(make-point 1 2)").toString();
    assertTrue(result.contains("point"));
  }

  @Test
  public void testRecordPredicate() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(point? (make-point 1 2))"));
  }

  @Test
  public void testRecordPredicateFalse() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(point? 42)"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(point? \"hello\")"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(point? '(1 2))"));
  }

  @Test
  public void testRecordAccessor() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    assertEquals(3, evalAsInt("(point-x (make-point 3 4))"));
    assertEquals(4, evalAsInt("(point-y (make-point 3 4))"));
  }

  @Test
  public void testRecordMutator() {
    lisp.evaluate(
        "(define-record-type point (make-point x y) point? (x point-x) (y point-y set-point-y!))");
    assertEquals(5, evalAsInt("(let ((p (make-point 1 2))) (set-point-y! p 5) (point-y p))"));
  }

  @Test
  public void testRecordMutatorPreservesOtherFields() {
    lisp.evaluate(
        "(define-record-type point (make-point x y) point? (x point-x) (y point-y set-point-y!))");
    assertEquals(1, evalAsInt("(let ((p (make-point 1 2))) (set-point-y! p 5) (point-x p))"));
  }

  @Test
  public void testRecordImmutableField() {
    lisp.evaluate(
        "(define-record-type point (make-point x y) point? (x point-x) (y point-y set-point-y!))");
    // x has no mutator, so point-x! should not exist
    assertThrows(Exception.class, () -> lisp.evaluate("(set-point-x! (make-point 1 2) 99)"));
  }

  @Test
  public void testMultipleRecordTypes() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    lisp.evaluate("(define-record-type rect (make-rect w h) rect? (w rect-w) (h rect-h))");

    assertEquals(BooleanObject.TRUE, lisp.evaluate("(point? (make-point 1 2))"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(rect? (make-point 1 2))"));
    assertEquals(BooleanObject.TRUE, lisp.evaluate("(rect? (make-rect 10 20))"));
    assertEquals(BooleanObject.FALSE, lisp.evaluate("(point? (make-rect 10 20))"));
  }

  @Test
  public void testRecordWithThreeFields() {
    lisp.evaluate(
        "(define-record-type person (make-person name age city) person? "
            + "(name person-name set-person-name!) "
            + "(age person-age set-person-age!) "
            + "(city person-city))");

    assertEquals(
        "Alice",
        lisp.evaluate("(person-name (make-person \"Alice\" 30 \"NYC\"))")
            .toString()
            .replace("\"", ""));
    assertEquals(30, evalAsInt("(person-age (make-person \"Alice\" 30 \"NYC\"))"));
  }

  @Test
  public void testRecordMutatorChaining() {
    lisp.evaluate(
        "(define-record-type point (make-point x y) point? "
            + "(x point-x set-point-x!) "
            + "(y point-y set-point-y!))");

    String code =
        "(let ((p (make-point 1 2))) "
            + "(set-point-x! p 10) "
            + "(set-point-y! p 20) "
            + "(+ (point-x p) (point-y p)))";

    assertEquals(30, evalAsInt(code));
  }

  @Test
  public void testRecordConstructorPartialFields() {
    // Constructor only takes x, y field is implicitly nil
    lisp.evaluate("(define-record-type point (make-point x) point? (x point-x) (y point-y))");

    assertEquals(5, evalAsInt("(point-x (make-point 5))"));
    assertEquals("()", lisp.evaluate("(point-y (make-point 5))").toString());
  }

  @Test
  public void testRecordConstructorFieldOrder() {
    // Constructor args in different order than fields
    lisp.evaluate("(define-record-type point (make-point y x) point? (x point-x) (y point-y))");

    assertEquals(2, evalAsInt("(point-x (make-point 1 2))"));
    assertEquals(1, evalAsInt("(point-y (make-point 1 2))"));
  }

  @Test
  public void testRecordInList() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");

    String code = "(let ((pts (list (make-point 1 2) (make-point 3 4)))) " + "(point-x (car pts)))";

    assertEquals(1, evalAsInt(code));
  }

  @Test
  public void testRecordAsReturnValue() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    lisp.evaluate("(define (get-origin) (make-point 0 0))");

    assertEquals(0, evalAsInt("(point-x (get-origin))"));
    assertEquals(0, evalAsInt("(point-y (get-origin))"));
  }

  @Test
  public void testRecordWrongArgCount() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    assertThrows(Exception.class, () -> lisp.evaluate("(make-point 1)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(make-point 1 2 3)"));
  }

  @Test
  public void testRecordAccessorWrongType() {
    lisp.evaluate("(define-record-type point (make-point x y) point? (x point-x) (y point-y))");
    assertThrows(Exception.class, () -> lisp.evaluate("(point-x 42)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(point-x \"not-a-point\")"));
  }

  @Test
  public void testRecordMutatorWrongType() {
    lisp.evaluate(
        "(define-record-type point (make-point x y) point? (x point-x) (y point-y set-point-y!))");
    assertThrows(Exception.class, () -> lisp.evaluate("(set-point-y! 42 99)"));
  }

  @Test
  public void testRecordWithSymbolicValues() {
    lisp.evaluate(
        "(define-record-type tagged (make-tagged tag value) tagged? (tag tagged-tag) (value"
            + " tagged-value))");

    assertEquals("foo", lisp.evaluate("(tagged-tag (make-tagged 'foo 42))").toString());
    assertEquals(42, evalAsInt("(tagged-value (make-tagged 'foo 42))"));
  }
}
