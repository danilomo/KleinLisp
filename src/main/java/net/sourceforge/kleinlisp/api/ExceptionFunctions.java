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
import net.sourceforge.kleinlisp.LispRaisedException;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.ErrorObject;
import net.sourceforge.kleinlisp.objects.StringObject;

/**
 * R7RS exception handling functions: raise, raise-continuable, and error object accessors.
 */
public class ExceptionFunctions {

  /**
   * (raise obj) - raises an exception with the given object.
   *
   * <p>The exception can be caught by a guard form. If no guard catches it, the exception
   * propagates to the top level.
   */
  public static LispObject raise(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError("raise requires exactly 1 argument, got " + params.length);
    }
    throw new LispRaisedException(params[0], false);
  }

  /**
   * (raise-continuable obj) - raises a continuable exception.
   *
   * <p>Note: Without call/cc support, this behaves identically to raise. In a full R7RS
   * implementation, the handler could return a value and execution would continue.
   */
  public static LispObject raiseContinuable(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError(
          "raise-continuable requires exactly 1 argument, got " + params.length);
    }
    throw new LispRaisedException(params[0], true);
  }

  /**
   * (error-object? obj) - returns #t if obj is an error object, #f otherwise.
   */
  public static LispObject isErrorObject(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError("error-object? requires exactly 1 argument, got " + params.length);
    }
    return params[0] instanceof ErrorObject ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /**
   * (error-object-message error) - returns the message string from an error object.
   */
  public static LispObject errorObjectMessage(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError(
          "error-object-message requires exactly 1 argument, got " + params.length);
    }
    if (!(params[0] instanceof ErrorObject)) {
      throw new LispArgumentError(
          "error-object-message requires an error object, got " + params[0].getClass().getSimpleName());
    }
    return new StringObject(((ErrorObject) params[0]).getMessage());
  }

  /**
   * (error-object-irritants error) - returns the list of irritants from an error object.
   */
  public static LispObject errorObjectIrritants(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError(
          "error-object-irritants requires exactly 1 argument, got " + params.length);
    }
    if (!(params[0] instanceof ErrorObject)) {
      throw new LispArgumentError(
          "error-object-irritants requires an error object, got "
              + params[0].getClass().getSimpleName());
    }
    return ((ErrorObject) params[0]).getIrritantsList();
  }
}
