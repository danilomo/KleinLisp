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

/** Tests for the R7RS character type implementation. */
public class CharacterTest extends BaseTestClass {

  // Literal parsing tests

  @Test
  public void testCharLiteral() {
    assertEquals("#\\a", lisp.evaluate("#\\a").toString());
  }

  @Test
  public void testCharLiteralUppercase() {
    assertEquals("#\\A", lisp.evaluate("#\\A").toString());
  }

  @Test
  public void testCharLiteralSpace() {
    assertEquals("#\\space", lisp.evaluate("#\\space").toString());
  }

  @Test
  public void testCharLiteralNewline() {
    assertEquals("#\\newline", lisp.evaluate("#\\newline").toString());
  }

  @Test
  public void testCharLiteralTab() {
    assertEquals("#\\tab", lisp.evaluate("#\\tab").toString());
  }

  @Test
  public void testCharLiteralHex() {
    assertEquals("#\\A", lisp.evaluate("#\\x41").toString());
  }

  @Test
  public void testCharLiteralHexLowercase() {
    assertEquals("#\\a", lisp.evaluate("#\\x61").toString());
  }

  // Type predicate tests

  @Test
  public void testCharPredicateTrue() {
    assertEquals("true", lisp.evaluate("(char? #\\a)").toString());
  }

  @Test
  public void testCharPredicateOnString() {
    assertEquals("false", lisp.evaluate("(char? \"a\")").toString());
  }

  @Test
  public void testCharPredicateOnInteger() {
    assertEquals("false", lisp.evaluate("(char? 97)").toString());
  }

  @Test
  public void testCharPredicateOnList() {
    assertEquals("false", lisp.evaluate("(char? '(a b c))").toString());
  }

  // Character equality tests

  @Test
  public void testCharEqualSame() {
    assertEquals("true", lisp.evaluate("(char=? #\\a #\\a)").toString());
  }

  @Test
  public void testCharEqualDifferent() {
    assertEquals("false", lisp.evaluate("(char=? #\\a #\\b)").toString());
  }

  @Test
  public void testCharEqualMultiple() {
    assertEquals("true", lisp.evaluate("(char=? #\\a #\\a #\\a)").toString());
  }

  @Test
  public void testCharEqualMultipleFail() {
    assertEquals("false", lisp.evaluate("(char=? #\\a #\\a #\\b)").toString());
  }

  // Character comparison tests

  @Test
  public void testCharLessThan() {
    assertEquals("true", lisp.evaluate("(char<? #\\a #\\b)").toString());
  }

  @Test
  public void testCharLessThanFail() {
    assertEquals("false", lisp.evaluate("(char<? #\\b #\\a)").toString());
  }

  @Test
  public void testCharLessThanChain() {
    assertEquals("true", lisp.evaluate("(char<? #\\a #\\b #\\c)").toString());
  }

  @Test
  public void testCharGreaterThan() {
    assertEquals("true", lisp.evaluate("(char>? #\\b #\\a)").toString());
  }

  @Test
  public void testCharLessOrEqual() {
    assertEquals("true", lisp.evaluate("(char<=? #\\a #\\a)").toString());
    assertEquals("true", lisp.evaluate("(char<=? #\\a #\\b)").toString());
  }

  @Test
  public void testCharGreaterOrEqual() {
    assertEquals("true", lisp.evaluate("(char>=? #\\b #\\b)").toString());
    assertEquals("true", lisp.evaluate("(char>=? #\\b #\\a)").toString());
  }

  // Case-insensitive comparison tests

  @Test
  public void testCharCiEqual() {
    assertEquals("true", lisp.evaluate("(char-ci=? #\\a #\\A)").toString());
  }

  @Test
  public void testCharCiLessThan() {
    assertEquals("true", lisp.evaluate("(char-ci<? #\\a #\\B)").toString());
  }

  @Test
  public void testCharCiGreaterThan() {
    assertEquals("true", lisp.evaluate("(char-ci>? #\\B #\\a)").toString());
  }

  // Character predicates tests

  @Test
  public void testCharAlphabeticTrue() {
    assertEquals("true", lisp.evaluate("(char-alphabetic? #\\a)").toString());
    assertEquals("true", lisp.evaluate("(char-alphabetic? #\\Z)").toString());
  }

  @Test
  public void testCharAlphabeticFalse() {
    assertEquals("false", lisp.evaluate("(char-alphabetic? #\\1)").toString());
    assertEquals("false", lisp.evaluate("(char-alphabetic? #\\space)").toString());
  }

  @Test
  public void testCharNumericTrue() {
    assertEquals("true", lisp.evaluate("(char-numeric? #\\5)").toString());
    assertEquals("true", lisp.evaluate("(char-numeric? #\\0)").toString());
  }

  @Test
  public void testCharNumericFalse() {
    assertEquals("false", lisp.evaluate("(char-numeric? #\\a)").toString());
  }

  @Test
  public void testCharWhitespaceTrue() {
    assertEquals("true", lisp.evaluate("(char-whitespace? #\\space)").toString());
    assertEquals("true", lisp.evaluate("(char-whitespace? #\\newline)").toString());
    assertEquals("true", lisp.evaluate("(char-whitespace? #\\tab)").toString());
  }

  @Test
  public void testCharWhitespaceFalse() {
    assertEquals("false", lisp.evaluate("(char-whitespace? #\\a)").toString());
  }

  @Test
  public void testCharUpperCase() {
    assertEquals("true", lisp.evaluate("(char-upper-case? #\\A)").toString());
    assertEquals("false", lisp.evaluate("(char-upper-case? #\\a)").toString());
    assertEquals("false", lisp.evaluate("(char-upper-case? #\\1)").toString());
  }

  @Test
  public void testCharLowerCase() {
    assertEquals("true", lisp.evaluate("(char-lower-case? #\\a)").toString());
    assertEquals("false", lisp.evaluate("(char-lower-case? #\\A)").toString());
    assertEquals("false", lisp.evaluate("(char-lower-case? #\\1)").toString());
  }

  // Conversion tests

  @Test
  public void testCharToInteger() {
    assertEquals("97", lisp.evaluate("(char->integer #\\a)").toString());
    assertEquals("65", lisp.evaluate("(char->integer #\\A)").toString());
    assertEquals("32", lisp.evaluate("(char->integer #\\space)").toString());
  }

  @Test
  public void testIntegerToChar() {
    assertEquals("#\\a", lisp.evaluate("(integer->char 97)").toString());
    assertEquals("#\\A", lisp.evaluate("(integer->char 65)").toString());
    assertEquals("#\\space", lisp.evaluate("(integer->char 32)").toString());
  }

  @Test
  public void testCharUpcase() {
    assertEquals("#\\A", lisp.evaluate("(char-upcase #\\a)").toString());
    assertEquals("#\\A", lisp.evaluate("(char-upcase #\\A)").toString());
  }

  @Test
  public void testCharDowncase() {
    assertEquals("#\\a", lisp.evaluate("(char-downcase #\\A)").toString());
    assertEquals("#\\a", lisp.evaluate("(char-downcase #\\a)").toString());
  }

  @Test
  public void testCharFoldcase() {
    assertEquals("#\\a", lisp.evaluate("(char-foldcase #\\A)").toString());
    assertEquals("#\\a", lisp.evaluate("(char-foldcase #\\a)").toString());
    assertEquals("#\\z", lisp.evaluate("(char-foldcase #\\Z)").toString());
  }

  @Test
  public void testDigitValueSuccess() {
    assertEquals("5", lisp.evaluate("(digit-value #\\5)").toString());
    assertEquals("0", lisp.evaluate("(digit-value #\\0)").toString());
    assertEquals("9", lisp.evaluate("(digit-value #\\9)").toString());
  }

  @Test
  public void testDigitValueFail() {
    assertEquals("false", lisp.evaluate("(digit-value #\\a)").toString());
    assertEquals("false", lisp.evaluate("(digit-value #\\space)").toString());
  }

  // string-ref returns char tests

  @Test
  public void testStringRefReturnsChar() {
    assertEquals("#\\e", lisp.evaluate("(string-ref \"hello\" 1)").toString());
    assertEquals("#\\h", lisp.evaluate("(string-ref \"hello\" 0)").toString());
  }

  @Test
  public void testStringRefIsChar() {
    assertEquals("true", lisp.evaluate("(char? (string-ref \"hello\" 0))").toString());
  }

  // Round-trip tests

  @Test
  public void testCharIntegerRoundTrip() {
    assertEquals("#\\a", lisp.evaluate("(integer->char (char->integer #\\a))").toString());
  }

  @Test
  public void testCharCaseRoundTrip() {
    assertEquals("#\\a", lisp.evaluate("(char-downcase (char-upcase #\\a))").toString());
  }

  // Character in list context

  @Test
  public void testCharInList() {
    assertEquals("(#\\a #\\b #\\c)", lisp.evaluate("(list #\\a #\\b #\\c)").toString());
  }

  @Test
  public void testCharInQuote() {
    assertEquals("#\\a", lisp.evaluate("'#\\a").toString());
  }

  // Edge cases

  @Test
  public void testCharDigit() {
    assertEquals("#\\0", lisp.evaluate("#\\0").toString());
    assertEquals("#\\9", lisp.evaluate("#\\9").toString());
  }

  @Test
  public void testCharSpecialChars() {
    assertEquals("#\\!", lisp.evaluate("#\\!").toString());
    assertEquals("#\\@", lisp.evaluate("#\\@").toString());
  }
}
