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
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.ClosureVisitor;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
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
    List<LispObject> parameters = new ArrayList<>();
    List<LispObject> values = new ArrayList<>();
    ListObject list = obj.asList().cdr();

    LispObject head = list.car();
    LispObject tail = list.cdr();

    for (LispObject elem : head.asList()) {
      ListObject tuple = elem.asList();
      parameters.add(tuple.car());
      values.add(tuple.cdr().car());
    }

    ListObject lambda =
        new ListObject(
            environment.atomOf("lambda"), new ListObject(ListObject.fromList(parameters), tail));

    LispObject transformedExp = new ListObject(lambda, ListObject.fromList(values));
    transformedExp = ClosureVisitor.addClosureMeta(transformedExp);
    return transformedExp.accept(evaluator);
  }
}
