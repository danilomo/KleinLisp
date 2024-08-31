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
        LispObject transformed = ClosureVisitor.addClosureMeta(obj);
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
        LispObject head = list.head();

        Optional<SpecialForm> form = forms.get(head);

        if (form.isPresent()) {
            return form.get().apply(list);
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
