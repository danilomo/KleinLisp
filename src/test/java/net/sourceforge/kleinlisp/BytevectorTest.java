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

/** Unit tests for R7RS bytevector functions. */
public class BytevectorTest extends BaseTestClass {

  @Test
  public void testMakeBytevector() {
    assertEquals("#u8(0 0 0 0 0)", lisp.evaluate("(make-bytevector 5)").toString());
  }

  @Test
  public void testMakeBytevectorWithFill() {
    assertEquals("#u8(255 255 255)", lisp.evaluate("(make-bytevector 3 255)").toString());
  }

  @Test
  public void testMakeBytevectorEmpty() {
    assertEquals("#u8()", lisp.evaluate("(make-bytevector 0)").toString());
  }

  @Test
  public void testBytevector() {
    assertEquals("#u8(1 2 3 4 5)", lisp.evaluate("(bytevector 1 2 3 4 5)").toString());
  }

  @Test
  public void testBytevectorEmpty() {
    assertEquals("#u8()", lisp.evaluate("(bytevector)").toString());
  }

  @Test
  public void testBytevectorPredicate() {
    assertEquals("true", lisp.evaluate("(bytevector? (bytevector 1 2 3))").toString());
    assertEquals("false", lisp.evaluate("(bytevector? '(1 2 3))").toString());
    assertEquals("false", lisp.evaluate("(bytevector? \"hello\")").toString());
    assertEquals("false", lisp.evaluate("(bytevector? 42)").toString());
  }

  @Test
  public void testBytevectorLength() {
    assertEquals("5", lisp.evaluate("(bytevector-length (bytevector 1 2 3 4 5))").toString());
    assertEquals("0", lisp.evaluate("(bytevector-length (bytevector))").toString());
  }

  @Test
  public void testBytevectorRef() {
    lisp.evaluate("(define bv (bytevector 10 20 30))");
    assertEquals("10", lisp.evaluate("(bytevector-u8-ref bv 0)").toString());
    assertEquals("20", lisp.evaluate("(bytevector-u8-ref bv 1)").toString());
    assertEquals("30", lisp.evaluate("(bytevector-u8-ref bv 2)").toString());
  }

  @Test
  public void testBytevectorSet() {
    lisp.evaluate("(define bv (make-bytevector 3 0))");
    lisp.evaluate("(bytevector-u8-set! bv 1 255)");
    assertEquals("#u8(0 255 0)", lisp.evaluate("bv").toString());
  }

  @Test
  public void testBytevectorCopy() {
    lisp.evaluate("(define bv (bytevector 1 2 3 4 5))");
    assertEquals("#u8(1 2 3 4 5)", lisp.evaluate("(bytevector-copy bv)").toString());
    assertEquals("#u8(2 3 4 5)", lisp.evaluate("(bytevector-copy bv 1)").toString());
    assertEquals("#u8(2 3)", lisp.evaluate("(bytevector-copy bv 1 3)").toString());
  }

  @Test
  public void testBytevectorCopyIndependent() {
    lisp.evaluate("(define bv (bytevector 1 2 3))");
    lisp.evaluate("(define copy (bytevector-copy bv))");
    lisp.evaluate("(bytevector-u8-set! copy 0 99)");
    assertEquals("#u8(1 2 3)", lisp.evaluate("bv").toString());
    assertEquals("#u8(99 2 3)", lisp.evaluate("copy").toString());
  }

  @Test
  public void testBytevectorCopyMutate() {
    lisp.evaluate("(define src (bytevector 1 2 3))");
    lisp.evaluate("(define dst (make-bytevector 5 0))");
    lisp.evaluate("(bytevector-copy! dst 1 src)");
    assertEquals("#u8(0 1 2 3 0)", lisp.evaluate("dst").toString());
  }

  @Test
  public void testBytevectorCopyMutateWithRange() {
    lisp.evaluate("(define src (bytevector 1 2 3 4 5))");
    lisp.evaluate("(define dst (make-bytevector 5 0))");
    lisp.evaluate("(bytevector-copy! dst 1 src 1 3)");
    assertEquals("#u8(0 2 3 0 0)", lisp.evaluate("dst").toString());
  }

  @Test
  public void testBytevectorAppend() {
    assertEquals(
        "#u8(1 2 3 4 5 6)",
        lisp.evaluate("(bytevector-append (bytevector 1 2) (bytevector 3 4) (bytevector 5 6))")
            .toString());
  }

  @Test
  public void testBytevectorAppendEmpty() {
    assertEquals("#u8()", lisp.evaluate("(bytevector-append)").toString());
  }

  @Test
  public void testBytevectorAppendSingle() {
    assertEquals("#u8(1 2 3)", lisp.evaluate("(bytevector-append (bytevector 1 2 3))").toString());
  }

  @Test
  public void testStringToUtf8() {
    assertEquals("#u8(104 101 108 108 111)", lisp.evaluate("(string->utf8 \"hello\")").toString());
  }

  @Test
  public void testUtf8ToString() {
    assertEquals(
        "\"hello\"", lisp.evaluate("(utf8->string (bytevector 104 101 108 108 111))").toString());
  }

  @Test
  public void testStringToUtf8Partial() {
    assertEquals("#u8(101 108)", lisp.evaluate("(string->utf8 \"hello\" 1 3)").toString());
  }

  @Test
  public void testUtf8ToStringPartial() {
    lisp.evaluate("(define bv (bytevector 104 101 108 108 111))");
    assertEquals("\"ell\"", lisp.evaluate("(utf8->string bv 1 4)").toString());
  }

  @Test
  public void testStringToUtf8WithStart() {
    assertEquals("#u8(108 108 111)", lisp.evaluate("(string->utf8 \"hello\" 2)").toString());
  }

  @Test
  public void testUtf8ToStringWithStart() {
    lisp.evaluate("(define bv (bytevector 104 101 108 108 111))");
    assertEquals("\"llo\"", lisp.evaluate("(utf8->string bv 2)").toString());
  }

  @Test
  public void testBytevectorUnicode() {
    // UTF-8 encoding of e-acute (U+00E9) is 2 bytes: 0xC3 0xA9
    lisp.evaluate("(define bv (string->utf8 \"\u00e9\"))");
    assertEquals("2", lisp.evaluate("(bytevector-length bv)").toString());
  }

  @Test
  public void testBytevectorEmoji() {
    // UTF-8 encoding of a simple emoji (like a smiley) is 4 bytes
    lisp.evaluate("(define bv (string->utf8 \"\ud83d\ude00\"))");
    assertEquals("4", lisp.evaluate("(bytevector-length bv)").toString());
  }

  @Test
  public void testBytevectorValueRange() {
    // Test that values outside 0-255 are rejected
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector 256)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector -1)"));
  }

  @Test
  public void testBytevectorSetRange() {
    lisp.evaluate("(define bv (make-bytevector 3 0))");
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector-u8-set! bv 0 256)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector-u8-set! bv 0 -1)"));
  }

  @Test
  public void testBytevectorIndexOutOfBounds() {
    lisp.evaluate("(define bv (bytevector 1 2 3))");
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector-u8-ref bv 3)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector-u8-ref bv -1)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector-u8-set! bv 3 0)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(bytevector-u8-set! bv -1 0)"));
  }

  @Test
  public void testMakeBytevectorFillRange() {
    assertThrows(Exception.class, () -> lisp.evaluate("(make-bytevector 3 256)"));
    assertThrows(Exception.class, () -> lisp.evaluate("(make-bytevector 3 -1)"));
  }

  @Test
  public void testMakeBytevectorNegativeSize() {
    assertThrows(Exception.class, () -> lisp.evaluate("(make-bytevector -1)"));
  }

  @Test
  public void testBytevectorMaxValue() {
    lisp.evaluate("(define bv (bytevector 255))");
    assertEquals("255", lisp.evaluate("(bytevector-u8-ref bv 0)").toString());
  }

  @Test
  public void testBytevectorCopyEmptyRange() {
    lisp.evaluate("(define bv (bytevector 1 2 3))");
    assertEquals("#u8()", lisp.evaluate("(bytevector-copy bv 1 1)").toString());
  }

  @Test
  public void testBytevectorRoundTrip() {
    lisp.evaluate("(define original \"Hello, World!\")");
    lisp.evaluate("(define bv (string->utf8 original))");
    lisp.evaluate("(define result (utf8->string bv))");
    assertEquals("true", lisp.evaluate("(string=? original result)").toString());
  }
}
