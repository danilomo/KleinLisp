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
import net.sourceforge.kleinlisp.objects.ValuesObject;

public class MathFunctions {

  public static LispObject add(LispObject[] params) {
    boolean useDouble = false;
    for (LispObject i : params) {
      if (i instanceof DoubleObject) {
        useDouble = true;
        break;
      } else if (!(i instanceof IntObject)) {
        // Not a direct IntObject/DoubleObject - check via conversion methods
        // Check if it's effectively an integer or a true double
        if (isEffectivelyDouble(i)) {
          useDouble = true;
          break;
        } else if (i.asInt() == null) {
          throw new LispArgumentError("Wrong argument type passed to + function");
        }
      }
    }
    if (useDouble) {
      double sum = 0.0;
      for (LispObject i : params) {
        sum += asDouble("+", i);
      }
      return new DoubleObject(sum);
    } else {
      int sum = 0;
      for (LispObject i : params) {
        IntObject intVal = i.asInt();
        if (intVal == null) {
          throw new LispArgumentError("Wrong argument type passed to + function");
        }
        sum += intVal.value;
      }
      return IntObject.valueOf(sum);
    }
  }

  public static LispObject sub(LispObject[] params) {
    if (params.length == 0) {
      throw new LispArgumentError("-: expected at least 1 argument");
    }
    boolean useDouble = false;
    for (LispObject i : params) {
      if (i instanceof DoubleObject) {
        useDouble = true;
        break;
      } else if (!(i instanceof IntObject)) {
        if (isEffectivelyDouble(i)) {
          useDouble = true;
          break;
        } else if (i.asInt() == null) {
          throw new LispArgumentError("Wrong argument type passed to - function");
        }
      }
    }
    if (useDouble) {
      double result = asDouble("-", params[0]);
      if (params.length == 1) {
        return new DoubleObject(-result);
      }
      for (int i = 1; i < params.length; i++) {
        result -= asDouble("-", params[i]);
      }
      return new DoubleObject(result);
    } else {
      IntObject firstInt = params[0].asInt();
      if (firstInt == null) {
        throw new LispArgumentError("Wrong argument type passed to - function");
      }
      int result = firstInt.value;
      if (params.length == 1) {
        return IntObject.valueOf(-result);
      }
      for (int i = 1; i < params.length; i++) {
        IntObject intVal = params[i].asInt();
        if (intVal == null) {
          throw new LispArgumentError("Wrong argument type passed to - function");
        }
        result -= intVal.value;
      }
      return IntObject.valueOf(result);
    }
  }

  public static LispObject mul(LispObject[] params) {
    boolean useDouble = false;
    for (LispObject i : params) {
      if (i instanceof DoubleObject) {
        useDouble = true;
        break;
      } else if (!(i instanceof IntObject)) {
        if (isEffectivelyDouble(i)) {
          useDouble = true;
          break;
        } else if (i.asInt() == null) {
          throw new LispArgumentError("Wrong argument type passed to * function");
        }
      }
    }
    if (useDouble) {
      double prod = 1.0;
      for (LispObject i : params) {
        prod *= asDouble("*", i);
      }
      return new DoubleObject(prod);
    } else {
      int prod = 1;
      for (LispObject i : params) {
        IntObject intVal = i.asInt();
        if (intVal == null) {
          throw new LispArgumentError("Wrong argument type passed to * function");
        }
        prod *= intVal.value;
      }
      return IntObject.valueOf(prod);
    }
  }

  public static LispObject div(LispObject[] params) {
    if (params.length == 0) {
      throw new LispArgumentError("/: expected at least 1 argument");
    }
    boolean useDouble = false;
    for (LispObject i : params) {
      if (i instanceof DoubleObject) {
        useDouble = true;
        break;
      } else if (!(i instanceof IntObject)) {
        if (isEffectivelyDouble(i)) {
          useDouble = true;
          break;
        } else if (i.asInt() == null) {
          throw new LispArgumentError("Wrong argument type passed to / function");
        }
      }
    }
    if (useDouble) {
      double result = asDouble("/", params[0]);
      if (params.length == 1) {
        if (result == 0) {
          throw new ArithmeticException("/: division by zero");
        }
        return new DoubleObject(1.0 / result);
      }
      for (int i = 1; i < params.length; i++) {
        double divisor = asDouble("/", params[i]);
        if (divisor == 0) {
          throw new ArithmeticException("/: division by zero");
        }
        result /= divisor;
      }
      return new DoubleObject(result);
    } else {
      // All integers - check if result is exact
      IntObject firstInt = params[0].asInt();
      if (firstInt == null) {
        throw new LispArgumentError("Wrong argument type passed to / function");
      }
      int num = firstInt.value;
      if (params.length == 1) {
        if (num == 0) {
          throw new ArithmeticException("/: division by zero");
        }
        if (num == 1 || num == -1) {
          return IntObject.valueOf(num);
        }
        return new DoubleObject(1.0 / num);
      }
      int denom = 1;
      for (int i = 1; i < params.length; i++) {
        IntObject intVal = params[i].asInt();
        if (intVal == null) {
          throw new LispArgumentError("Wrong argument type passed to / function");
        }
        if (intVal.value == 0) {
          throw new ArithmeticException("/: division by zero");
        }
        denom *= intVal.value;
      }
      if (num % denom == 0) {
        return IntObject.valueOf(num / denom);
      }
      return new DoubleObject((double) num / denom);
    }
  }

  public static LispObject mod(LispObject[] params) {
    if (params.length != 2) {
      throw new LispArgumentError("mod: expected 2 arguments, got " + params.length);
    }
    boolean useDouble = false;
    for (LispObject i : params) {
      if (i instanceof DoubleObject) {
        useDouble = true;
        break;
      } else if (!(i instanceof IntObject)) {
        if (isEffectivelyDouble(i)) {
          useDouble = true;
          break;
        } else if (i.asInt() == null) {
          throw new LispArgumentError("Wrong argument type passed to mod function");
        }
      }
    }
    if (useDouble) {
      double a = asDouble("mod", params[0]);
      double b = asDouble("mod", params[1]);
      if (b == 0) {
        throw new ArithmeticException("mod: division by zero");
      }
      double result = a % b;
      // Make result have same sign as divisor (like R7RS modulo)
      if ((result < 0 && b > 0) || (result > 0 && b < 0)) {
        result += b;
      }
      return new DoubleObject(result);
    } else {
      IntObject aInt = params[0].asInt();
      IntObject bInt = params[1].asInt();
      if (aInt == null || bInt == null) {
        throw new LispArgumentError("Wrong argument type passed to mod function");
      }
      int a = aInt.value;
      int b = bInt.value;
      if (b == 0) {
        throw new ArithmeticException("mod: division by zero");
      }
      int result = a % b;
      // Make result have same sign as divisor (like R7RS modulo)
      if ((result < 0 && b > 0) || (result > 0 && b < 0)) {
        result += b;
      }
      return IntObject.valueOf(result);
    }
  }

  public static LispObject abs(LispObject[] params) {
    assertArgCount("abs", params, 1);
    if (params[0] instanceof IntObject) {
      return IntObject.valueOf(Math.abs(((IntObject) params[0]).value));
    } else if (params[0] instanceof DoubleObject) {
      return new DoubleObject(Math.abs(((DoubleObject) params[0]).value));
    }
    throw new LispArgumentError("abs requires a numeric argument");
  }

  public static LispObject min(LispObject[] params) {
    if (params.length == 0) {
      throw new LispArgumentError("min requires at least one argument");
    }
    boolean hasDouble = false;
    for (LispObject i : params) {
      if (i instanceof DoubleObject) {
        hasDouble = true;
        break;
      } else if (!(i instanceof IntObject)) {
        throw new LispArgumentError("min: expected numeric argument");
      }
    }
    if (hasDouble) {
      double result = asDouble("min", params[0]);
      for (int i = 1; i < params.length; i++) {
        double val = asDouble("min", params[i]);
        if (val < result) {
          result = val;
        }
      }
      return new DoubleObject(result);
    } else {
      int result = ((IntObject) params[0]).value;
      for (int i = 1; i < params.length; i++) {
        int val = ((IntObject) params[i]).value;
        if (val < result) {
          result = val;
        }
      }
      return IntObject.valueOf(result);
    }
  }

  public static LispObject max(LispObject[] params) {
    if (params.length == 0) {
      throw new LispArgumentError("max requires at least one argument");
    }
    boolean hasDouble = false;
    for (LispObject i : params) {
      if (i instanceof DoubleObject) {
        hasDouble = true;
        break;
      } else if (!(i instanceof IntObject)) {
        throw new LispArgumentError("max: expected numeric argument");
      }
    }
    if (hasDouble) {
      double result = asDouble("max", params[0]);
      for (int i = 1; i < params.length; i++) {
        double val = asDouble("max", params[i]);
        if (val > result) {
          result = val;
        }
      }
      return new DoubleObject(result);
    } else {
      int result = ((IntObject) params[0]).value;
      for (int i = 1; i < params.length; i++) {
        int val = ((IntObject) params[i]).value;
        if (val > result) {
          result = val;
        }
      }
      return IntObject.valueOf(result);
    }
  }

  /**
   * Check if a wrapper type (like ValuesObject) contains a double value rather than an integer.
   * Returns true if the value has a fractional part (true double) or is an inexact integer. Returns
   * false if the value is effectively an exact integer.
   */
  private static boolean isEffectivelyDouble(LispObject obj) {
    if (obj instanceof DoubleObject) {
      return true;
    }
    if (obj instanceof IntObject) {
      return false;
    }
    // For wrapper types, check if converting to int loses precision
    IntObject intVal = obj.asInt();
    DoubleObject dblVal = obj.asDouble();
    if (intVal != null && dblVal != null) {
      // If the int conversion equals the double, it's an integer value
      return (double) intVal.value != dblVal.value;
    }
    // If only asDouble() works, it's a double
    return dblVal != null;
  }

  // Helper methods for numeric tower
  private static int gcd(int a, int b) {
    while (b != 0) {
      int t = b;
      b = a % b;
      a = t;
    }
    return a;
  }

  private static int lcm(int a, int b) {
    if (a == 0 || b == 0) return 0;
    return Math.abs(a / gcd(a, b) * b);
  }

  private static int asInt(String name, LispObject obj) {
    if (obj instanceof IntObject) {
      return ((IntObject) obj).value;
    }
    throw new LispArgumentError(name + ": expected integer, got " + obj.getClass().getSimpleName());
  }

  private static double asDouble(String name, LispObject obj) {
    if (obj instanceof IntObject) {
      return ((IntObject) obj).value;
    }
    if (obj instanceof DoubleObject) {
      return ((DoubleObject) obj).value;
    }
    // Try conversion methods for wrapper types (CellObject, etc.)
    DoubleObject dbl = obj.asDouble();
    if (dbl != null) {
      return dbl.value;
    }
    IntObject intVal = obj.asInt();
    if (intVal != null) {
      return intVal.value;
    }
    throw new LispArgumentError(name + ": expected number, got " + obj.getClass().getSimpleName());
  }

  private static void assertArgCount(String name, LispObject[] args, int expected) {
    if (args.length != expected) {
      throw new LispArgumentError(
          name + ": expected " + expected + " arguments, got " + args.length);
    }
  }

  private static BooleanObject fromBoolean(boolean value) {
    return value ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  // Rounding operations
  public static LispObject floor(LispObject[] params) {
    assertArgCount("floor", params, 1);
    double val = asDouble("floor", params[0]);
    double result = Math.floor(val);
    if (params[0] instanceof IntObject) {
      return params[0];
    }
    return new DoubleObject(result);
  }

  public static LispObject ceiling(LispObject[] params) {
    assertArgCount("ceiling", params, 1);
    double val = asDouble("ceiling", params[0]);
    double result = Math.ceil(val);
    if (params[0] instanceof IntObject) {
      return params[0];
    }
    return new DoubleObject(result);
  }

  public static LispObject truncate(LispObject[] params) {
    assertArgCount("truncate", params, 1);
    double val = asDouble("truncate", params[0]);
    double result = val >= 0 ? Math.floor(val) : Math.ceil(val);
    if (params[0] instanceof IntObject) {
      return params[0];
    }
    return new DoubleObject(result);
  }

  public static LispObject round(LispObject[] params) {
    assertArgCount("round", params, 1);
    double val = asDouble("round", params[0]);
    // R7RS: round to even (banker's rounding)
    double result = Math.rint(val);
    if (params[0] instanceof IntObject) {
      return params[0];
    }
    return new DoubleObject(result);
  }

  // Integer division
  public static LispObject quotient(LispObject[] params) {
    assertArgCount("quotient", params, 2);
    int a = asInt("quotient", params[0]);
    int b = asInt("quotient", params[1]);
    if (b == 0) {
      throw new ArithmeticException("quotient: division by zero");
    }
    return IntObject.valueOf(a / b);
  }

  public static LispObject remainder(LispObject[] params) {
    assertArgCount("remainder", params, 2);
    int a = asInt("remainder", params[0]);
    int b = asInt("remainder", params[1]);
    if (b == 0) {
      throw new ArithmeticException("remainder: division by zero");
    }
    return IntObject.valueOf(a % b);
  }

  public static LispObject modulo(LispObject[] params) {
    assertArgCount("modulo", params, 2);
    int a = asInt("modulo", params[0]);
    int b = asInt("modulo", params[1]);
    if (b == 0) {
      throw new ArithmeticException("modulo: division by zero");
    }
    // R7RS modulo: result has same sign as divisor
    int result = a % b;
    if ((result < 0 && b > 0) || (result > 0 && b < 0)) {
      result += b;
    }
    return IntObject.valueOf(result);
  }

  /** Floor division - returns quotient and remainder. (floor/ n d) */
  public static LispObject floorDiv(LispObject[] params) {
    assertArgCount("floor/", params, 2);
    int n = asInt("floor/", params[0]);
    int d = asInt("floor/", params[1]);
    if (d == 0) {
      throw new ArithmeticException("floor/: division by zero");
    }

    // Floor division: quotient rounds toward negative infinity
    int q = (int) Math.floor((double) n / d);
    int r = n - q * d;

    return new ValuesObject(new LispObject[] {IntObject.valueOf(q), IntObject.valueOf(r)});
  }

  /** Floor quotient. (floor-quotient n d) */
  public static LispObject floorQuotient(LispObject[] params) {
    assertArgCount("floor-quotient", params, 2);
    int n = asInt("floor-quotient", params[0]);
    int d = asInt("floor-quotient", params[1]);
    if (d == 0) {
      throw new ArithmeticException("floor-quotient: division by zero");
    }
    return IntObject.valueOf((int) Math.floor((double) n / d));
  }

  /** Floor remainder. (floor-remainder n d) */
  public static LispObject floorRemainder(LispObject[] params) {
    assertArgCount("floor-remainder", params, 2);
    int n = asInt("floor-remainder", params[0]);
    int d = asInt("floor-remainder", params[1]);
    if (d == 0) {
      throw new ArithmeticException("floor-remainder: division by zero");
    }
    int q = (int) Math.floor((double) n / d);
    return IntObject.valueOf(n - q * d);
  }

  /** Truncate division - returns quotient and remainder. (truncate/ n d) */
  public static LispObject truncateDiv(LispObject[] params) {
    assertArgCount("truncate/", params, 2);
    int n = asInt("truncate/", params[0]);
    int d = asInt("truncate/", params[1]);
    if (d == 0) {
      throw new ArithmeticException("truncate/: division by zero");
    }

    // Truncate division: quotient rounds toward zero (Java's default)
    int q = n / d;
    int r = n % d;

    return new ValuesObject(new LispObject[] {IntObject.valueOf(q), IntObject.valueOf(r)});
  }

  /** Truncate quotient. (truncate-quotient n d) */
  public static LispObject truncateQuotient(LispObject[] params) {
    assertArgCount("truncate-quotient", params, 2);
    int n = asInt("truncate-quotient", params[0]);
    int d = asInt("truncate-quotient", params[1]);
    if (d == 0) {
      throw new ArithmeticException("truncate-quotient: division by zero");
    }
    return IntObject.valueOf(n / d);
  }

  /** Truncate remainder. (truncate-remainder n d) */
  public static LispObject truncateRemainder(LispObject[] params) {
    assertArgCount("truncate-remainder", params, 2);
    int n = asInt("truncate-remainder", params[0]);
    int d = asInt("truncate-remainder", params[1]);
    if (d == 0) {
      throw new ArithmeticException("truncate-remainder: division by zero");
    }
    return IntObject.valueOf(n % d);
  }

  // GCD and LCM
  public static LispObject gcdFn(LispObject[] params) {
    if (params.length == 0) {
      return IntObject.valueOf(0);
    }
    int result = Math.abs(asInt("gcd", params[0]));
    for (int i = 1; i < params.length; i++) {
      result = gcd(result, Math.abs(asInt("gcd", params[i])));
    }
    return IntObject.valueOf(result);
  }

  public static LispObject lcmFn(LispObject[] params) {
    if (params.length == 0) {
      return IntObject.valueOf(1);
    }
    int result = Math.abs(asInt("lcm", params[0]));
    for (int i = 1; i < params.length; i++) {
      int b = Math.abs(asInt("lcm", params[i]));
      result = lcm(result, b);
    }
    return IntObject.valueOf(result);
  }

  // Exactness predicates
  public static LispObject isExact(LispObject[] params) {
    assertArgCount("exact?", params, 1);
    if (params[0] instanceof IntObject) {
      return BooleanObject.TRUE;
    }
    return BooleanObject.FALSE;
  }

  public static LispObject isInexact(LispObject[] params) {
    assertArgCount("inexact?", params, 1);
    if (params[0] instanceof DoubleObject) {
      return BooleanObject.TRUE;
    }
    return BooleanObject.FALSE;
  }

  // Exactness conversion
  public static LispObject exact(LispObject[] params) {
    assertArgCount("exact", params, 1);
    if (params[0] instanceof IntObject) {
      return params[0];
    }
    double val = asDouble("exact", params[0]);
    if (val != Math.floor(val) || val > Integer.MAX_VALUE || val < Integer.MIN_VALUE) {
      throw new LispArgumentError("exact: cannot convert " + val + " to exact integer");
    }
    return IntObject.valueOf((int) val);
  }

  public static LispObject inexact(LispObject[] params) {
    assertArgCount("inexact", params, 1);
    if (params[0] instanceof DoubleObject) {
      return params[0];
    }
    return new DoubleObject(asDouble("inexact", params[0]));
  }

  // Transcendental functions
  public static LispObject sqrt(LispObject[] params) {
    assertArgCount("sqrt", params, 1);
    double val = asDouble("sqrt", params[0]);
    if (val < 0) {
      throw new LispArgumentError("sqrt: negative argument: " + val);
    }
    return new DoubleObject(Math.sqrt(val));
  }

  public static LispObject expt(LispObject[] params) {
    assertArgCount("expt", params, 2);
    double base = asDouble("expt", params[0]);
    double exp = asDouble("expt", params[1]);
    double result = Math.pow(base, exp);
    // Return int if both args are int and result is whole
    if (params[0] instanceof IntObject
        && params[1] instanceof IntObject
        && result == Math.floor(result)
        && result <= Integer.MAX_VALUE
        && result >= Integer.MIN_VALUE) {
      return IntObject.valueOf((int) result);
    }
    return new DoubleObject(result);
  }

  public static LispObject exp(LispObject[] params) {
    assertArgCount("exp", params, 1);
    return new DoubleObject(Math.exp(asDouble("exp", params[0])));
  }

  public static LispObject log(LispObject[] params) {
    if (params.length == 1) {
      double val = asDouble("log", params[0]);
      if (val <= 0) {
        throw new LispArgumentError("log: non-positive argument: " + val);
      }
      return new DoubleObject(Math.log(val));
    } else if (params.length == 2) {
      double val = asDouble("log", params[0]);
      double base = asDouble("log", params[1]);
      if (val <= 0 || base <= 0 || base == 1) {
        throw new LispArgumentError("log: invalid arguments");
      }
      return new DoubleObject(Math.log(val) / Math.log(base));
    } else {
      throw new LispArgumentError("log: expected 1 or 2 arguments, got " + params.length);
    }
  }

  public static LispObject sin(LispObject[] params) {
    assertArgCount("sin", params, 1);
    return new DoubleObject(Math.sin(asDouble("sin", params[0])));
  }

  public static LispObject cos(LispObject[] params) {
    assertArgCount("cos", params, 1);
    return new DoubleObject(Math.cos(asDouble("cos", params[0])));
  }

  public static LispObject tan(LispObject[] params) {
    assertArgCount("tan", params, 1);
    return new DoubleObject(Math.tan(asDouble("tan", params[0])));
  }

  public static LispObject asin(LispObject[] params) {
    assertArgCount("asin", params, 1);
    return new DoubleObject(Math.asin(asDouble("asin", params[0])));
  }

  public static LispObject acos(LispObject[] params) {
    assertArgCount("acos", params, 1);
    return new DoubleObject(Math.acos(asDouble("acos", params[0])));
  }

  public static LispObject atan(LispObject[] params) {
    if (params.length == 1) {
      return new DoubleObject(Math.atan(asDouble("atan", params[0])));
    } else if (params.length == 2) {
      double y = asDouble("atan", params[0]);
      double x = asDouble("atan", params[1]);
      return new DoubleObject(Math.atan2(y, x));
    } else {
      throw new LispArgumentError("atan: expected 1 or 2 arguments, got " + params.length);
    }
  }

  public static LispObject square(LispObject[] params) {
    assertArgCount("square", params, 1);
    if (params[0] instanceof IntObject) {
      int val = ((IntObject) params[0]).value;
      return IntObject.valueOf(val * val);
    }
    double val = asDouble("square", params[0]);
    return new DoubleObject(val * val);
  }

  public static LispObject exactIntegerSqrt(LispObject[] params) {
    assertArgCount("exact-integer-sqrt", params, 1);
    int n = asInt("exact-integer-sqrt", params[0]);
    if (n < 0) {
      throw new LispArgumentError("exact-integer-sqrt: negative argument: " + n);
    }
    int s = (int) Math.sqrt(n);
    int r = n - s * s;
    // Return two values using R7RS multiple values
    return new ValuesObject(new LispObject[] {IntObject.valueOf(s), IntObject.valueOf(r)});
  }

  // Rational number functions (simplified for integers only)
  public static LispObject numerator(LispObject[] params) {
    assertArgCount("numerator", params, 1);
    if (params[0] instanceof IntObject) {
      return params[0];
    }
    throw new LispArgumentError("numerator: expected rational number");
  }

  public static LispObject denominator(LispObject[] params) {
    assertArgCount("denominator", params, 1);
    if (params[0] instanceof IntObject) {
      return IntObject.valueOf(1);
    }
    throw new LispArgumentError("denominator: expected rational number");
  }

  public static LispObject rationalize(LispObject[] params) {
    assertArgCount("rationalize", params, 2);
    double x = asDouble("rationalize", params[0]);
    // Simplified: return nearest integer
    return IntObject.valueOf((int) Math.round(x));
  }

  // Additional numeric predicates
  public static LispObject isInteger(LispObject[] params) {
    assertArgCount("integer?", params, 1);
    if (params[0] instanceof IntObject) {
      return BooleanObject.TRUE;
    }
    if (params[0] instanceof DoubleObject) {
      double val = ((DoubleObject) params[0]).value;
      return fromBoolean(val == Math.floor(val) && !Double.isInfinite(val));
    }
    return BooleanObject.FALSE;
  }

  public static LispObject isRational(LispObject[] params) {
    assertArgCount("rational?", params, 1);
    // In our simplified implementation, only integers are rational
    return fromBoolean(params[0] instanceof IntObject);
  }

  public static LispObject isReal(LispObject[] params) {
    assertArgCount("real?", params, 1);
    return fromBoolean(params[0] instanceof IntObject || params[0] instanceof DoubleObject);
  }

  public static LispObject isComplex(LispObject[] params) {
    assertArgCount("complex?", params, 1);
    // We don't support complex numbers, so same as real?
    return fromBoolean(params[0] instanceof IntObject || params[0] instanceof DoubleObject);
  }

  public static LispObject isFinite(LispObject[] params) {
    assertArgCount("finite?", params, 1);
    if (params[0] instanceof IntObject) {
      return BooleanObject.TRUE;
    }
    if (params[0] instanceof DoubleObject) {
      return fromBoolean(Double.isFinite(((DoubleObject) params[0]).value));
    }
    throw new LispArgumentError("finite?: expected number");
  }

  public static LispObject isInfinite(LispObject[] params) {
    assertArgCount("infinite?", params, 1);
    if (params[0] instanceof IntObject) {
      return BooleanObject.FALSE;
    }
    if (params[0] instanceof DoubleObject) {
      return fromBoolean(Double.isInfinite(((DoubleObject) params[0]).value));
    }
    throw new LispArgumentError("infinite?: expected number");
  }

  public static LispObject isNan(LispObject[] params) {
    assertArgCount("nan?", params, 1);
    if (params[0] instanceof IntObject) {
      return BooleanObject.FALSE;
    }
    if (params[0] instanceof DoubleObject) {
      return fromBoolean(Double.isNaN(((DoubleObject) params[0]).value));
    }
    throw new LispArgumentError("nan?: expected number");
  }
}
