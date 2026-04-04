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
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.IdentifierObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Implements R7RS 'letrec*' - sequential recursive bindings.
 *
 * <p>Like letrec, but with left-to-right evaluation order guarantee. Each binding is evaluated in
 * sequence, and later expressions can reference earlier bindings (not just for lambdas).
 *
 * <p>Example: (letrec* ((a 1) (b (+ a 1)) ; Can reference 'a' since it's evaluated first (c (+ b
 * 1))) c) ; => 3
 *
 * @author danilo
 */
public class LetrecStarForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public LetrecStarForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject list = obj.asList().cdr();

    LispObject head = list.car();
    ListObject body = list.cdr();

    // Parse bindings: ((var1 val1) (var2 val2) ...)
    List<AtomObject> names = new ArrayList<>();
    List<LispObject> valueExprs = new ArrayList<>();

    for (LispObject elem : head.asList()) {
      ListObject tuple = elem.asList();
      AtomObject name = extractAtom(tuple.car());
      LispObject valueExpr = tuple.cdr().car();

      names.add(name);
      valueExprs.add(valueExpr);
    }

    // Apply TCO optimization for ALL function names to each lambda binding
    // This enables mutual recursion with TCO
    for (int i = 0; i < valueExprs.size(); i++) {
      LispObject valueExpr = valueExprs.get(i);
      for (AtomObject targetName : names) {
        optimizeTailCallsForLambda(targetName, valueExpr);
      }
    }

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : body) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      // Create a new environment for the letrec* bindings
      Environment letEnv = new LambdaForm.MapEnvironment();

      // First, bind all names to undefined (allows forward references for lambdas)
      for (AtomObject name : names) {
        letEnv.set(name, ListObject.NIL);
      }

      // Push environment so value expressions can see all names
      environment.pushLetEnv(letEnv);

      try {
        // Evaluate expressions left-to-right, updating bindings immediately
        // This allows later expressions to reference earlier bindings
        for (int i = 0; i < names.size(); i++) {
          Supplier<LispObject> valueSupplier = valueExprs.get(i).accept(evaluator);
          LispObject value = valueSupplier.get();
          letEnv.set(names.get(i), value);
        }

        // Evaluate body expressions, return the last one
        LispObject result = ListObject.NIL;
        for (Supplier<LispObject> supplier : bodySuppliers) {
          result = supplier.get();
        }
        return result;
      } finally {
        // Pop the let environment
        environment.popLetEnv();
      }
    };
  }

  /** Extracts the AtomObject from either an AtomObject or IdentifierObject. */
  private AtomObject extractAtom(LispObject obj) {
    if (obj.asAtom() != null) {
      return obj.asAtom();
    }
    IdentifierObject id = obj.asIdentifier();
    if (id != null) {
      return id.asAtom();
    }
    return null;
  }

  /**
   * Applies TCO optimization to a lambda expression bound to the given name. This enables tail call
   * optimization for recursive functions defined in letrec*.
   */
  private void optimizeTailCallsForLambda(AtomObject name, LispObject valueExpr) {
    ListObject lambdaList = valueExpr.asList();
    if (lambdaList == null || lambdaList.length() < 2) {
      return;
    }

    AtomObject head = extractAtom(lambdaList.car());
    if (head == null || head.specialForm() != SpecialFormEnum.LAMBDA) {
      return;
    }

    // Get the lambda body: skip 'lambda' and parameters
    // (lambda (params...) body...) -> body is cdr().cdr()
    ListObject body = lambdaList.cdr().cdr();
    TcoOptimizer optm = new TcoOptimizer(body, name, environment);
    optm.optimize();
  }
}
