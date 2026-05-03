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
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.evaluator.Continuation;
import net.sourceforge.kleinlisp.evaluator.ContinuationResumeException;
import net.sourceforge.kleinlisp.evaluator.TcoResult;
import net.sourceforge.kleinlisp.evaluator.WindFrame;
import net.sourceforge.kleinlisp.evaluator.WindManager;

/**
 * A first-class CPS continuation captured by call/cc.
 *
 * <p>Stores both the continuation proper ({@link Continuation}) and the dynamic-wind extent ({@link
 * WindFrame}) at capture time. When invoked:
 *
 * <ul>
 *   <li>From a CPS context (via {@link #jump}): runs {@link WindManager#windTo} to transition the
 *       current dynamic extent to the captured one, then returns {@code cont.apply(value)} as a
 *       {@link TcoResult} for the top-level trampoline.
 *   <li>From a non-CPS context (via {@link FunctionObject#function()}): throws {@link
 *       ContinuationResumeException} carrying the continuation and captured extent so that {@link
 *       net.sourceforge.kleinlisp.evaluator.Evaluator#evaluate} can resume it correctly.
 * </ul>
 */
public class ContinuationObject implements LispObject {

  private final Continuation cont;
  private final WindFrame capturedWind;
  private final FunctionObject functionObject;

  public ContinuationObject(Continuation cont) {
    this.cont = cont;
    this.capturedWind = WindManager.snapshot();
    this.functionObject =
        new FunctionObject(
            params -> {
              LispObject value = (params != null && params.length > 0) ? params[0] : ListObject.NIL;
              throw new ContinuationResumeException(cont, value, capturedWind);
            });
  }

  /**
   * CPS-aware invocation: transitions the dynamic extent to the captured one, then returns the
   * TcoResult of applying the continuation.
   */
  public TcoResult jump(LispObject value) {
    WindManager.windTo(capturedWind);
    return cont.apply(value);
  }

  @Override
  public FunctionObject asFunction() {
    return functionObject;
  }

  @Override
  public Object asObject() {
    return this;
  }

  @Override
  public boolean truthiness() {
    return true;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(functionObject);
  }

  @Override
  public String toString() {
    return "#<continuation>";
  }
}
