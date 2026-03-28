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
 * Implements R7RS let*-values.
 *
 * <p>(let*-values (((var ...) expr) ...) body ...)
 *
 * <p>Like let-values, but bindings happen sequentially. Each expression can reference variables
 * from earlier bindings.
 *
 * @author danilo
 */
public class LetStarValuesForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public LetStarValuesForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject list = obj.asList().cdr();

    LispObject bindingsObj = list.car();
    ListObject body = list.cdr();

    if (body == ListObject.NIL) {
      throw new LispArgumentError("let*-values: requires at least one body expression");
    }

    // Parse bindings: (((var1 var2) expr1) ((var3) expr2) ...)
    List<List<AtomObject>> allNames = new ArrayList<>();
    List<LispObject> valueExprs = new ArrayList<>();

    for (LispObject binding : bindingsObj.asList()) {
      ListObject bindingList = binding.asList();

      if (bindingList.length() != 2) {
        throw new LispArgumentError("let*-values: binding must be ((vars ...) expr)");
      }

      // Parse formals: (var ...)
      List<AtomObject> names = parseFormals(bindingList.car());
      allNames.add(names);

      // Store expression (evaluated later in sequence)
      LispObject valueExpr = bindingList.cdr().car();
      valueExprs.add(valueExpr);
    }

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : body) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      // Create a new environment for the let*-values bindings
      Environment letEnv = new LambdaForm.MapEnvironment();
      environment.pushLetEnv(letEnv);

      try {
        // Evaluate each binding in sequence, adding to environment before next
        for (int i = 0; i < allNames.size(); i++) {
          List<AtomObject> names = allNames.get(i);

          // Re-evaluate the expression in the current environment context
          Supplier<LispObject> valueSupplier = valueExprs.get(i).accept(evaluator);
          LispObject result = valueSupplier.get();

          // Extract values
          LispObject[] values = extractValues(result, names.size());

          // Bind the values
          for (int j = 0; j < names.size(); j++) {
            letEnv.set(names.get(j), values[j]);
          }
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

  private List<AtomObject> parseFormals(LispObject formalsObj) {
    List<AtomObject> names = new ArrayList<>();

    for (LispObject formal : formalsObj.asList()) {
      AtomObject atom = formal.asAtom();
      if (atom == null) {
        throw new LispArgumentError("let*-values: each formal must be an identifier");
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
          "let*-values: expected " + expected + " values, got " + values.length);
    }

    return values;
  }
}
