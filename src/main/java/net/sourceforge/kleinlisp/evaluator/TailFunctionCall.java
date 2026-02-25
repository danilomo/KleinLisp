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
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.TailCallObject;
import net.sourceforge.kleinlisp.special_forms.LambdaForm.LambdaFunction;

/**
 * Handles tail calls by returning a TailCallObject marker instead of recursing. The trampoline in
 * LambdaFunction.evaluate() will handle the continuation.
 *
 * @author Danilo Oliveira
 */
public class TailFunctionCall implements Supplier<LispObject> {

  private final LispEnvironment env;
  private final Supplier<LispObject> head;
  private final Supplier<LispObject>[] parameters;

  @SuppressWarnings("unchecked")
  public TailFunctionCall(
      LispEnvironment env,
      Supplier<LispObject> head,
      java.util.List<Supplier<LispObject>> parameters) {
    this.env = env;
    this.head = head;
    this.parameters = parameters.toArray(new Supplier[0]);
  }

  @Override
  public LispObject get() {
    FunctionObject functionObj = head.get().asFunction();

    if (functionObj == null) {
      throw new LispArgumentError(String.format("Value [%s] isn't a valid function"));
    }

    Function function = functionObj.function();

    int len = parameters.length;
    LispObject[] params = new LispObject[len];
    for (int i = 0; i < len; i++) {
      params[i] = parameters[i].get();
    }

    // Return a TailCallObject marker instead of recursing
    // The trampoline in LambdaFunction.evaluate() will handle this
    if (function instanceof LambdaFunction) {
      return new TailCallObject((LambdaFunction) function, params);
    }

    // For non-lambda functions (builtins), just call directly
    return function.evaluate(params);
  }
}
