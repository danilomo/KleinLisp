package net.sourceforge.kleinlisp.special_forms;

import java.util.Optional;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;


public class DefineForm implements SpecialForm {

    private final Evaluator evaluator;  
    private final LispEnvironment environment; 
    private final AtomObject lambdaAtom;

    public DefineForm(Evaluator evaluator, LispEnvironment environment) {
        this.evaluator = evaluator;
        this.environment = environment;
        this.lambdaAtom = environment.atomOf("lambda");
    }
    
    @Override
    public Supplier<LispObject> apply(LispObject t) {
        ListObject list = t.asList().get();
        list = list.cdr();
        LispObject first = list.car();
        
        Optional<AtomObject> idOpt = first.asAtom();
        
        if (idOpt.isPresent()) {
            AtomObject id = idOpt.get();
            LispObject value = list.cdr().car();
            environment.set(id, evaluator.evaluate(value));
        } else {
            ListObject signature = first.asList().get();
            
            defineFunction(signature, list.cdr());
        }

        return () -> VoidObject.VOID;
    }

    private void defineFunction(ListObject signature, LispObject value) {
        AtomObject id = signature.car().asAtom().get();
        LispObject parameters = signature.cdr();
        
        ListObject lambda = new ListObject(
                lambdaAtom,
                new ListObject(parameters, value)
        );
        LispObject function = evaluator.evaluate(lambda);
        
        environment.set(id, function);
    }

}
