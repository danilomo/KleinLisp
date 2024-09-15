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
package net.sourceforge.kleinlisp.evaluator;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.objects.*;
import net.sourceforge.kleinlisp.special_forms.SpecialForm;
import net.sourceforge.kleinlisp.special_forms.SpecialForms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;

/**
 * @author danilo
 */
public class Evaluator implements LispVisitor<Supplier<LispObject>> {

    private final SpecialForms forms;
    private final LispEnvironment environment;

    public Evaluator(LispEnvironment environment) {
        this.environment = environment;
        this.forms = new SpecialForms(environment, this);
    }

    public LispObject evaluate(LispObject obj) {

        LispObject transformed = ClosureVisitor
                .addClosureMeta(environment.expandMacros(
                        obj
                ));
        return transformed.accept(this).get();
    }

    @Override
    public Supplier<LispObject> visit(AtomObject obj) {
        return () -> environment.lookupValue(obj);
    }

    @Override
    public Supplier<LispObject> visit(BooleanObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(DoubleObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(IntObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(JavaObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(ListObject list) {
        if (list == ListObject.NIL) {
            return () -> ListObject.NIL;
        }

        LispObject head = list.head();

        Optional<SpecialForm> form = forms.get(head);

        if (form.isPresent()) {
            return form.get().apply(list);
        }
        
        Supplier<LispObject> commonCase = CommonCases.apply(list, this);
        
        if (commonCase != null) {
            return commonCase;
        }

        Supplier<LispObject> headEval = head.accept(this);
        List<Supplier<LispObject>> parameters = new ArrayList<>();

        for (LispObject obj : list.cdr()) {
            parameters.add(obj.accept(this));
        }

        return new FunctionCall(headEval, parameters);
    }

    @Override
    public Supplier<LispObject> visit(StringObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(FunctionObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(ErrorObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(VoidObject obj) {
        return () -> obj;
    }

    @Override
    public Supplier<LispObject> visit(ComputedLispObject obj) {
        return obj.getComputed();
    }

    @Override
    public Supplier<LispObject> visit(CellObject obj) {
        return () -> obj.get();
    }

}
