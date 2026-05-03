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
import net.sourceforge.kleinlisp.evaluator.ContinuationException;
import net.sourceforge.kleinlisp.evaluator.WindFrame;
import net.sourceforge.kleinlisp.evaluator.WindManager;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.ContinuationObject;
import net.sourceforge.kleinlisp.objects.EscapeContinuationObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;

/**
 * R7RS continuation and control functions.
 *
 * <p>Phase 1 implementation: escape-only call/cc via ContinuationException. The captured
 * continuation is valid only within the dynamic extent of the call/cc call (downward
 * continuations). Multi-shot and upward continuations require a full CPS evaluator rewrite (Phase
 * 3).
 */
public class ContinuationFunctions {

  /**
   * (call-with-current-continuation proc) — also bound as (call/cc proc).
   *
   * <p>Captures the current continuation as an escape procedure and passes it to proc. If proc
   * invokes the escape procedure, execution aborts to this call/cc with the supplied value.
   * Invoking the escape procedure after call/cc has returned raises an error (escape-only
   * limitation).
   */
  public static LispObject callWithCurrentContinuation(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError(
          "call-with-current-continuation requires exactly 1 argument, got " + params.length);
    }
    FunctionObject proc = params[0].asFunction();
    if (proc == null) {
      throw new LispArgumentError(
          "call-with-current-continuation: argument must be a procedure, got "
              + params[0].getClass().getSimpleName());
    }

    Object key = new Object();
    EscapeContinuationObject cont = new EscapeContinuationObject(key);

    try {
      return proc.function().evaluate(new LispObject[] {cont});
    } catch (ContinuationException e) {
      if (e.getKey() == key) {
        return e.getValue();
      }
      throw e; // belongs to an enclosing call/cc — propagate
    }
  }

  /**
   * (dynamic-wind before thunk after)
   *
   * <p>Full R7RS §6.10 implementation. Maintains the {@link WindManager} extent stack so that
   * continuation capture/invocation can run the correct before/after thunks when crossing
   * dynamic-wind boundaries in either direction.
   */
  public static LispObject dynamicWind(LispObject[] params) {
    if (params.length != 3) {
      throw new LispArgumentError(
          "dynamic-wind requires exactly 3 arguments, got " + params.length);
    }
    FunctionObject before = params[0].asFunction();
    FunctionObject thunk = params[1].asFunction();
    FunctionObject after = params[2].asFunction();

    if (before == null || thunk == null || after == null) {
      throw new LispArgumentError("dynamic-wind: all three arguments must be procedures");
    }

    WindFrame parent = WindManager.snapshot();
    WindFrame frame = new WindFrame(before, after, parent);

    before.function().evaluate(new LispObject[0]);
    WindManager.push(frame);
    try {
      return thunk.function().evaluate(new LispObject[0]);
    } finally {
      // Runs on normal return AND on any Error/Exception exit (including ContinuationException /
      // ContinuationResumeException). Only pop if we haven't already been popped by windTo.
      if (WindManager.snapshot() == frame) {
        WindManager.pop(parent);
        after.function().evaluate(new LispObject[0]);
      }
    }
  }

  /**
   * (with-exception-handler handler thunk)
   *
   * <p>Runs thunk with handler installed as the current exception handler. If an exception is
   * raised within thunk, handler is called with the raised object. If handler returns normally, its
   * return value becomes the result of with-exception-handler.
   */
  public static LispObject withExceptionHandler(LispObject[] params) {
    if (params.length != 2) {
      throw new LispArgumentError(
          "with-exception-handler requires exactly 2 arguments, got " + params.length);
    }
    FunctionObject handler = params[0].asFunction();
    FunctionObject thunk = params[1].asFunction();

    if (handler == null || thunk == null) {
      throw new LispArgumentError("with-exception-handler: both arguments must be procedures");
    }

    try {
      return thunk.function().evaluate(new LispObject[0]);
    } catch (LispRaisedException e) {
      return handler.function().evaluate(new LispObject[] {e.getRaisedValue()});
    }
  }

  /** (continuation? obj) — returns #t if obj is any captured continuation (escape or CPS). */
  public static LispObject isContinuation(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError(
          "continuation? requires exactly 1 argument, got " + params.length);
    }
    boolean isCont =
        params[0] instanceof EscapeContinuationObject || params[0] instanceof ContinuationObject;
    return isCont ? BooleanObject.TRUE : BooleanObject.FALSE;
  }
}
