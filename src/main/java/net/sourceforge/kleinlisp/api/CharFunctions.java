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
package net.sourceforge.kleinlisp.api;

import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.CharObject;
import net.sourceforge.kleinlisp.objects.IntObject;

/** Character manipulation functions for KleinLisp (R7RS character type). */
public class CharFunctions {

  /** Type predicate. (char? obj) */
  public static LispObject isChar(LispObject[] params) {
    assertArgCount("char?", params, 1);
    return fromBoolean(params[0] instanceof CharObject);
  }

  /** Character equality. (char=? c1 c2 ...) */
  public static LispObject charEqual(LispObject[] params) {
    return charCompare(params, "char=?", (a, b) -> a == b);
  }

  /** Character less-than. (char<? c1 c2 ...) */
  public static LispObject charLessThan(LispObject[] params) {
    return charCompare(params, "char<?", (a, b) -> a < b);
  }

  /** Character greater-than. (char>? c1 c2 ...) */
  public static LispObject charGreaterThan(LispObject[] params) {
    return charCompare(params, "char>?", (a, b) -> a > b);
  }

  /** Character less-than-or-equal. (char<=? c1 c2 ...) */
  public static LispObject charLessOrEqual(LispObject[] params) {
    return charCompare(params, "char<=?", (a, b) -> a <= b);
  }

  /** Character greater-than-or-equal. (char>=? c1 c2 ...) */
  public static LispObject charGreaterOrEqual(LispObject[] params) {
    return charCompare(params, "char>=?", (a, b) -> a >= b);
  }

  /** Case-insensitive character equality. (char-ci=? c1 c2 ...) */
  public static LispObject charCiEqual(LispObject[] params) {
    return charCompareCi(params, "char-ci=?", (a, b) -> a == b);
  }

  /** Case-insensitive character less-than. (char-ci<? c1 c2 ...) */
  public static LispObject charCiLessThan(LispObject[] params) {
    return charCompareCi(params, "char-ci<?", (a, b) -> a < b);
  }

  /** Case-insensitive character greater-than. (char-ci>? c1 c2 ...) */
  public static LispObject charCiGreaterThan(LispObject[] params) {
    return charCompareCi(params, "char-ci>?", (a, b) -> a > b);
  }

  /** Case-insensitive character less-than-or-equal. (char-ci<=? c1 c2 ...) */
  public static LispObject charCiLessOrEqual(LispObject[] params) {
    return charCompareCi(params, "char-ci<=?", (a, b) -> a <= b);
  }

  /** Case-insensitive character greater-than-or-equal. (char-ci>=? c1 c2 ...) */
  public static LispObject charCiGreaterOrEqual(LispObject[] params) {
    return charCompareCi(params, "char-ci>=?", (a, b) -> a >= b);
  }

  /** Tests if character is alphabetic. (char-alphabetic? c) */
  public static LispObject charAlphabetic(LispObject[] params) {
    assertArgCount("char-alphabetic?", params, 1);
    char c = asChar("char-alphabetic?", params[0]);
    return fromBoolean(Character.isLetter(c));
  }

  /** Tests if character is numeric. (char-numeric? c) */
  public static LispObject charNumeric(LispObject[] params) {
    assertArgCount("char-numeric?", params, 1);
    char c = asChar("char-numeric?", params[0]);
    return fromBoolean(Character.isDigit(c));
  }

  /** Tests if character is whitespace. (char-whitespace? c) */
  public static LispObject charWhitespace(LispObject[] params) {
    assertArgCount("char-whitespace?", params, 1);
    char c = asChar("char-whitespace?", params[0]);
    return fromBoolean(Character.isWhitespace(c));
  }

  /** Tests if character is uppercase. (char-upper-case? c) */
  public static LispObject charUpperCase(LispObject[] params) {
    assertArgCount("char-upper-case?", params, 1);
    char c = asChar("char-upper-case?", params[0]);
    return fromBoolean(Character.isUpperCase(c));
  }

  /** Tests if character is lowercase. (char-lower-case? c) */
  public static LispObject charLowerCase(LispObject[] params) {
    assertArgCount("char-lower-case?", params, 1);
    char c = asChar("char-lower-case?", params[0]);
    return fromBoolean(Character.isLowerCase(c));
  }

  /** Converts character to integer. (char->integer c) */
  public static LispObject charToInteger(LispObject[] params) {
    assertArgCount("char->integer", params, 1);
    char c = asChar("char->integer", params[0]);
    return IntObject.valueOf((int) c);
  }

  /** Converts integer to character. (integer->char n) */
  public static LispObject integerToChar(LispObject[] params) {
    assertArgCount("integer->char", params, 1);
    int i = asInt("integer->char", params[0]);
    if (i < 0 || i > Character.MAX_VALUE) {
      throw new LispArgumentError("integer->char: value out of range: " + i);
    }
    return new CharObject((char) i);
  }

  /** Converts character to uppercase. (char-upcase c) */
  public static LispObject charUpcase(LispObject[] params) {
    assertArgCount("char-upcase", params, 1);
    char c = asChar("char-upcase", params[0]);
    return new CharObject(Character.toUpperCase(c));
  }

  /** Converts character to lowercase. (char-downcase c) */
  public static LispObject charDowncase(LispObject[] params) {
    assertArgCount("char-downcase", params, 1);
    char c = asChar("char-downcase", params[0]);
    return new CharObject(Character.toLowerCase(c));
  }

  /** Returns the numeric value of a digit character, or #f if not a digit. (digit-value c) */
  public static LispObject digitValue(LispObject[] params) {
    assertArgCount("digit-value", params, 1);
    char c = asChar("digit-value", params[0]);
    int digit = Character.digit(c, 10);
    if (digit == -1) {
      return BooleanObject.FALSE;
    }
    return IntObject.valueOf(digit);
  }

  // Helper interface for character comparison
  private interface CharComparator {
    boolean compare(char a, char b);
  }

  // Helper method for character comparison
  private static LispObject charCompare(LispObject[] args, String name, CharComparator cmp) {
    assertMinArgCount(name, args, 2);
    char prev = asChar(name, args[0]);
    for (int i = 1; i < args.length; i++) {
      char curr = asChar(name, args[i]);
      if (!cmp.compare(prev, curr)) {
        return BooleanObject.FALSE;
      }
      prev = curr;
    }
    return BooleanObject.TRUE;
  }

  // Helper method for case-insensitive character comparison
  private static LispObject charCompareCi(LispObject[] args, String name, CharComparator cmp) {
    assertMinArgCount(name, args, 2);
    char prev = Character.toLowerCase(asChar(name, args[0]));
    for (int i = 1; i < args.length; i++) {
      char curr = Character.toLowerCase(asChar(name, args[i]));
      if (!cmp.compare(prev, curr)) {
        return BooleanObject.FALSE;
      }
      prev = curr;
    }
    return BooleanObject.TRUE;
  }

  // Helper to extract char from LispObject
  private static char asChar(String name, LispObject obj) {
    if (obj instanceof CharObject) {
      return ((CharObject) obj).getValue();
    }
    throw new LispArgumentError(name + ": expected character, got " + obj.getClass().getSimpleName());
  }

  // Helper to extract int from LispObject
  private static int asInt(String name, LispObject obj) {
    if (obj.asInt() != null) {
      return obj.asInt().value;
    }
    throw new LispArgumentError(name + ": expected integer, got " + obj.getClass().getSimpleName());
  }

  // Helper to assert exact argument count
  private static void assertArgCount(String name, LispObject[] args, int expected) {
    if (args.length != expected) {
      throw new LispArgumentError(
          name + ": expected " + expected + " argument(s), got " + args.length);
    }
  }

  // Helper to assert minimum argument count
  private static void assertMinArgCount(String name, LispObject[] args, int min) {
    if (args.length < min) {
      throw new LispArgumentError(
          name + ": expected at least " + min + " argument(s), got " + args.length);
    }
  }

  // Helper to convert boolean to BooleanObject
  private static BooleanObject fromBoolean(boolean value) {
    return value ? BooleanObject.TRUE : BooleanObject.FALSE;
  }
}
