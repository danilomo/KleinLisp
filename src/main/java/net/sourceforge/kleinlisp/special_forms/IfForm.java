package net.sourceforge.kleinlisp.special_forms;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;

import java.util.function.Supplier;

public class IfForm implements SpecialForm {

    private final Evaluator evaluator;

    public IfForm(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Supplier<LispObject> apply(LispObject obj) {

        ListObject parameters = obj.asList().get();

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
