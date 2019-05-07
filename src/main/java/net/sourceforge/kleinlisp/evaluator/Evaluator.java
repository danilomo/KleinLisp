/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.ErrorObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.JavaObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;
import net.sourceforge.kleinlisp.special_forms.SpecialForm;
import net.sourceforge.kleinlisp.special_forms.SpecialForms;

/**
 *
 * @author danilo
 */
public class Evaluator implements LispVisitor<Supplier<LispObject>>{
    
    private Environment environment;
    private final SpecialForms forms;

    public Evaluator(Environment environment) {
        this.environment = environment;
        this.forms = new SpecialForms(environment, this);
    }  
    
    public LispObject evaluate(LispObject obj){
        return obj.accept(this).get();
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

        LispObject head = list.head();

        Optional<SpecialForm> form = forms.get(head);

        if(form.isPresent()){
            return form.get().apply(list.cdr());
        }

        Supplier<LispObject> headEval = head.accept(this);
        List<Supplier<LispObject>> parameters = new ArrayList<>();

        for(LispObject obj: list.cdr()){
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
    
}
