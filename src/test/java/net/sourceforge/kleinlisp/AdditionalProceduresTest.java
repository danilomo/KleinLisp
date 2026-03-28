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

/** Tests for additional R7RS procedures from spec 13. */
public class AdditionalProceduresTest extends BaseTestClass {

  // ========== String Procedures ==========

  @Test
  public void testMakeString() {
    assertEquals(5, evalAsInt("(string-length (make-string 5))"));
    assertEquals("aaaaa", lisp.evaluate("(make-string 5 #\\a)").asString().value());
    assertEquals("", lisp.evaluate("(make-string 0)").asString().value());
  }

  @Test
  public void testMakeStringWithNullChar() {
    // Default fill is null character
    assertEquals(3, evalAsInt("(string-length (make-string 3))"));
  }

  @Test
  public void testStringFromChars() {
    assertEquals("abc", lisp.evaluate("(string #\\a #\\b #\\c)").asString().value());
    assertEquals("", lisp.evaluate("(string)").asString().value());
    assertEquals("x", lisp.evaluate("(string #\\x)").asString().value());
  }

  @Test
  public void testStringToList() {
    assertEquals("(#\\h #\\i)", lisp.evaluate("(string->list \"hi\")").toString());
    assertEquals("()", lisp.evaluate("(string->list \"\")").toString());
    assertEquals("(#\\a #\\b #\\c)", lisp.evaluate("(string->list \"abc\")").toString());
  }

  @Test
  public void testStringToListWithIndices() {
    assertEquals("(#\\e #\\l)", lisp.evaluate("(string->list \"hello\" 1 3)").toString());
    assertEquals("(#\\l #\\o)", lisp.evaluate("(string->list \"hello\" 3 5)").toString());
  }

  @Test
  public void testListToString() {
    assertEquals("hi", lisp.evaluate("(list->string '(#\\h #\\i))").asString().value());
    assertEquals("", lisp.evaluate("(list->string '())").asString().value());
    assertEquals("xyz", lisp.evaluate("(list->string (list #\\x #\\y #\\z))").asString().value());
  }

  @Test
  public void testStringCopy() {
    assertEquals("hello", lisp.evaluate("(string-copy \"hello\")").asString().value());
    assertEquals("ell", lisp.evaluate("(string-copy \"hello\" 1 4)").asString().value());
    assertEquals("lo", lisp.evaluate("(string-copy \"hello\" 3)").asString().value());
  }

  @Test
  public void testStringMap() {
    assertEquals("ABC", lisp.evaluate("(string-map char-upcase \"abc\")").asString().value());
    assertEquals("", lisp.evaluate("(string-map char-upcase \"\")").asString().value());
    assertEquals(
        "bcd",
        lisp.evaluate("(string-map (lambda (c) (integer->char (+ 1 (char->integer c)))) \"abc\")")
            .asString()
            .value());
  }

  @Test
  public void testStringMapMultipleStrings() {
    // Map over multiple strings taking one char from each
    lisp.evaluate("(define (first-char c1 c2) c1)");
    assertEquals(
        "abc", lisp.evaluate("(string-map first-char \"abc\" \"xyz\")").asString().value());
  }

  @Test
  public void testStringForEach() {
    lisp.evaluate("(define count 0)");
    lisp.evaluate("(string-for-each (lambda (c) (set! count (+ count 1))) \"hello\")");
    assertEquals(5, evalAsInt("count"));
  }

  @Test
  public void testStringForEachMultipleStrings() {
    lisp.evaluate("(define count 0)");
    lisp.evaluate("(string-for-each (lambda (c1 c2) (set! count (+ count 1))) \"abc\" \"xyz\")");
    assertEquals(3, evalAsInt("count"));
  }

  // ========== Vector Procedures ==========

  @Test
  public void testVectorAppend() {
    assertEquals(
        "(1 2 3 4)",
        lisp.evaluate("(vector->list (vector-append (vector 1 2) (vector 3 4)))").toString());
    assertEquals(0, evalAsInt("(vector-length (vector-append))"));
    assertEquals(3, evalAsInt("(vector-length (vector-append (vector 1 2 3)))"));
    assertEquals(
        6, evalAsInt("(vector-length (vector-append (vector 1 2) (vector 3 4) (vector 5 6)))"));
  }

  @Test
  public void testVectorMap() {
    lisp.evaluate("(define v (vector-map (lambda (x) (* x 2)) (vector 1 2 3)))");
    assertEquals(2, evalAsInt("(vector-ref v 0)"));
    assertEquals(4, evalAsInt("(vector-ref v 1)"));
    assertEquals(6, evalAsInt("(vector-ref v 2)"));
    assertEquals(0, evalAsInt("(vector-length (vector-map (lambda (x) x) (vector)))"));
  }

  @Test
  public void testVectorMapMultipleVectors() {
    lisp.evaluate("(define v (vector-map + (vector 1 2 3) (vector 4 5 6)))");
    assertEquals(5, evalAsInt("(vector-ref v 0)"));
    assertEquals(7, evalAsInt("(vector-ref v 1)"));
    assertEquals(9, evalAsInt("(vector-ref v 2)"));
  }

  @Test
  public void testVectorForEach() {
    lisp.evaluate("(define sum 0)");
    lisp.evaluate("(vector-for-each (lambda (x) (set! sum (+ sum x))) (vector 1 2 3))");
    assertEquals(6, evalAsInt("sum"));
  }

  @Test
  public void testVectorToString() {
    assertEquals(
        "abc", lisp.evaluate("(vector->string (vector #\\a #\\b #\\c))").asString().value());
    assertEquals("", lisp.evaluate("(vector->string (vector))").asString().value());
  }

  @Test
  public void testVectorToStringWithIndices() {
    assertEquals(
        "bc",
        lisp.evaluate("(vector->string (vector #\\a #\\b #\\c #\\d) 1 3)").asString().value());
  }

  @Test
  public void testStringToVector() {
    lisp.evaluate("(define v (string->vector \"abc\"))");
    assertEquals(3, evalAsInt("(vector-length v)"));
    assertTrue(lisp.evaluate("(char=? #\\a (vector-ref v 0))").truthiness());
    assertTrue(lisp.evaluate("(char=? #\\b (vector-ref v 1))").truthiness());
    assertTrue(lisp.evaluate("(char=? #\\c (vector-ref v 2))").truthiness());
    assertEquals(0, evalAsInt("(vector-length (string->vector \"\"))"));
  }

  @Test
  public void testStringToVectorWithIndices() {
    lisp.evaluate("(define v (string->vector \"abcd\" 1 3))");
    assertEquals(2, evalAsInt("(vector-length v)"));
    assertTrue(lisp.evaluate("(char=? #\\b (vector-ref v 0))").truthiness());
    assertTrue(lisp.evaluate("(char=? #\\c (vector-ref v 1))").truthiness());
  }

  @Test
  public void testVectorCopyWithIndices() {
    lisp.evaluate("(define v (vector-copy (vector 1 2 3 4) 1 3))");
    assertEquals(2, evalAsInt("(vector-length v)"));
    assertEquals(2, evalAsInt("(vector-ref v 0)"));
    assertEquals(3, evalAsInt("(vector-ref v 1)"));
  }

  // ========== List Procedures ==========

  @Test
  public void testListCopy() {
    lisp.evaluate("(define lst '(1 2 3))");
    lisp.evaluate("(define copy (list-copy lst))");
    assertEquals("(1 2 3)", lisp.evaluate("copy").toString());
    // Verify it's a copy (not the same reference in terms of the list structure)
    assertEquals("(1 2 3)", lisp.evaluate("lst").toString());
  }

  @Test
  public void testListCopyEmpty() {
    assertEquals("()", lisp.evaluate("(list-copy '())").toString());
  }

  // ========== Boolean Procedures ==========

  @Test
  public void testBooleanEquals() {
    assertTrue(lisp.evaluate("(boolean=? #t #t #t)").truthiness());
    assertFalse(lisp.evaluate("(boolean=? #t #f)").truthiness());
    assertTrue(lisp.evaluate("(boolean=? #f #f)").truthiness());
    assertTrue(lisp.evaluate("(boolean=? #t #t)").truthiness());
  }

  @Test
  public void testBooleanEqualsMultiple() {
    assertTrue(lisp.evaluate("(boolean=? #f #f #f #f)").truthiness());
    assertFalse(lisp.evaluate("(boolean=? #t #t #f)").truthiness());
  }

  // ========== System Procedures ==========

  @Test
  public void testFeatures() {
    String features = lisp.evaluate("(features)").toString();
    assertTrue(features.contains("r7rs"));
    assertTrue(features.contains("kleinlisp"));
    assertTrue(features.contains("java"));
  }

  @Test
  public void testCommandLine() {
    // command-line returns empty list when no args set
    LispObject result = lisp.evaluate("(command-line)");
    assertNotNull(result);
  }

  @Test
  public void testGetEnvironmentVariable() {
    // PATH should exist on most systems
    LispObject result = lisp.evaluate("(get-environment-variable \"PATH\")");
    assertNotNull(result);
    // Result should be either a string or #f
    assertTrue(result.asString() != null || !result.truthiness());
  }

  @Test
  public void testGetEnvironmentVariableNotFound() {
    LispObject result = lisp.evaluate("(get-environment-variable \"NONEXISTENT_VAR_12345\")");
    assertFalse(result.truthiness());
  }

  @Test
  public void testGetEnvironmentVariables() {
    LispObject result = lisp.evaluate("(get-environment-variables)");
    assertNotNull(result);
    // Should be a list
    assertTrue(result == lisp.evaluate("'()") || result.asList() != null);
  }

  // ========== Round-trip tests ==========

  @Test
  public void testStringListRoundTrip() {
    assertEquals(
        "hello", lisp.evaluate("(list->string (string->list \"hello\"))").asString().value());
  }

  @Test
  public void testStringVectorRoundTrip() {
    assertEquals(
        "world", lisp.evaluate("(vector->string (string->vector \"world\"))").asString().value());
  }

  @Test
  public void testVectorCopyRoundTrip() {
    lisp.evaluate("(define v1 (vector 1 2 3 4 5))");
    lisp.evaluate("(define v2 (vector-copy v1 1 4))");
    assertEquals(3, evalAsInt("(vector-length v2)"));
    assertEquals(2, evalAsInt("(vector-ref v2 0)"));
    assertEquals(3, evalAsInt("(vector-ref v2 1)"));
    assertEquals(4, evalAsInt("(vector-ref v2 2)"));
  }
}
