package net.sourceforge.kleinlisp.special_forms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
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
        
        LispObject head = obj.asList().get().car();
        LispObject tail = obj.asList().get().cdr();
        
        for(LispObject elem: head.asList().get()) {
            ListObject tuple = elem.asList().get();
            parameters.add(tuple.car());
            values.add(tuple.cdr().car());            
        }
        
        ListObject lambda = new ListObject(
                environment.atomOf("lambda"),
                new ListObject(
                        ListObject.fromList(parameters),
                        tail
                )
        );
        
        LispObject transformedExp = new ListObject(
                lambda,
                ListObject.fromList(values)
        );
        
        return transformedExp.accept(evaluator);
    }
    
}
