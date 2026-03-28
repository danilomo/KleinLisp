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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispRaisedException;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.ErrorObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Implements R7RS guard special form for exception handling.
 *
 * <pre>
 * (guard (variable
 *          (test expr ...)
 *          ...)
 *   body ...)
 * </pre>
 *
 * <p>Evaluates the body expressions. If an exception is raised (via raise or error), the exception
 * value is bound to 'variable' and the handler clauses are evaluated like cond clauses. The first
 * clause whose test returns true has its expressions evaluated and the result returned. If no
 * handler matches, the exception is re-raised.
 */
public class GuardForm implements SpecialForm {

  private static class Handler {
    final boolean isElse;
    final Supplier<LispObject> test;
    final List<Supplier<LispObject>> body;

    Handler(boolean isElse, Supplier<LispObject> test, List<Supplier<LispObject>> body) {
      this.isElse = isElse;
      this.test = test;
      this.body = body;
    }
  }

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public GuardForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject form = obj.asList();
    ListObject args = form.cdr(); // Skip 'guard'

    if (args.length() < 2) {
      throw new LispArgumentError("guard: requires exception clause and body");
    }

    // Parse guard clause: (variable (test expr ...) ...)
    LispObject clauseObj = args.car();
    if (!(clauseObj instanceof ListObject)) {
      throw new LispArgumentError("guard: clause must be a list");
    }
    ListObject clause = (ListObject) clauseObj;

    if (clause == ListObject.NIL) {
      throw new LispArgumentError("guard: clause cannot be empty");
    }

    LispObject varObj = clause.car();
    AtomObject varAtom = varObj.asAtom();
    if (varAtom == null) {
      throw new LispArgumentError("guard: first element must be variable name");
    }

    ListObject handlerClauses = clause.cdr(); // List of (test expr ...) clauses
    ListObject bodyExprs = args.cdr(); // Body expressions

    // Compile handlers
    List<Handler> handlers = compileHandlers(handlerClauses);

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : bodyExprs) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      try {
        // Evaluate body expressions, return last result
        LispObject result = ListObject.NIL;
        for (Supplier<LispObject> supplier : bodySuppliers) {
          result = supplier.get();
        }
        return result;
      } catch (LispRaisedException e) {
        return handleException(varAtom, handlers, e.getRaisedValue(), e);
      } catch (RuntimeException e) {
        // Wrap Java runtime exceptions in an ErrorObject
        ErrorObject errorObj = new ErrorObject(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        return handleException(varAtom, handlers, errorObj, e);
      }
    };
  }

  private LispObject handleException(
      AtomObject varAtom,
      List<Handler> handlers,
      LispObject exceptionValue,
      RuntimeException originalException) {
    // Create a new environment with the exception bound
    Environment handlerEnv = new LambdaForm.MapEnvironment();
    handlerEnv.set(varAtom, exceptionValue);

    // Push the handler environment
    environment.pushLetEnv(handlerEnv);

    try {
      // Try each handler clause
      for (Handler handler : handlers) {
        boolean matches;

        if (handler.isElse) {
          matches = true;
        } else {
          LispObject testResult = handler.test.get();
          matches = isTruthy(testResult);

          // If no body expressions and test matched, return test result (like cond)
          if (matches && handler.body.isEmpty()) {
            return testResult;
          }
        }

        if (matches) {
          // Evaluate handler body expressions
          LispObject result = ListObject.NIL;
          for (Supplier<LispObject> supplier : handler.body) {
            result = supplier.get();
          }
          return result;
        }
      }

      // No handler matched - re-raise the exception
      throw originalException;
    } finally {
      environment.popLetEnv();
    }
  }

  private List<Handler> compileHandlers(ListObject handlerClauses) {
    List<Handler> handlers = new ArrayList<>();

    for (LispObject handlerObj : handlerClauses) {
      if (!(handlerObj instanceof ListObject)) {
        throw new LispArgumentError("guard: each handler must be a list");
      }
      ListObject handlerList = (ListObject) handlerObj;

      if (handlerList == ListObject.NIL) {
        continue;
      }

      LispObject test = handlerList.car();
      ListObject bodyList = handlerList.cdr();

      // Check for else clause
      boolean isElse =
          test.asAtom() != null && test.asAtom().toString().equals("else");

      Supplier<LispObject> testSupplier;
      if (isElse) {
        testSupplier = () -> BooleanObject.TRUE;
      } else {
        testSupplier = test.accept(evaluator);
      }

      // Compile body expressions
      List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
      for (LispObject expr : bodyList) {
        bodySuppliers.add(expr.accept(evaluator));
      }

      handlers.add(new Handler(isElse, testSupplier, bodySuppliers));
    }

    return handlers;
  }

  private boolean isTruthy(LispObject obj) {
    return obj.truthiness();
  }
}
