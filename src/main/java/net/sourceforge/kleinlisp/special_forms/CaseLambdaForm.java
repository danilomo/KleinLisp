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
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispException;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.ClosureVisitor;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Implements R7RS case-lambda, which creates a procedure that dispatches based on argument count.
 *
 * <p>Syntax: (case-lambda clause ...)
 *
 * <p>Each clause is: (formals body ...) where formals can be:
 *
 * <ul>
 *   <li>(x y z) - exact argument count
 *   <li>(x y . z) - minimum argument count with rest
 *   <li>args - any number of arguments (rest only)
 * </ul>
 *
 * @author Danilo Oliveira
 */
public class CaseLambdaForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public CaseLambdaForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject form = obj.asList();
    FormErrors.assertMinArgs("case-lambda", form, 1);

    ListObject clauses = form.cdr();
    List<ClauseInfo> clauseInfos = new ArrayList<>();

    // Parse all clauses
    for (LispObject clauseObj : clauses) {
      ListObject clause = clauseObj.asList();
      if (clause == null || clause == ListObject.NIL) {
        throw FormErrors.badForm("case-lambda", form);
      }

      LispObject formals = clause.car();
      ListObject body = clause.cdr();

      if (body == ListObject.NIL) {
        throw FormErrors.badForm("case-lambda", form);
      }

      ClauseInfo info = parseFormals(formals);
      info.formals = formals;
      info.body = body;
      clauseInfos.add(info);
    }

    // Create lambda forms for each clause at compile time
    List<Supplier<LispObject>> lambdaSuppliers = new ArrayList<>();
    for (ClauseInfo info : clauseInfos) {
      // Build a lambda form: (lambda formals body...)
      ListObject lambdaForm =
          new ListObject(environment.atomOf("lambda"), new ListObject(info.formals, info.body));

      // Add closure metadata (required by LambdaForm)
      LispObject processedLambda = ClosureVisitor.addClosureMeta(lambdaForm);

      // Get the supplier for this lambda (uses LambdaForm internally via evaluator)
      Supplier<LispObject> lambdaSupplier = processedLambda.accept(evaluator);
      lambdaSuppliers.add(lambdaSupplier);
    }

    // Return a supplier that creates the case-lambda function at runtime
    return () -> {
      // Evaluate all lambda suppliers to get actual function objects
      List<FunctionObject> lambdas = new ArrayList<>();
      for (Supplier<LispObject> supplier : lambdaSuppliers) {
        LispObject result = supplier.get();
        FunctionObject func = result.asFunction();
        if (func == null) {
          throw new LispException("case-lambda: internal error - expected function");
        }
        lambdas.add(func);
      }

      // Create the dispatching function
      Function caseLambdaFunc = new CaseLambdaFunction(clauseInfos, lambdas);
      return new FunctionObject(caseLambdaFunc);
    };
  }

  private ClauseInfo parseFormals(LispObject formals) {
    ClauseInfo info = new ClauseInfo();

    // Case 1: Single symbol (rest-only): args
    if (formals.asAtom() != null || formals.asIdentifier() != null) {
      info.minArgs = 0;
      info.hasRest = true;
      return info;
    }

    // Case 2: Empty list: ()
    if (formals == ListObject.NIL) {
      info.minArgs = 0;
      info.hasRest = false;
      return info;
    }

    // Case 3: Proper or improper list
    ListObject formalsList = formals.asList();
    if (formalsList == null) {
      throw new LispException("case-lambda: invalid formals");
    }

    int count = 0;
    ListObject current = formalsList;

    while (current != ListObject.NIL) {
      LispObject head = current.car();

      // Each formal must be a symbol
      if (head.asAtom() == null && head.asIdentifier() == null) {
        throw new LispException("case-lambda: invalid formal parameter");
      }

      count++;

      // Get tail - could be a ListObject (proper list) or atom/identifier (improper list)
      LispObject tailObj = current.tail();

      // Check for improper list (rest parameter)
      if (tailObj != ListObject.NIL && tailObj.asList() == null) {
        // Improper list - tailObj is the rest parameter symbol
        if (tailObj.asAtom() == null && tailObj.asIdentifier() == null) {
          throw new LispException("case-lambda: invalid rest parameter");
        }
        info.minArgs = count;
        info.hasRest = true;
        return info;
      }

      // Move to next element if it's a proper list
      if (tailObj.asList() != null) {
        current = tailObj.asList();
      } else {
        break;
      }
    }

    info.minArgs = count;
    info.hasRest = false;
    return info;
  }

  private static class ClauseInfo {
    int minArgs;
    boolean hasRest;
    LispObject formals;
    ListObject body;
  }

  private static class CaseLambdaFunction implements Function {
    private final List<ClauseInfo> clauseInfos;
    private final List<FunctionObject> lambdas;

    CaseLambdaFunction(List<ClauseInfo> clauseInfos, List<FunctionObject> lambdas) {
      this.clauseInfos = clauseInfos;
      this.lambdas = lambdas;
    }

    @Override
    public LispObject evaluate(LispObject[] parameters) {
      int argCount = parameters.length;

      // Find matching clause
      for (int i = 0; i < clauseInfos.size(); i++) {
        ClauseInfo info = clauseInfos.get(i);

        if (info.hasRest) {
          // With rest parameter: matches if argCount >= minArgs
          if (argCount >= info.minArgs) {
            return lambdas.get(i).function().evaluate(parameters);
          }
        } else {
          // Without rest: exact match required
          if (argCount == info.minArgs) {
            return lambdas.get(i).function().evaluate(parameters);
          }
        }
      }

      // No matching clause found
      throw new LispException("case-lambda: no matching clause for " + argCount + " argument(s)");
    }
  }
}
