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

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ParameterObject;

/** R7RS parameter functions: make-parameter, parameter?. */
public class ParameterFunctions {

  /**
   * (make-parameter init) or (make-parameter init converter)
   *
   * <p>Creates a new parameter object. If converter is provided, it is applied to the initial value
   * and to all values set via the parameter or parameterize.
   */
  public static LispObject makeParameter(LispObject[] args) {
    if (args.length < 1 || args.length > 2) {
      throw new LispArgumentError("make-parameter: expected 1 or 2 arguments, got " + args.length);
    }

    LispObject initialValue = args[0];
    Function converter = null;

    if (args.length == 2) {
      FunctionObject funcObj = args[1].asFunction();
      if (funcObj == null) {
        throw new LispArgumentError("make-parameter: converter must be a procedure");
      }
      converter = funcObj.function();
    }

    return new ParameterObject(initialValue, converter);
  }

  /**
   * (parameter? obj)
   *
   * @return #t if obj is a parameter, #f otherwise
   */
  public static LispObject isParameter(LispObject[] args) {
    assertArgCount("parameter?", args, 1);
    return args[0] instanceof ParameterObject ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  private static void assertArgCount(String name, LispObject[] args, int expected) {
    if (args.length != expected) {
      throw new LispArgumentError(
          name + ": expected " + expected + " arguments, got " + args.length);
    }
  }
}
