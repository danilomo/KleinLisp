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
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Implements 'letrec' for recursive bindings.
 * All bindings are visible to all value expressions, allowing mutual recursion.
 * (letrec ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1)))))
 *          (odd? (lambda (n) (if (= n 0) #f (even? (- n 1))))))
 *   body...)
 *
 * @author danilo
 */
public class LetrecForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public LetrecForm(Evaluator evaluator, LispEnvironment environment) {
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
      AtomObject name = tuple.car().asAtom();
      LispObject valueExpr = tuple.cdr().car();

      names.add(name);
      valueExprs.add(valueExpr);
    }

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : body) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      // Create a new environment for the letrec bindings
      Environment letEnv = new LambdaForm.MapEnvironment();

      // First, bind all names to undefined (allows forward references)
      for (AtomObject name : names) {
        letEnv.set(name, ListObject.NIL);
      }

      // Push environment so value expressions can see all names
      environment.pushLetEnv(letEnv);

      try {
        // Now evaluate all expressions and update bindings
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
}
