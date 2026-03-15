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

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VectorObject;

/** Type predicate functions for KleinLisp. */
public class TypePredicates {

  /** Tests if the value is a string. (string? x) */
  public static LispObject isString(LispObject[] params) {
    return params[0].asString() != null ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the value is a number (int or double). (number? x) */
  public static LispObject isNumber(LispObject[] params) {
    LispObject obj = params[0];
    return (obj.asInt() != null || obj.asDouble() != null)
        ? BooleanObject.TRUE
        : BooleanObject.FALSE;
  }

  /** Tests if the value is an integer. (integer? x) */
  public static LispObject isInteger(LispObject[] params) {
    return params[0].asInt() != null ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the value is a double/real number. (real? x) or (double? x) */
  public static LispObject isDouble(LispObject[] params) {
    LispObject obj = params[0];
    // In Scheme, real? returns true for all real numbers including integers
    return (obj.asInt() != null || obj.asDouble() != null)
        ? BooleanObject.TRUE
        : BooleanObject.FALSE;
  }

  /** Tests if the value is a cons pair (non-nil list or improper list). (pair? x) */
  public static LispObject isPair(LispObject[] params) {
    LispObject obj = params[0];
    ListObject list = obj.asList();
    // pair? returns true for non-empty lists (cons cells)
    return (list != null && list != ListObject.NIL) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the value is a proper list (including nil). (list? x) */
  public static LispObject isList(LispObject[] params) {
    LispObject obj = params[0];
    // Check if it's nil (empty list)
    if (obj == ListObject.NIL || obj == null) {
      return BooleanObject.TRUE;
    }
    ListObject list = obj.asList();
    if (list == null) {
      return BooleanObject.FALSE;
    }
    // A proper list has nil at the end
    // Since ListObject in KleinLisp always maintains proper lists, we just check if it's a list
    return BooleanObject.TRUE;
  }

  /** Tests if the value is a symbol/atom. (symbol? x) */
  public static LispObject isSymbol(LispObject[] params) {
    LispObject obj = params[0];
    // Check for both AtomObject and IdentifierObject (which wraps AtomObject)
    return (obj.asAtom() != null) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the value is a boolean. (boolean? x) */
  public static LispObject isBoolean(LispObject[] params) {
    LispObject obj = params[0];
    return (obj == BooleanObject.TRUE || obj == BooleanObject.FALSE)
        ? BooleanObject.TRUE
        : BooleanObject.FALSE;
  }

  /** Tests if the value is a procedure/function. (procedure? x) */
  public static LispObject isProcedure(LispObject[] params) {
    return params[0].asFunction() != null ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /**
   * Tests if the value is nil (empty list). (null? x) - already exists but included for
   * completeness
   */
  public static LispObject isNull(LispObject[] params) {
    LispObject obj = params[0];
    return (obj == null || obj == ListObject.NIL) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the value is a vector. (vector? x) */
  public static LispObject isVector(LispObject[] params) {
    LispObject obj = params[0];
    return (obj instanceof VectorObject) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the value is zero. (zero? x) */
  public static LispObject isZero(LispObject[] params) {
    IntObject intVal = params[0].asInt();
    if (intVal != null) {
      return intVal.value == 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    DoubleObject doubleVal = params[0].asDouble();
    if (doubleVal != null) {
      return doubleVal.value == 0.0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    return BooleanObject.FALSE;
  }

  /** Tests if the value is positive. (positive? x) */
  public static LispObject isPositive(LispObject[] params) {
    IntObject intVal = params[0].asInt();
    if (intVal != null) {
      return intVal.value > 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    DoubleObject doubleVal = params[0].asDouble();
    if (doubleVal != null) {
      return doubleVal.value > 0.0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    return BooleanObject.FALSE;
  }

  /** Tests if the value is negative. (negative? x) */
  public static LispObject isNegative(LispObject[] params) {
    IntObject intVal = params[0].asInt();
    if (intVal != null) {
      return intVal.value < 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    DoubleObject doubleVal = params[0].asDouble();
    if (doubleVal != null) {
      return doubleVal.value < 0.0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    return BooleanObject.FALSE;
  }

  /** Tests if the value is odd. (odd? x) */
  public static LispObject isOdd(LispObject[] params) {
    IntObject intVal = params[0].asInt();
    if (intVal != null) {
      return (intVal.value & 1) == 1 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    return BooleanObject.FALSE;
  }

  /** Tests if the value is even. (even? x) */
  public static LispObject isEven(LispObject[] params) {
    IntObject intVal = params[0].asInt();
    if (intVal != null) {
      return (intVal.value & 1) == 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    return BooleanObject.FALSE;
  }
}
