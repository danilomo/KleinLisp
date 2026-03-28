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
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.ParameterObject;

/**
 * Implements R7RS parameterize special form.
 *
 * <p>(parameterize ((param value) ...) body ...)
 *
 * <p>Temporarily binds parameters to new values for the dynamic extent of body.
 */
public class ParameterizeForm implements SpecialForm {

  private final Evaluator evaluator;

  public ParameterizeForm(Evaluator evaluator) {
    this.evaluator = evaluator;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject list = obj.asList().cdr();

    if (list == ListObject.NIL) {
      throw new LispArgumentError("parameterize: requires bindings and body");
    }

    LispObject bindingsObj = list.car();
    ListObject body = list.cdr();

    if (body == ListObject.NIL) {
      throw new LispArgumentError("parameterize: requires at least one body expression");
    }

    if (bindingsObj.asList() == null) {
      throw new LispArgumentError("parameterize: bindings must be a list");
    }
    ListObject bindings = bindingsObj.asList();

    // Parse bindings: ((param value) ...)
    List<Supplier<LispObject>> paramSuppliers = new ArrayList<>();
    List<Supplier<LispObject>> valueSuppliers = new ArrayList<>();

    for (LispObject binding : bindings) {
      if (binding.asList() == null) {
        throw new LispArgumentError("parameterize: each binding must be a list");
      }
      ListObject bindingList = binding.asList();

      if (bindingList.length() != 2) {
        throw new LispArgumentError("parameterize: binding must be (parameter value)");
      }

      // Compile the parameter expression
      LispObject paramExpr = bindingList.car();
      paramSuppliers.add(paramExpr.accept(evaluator));

      // Compile the new value expression
      LispObject valueExpr = bindingList.cdr().car();
      valueSuppliers.add(valueExpr.accept(evaluator));
    }

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : body) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      // Evaluate parameter expressions and new values
      List<ParameterObject> parameters = new ArrayList<>();
      List<LispObject> newValues = new ArrayList<>();

      for (int i = 0; i < paramSuppliers.size(); i++) {
        LispObject paramObj = paramSuppliers.get(i).get();

        if (!(paramObj instanceof ParameterObject)) {
          throw new LispArgumentError("parameterize: " + paramObj + " is not a parameter");
        }

        LispObject newValue = valueSuppliers.get(i).get();

        parameters.add((ParameterObject) paramObj);
        newValues.add(newValue);
      }

      // Push new values onto all parameters
      for (int i = 0; i < parameters.size(); i++) {
        parameters.get(i).pushValue(newValues.get(i));
      }

      try {
        // Evaluate body expressions
        LispObject result = ListObject.NIL;
        for (Supplier<LispObject> supplier : bodySuppliers) {
          result = supplier.get();
        }
        return result;
      } finally {
        // Pop values from all parameters (in reverse order for safety)
        for (int i = parameters.size() - 1; i >= 0; i--) {
          parameters.get(i).popValue();
        }
      }
    };
  }
}
