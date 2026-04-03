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
package net.sourceforge.kleinlisp.evaluator;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Simple static type inference for numeric expressions. Used to enable specialized code paths that
 * skip runtime type checks.
 */
public class TypeAnalyzer {

  public enum ExpressionType {
    INT, // Definitely integer
    DOUBLE, // Definitely double
    NUMERIC, // Integer or double
    UNKNOWN // Could be anything
  }

  /**
   * Analyze an expression to determine its type. Returns INT only for literal integers and
   * expressions that definitely produce integers.
   */
  public static ExpressionType analyze(LispObject obj) {
    // Use instanceof to check actual type, not asInt()/asDouble() which can convert
    if (obj instanceof net.sourceforge.kleinlisp.objects.IntObject) {
      return ExpressionType.INT;
    }
    if (obj instanceof net.sourceforge.kleinlisp.objects.DoubleObject) {
      return ExpressionType.DOUBLE;
    }

    ListObject list = obj.asList();
    if (list != null && list != ListObject.NIL) {
      return analyzeCall(list);
    }

    return ExpressionType.UNKNOWN;
  }

  private static ExpressionType analyzeCall(ListObject call) {
    AtomObject head = call.car().asAtom();
    if (head == null) {
      return ExpressionType.UNKNOWN;
    }

    String op = head.toString();
    switch (op) {
      case "+":
      case "-":
      case "*":
      case "mod":
        // If both args are INT, result is INT
        if (call.length() == 3) {
          ExpressionType left = analyze(call.cdr().car());
          ExpressionType right = analyze(call.cdr().cdr().car());
          if (left == ExpressionType.INT && right == ExpressionType.INT) {
            return ExpressionType.INT;
          }
          if (left == ExpressionType.DOUBLE || right == ExpressionType.DOUBLE) {
            return ExpressionType.DOUBLE;
          }
          if ((left == ExpressionType.INT || left == ExpressionType.NUMERIC)
              && (right == ExpressionType.INT || right == ExpressionType.NUMERIC)) {
            return ExpressionType.NUMERIC;
          }
        }
        return ExpressionType.UNKNOWN;

      case "/":
        // Division can produce double from ints
        if (call.length() == 3) {
          ExpressionType left = analyze(call.cdr().car());
          ExpressionType right = analyze(call.cdr().cdr().car());
          if (left == ExpressionType.INT && right == ExpressionType.INT) {
            return ExpressionType.INT; // Integer division in Lisp
          }
        }
        return ExpressionType.NUMERIC;

      default:
        return ExpressionType.UNKNOWN;
    }
  }

  /** Check if expression is a literal integer. */
  public static boolean isLiteralInt(LispObject obj) {
    return obj.asInt() != null;
  }
}
