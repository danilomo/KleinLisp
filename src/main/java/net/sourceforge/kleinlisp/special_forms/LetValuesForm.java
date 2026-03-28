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
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.ValuesObject;

/**
 * Implements R7RS let-values.
 *
 * <p>(let-values (((var ...) expr) ...) body ...)
 *
 * <p>All expressions are evaluated in the outer environment, then all bindings happen
 * simultaneously.
 *
 * @author danilo
 */
public class LetValuesForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public LetValuesForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject list = obj.asList().cdr();

    LispObject bindingsObj = list.car();
    ListObject body = list.cdr();

    if (body == ListObject.NIL) {
      throw new LispArgumentError("let-values: requires at least one body expression");
    }

    // Parse bindings: (((var1 var2) expr1) ((var3) expr2) ...)
    List<List<AtomObject>> allNames = new ArrayList<>();
    List<Supplier<LispObject>> valueSuppliers = new ArrayList<>();

    for (LispObject binding : bindingsObj.asList()) {
      ListObject bindingList = binding.asList();

      if (bindingList.length() != 2) {
        throw new LispArgumentError("let-values: binding must be ((vars ...) expr)");
      }

      // Parse formals: (var ...)
      List<AtomObject> names = parseFormals(bindingList.car());
      allNames.add(names);

      // Compile expression
      LispObject valueExpr = bindingList.cdr().car();
      valueSuppliers.add(valueExpr.accept(evaluator));
    }

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : body) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      // Evaluate all binding expressions in the outer environment
      List<LispObject[]> allValues = new ArrayList<>();
      for (int i = 0; i < valueSuppliers.size(); i++) {
        LispObject result = valueSuppliers.get(i).get();
        LispObject[] values = extractValues(result, allNames.get(i).size());
        allValues.add(values);
      }

      // Create a new environment with all the bindings
      Environment letEnv = new LambdaForm.MapEnvironment();

      for (int i = 0; i < allNames.size(); i++) {
        List<AtomObject> names = allNames.get(i);
        LispObject[] values = allValues.get(i);

        for (int j = 0; j < names.size(); j++) {
          letEnv.set(names.get(j), values[j]);
        }
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

  private List<AtomObject> parseFormals(LispObject formalsObj) {
    List<AtomObject> names = new ArrayList<>();

    for (LispObject formal : formalsObj.asList()) {
      AtomObject atom = formal.asAtom();
      if (atom == null) {
        throw new LispArgumentError("let-values: each formal must be an identifier");
      }
      names.add(atom);
    }

    return names;
  }

  private LispObject[] extractValues(LispObject result, int expected) {
    LispObject[] values;

    if (result instanceof ValuesObject) {
      values = ((ValuesObject) result).getValues();
    } else {
      // Single value
      values = new LispObject[] {result};
    }

    if (values.length != expected) {
      throw new LispArgumentError(
          "let-values: expected " + expected + " values, got " + values.length);
    }

    return values;
  }
}
