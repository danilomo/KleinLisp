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
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.TailCallObject;
import net.sourceforge.kleinlisp.special_forms.LambdaForm.LambdaFunction;

/**
 * Specialized tail function call for two-argument functions. Returns a TailCallObject marker
 * instead of recursing.
 */
public class TailFunctionCall2 implements Supplier<LispObject> {

  private static final ThreadLocal<LispObject[]> PARAMS_HOLDER =
      ThreadLocal.withInitial(() -> new LispObject[2]);

  private final Supplier<LispObject> head;
  private final Supplier<LispObject> param0;
  private final Supplier<LispObject> param1;

  public TailFunctionCall2(
      Supplier<LispObject> head, Supplier<LispObject> param0, Supplier<LispObject> param1) {
    this.head = head;
    this.param0 = param0;
    this.param1 = param1;
  }

  @Override
  public LispObject get() {
    FunctionObject functionObj = head.get().asFunction();

    if (functionObj == null) {
      throw new LispArgumentError("Value isn't a valid function");
    }

    Function function = functionObj.function();

    LispObject[] params = PARAMS_HOLDER.get();
    params[0] = param0.get();
    params[1] = param1.get();

    // Return a TailCallObject marker instead of recursing
    if (function instanceof LambdaFunction) {
      // Must create a new array for tail call since it will be stored
      return new TailCallObject((LambdaFunction) function, new LispObject[] {params[0], params[1]});
    }

    // For non-lambda functions (builtins), just call directly
    return function.evaluate(params);
  }
}
