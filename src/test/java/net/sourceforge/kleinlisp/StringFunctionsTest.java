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

/** Tests for string manipulation functions. */
public class StringFunctionsTest extends BaseTestClass {

  @Test
  public void testStringAppend() {
    assertEquals("hello", lisp.evaluate("(string-append \"hello\")").asString().value());
    assertEquals(
        "helloworld", lisp.evaluate("(string-append \"hello\" \"world\")").asString().value());
    assertEquals("abc", lisp.evaluate("(string-append \"a\" \"b\" \"c\")").asString().value());
    assertEquals("", lisp.evaluate("(string-append)").asString().value());
  }

  @Test
  public void testStringLength() {
    assertEquals(5, lisp.evaluate("(string-length \"hello\")").asInt().value);
    assertEquals(0, lisp.evaluate("(string-length \"\")").asInt().value);
    assertEquals(11, lisp.evaluate("(string-length \"hello world\")").asInt().value);
  }

  @Test
  public void testStringRef() {
    // string-ref returns a character object, not a string
    assertEquals("#\\h", lisp.evaluate("(string-ref \"hello\" 0)").toString());
    assertEquals("#\\e", lisp.evaluate("(string-ref \"hello\" 1)").toString());
    assertEquals("#\\o", lisp.evaluate("(string-ref \"hello\" 4)").toString());
  }

  @Test
  public void testSubstring() {
    assertEquals("ell", lisp.evaluate("(substring \"hello\" 1 4)").asString().value());
    assertEquals("hello", lisp.evaluate("(substring \"hello\" 0 5)").asString().value());
    assertEquals("", lisp.evaluate("(substring \"hello\" 2 2)").asString().value());
  }

  @Test
  public void testStringEqual() {
    assertTrue(lisp.evaluate("(string=? \"hello\" \"hello\")").truthiness());
    assertFalse(lisp.evaluate("(string=? \"hello\" \"world\")").truthiness());
    assertFalse(lisp.evaluate("(string=? \"Hello\" \"hello\")").truthiness());
  }

  @Test
  public void testStringLessThan() {
    assertTrue(lisp.evaluate("(string<? \"apple\" \"banana\")").truthiness());
    assertFalse(lisp.evaluate("(string<? \"banana\" \"apple\")").truthiness());
    assertFalse(lisp.evaluate("(string<? \"apple\" \"apple\")").truthiness());
  }

  @Test
  public void testStringGreaterThan() {
    assertFalse(lisp.evaluate("(string>? \"apple\" \"banana\")").truthiness());
    assertTrue(lisp.evaluate("(string>? \"banana\" \"apple\")").truthiness());
  }

  @Test
  public void testStringComparisons() {
    assertTrue(lisp.evaluate("(string<=? \"apple\" \"apple\")").truthiness());
    assertTrue(lisp.evaluate("(string<=? \"apple\" \"banana\")").truthiness());
    assertTrue(lisp.evaluate("(string>=? \"banana\" \"apple\")").truthiness());
    assertTrue(lisp.evaluate("(string>=? \"apple\" \"apple\")").truthiness());
  }

  @Test
  public void testNumberToString() {
    assertEquals("42", lisp.evaluate("(number->string 42)").asString().value());
    assertEquals("-10", lisp.evaluate("(number->string -10)").asString().value());
    assertEquals("0", lisp.evaluate("(number->string 0)").asString().value());
  }

  @Test
  public void testStringToNumber() {
    assertEquals(42, lisp.evaluate("(string->number \"42\")").asInt().value);
    assertEquals(-10, lisp.evaluate("(string->number \"-10\")").asInt().value);
    assertFalse(lisp.evaluate("(string->number \"not-a-number\")").truthiness());
  }

  @Test
  public void testStringUpcase() {
    assertEquals("HELLO", lisp.evaluate("(string-upcase \"hello\")").asString().value());
    assertEquals(
        "HELLO WORLD", lisp.evaluate("(string-upcase \"Hello World\")").asString().value());
  }

  @Test
  public void testStringDowncase() {
    assertEquals("hello", lisp.evaluate("(string-downcase \"HELLO\")").asString().value());
    assertEquals(
        "hello world", lisp.evaluate("(string-downcase \"Hello World\")").asString().value());
  }

  @Test
  public void testStringSplit() {
    LispObject result = lisp.evaluate("(string-split \"a,b,c\" \",\")");
    assertEquals(3, result.asList().length());
    assertEquals("a", result.asList().car().asString().value());
  }

  @Test
  public void testStringJoin() {
    assertEquals(
        "a,b,c", lisp.evaluate("(string-join (list \"a\" \"b\" \"c\") \",\")").asString().value());
    assertEquals(
        "abc", lisp.evaluate("(string-join (list \"a\" \"b\" \"c\") \"\")").asString().value());
  }

  @Test
  public void testStringTrim() {
    assertEquals("hello", lisp.evaluate("(string-trim \"  hello  \")").asString().value());
    assertEquals(
        "hello world", lisp.evaluate("(string-trim \"  hello world  \")").asString().value());
  }

  @Test
  public void testStringContains() {
    assertTrue(lisp.evaluate("(string-contains? \"hello world\" \"world\")").truthiness());
    assertFalse(lisp.evaluate("(string-contains? \"hello\" \"world\")").truthiness());
  }

  @Test
  public void testStringPrefix() {
    assertTrue(lisp.evaluate("(string-prefix? \"hello world\" \"hello\")").truthiness());
    assertFalse(lisp.evaluate("(string-prefix? \"hello world\" \"world\")").truthiness());
  }

  @Test
  public void testStringSuffix() {
    assertTrue(lisp.evaluate("(string-suffix? \"hello world\" \"world\")").truthiness());
    assertFalse(lisp.evaluate("(string-suffix? \"hello world\" \"hello\")").truthiness());
  }

  @Test
  public void testStringReplace() {
    assertEquals(
        "hello universe",
        lisp.evaluate("(string-replace \"hello world\" \"world\" \"universe\")")
            .asString()
            .value());
    assertEquals(
        "helloworld",
        lisp.evaluate("(string-replace \"hello world\" \" \" \"\")").asString().value());
  }
}
