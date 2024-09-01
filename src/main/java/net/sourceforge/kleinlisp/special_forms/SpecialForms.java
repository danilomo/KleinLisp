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

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.sourceforge.kleinlisp.LispEnvironment;

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
        this.insertForm("let", new LetForm(evaluator, environment));
        this.insertForm("lambda", new LambdaForm(evaluator, environment));
        this.insertForm("define", new DefineForm(evaluator, environment));
        this.insertForm("set!", new SetForm(evaluator, environment));
        this.insertForm("begin", new BeginForm(evaluator));
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
        if (obj instanceof AtomObject) {
            return get((AtomObject) obj);
        }

        return Optional.empty();
    }
}
