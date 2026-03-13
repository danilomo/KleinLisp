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
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;

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

  /** Returns the character at the given index as a single-character string. (string-ref s i) */
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
    return new StringObject(String.valueOf(value.charAt(i)));
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

  /** Tests string equality. (string=? s1 s2) */
  public static LispObject stringEqual(LispObject[] params) {
    StringObject s1 = params[0].asString();
    StringObject s2 = params[1].asString();
    if (s1 == null || s2 == null) {
      throw new LispArgumentError("string=? requires string arguments");
    }
    return s1.value().equals(s2.value()) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** String lexicographic comparison. (string<? s1 s2) */
  public static LispObject stringLessThan(LispObject[] params) {
    StringObject s1 = params[0].asString();
    StringObject s2 = params[1].asString();
    if (s1 == null || s2 == null) {
      throw new LispArgumentError("string<? requires string arguments");
    }
    return s1.value().compareTo(s2.value()) < 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** String lexicographic comparison. (string>? s1 s2) */
  public static LispObject stringGreaterThan(LispObject[] params) {
    StringObject s1 = params[0].asString();
    StringObject s2 = params[1].asString();
    if (s1 == null || s2 == null) {
      throw new LispArgumentError("string>? requires string arguments");
    }
    return s1.value().compareTo(s2.value()) > 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** String lexicographic comparison. (string<=? s1 s2) */
  public static LispObject stringLessOrEqual(LispObject[] params) {
    StringObject s1 = params[0].asString();
    StringObject s2 = params[1].asString();
    if (s1 == null || s2 == null) {
      throw new LispArgumentError("string<=? requires string arguments");
    }
    return s1.value().compareTo(s2.value()) <= 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** String lexicographic comparison. (string>=? s1 s2) */
  public static LispObject stringGreaterOrEqual(LispObject[] params) {
    StringObject s1 = params[0].asString();
    StringObject s2 = params[1].asString();
    if (s1 == null || s2 == null) {
      throw new LispArgumentError("string>=? requires string arguments");
    }
    return s1.value().compareTo(s2.value()) >= 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
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
}
