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
package net.sourceforge.kleinlisp.special_forms;

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.PromiseObject;

/**
 * Implements R7RS delay special form.
 *
 * <p>(delay expr) creates a promise that will evaluate expr when forced. The expression captures
 * the current execution context (stack frame) at the time delay is evaluated, so that lambda
 * parameters can be properly resolved when the promise is forced.
 */
public class DelayForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public DelayForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject list = t.asList();
    ListObject args = list.cdr();

    if (args == ListObject.NIL) {
      throw new LispArgumentError("delay: requires an expression");
    }

    if (args.length() > 1) {
      throw new LispArgumentError("delay: expects exactly one expression");
    }

    LispObject expression = args.car();
    // Compile the expression to a supplier (lazy evaluation)
    Supplier<LispObject> exprSupplier = expression.accept(evaluator);

    // Return a supplier that creates the promise, capturing the current execution context
    return () -> {
      // Capture current stack frame at the time delay is evaluated
      LispObject[] capturedParams = null;
      Environment capturedEnv = null;

      if (!environment.isStackEmpty()) {
        LispEnvironment.FunctionStack stackTop = environment.stackTop();
        // Make a copy of the parameters to capture their current values
        LispObject[] params = stackTop.getParameters();
        if (params != null) {
          capturedParams = params.clone();
        }
        capturedEnv = stackTop.getEnv();
      }

      // Create the promise with a supplier that restores the captured context
      final LispObject[] finalParams = capturedParams;
      final Environment finalEnv = capturedEnv;

      Supplier<LispObject> contextualSupplier =
          () -> {
            if (finalParams != null) {
              // Push the captured context onto the stack before evaluation
              environment.stackPush(finalParams, finalEnv);
              try {
                return exprSupplier.get();
              } finally {
                environment.stackPop();
              }
            } else {
              // No captured context, evaluate directly
              return exprSupplier.get();
            }
          };

      return new PromiseObject(contextualSupplier);
    };
  }
}
