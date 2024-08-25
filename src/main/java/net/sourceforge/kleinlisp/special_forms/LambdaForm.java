package net.sourceforge.kleinlisp.special_forms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ComputedLispObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class LambdaForm implements SpecialForm {
    
    private class LambdaFunction implements Function {
        private final LispObject body;

        public LambdaFunction(LispObject body) {
            this.body = body;
        }
                
        @Override
        public LispObject evaluate(List<LispObject> parameters) {
            environment.stackPush(parameters);
            LispObject result = evaluator.evaluate(body);
            environment.stackPop();
            
            return result;
        }        
    }
        
    private final Evaluator evaluator;  
    private final Environment environment;  

    public LambdaForm(Evaluator evaluator, Environment environment) {
        this.evaluator = evaluator;
        this.environment = environment;
    }

    @Override
    public Supplier<LispObject> apply(LispObject obj) {
        ListObject list = obj.asList().get();
        ListObject parameters = list.car().asList().get();
        LispObject body = list.cdr().car();

        Map<AtomObject, Integer> map = new HashMap<>();
        int i = 0;
        for (LispObject param : parameters) {
            AtomObject atom = param.asAtom().get();

            map.put(atom, i);
            i++;
        }

        LispObject pBody = processBody(body, map);

        return () -> {
            Function lambdaFunction = new LambdaFunction(pBody);
            return new FunctionObject(lambdaFunction);
        };
    }

    private LispObject processBody(LispObject body, Map<AtomObject, Integer> map) {
        LispVisitor<LispObject> visitor = new DefaultVisitor() {
            @Override
            public LispObject visit(AtomObject obj) {
                if (map.containsKey(obj)) {
                    Supplier<LispObject> supplier = getParam(map.get(obj));
                    
                    return new ComputedLispObject(supplier);
                }
                
                return obj;
            }
        };
        
        return body.accept(visitor);
    }
    
    private Supplier<LispObject> getParam(int pos) {
        return () -> {
            return environment.stackTop().get(pos);
        };
    }
    
}
