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
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.WrongTypeToApplyException;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.special_forms.LambdaForm.LambdaFunction;

/**
 * Specialized function call for two-argument functions. Uses ParameterArrayPool to avoid array
 * allocation overhead for all function types including lambdas.
 */
public class FunctionCall2 implements Supplier<LispObject> {

  private static final ThreadLocal<LispObject[]> PARAMS_HOLDER =
      ThreadLocal.withInitial(() -> new LispObject[2]);

  private final LispEnvironment env;
  private final SourceRef ref;
  private final Supplier<LispObject> head;
  private final Supplier<LispObject> param0;
  private final Supplier<LispObject> param1;

  public FunctionCall2(
      LispEnvironment env,
      SourceRef ref,
      Supplier<LispObject> head,
      Supplier<LispObject> param0,
      Supplier<LispObject> param1) {
    this.env = env;
    this.ref = ref;
    this.head = head;
    this.param0 = param0;
    this.param1 = param1;
  }

  @Override
  public LispObject get() {
    LispObject headValue = head.get();
    FunctionObject functionObj = headValue.asFunction();

    if (functionObj == null) {
      throw new WrongTypeToApplyException(headValue);
    }

    Function function = functionObj.function();
    LispObject p0 = param0.get();
    LispObject p1 = param1.get();

    LispObject[] params;
    if (function instanceof LambdaFunction) {
      // Use pool for lambda functions - stack-based allocation handles recursion
      ParameterArrayPool pool = ParameterArrayPool.get();
      params = pool.acquire2();
      params[0] = p0;
      params[1] = p1;

      env.addFuncCall(functionObj.functionName(), ref);
      LispObject result;
      try {
        result = function.evaluate(params);
      } catch (RuntimeException | Error e) {
        // On exception, release pool but don't pop stack trace
        pool.release2();
        throw e;
      }
      env.popFuncCall();
      pool.release2();
      return result;
    } else {
      // Built-in functions consume values immediately, use ThreadLocal
      params = PARAMS_HOLDER.get();
      params[0] = p0;
      params[1] = p1;

      env.addFuncCall(functionObj.functionName(), ref);
      LispObject result = function.evaluate(params);
      env.popFuncCall();
      return result;
    }
  }
}
