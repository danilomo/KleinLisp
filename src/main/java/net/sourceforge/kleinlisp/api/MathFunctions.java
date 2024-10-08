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
    return new IntObject(sum);
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
    return new IntObject(sum);
  }

  public static LispObject mul(LispObject[] params) {
    int prod = 1;

    for (LispObject i : params) {
      prod *= i.asInt().value;
    }
    return new IntObject(prod);
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
    return new IntObject(prod);
  }

  public static LispObject mod(LispObject[] params) {
    int prod = 1;

    for (LispObject i : params) {
      prod %= i.asInt().value;
    }
    return new IntObject(prod);
  }

  public static LispObject lt(LispObject[] params) {
    Integer i1 = params[0].asInt().value;
    Integer i2 = params[1].asInt().value;

    return new BooleanObject(i1 < i2);
  }
}
