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
import net.sourceforge.kleinlisp.objects.IntObject;

public class MathFunctions {

  public static LispObject add(LispObject[] params) {
    int sum = 0;
    for (LispObject i : params) {
      if (i.asInt() == null) {
        throw new LispArgumentError("Wrong argument type passed to + function");
      }
      sum += i.asInt().value;
    }
    return IntObject.valueOf(sum);
  }

  public static LispObject sub(LispObject[] params) {
    boolean first = true;
    int sum = 0;

    for (LispObject i : params) {
      if (first) {
        first = false;
        sum = i.asInt().value;
      } else {
        sum -= i.asInt().value;
      }
    }
    return IntObject.valueOf(sum);
  }

  public static LispObject mul(LispObject[] params) {
    int prod = 1;

    for (LispObject i : params) {
      prod *= i.asInt().value;
    }
    return IntObject.valueOf(prod);
  }

  public static LispObject div(LispObject[] params) {
    int prod = 1;
    boolean first = true;

    for (LispObject i : params) {
      if (first) {
        first = false;
        prod = i.asInt().value;
      } else {
        prod /= i.asInt().value;
      }
    }
    return IntObject.valueOf(prod);
  }

  public static LispObject mod(LispObject[] params) {
    int prod = 1;

    for (LispObject i : params) {
      prod %= i.asInt().value;
    }
    return IntObject.valueOf(prod);
  }

  public static LispObject lt(LispObject[] params) {
    int i1 = params[0].asInt().value;
    int i2 = params[1].asInt().value;

    return i1 < i2 ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  public static LispObject abs(LispObject[] params) {
    IntObject intVal = params[0].asInt();
    if (intVal == null) {
      throw new LispArgumentError("abs requires a numeric argument");
    }
    return IntObject.valueOf(Math.abs(intVal.value));
  }

  public static LispObject min(LispObject[] params) {
    if (params.length == 0) {
      throw new LispArgumentError("min requires at least one argument");
    }
    int result = params[0].asInt().value;
    for (int i = 1; i < params.length; i++) {
      int val = params[i].asInt().value;
      if (val < result) {
        result = val;
      }
    }
    return IntObject.valueOf(result);
  }

  public static LispObject max(LispObject[] params) {
    if (params.length == 0) {
      throw new LispArgumentError("max requires at least one argument");
    }
    int result = params[0].asInt().value;
    for (int i = 1; i < params.length; i++) {
      int val = params[i].asInt().value;
      if (val > result) {
        result = val;
      }
    }
    return IntObject.valueOf(result);
  }
}
