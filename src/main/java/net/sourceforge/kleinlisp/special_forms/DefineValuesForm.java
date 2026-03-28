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
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.ValuesObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Implements R7RS define-values.
 *
 * <p>(define-values (var ...) expr)
 *
 * <p>Defines multiple variables at once from multiple values.
 *
 * @author danilo
 */
public class DefineValuesForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public DefineValuesForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject list = obj.asList().cdr();

    if (list.length() != 2) {
      throw new LispArgumentError("define-values: requires (formals) and expression");
    }

    // Parse formals: (var ...)
    List<AtomObject> names = parseFormals(list.car());

    // Compile expression
    LispObject valueExpr = list.cdr().car();
    Supplier<LispObject> valueSupplier = valueExpr.accept(evaluator);

    return () -> {
      // Evaluate expression
      LispObject result = valueSupplier.get();

      // Extract values
      LispObject[] values = extractValues(result, names.size());

      // Define variables in the global environment
      for (int i = 0; i < names.size(); i++) {
        environment.set(names.get(i), values[i]);
      }

      return VoidObject.VOID;
    };
  }

  private List<AtomObject> parseFormals(LispObject formalsObj) {
    List<AtomObject> names = new ArrayList<>();

    for (LispObject formal : formalsObj.asList()) {
      AtomObject atom = formal.asAtom();
      if (atom == null) {
        throw new LispArgumentError("define-values: each formal must be an identifier");
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
          "define-values: expected " + expected + " values, got " + values.length);
    }

    return values;
  }
}
