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

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;

/**
 * Represents the result of evaluating a lambda body. Three variants:
 *
 * <ul>
 *   <li>{@link #ofValue} — final value; terminates the trampoline.
 *   <li>{@link #ofContinuation} — deferred step; the trampoline calls the supplier.
 *   <li>{@link #ofTailApply} — tail-call signal for {@link
 *       net.sourceforge.kleinlisp.special_forms.LambdaForm.LambdaFunction#evaluateCps}; the TCO
 *       loop reuses the current stack frame instead of pushing a new one.
 * </ul>
 */
public class TcoResult {
  private final LispObject value;
  private final Supplier<TcoResult> continuation;

  // Tail-call fields (non-null only for ofTailApply)
  private final LispObject tailApplyFn;
  private final LispObject[] tailApplyParams;
  private final Continuation tailApplyCont;

  private TcoResult(
      LispObject value,
      Supplier<TcoResult> continuation,
      LispObject tailApplyFn,
      LispObject[] tailApplyParams,
      Continuation tailApplyCont) {
    this.value = value;
    this.continuation = continuation;
    this.tailApplyFn = tailApplyFn;
    this.tailApplyParams = tailApplyParams;
    this.tailApplyCont = tailApplyCont;
  }

  public static TcoResult ofValue(LispObject value) {
    return new TcoResult(value, null, null, null, null);
  }

  public static TcoResult ofContinuation(Supplier<TcoResult> continuation) {
    return new TcoResult(null, continuation, null, null, null);
  }

  /**
   * Signal to the TCO loop in {@code LambdaFunction.evaluateCps} that {@code fn} should be applied
   * to {@code params} with continuation {@code k} by reusing the current stack frame rather than
   * pushing a new one.
   */
  public static TcoResult ofTailApply(LispObject fn, LispObject[] params, Continuation k) {
    return new TcoResult(null, null, fn, params, k);
  }

  public boolean isContinuation() {
    return continuation != null;
  }

  public boolean isTailApply() {
    return tailApplyFn != null;
  }

  public LispObject getValue() {
    return value;
  }

  public Supplier<TcoResult> getContinuation() {
    return continuation;
  }

  public LispObject getTailApplyFn() {
    return tailApplyFn;
  }

  public LispObject[] getTailApplyParams() {
    return tailApplyParams;
  }

  public Continuation getTailApplyCont() {
    return tailApplyCont;
  }

  /** Trampoline: repeatedly invoke continuations until a final value is reached. */
  public LispObject trampoline() {
    TcoResult result = this;
    while (result.isContinuation()) {
      result = result.getContinuation().get();
    }
    // isTailApply() here would mean a tail-call signal leaked to the top — shouldn't happen
    // in normal usage; treat the fn as a value to surface the bug rather than silently fail.
    return result.getValue();
  }
}
