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

/**
 * Thrown by {@link net.sourceforge.kleinlisp.objects.ContinuationObject#evaluate} when a CPS
 * continuation is invoked from a non-CPS context (e.g. inside a built-in higher-order function such
 * as {@code for-each} or {@code map}).
 *
 * <p>Extends {@link Error} so it bypasses all {@code catch (RuntimeException)} handlers in guard
 * forms and other exception-handling forms, exactly like {@link ContinuationException} does for
 * escape continuations.
 *
 * <p>{@link net.sourceforge.kleinlisp.evaluator.Evaluator#evaluate} catches this and resumes the
 * captured continuation at the top-level trampoline.
 */
public class ContinuationResumeException extends Error {

  private final Continuation continuation;
  private final LispObject value;
  private final WindFrame capturedWind;

  public ContinuationResumeException(
      Continuation continuation, LispObject value, WindFrame capturedWind) {
    super(null, null, true, false); // suppress stack-trace collection for performance
    this.continuation = continuation;
    this.value = value;
    this.capturedWind = capturedWind;
  }

  public Continuation getContinuation() {
    return continuation;
  }

  public LispObject getValue() {
    return value;
  }

  /** The dynamic-wind extent captured when this continuation was created via call/cc. */
  public WindFrame getCapturedWind() {
    return capturedWind;
  }
}
