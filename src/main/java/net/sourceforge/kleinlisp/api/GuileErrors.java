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
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;

/**
 * Utility class for generating Guile-compatible error messages. Error formats follow Guile Scheme
 * conventions for better compatibility.
 *
 * @author Danilo Oliveira
 */
public final class GuileErrors {

  private GuileErrors() {}

  /**
   * Wrong type argument error. Format: "In procedure <name>: Wrong type argument in position <n>
   * (expecting <type>): <value>"
   */
  public static LispArgumentError wrongType(
      String proc, int position, String expecting, LispObject actual) {
    return new LispArgumentError(
        "In procedure "
            + proc
            + ": Wrong type argument in position "
            + position
            + " (expecting "
            + expecting
            + "): "
            + actual);
  }

  /** Value out of range error. Format: "In procedure <name>: Value out of range: <value>" */
  public static LispArgumentError outOfRange(String proc, Object value) {
    return new LispArgumentError("In procedure " + proc + ": Value out of range: " + value);
  }

  /**
   * Argument out of range error. Format: "In procedure <name>: Argument <n> out of range: <value>"
   */
  public static LispArgumentError argOutOfRange(String proc, int argNum, Object value) {
    return new LispArgumentError(
        "In procedure " + proc + ": Argument " + argNum + " out of range: " + value);
  }

  /** Requires a non-empty pair (cons cell). Throws if obj is nil or not a list. */
  public static ListObject requirePair(String proc, LispObject obj, int position) {
    if (obj == ListObject.NIL) {
      throw wrongType(proc, position, "pair", obj);
    }
    ListObject list = obj.asList();
    if (list == null) {
      throw wrongType(proc, position, "pair", obj);
    }
    return list;
  }

  /** Requires a list (including nil). Throws if obj is not a list. */
  public static ListObject requireList(String proc, LispObject obj, int position) {
    if (obj == ListObject.NIL) {
      return ListObject.NIL;
    }
    ListObject list = obj.asList();
    if (list == null) {
      throw wrongType(proc, position, "list", obj);
    }
    return list;
  }

  /** Requires an integer. Throws if obj is not an integer. */
  public static int requireInt(String proc, LispObject obj, int position) {
    IntObject intObj = obj.asInt();
    if (intObj == null) {
      throw wrongType(proc, position, "integer", obj);
    }
    return intObj.value;
  }

  /** Requires a number (int or double). Returns the value as double. */
  public static double requireNumber(String proc, LispObject obj, int position) {
    IntObject intObj = obj.asInt();
    if (intObj != null) {
      return intObj.value;
    }
    DoubleObject doubleObj = obj.asDouble();
    if (doubleObj != null) {
      return doubleObj.value;
    }
    throw wrongType(proc, position, "number", obj);
  }

  /** Requires a string. Throws if obj is not a string. */
  public static String requireString(String proc, LispObject obj, int position) {
    StringObject strObj = obj.asString();
    if (strObj == null) {
      throw wrongType(proc, position, "string", obj);
    }
    return strObj.value();
  }

  /** Requires a procedure. Throws if obj is not a function. */
  public static FunctionObject requireProcedure(String proc, LispObject obj, int position) {
    FunctionObject funcObj = obj.asFunction();
    if (funcObj == null) {
      throw wrongType(proc, position, "procedure", obj);
    }
    return funcObj;
  }

  /** Requires a non-negative integer. Throws if out of range. */
  public static int requireNonNegativeInt(String proc, LispObject obj, int position) {
    int value = requireInt(proc, obj, position);
    if (value < 0) {
      throw argOutOfRange(proc, position, value);
    }
    return value;
  }
}
