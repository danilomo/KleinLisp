package net.sourceforge.kleinlisp.special_forms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import static net.sourceforge.kleinlisp.evaluator.ClosureVisitor.LambdaMeta;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class LambdaForm implements SpecialForm {
      
    private final Evaluator evaluator;  
    private final LispEnvironment environment;  

    public LambdaForm(Evaluator evaluator, LispEnvironment environment) {
        this.evaluator = evaluator;
        this.environment = environment;
    }

    @Override
    public Supplier<LispObject> apply(LispObject obj) {
        ListObject list = obj.asList().get();
        LambdaMeta meta = list.getMeta(LambdaMeta.class);
        
        ListObject body = list.cdr().cdr();
        
        // body = parseBody(body); -> it will fix the symbols
        
        Supplier<LispObject> functionSupplier = evaluateBody(body);
        
        return () -> {
            Environment environment = upvaluesObj(meta);
            
            // function = function(body, environment);
            return null;
        };
    }

    private Environment upvaluesObj(LambdaMeta meta) {
        
    }
    
    private LispObject transformSymbols(LambdaMeta meta, LispObject body) {
        DefaultVisitor visitor = new DefaultVisitor() {
            
        };
        
        return null;
    }

    private Supplier<LispObject> evaluateBody(ListObject body) {
        List<Supplier<LispObject>> suppliers = new ArrayList<>();
        for (LispObject obj: body) {
            suppliers.add(obj.accept(evaluator));
        }
        
        return () -> {
            LispObject returnValue = null;
            
            for (Supplier<LispObject> supplier: suppliers) {
                returnValue = supplier.get();
            }
            
            return returnValue;
        };
    }
    
}
