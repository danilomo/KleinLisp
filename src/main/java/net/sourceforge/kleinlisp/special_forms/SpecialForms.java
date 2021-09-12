/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.special_forms;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author danilo
 */
public class SpecialForms {
    private final Evaluator evaluator;
    private final Environment environment;
    private final Map<AtomObject, SpecialForm> forms;

    public SpecialForms(Environment environment, Evaluator evaluator) {
        this.environment = environment;
        this.evaluator = evaluator;
        this.forms = new HashMap<>();
        initForms();
    }

    private void initForms() {
        this.insertForm("if", new IfForm(evaluator));
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
