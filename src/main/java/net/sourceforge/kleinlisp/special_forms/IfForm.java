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

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;

public class IfForm implements SpecialForm {

  private final Evaluator evaluator;

  public IfForm(Evaluator evaluator) {
    this.evaluator = evaluator;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject parameters = obj.asList();
    parameters = parameters.cdr();

    LispObject cond = parameters.car();
    LispObject trueForm = parameters.cdr().car();
    LispObject elseForm = parameters.cdr().cdr().car();

    final Supplier<LispObject> condS = cond.accept(evaluator);
    final Supplier<LispObject> trueS = trueForm.accept(evaluator);
    final Supplier<LispObject> elseS = elseForm.accept(evaluator);

    return () -> {
      if (condS.get().truthiness()) {
        return trueS.get();
      } else {
        return elseS.get();
      }
    };
  }
}
