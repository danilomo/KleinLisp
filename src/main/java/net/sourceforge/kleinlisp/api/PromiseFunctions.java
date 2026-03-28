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
import net.sourceforge.kleinlisp.objects.PromiseObject;

/** R7RS promise functions: force, promise?, make-promise. */
public class PromiseFunctions {

  /**
   * (force promise) - force evaluation of promise.
   *
   * <p>R7RS: force on a non-promise returns the value unchanged.
   */
  public static LispObject force(LispObject[] args) {
    assertArgCount("force", args, 1);

    if (args[0] instanceof PromiseObject) {
      return ((PromiseObject) args[0]).force();
    }

    // R7RS: force on a non-promise returns the value unchanged
    return args[0];
  }

  /**
   * (promise? obj) - test if obj is a promise.
   *
   * @return #t if obj is a promise, #f otherwise
   */
  public static LispObject isPromise(LispObject[] args) {
    assertArgCount("promise?", args, 1);
    return args[0] instanceof PromiseObject ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /**
   * (make-promise obj) - wrap a value in a forced promise.
   *
   * <p>If obj is already a promise, returns it unchanged. Otherwise, creates an already-forced
   * promise containing obj.
   */
  public static LispObject makePromise(LispObject[] args) {
    assertArgCount("make-promise", args, 1);

    if (args[0] instanceof PromiseObject) {
      // If already a promise, return it unchanged
      return args[0];
    }

    // Create an already-forced promise
    return new PromiseObject(args[0]);
  }

  private static void assertArgCount(String name, LispObject[] args, int expected) {
    if (args.length != expected) {
      throw new LispArgumentError(
          name + ": expected " + expected + " arguments, got " + args.length);
    }
  }
}
