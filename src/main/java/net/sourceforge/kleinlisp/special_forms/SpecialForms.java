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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 * @author danilo
 */
public class SpecialForms {
  private final Evaluator evaluator;
  private final LispEnvironment environment;
  private final Map<AtomObject, SpecialForm> forms;

  public SpecialForms(LispEnvironment environment, Evaluator evaluator) {
    this.environment = environment;
    this.evaluator = evaluator;
    this.forms = new HashMap<>();
    initForms();
  }

  private void initForms() {
    this.insertForm("if", new IfForm(evaluator));
    this.insertForm("lambda", new LambdaForm(evaluator, environment));
    this.insertForm("define", new DefineForm(evaluator, environment));
    this.insertForm("set!", new SetForm(evaluator, environment));
    this.insertForm("begin", new BeginForm(evaluator));
    this.insertForm("define-syntax", new DefineSyntaxForm(environment));
    this.insertForm("quote", new QuoteForm());
    this.insertForm("quasiquote", new QuasiquoteForm(evaluator, environment));
    this.insertForm("cond", new CondForm(evaluator));
    this.insertForm("case", new CaseForm(evaluator));
    this.insertForm("let", new LetForm(evaluator, environment));
    this.insertForm("let*", new LetStarForm(evaluator, environment));
    this.insertForm("letrec", new LetrecForm(evaluator, environment));
    this.insertForm("letrec*", new LetrecStarForm(evaluator, environment));
    this.insertForm("and", new AndForm(evaluator));
    this.insertForm("or", new OrForm(evaluator));
    this.insertForm("do", new DoForm(evaluator, environment));
    this.insertForm("delay", new DelayForm(evaluator, environment));
    this.insertForm("delay-force", new DelayForceForm(evaluator, environment));
    this.insertForm("guard", new GuardForm(evaluator, environment));
    this.insertForm("parameterize", new ParameterizeForm(evaluator));
    this.insertForm("let-values", new LetValuesForm(evaluator, environment));
    this.insertForm("let*-values", new LetStarValuesForm(evaluator, environment));
    this.insertForm("define-values", new DefineValuesForm(evaluator, environment));
    this.insertForm("let-syntax", new LetSyntaxForm(evaluator, environment));
    this.insertForm("letrec-syntax", new LetrecSyntaxForm(evaluator, environment));
    this.insertForm("case-lambda", new CaseLambdaForm(evaluator, environment));
    this.insertForm("cond-expand", new CondExpandForm(evaluator, environment));
    this.insertForm("syntax-error", new SyntaxErrorForm());
    this.insertForm("define-record-type", new DefineRecordTypeForm(environment));
    this.insertForm("import", new ImportForm(environment));
    this.insertForm("define-library", new DefineLibraryForm(evaluator, environment));
    this.insertForm("include", new IncludeForm(evaluator, environment));
    this.insertForm("include-ci", new IncludeCiForm(evaluator, environment));
  }

  SpecialForms insertForm(String name, SpecialForm form) {
    forms.put(environment.atomOf(name), form);
    return this;
  }

  public Optional<SpecialForm> get(AtomObject atom) {
    if (forms.containsKey(atom)) {
      return Optional.of(forms.get(atom));
    }

    return Optional.empty();
  }

  public Optional<SpecialForm> get(LispObject obj) {
    /*if (obj instanceof AtomObject) {
           return get((AtomObject) obj);
    }*/
    AtomObject atom = obj.asAtom();

    if (atom != null) {
      return get(atom);
    }

    return Optional.empty();
  }
}
