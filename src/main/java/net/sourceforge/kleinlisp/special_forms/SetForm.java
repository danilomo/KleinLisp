package net.sourceforge.kleinlisp.special_forms;

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 *
 * @author danilo
 */
public class SetForm implements SpecialForm {

    private final Evaluator evaluator;  
    private final Environment environment;

    public SetForm(Evaluator evaluator, Environment environment) {
        this.evaluator = evaluator;
        this.environment = environment;
    }

    @Override
    public Supplier<LispObject> apply(LispObject obj) {
        ListObject list = obj.asList().get().cdr();
        
        LispObject symbol = list.car();      
        Supplier<LispObject> value = list.cdr().car().accept(evaluator);
        
        return () -> {
            LispObject o = symbol;
            LispObject val = value.get();
            
            o.set(val);
            
            return VoidObject.VOID;
        };
    }
    
}
