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
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Short-circuit AND form.
 *
 * <p>(and expr1 expr2 ...) evaluates expressions left to right. Returns #f if any expression
 * evaluates to false; otherwise returns the value of the last expression. If no expressions are
 * provided, returns #t.
 */
public class AndForm implements SpecialForm {

  private final Evaluator evaluator;

  public AndForm(Evaluator evaluator) {
    this.evaluator = evaluator;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject parameters = obj.asList();
    parameters = parameters.cdr(); // Skip 'and' keyword

    // (and) with no arguments returns #t
    if (parameters == ListObject.NIL) {
      return () -> BooleanObject.TRUE;
    }

    // Build list of suppliers for each expression (lazy evaluation)
    final List<Supplier<LispObject>> expressions = new ArrayList<>();
    for (LispObject expr : parameters) {
      expressions.add(expr.accept(evaluator));
    }

    return () -> {
      LispObject result = BooleanObject.TRUE;
      for (Supplier<LispObject> exprSupplier : expressions) {
        result = exprSupplier.get();
        if (!result.truthiness()) {
          return BooleanObject.FALSE;
        }
      }
      return result;
    };
  }
}
