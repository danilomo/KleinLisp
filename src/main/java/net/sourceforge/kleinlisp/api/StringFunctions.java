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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.CharObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/** String manipulation functions for KleinLisp. */
public class StringFunctions {

  /** Concatenates multiple strings. (string-append s1 s2 ...) */
  public static LispObject stringAppend(LispObject[] params) {
    StringBuilder sb = new StringBuilder();
    for (LispObject param : params) {
      StringObject str = param.asString();
      if (str == null) {
        throw new LispArgumentError("string-append requires string arguments, got: " + param);
      }
      sb.append(str.value());
    }
    return new StringObject(sb.toString());
  }

  /** Returns the length of a string. (string-length s) */
  public static LispObject stringLength(LispObject[] params) {
    StringObject str = params[0].asString();
    if (str == null) {
      throw new LispArgumentError("string-length requires a string argument");
    }
    return IntObject.valueOf(str.value().length());
  }

  /** Returns the character at the given index. (string-ref s i) */
  public static LispObject stringRef(LispObject[] params) {
    StringObject str = params[0].asString();
    IntObject idx = params[1].asInt();
    if (str == null) {
      throw new LispArgumentError("string-ref requires a string as first argument");
    }
    if (idx == null) {
      throw new LispArgumentError("string-ref requires an integer index");
    }
    int i = idx.value;
    String value = str.value();
    if (i < 0 || i >= value.length()) {
      throw new LispArgumentError(
          "string-ref index out of bounds: " + i + " for string of length " + value.length());
    }
    return new CharObject(value.charAt(i));
  }

  /** Extracts a substring. (substring s start end) */
  public static LispObject substring(LispObject[] params) {
    StringObject str = params[0].asString();
    IntObject start = params[1].asInt();
    IntObject end = params[2].asInt();
    if (str == null) {
      throw new LispArgumentError("substring requires a string as first argument");
    }
    if (start == null || end == null) {
      throw new LispArgumentError("substring requires integer indices");
    }
    String value = str.value();
    int s = start.value;
    int e = end.value;
    if (s < 0 || e > value.length() || s > e) {
      throw new LispArgumentError(
          "substring indices out of bounds: "
              + s
              + " to "
              + e
              + " for string of length "
              + value.length());
    }
    return new StringObject(value.substring(s, e));
  }

  /** Tests string equality. (string=? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringEqual(LispObject[] params) {
    return stringCompare(params, "string=?", (a, b) -> a.equals(b));
  }

  /** Tests case-insensitive string equality. (string-ci=? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringCiEqual(LispObject[] params) {
    return stringCompareCi(params, "string-ci=?", (a, b) -> a.equals(b));
  }

  /** String lexicographic comparison. (string<? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringLessThan(LispObject[] params) {
    return stringCompare(params, "string<?", (a, b) -> a.compareTo(b) < 0);
  }

  /** String lexicographic comparison. (string>? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringGreaterThan(LispObject[] params) {
    return stringCompare(params, "string>?", (a, b) -> a.compareTo(b) > 0);
  }

  /** String lexicographic comparison. (string<=? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringLessOrEqual(LispObject[] params) {
    return stringCompare(params, "string<=?", (a, b) -> a.compareTo(b) <= 0);
  }

  /** String lexicographic comparison. (string>=? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringGreaterOrEqual(LispObject[] params) {
    return stringCompare(params, "string>=?", (a, b) -> a.compareTo(b) >= 0);
  }

  /** Case-insensitive string less-than. (string-ci<? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringCiLessThan(LispObject[] params) {
    return stringCompareCi(params, "string-ci<?", (a, b) -> a.compareTo(b) < 0);
  }

  /** Case-insensitive string greater-than. (string-ci>? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringCiGreaterThan(LispObject[] params) {
    return stringCompareCi(params, "string-ci>?", (a, b) -> a.compareTo(b) > 0);
  }

  /** Case-insensitive string less-or-equal. (string-ci<=? s1 s2 ...) - R7RS compliant, variadic */
  public static LispObject stringCiLessOrEqual(LispObject[] params) {
    return stringCompareCi(params, "string-ci<=?", (a, b) -> a.compareTo(b) <= 0);
  }

  /**
   * Case-insensitive string greater-or-equal. (string-ci>=? s1 s2 ...) - R7RS compliant, variadic
   */
  public static LispObject stringCiGreaterOrEqual(LispObject[] params) {
    return stringCompareCi(params, "string-ci>=?", (a, b) -> a.compareTo(b) >= 0);
  }

  // Helper interface for string comparison
  private interface StringComparator {
    boolean compare(String a, String b);
  }

  // Helper method for variadic case-sensitive string comparison
  private static LispObject stringCompare(LispObject[] args, String name, StringComparator cmp) {
    assertMinArgCount(name, args, 2);
    String prev = asString(name, args[0]);
    for (int i = 1; i < args.length; i++) {
      String curr = asString(name, args[i]);
      if (!cmp.compare(prev, curr)) {
        return BooleanObject.FALSE;
      }
      prev = curr;
    }
    return BooleanObject.TRUE;
  }

  // Helper method for variadic case-insensitive string comparison
  private static LispObject stringCompareCi(LispObject[] args, String name, StringComparator cmp) {
    assertMinArgCount(name, args, 2);
    String prev = asString(name, args[0]).toLowerCase();
    for (int i = 1; i < args.length; i++) {
      String curr = asString(name, args[i]).toLowerCase();
      if (!cmp.compare(prev, curr)) {
        return BooleanObject.FALSE;
      }
      prev = curr;
    }
    return BooleanObject.TRUE;
  }

  // Helper to extract string value
  private static String asString(String name, LispObject obj) {
    StringObject str = obj.asString();
    if (str == null) {
      throw new LispArgumentError(
          name + ": expected string, got " + obj.getClass().getSimpleName());
    }
    return str.value();
  }

  // Helper to assert minimum argument count
  private static void assertMinArgCount(String name, LispObject[] args, int min) {
    if (args.length < min) {
      throw new LispArgumentError(
          name + ": expected at least " + min + " argument(s), got " + args.length);
    }
  }

  /** Converts a number to a string. (number->string n) */
  public static LispObject numberToString(LispObject[] params) {
    IntObject intVal = params[0].asInt();
    if (intVal != null) {
      return new StringObject(String.valueOf(intVal.value));
    }
    DoubleObject doubleVal = params[0].asDouble();
    if (doubleVal != null) {
      return new StringObject(String.valueOf(doubleVal.value));
    }
    throw new LispArgumentError("number->string requires a numeric argument");
  }

  /** Parses a string to a number. (string->number s) */
  public static LispObject stringToNumber(LispObject[] params) {
    StringObject str = params[0].asString();
    if (str == null) {
      throw new LispArgumentError("string->number requires a string argument");
    }
    String value = str.value();
    try {
      // Try parsing as integer first
      if (!value.contains(".") && !value.toLowerCase().contains("e")) {
        return IntObject.valueOf(Integer.parseInt(value));
      }
      // Parse as double
      return new DoubleObject(Double.parseDouble(value));
    } catch (NumberFormatException e) {
      // Return #f if parsing fails (standard Scheme behavior)
      return BooleanObject.FALSE;
    }
  }

  /** Converts a string to uppercase. (string-upcase s) */
  public static LispObject stringUpcase(LispObject[] params) {
    StringObject str = params[0].asString();
    if (str == null) {
      throw new LispArgumentError("string-upcase requires a string argument");
    }
    return new StringObject(str.value().toUpperCase());
  }

  /** Converts a string to lowercase. (string-downcase s) */
  public static LispObject stringDowncase(LispObject[] params) {
    StringObject str = params[0].asString();
    if (str == null) {
      throw new LispArgumentError("string-downcase requires a string argument");
    }
    return new StringObject(str.value().toLowerCase());
  }

  /** Splits a string by a delimiter. (string-split s delimiter) */
  public static LispObject stringSplit(LispObject[] params) {
    StringObject str = params[0].asString();
    StringObject delimiter = params[1].asString();
    if (str == null || delimiter == null) {
      throw new LispArgumentError("string-split requires string arguments");
    }
    String[] parts = str.value().split(java.util.regex.Pattern.quote(delimiter.value()), -1);
    LispObject[] result = new LispObject[parts.length];
    for (int i = 0; i < parts.length; i++) {
      result[i] = new StringObject(parts[i]);
    }
    return ListObject.fromList(result);
  }

  /** Joins a list of strings with a delimiter. (string-join list delimiter) */
  public static LispObject stringJoin(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return new StringObject("");
    }
    ListObject list = params[0].asList();
    StringObject delimiter = params[1].asString();
    if (list == null) {
      throw new LispArgumentError("string-join requires a list as first argument");
    }
    if (delimiter == null) {
      throw new LispArgumentError("string-join requires a string delimiter");
    }
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (LispObject item : list) {
      StringObject str = item.asString();
      if (str == null) {
        throw new LispArgumentError("string-join requires a list of strings");
      }
      if (!first) {
        sb.append(delimiter.value());
      }
      sb.append(str.value());
      first = false;
    }
    return new StringObject(sb.toString());
  }

  /** Trims whitespace from both ends of a string. (string-trim s) */
  public static LispObject stringTrim(LispObject[] params) {
    StringObject str = params[0].asString();
    if (str == null) {
      throw new LispArgumentError("string-trim requires a string argument");
    }
    return new StringObject(str.value().trim());
  }

  /** Checks if a string contains a substring. (string-contains? s substring) */
  public static LispObject stringContains(LispObject[] params) {
    StringObject str = params[0].asString();
    StringObject sub = params[1].asString();
    if (str == null || sub == null) {
      throw new LispArgumentError("string-contains? requires string arguments");
    }
    return str.value().contains(sub.value()) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Checks if a string starts with a prefix. (string-prefix? s prefix) */
  public static LispObject stringPrefix(LispObject[] params) {
    StringObject str = params[0].asString();
    StringObject prefix = params[1].asString();
    if (str == null || prefix == null) {
      throw new LispArgumentError("string-prefix? requires string arguments");
    }
    return str.value().startsWith(prefix.value()) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Checks if a string ends with a suffix. (string-suffix? s suffix) */
  public static LispObject stringSuffix(LispObject[] params) {
    StringObject str = params[0].asString();
    StringObject suffix = params[1].asString();
    if (str == null || suffix == null) {
      throw new LispArgumentError("string-suffix? requires string arguments");
    }
    return str.value().endsWith(suffix.value()) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Replaces all occurrences of a substring. (string-replace s old new) */
  public static LispObject stringReplace(LispObject[] params) {
    StringObject str = params[0].asString();
    StringObject old = params[1].asString();
    StringObject replacement = params[2].asString();
    if (str == null || old == null || replacement == null) {
      throw new LispArgumentError("string-replace requires string arguments");
    }
    return new StringObject(str.value().replace(old.value(), replacement.value()));
  }

  /** Creates a string of k characters. (make-string k) or (make-string k char) */
  public static LispObject makeString(LispObject[] params) {
    if (params.length < 1 || params.length > 2) {
      throw new LispArgumentError("make-string: expected 1 or 2 arguments");
    }
    IntObject kObj = params[0].asInt();
    if (kObj == null) {
      throw new LispArgumentError("make-string: expected integer length");
    }
    int k = kObj.value;
    if (k < 0) {
      throw new LispArgumentError("make-string: negative length");
    }
    char fill = '\0';
    if (params.length > 1) {
      if (!(params[1] instanceof CharObject)) {
        throw new LispArgumentError("make-string: expected character as second argument");
      }
      fill = ((CharObject) params[1]).getValue();
    }
    char[] chars = new char[k];
    Arrays.fill(chars, fill);
    return new StringObject(new String(chars));
  }

  /** Builds a string from characters. (string char ...) */
  public static LispObject string(LispObject[] params) {
    StringBuilder sb = new StringBuilder();
    for (LispObject arg : params) {
      if (!(arg instanceof CharObject)) {
        throw new LispArgumentError("string: expected character arguments");
      }
      sb.append(((CharObject) arg).getValue());
    }
    return new StringObject(sb.toString());
  }

  /** Converts a string to a list of characters. (string->list string) or with start/end */
  public static LispObject stringToList(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("string->list: expected 1-3 arguments");
    }
    StringObject strObj = params[0].asString();
    if (strObj == null) {
      throw new LispArgumentError("string->list: expected string");
    }
    String s = strObj.value();
    int start = 0;
    int end = s.length();

    if (params.length > 1) {
      IntObject startObj = params[1].asInt();
      if (startObj == null) {
        throw new LispArgumentError("string->list: expected integer start index");
      }
      start = startObj.value;
    }
    if (params.length > 2) {
      IntObject endObj = params[2].asInt();
      if (endObj == null) {
        throw new LispArgumentError("string->list: expected integer end index");
      }
      end = endObj.value;
    }

    if (start < 0 || end > s.length() || start > end) {
      throw new LispArgumentError("string->list: index out of bounds");
    }

    List<LispObject> chars = new ArrayList<>();
    for (int i = start; i < end; i++) {
      chars.add(new CharObject(s.charAt(i)));
    }
    return ListObject.fromList(chars.toArray(new LispObject[0]));
  }

  /** Converts a list of characters to a string. (list->string list) */
  public static LispObject listToString(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError("list->string: expected 1 argument");
    }
    if (params[0] == ListObject.NIL) {
      return new StringObject("");
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("list->string: expected list");
    }
    StringBuilder sb = new StringBuilder();
    for (LispObject obj : list) {
      if (!(obj instanceof CharObject)) {
        throw new LispArgumentError("list->string: list must contain only characters");
      }
      sb.append(((CharObject) obj).getValue());
    }
    return new StringObject(sb.toString());
  }

  /** Copies a string or portion of it. (string-copy string) or with start/end */
  public static LispObject stringCopy(LispObject[] params) {
    if (params.length < 1 || params.length > 3) {
      throw new LispArgumentError("string-copy: expected 1-3 arguments");
    }
    StringObject strObj = params[0].asString();
    if (strObj == null) {
      throw new LispArgumentError("string-copy: expected string");
    }
    String s = strObj.value();
    int start = 0;
    int end = s.length();

    if (params.length > 1) {
      IntObject startObj = params[1].asInt();
      if (startObj == null) {
        throw new LispArgumentError("string-copy: expected integer start index");
      }
      start = startObj.value;
    }
    if (params.length > 2) {
      IntObject endObj = params[2].asInt();
      if (endObj == null) {
        throw new LispArgumentError("string-copy: expected integer end index");
      }
      end = endObj.value;
    }

    if (start < 0 || end > s.length() || start > end) {
      throw new LispArgumentError("string-copy: index out of bounds");
    }

    return new StringObject(s.substring(start, end));
  }

  /** Maps a procedure over string characters. (string-map proc string ...) */
  public static LispObject stringMap(LispObject[] params) {
    if (params.length < 2) {
      throw new LispArgumentError("string-map: expected at least 2 arguments");
    }
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("string-map: expected procedure as first argument");
    }
    Function proc = funcObj.function();

    String[] strings = new String[params.length - 1];
    int minLen = Integer.MAX_VALUE;
    for (int i = 0; i < strings.length; i++) {
      StringObject strObj = params[i + 1].asString();
      if (strObj == null) {
        throw new LispArgumentError("string-map: expected string arguments");
      }
      strings[i] = strObj.value();
      minLen = Math.min(minLen, strings[i].length());
    }

    StringBuilder result = new StringBuilder();
    for (int i = 0; i < minLen; i++) {
      LispObject[] chars = new LispObject[strings.length];
      for (int j = 0; j < strings.length; j++) {
        chars[j] = new CharObject(strings[j].charAt(i));
      }
      LispObject r = proc.evaluate(chars);
      if (!(r instanceof CharObject)) {
        throw new LispArgumentError("string-map: procedure must return character");
      }
      result.append(((CharObject) r).getValue());
    }
    return new StringObject(result.toString());
  }

  /** Applies a procedure to each character for side effects. (string-for-each proc string ...) */
  public static LispObject stringForEach(LispObject[] params) {
    if (params.length < 2) {
      throw new LispArgumentError("string-for-each: expected at least 2 arguments");
    }
    FunctionObject funcObj = params[0].asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("string-for-each: expected procedure as first argument");
    }
    Function proc = funcObj.function();

    String[] strings = new String[params.length - 1];
    int minLen = Integer.MAX_VALUE;
    for (int i = 0; i < strings.length; i++) {
      StringObject strObj = params[i + 1].asString();
      if (strObj == null) {
        throw new LispArgumentError("string-for-each: expected string arguments");
      }
      strings[i] = strObj.value();
      minLen = Math.min(minLen, strings[i].length());
    }

    for (int i = 0; i < minLen; i++) {
      LispObject[] chars = new LispObject[strings.length];
      for (int j = 0; j < strings.length; j++) {
        chars[j] = new CharObject(strings[j].charAt(i));
      }
      proc.evaluate(chars);
    }
    return VoidObject.VOID;
  }
}
