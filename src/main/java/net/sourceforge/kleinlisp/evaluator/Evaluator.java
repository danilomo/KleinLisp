/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.evaluator;

import java.util.ArrayList;
import java.util.List;
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

/**
 *
 * @author danilo
 */
public class Evaluator implements LispVisitor<Supplier<LispObject>>{
    
    private Environment environment;

    public Evaluator(Environment environment) {
        this.environment = environment;
    }  
    
    public LispObject evaluate(LispObject obj){
        return obj.accept(this).get();
    }

    @Override
    public Supplier<LispObject> visit(AtomObject obj) {
        return () -> {
            return environment.lookupValue(obj);
        };
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
        Supplier<LispObject> head = list.head().accept(this);

        List<Supplier<LispObject>> parameters = new ArrayList<>();

        for(LispObject obj: list.cdr()){
            parameters.add(obj.accept(this));
        }

        return new FunctionCall(head, parameters);        
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
