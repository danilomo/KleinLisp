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
 * Implements R7RS delay-force special form.
 *
 * <p>(delay-force expr) creates a promise that will iteratively force the result when forced. The
 * expression captures the current execution context (stack frame) at the time delay-force is
 * evaluated, so that lambda parameters can be properly resolved when the promise is forced.
 *
 * <p>Unlike delay, delay-force continues forcing if the result is a promise. This prevents stack
 * overflow on long lazy sequences and is the proper way to implement iterative lazy algorithms.
 *
 * <p>Example:
 *
 * <pre>
 * (define (stream-filter p? s)
 *   (delay-force
 *     (if (null? (force s))
 *         (delay '())
 *         (let ((h (car (force s)))
 *               (t (cdr (force s))))
 *           (if (p? h)
 *               (delay (cons h (stream-filter p? t)))
 *               (stream-filter p? t))))))
 * </pre>
 */
public class DelayForceForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public DelayForceForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject list = t.asList();
    ListObject args = list.cdr();

    if (args == ListObject.NIL) {
      throw new LispArgumentError("delay-force: requires an expression");
    }

    if (args.length() > 1) {
      throw new LispArgumentError("delay-force: expects exactly one expression");
    }

    LispObject expression = args.car();
    // Compile the expression to a supplier (lazy evaluation)
    Supplier<LispObject> exprSupplier = expression.accept(evaluator);

    // Return a supplier that creates the promise, capturing the current execution context
    return () -> {
      // Capture current stack frame at the time delay-force is evaluated
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

      // Capture the current let environment stack (for let bindings)
      final java.util.List<Environment> capturedLetEnvs = environment.captureLetEnvStack();

      // Create the promise with a supplier that restores the captured context
      final LispObject[] finalParams = capturedParams;
      final Environment finalEnv = capturedEnv;

      Supplier<LispObject> contextualSupplier =
          () -> {
            // Restore let environment stack first
            environment.restoreLetEnvStack(capturedLetEnvs);
            int letEnvCount = capturedLetEnvs != null ? capturedLetEnvs.size() : 0;

            try {
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
            } finally {
              // Pop the restored let environments
              environment.popLetEnvs(letEnvCount);
            }
          };

      // Create promise with iterative forcing enabled
      return new PromiseObject(contextualSupplier, true);
    };
  }
}
