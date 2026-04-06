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
 * Implements 'let' with direct environment binding instead of lambda transformation. This allows
 * TCO to work correctly through let forms.
 *
 * <p>Note: Named let (let loop ((var init) ...) body...) is transformed to letrec by ClosureVisitor
 * before reaching this form.
 *
 * @author danilo
 */
public class LetForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public LetForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject form = obj.asList();
    FormErrors.assertMinArgs("let", form, 2);

    ListObject list = form.cdr();
    LispObject head = list.car();
    ListObject body = list.cdr();

    FormErrors.validateBindings("let", head, obj);

    // Parse bindings: ((var1 val1) (var2 val2) ...)
    List<AtomObject> names = new ArrayList<>();
    List<Supplier<LispObject>> valueSuppliers = new ArrayList<>();

    for (LispObject elem : head.asList()) {
      ListObject tuple = elem.asList();
      AtomObject name = tuple.car().asAtom();
      LispObject valueExpr = tuple.cdr().car();

      names.add(name);
      valueSuppliers.add(valueExpr.accept(evaluator));
    }

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : body) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      // Evaluate binding values
      LispObject[] values = new LispObject[valueSuppliers.size()];
      for (int i = 0; i < valueSuppliers.size(); i++) {
        values[i] = valueSuppliers.get(i).get();
      }

      // Create a new environment with the bindings
      Environment letEnv = new LambdaForm.MapEnvironment();
      for (int i = 0; i < names.size(); i++) {
        letEnv.set(names.get(i), values[i]);
      }

      // Push the let environment onto the stack
      environment.pushLetEnv(letEnv);

      try {
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
