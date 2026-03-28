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
import net.sourceforge.kleinlisp.objects.JavaObject;

public class BooleanFunctions {

  /** Numeric less-than. (< n1 n2 ...) - R7RS compliant, variadic */
  public static LispObject lt(LispObject[] params) {
    return numericCompare(params, "<", (a, b) -> a < b);
  }

  /** Numeric less-than-or-equal. (<= n1 n2 ...) - R7RS compliant, variadic */
  public static LispObject le(LispObject[] params) {
    return numericCompare(params, "<=", (a, b) -> a <= b);
  }

  /** Numeric greater-than. (> n1 n2 ...) - R7RS compliant, variadic */
  public static LispObject gt(LispObject[] params) {
    return numericCompare(params, ">", (a, b) -> a > b);
  }

  /** Numeric greater-than-or-equal. (>= n1 n2 ...) - R7RS compliant, variadic */
  public static LispObject ge(LispObject[] params) {
    return numericCompare(params, ">=", (a, b) -> a >= b);
  }

  /** Numeric equality. (= n1 n2 ...) - R7RS compliant, variadic */
  public static LispObject eq(LispObject[] params) {
    return numericCompare(params, "=", (a, b) -> a == b);
  }

  /** Numeric inequality. (!= n1 n2) - Non-standard extension */
  public static LispObject neq(LispObject[] params) {
    return numericCompare(params, "!=", (a, b) -> a != b);
  }

  // Helper interface for numeric comparison
  private interface NumericComparator {
    boolean compare(double a, double b);
  }

  // Helper interface for general comparison (used with Comparable)
  private interface IntComparator {
    boolean compare(int result);
  }

  // Helper method for variadic numeric comparison
  // Also supports JavaObjects wrapping Comparable objects
  private static LispObject numericCompare(LispObject[] args, String name, NumericComparator cmp) {
    assertMinArgCount(name, args, 2);

    // Check if we're dealing with JavaObjects wrapping Comparable
    if (args[0] instanceof JavaObject && ((JavaObject) args[0]).isComparable()) {
      return comparableCompare(
          args,
          name,
          result -> {
            if (cmp == (NumericComparator) ((a, b) -> a < b) || name.equals("<")) {
              return result < 0;
            } else if (cmp == (NumericComparator) ((a, b) -> a <= b) || name.equals("<=")) {
              return result <= 0;
            } else if (cmp == (NumericComparator) ((a, b) -> a > b) || name.equals(">")) {
              return result > 0;
            } else if (cmp == (NumericComparator) ((a, b) -> a >= b) || name.equals(">=")) {
              return result >= 0;
            } else if (cmp == (NumericComparator) ((a, b) -> a == b) || name.equals("=")) {
              return result == 0;
            } else if (name.equals("!=")) {
              return result != 0;
            }
            return false;
          });
    }

    // Standard numeric comparison
    double prev = asNumber(name, args[0]);
    for (int i = 1; i < args.length; i++) {
      double curr = asNumber(name, args[i]);
      if (!cmp.compare(prev, curr)) {
        return BooleanObject.FALSE;
      }
      prev = curr;
    }
    return BooleanObject.TRUE;
  }

  // Helper method for comparing JavaObjects wrapping Comparable
  @SuppressWarnings("unchecked")
  private static LispObject comparableCompare(LispObject[] args, String name, IntComparator cmp) {
    JavaObject prev = asComparableJavaObject(name, args[0]);
    for (int i = 1; i < args.length; i++) {
      JavaObject curr = asComparableJavaObject(name, args[i]);
      try {
        int result = prev.compareTo(curr);
        if (!cmp.compare(result)) {
          return BooleanObject.FALSE;
        }
        prev = curr;
      } catch (ClassCastException e) {
        throw new LispArgumentError(
            name
                + ": cannot compare "
                + prev.object().getClass().getSimpleName()
                + " with "
                + curr.object().getClass().getSimpleName());
      }
    }
    return BooleanObject.TRUE;
  }

  // Helper to extract a numeric value as double
  // Uses asDouble()/asInt() methods to support wrappers like CellObject
  private static double asNumber(String name, LispObject obj) {
    // Try double first (handles DoubleObject and wrappers)
    DoubleObject dbl = obj.asDouble();
    if (dbl != null) {
      return dbl.value;
    }
    // Try int (handles IntObject and wrappers)
    IntObject intObj = obj.asInt();
    if (intObj != null) {
      return intObj.value;
    }
    throw new LispArgumentError(name + ": expected number, got " + obj.getClass().getSimpleName());
  }

  // Helper to extract a JavaObject wrapping a Comparable
  private static JavaObject asComparableJavaObject(String name, LispObject obj) {
    if (obj instanceof JavaObject) {
      JavaObject jobj = (JavaObject) obj;
      if (jobj.isComparable()) {
        return jobj;
      }
      throw new LispArgumentError(
          name
              + ": JavaObject does not wrap a Comparable: "
              + jobj.object().getClass().getSimpleName());
    }
    throw new LispArgumentError(
        name + ": expected number or Comparable JavaObject, got " + obj.getClass().getSimpleName());
  }

  // Helper to assert minimum argument count
  private static void assertMinArgCount(String name, LispObject[] args, int min) {
    if (args.length < min) {
      throw new LispArgumentError(
          name + ": expected at least " + min + " argument(s), got " + args.length);
    }
  }

  public static LispObject not(LispObject[] params) {
    return params[0].truthiness() ? BooleanObject.FALSE : BooleanObject.TRUE;
  }

  /** Tests if all boolean arguments are equal. (boolean=? bool1 bool2 ...) */
  public static LispObject booleanEqual(LispObject[] params) {
    if (params.length < 2) {
      throw new LispArgumentError("boolean=?: expected at least 2 arguments");
    }

    if (!(params[0] instanceof BooleanObject)) {
      throw new LispArgumentError("boolean=?: expected boolean");
    }
    boolean first = ((BooleanObject) params[0]).truthiness();

    for (int i = 1; i < params.length; i++) {
      if (!(params[i] instanceof BooleanObject)) {
        throw new LispArgumentError("boolean=?: expected boolean");
      }
      if (((BooleanObject) params[i]).truthiness() != first) {
        return BooleanObject.FALSE;
      }
    }
    return BooleanObject.TRUE;
  }
}
