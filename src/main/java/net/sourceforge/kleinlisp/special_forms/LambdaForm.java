package net.sourceforge.kleinlisp.special_forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import static net.sourceforge.kleinlisp.evaluator.ClosureVisitor.LambdaMeta;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.CellObject;
import net.sourceforge.kleinlisp.objects.ComputedLispObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author danilo
 */
public class LambdaForm implements SpecialForm {

    public static class MapEnvironment implements Environment {

        private final Map<AtomObject, LispObject> map = new HashMap<>();

        @Override
        public LispObject lookupValue(AtomObject name) {
            return map.getOrDefault(name, ListObject.NIL);
        }

        @Override
        public void set(AtomObject name, LispObject obj) {
            map.put(name, obj);
        }

        @Override
        public boolean exists(AtomObject name) {
            return map.containsKey(name);
        }
    }

    private class LambdaFunction implements Function {

        private final Supplier<LispObject> body;
        private final Environment env;

        public LambdaFunction(Supplier<LispObject> body, Environment env) {
            this.body = body;
            this.env = env;
        }

        @Override
        public LispObject evaluate(List<LispObject> parameters) {
            environment.stackPush(parameters, env);
            LispObject result = body.get();
            environment.stackPop();

            return result;
        }
    }

    private final Evaluator evaluator;
    private final LispEnvironment environment;

    public LambdaForm(Evaluator evaluator, LispEnvironment environment) {
        this.evaluator = evaluator;
        this.environment = environment;
    }

    @Override
    public Supplier<LispObject> apply(LispObject obj) {
        ListObject orig = obj.asList().get();
        ListObject list = orig.cdr();
        LambdaMeta meta = orig.getMeta(LambdaMeta.class);

        ListObject body = list.cdr();

        ListObject transformed = transformBodySymbols(body, meta.getParameters(), meta.getClosureInfo().keySet());
        Supplier<LispObject> functionSupplier = evaluateBody(transformed);
        
        return () -> {
            Environment closureEnv = upvaluesObj(meta);    
            LambdaFunction function = new LambdaFunction(functionSupplier, closureEnv);

            return new FunctionObject(function);
        };
    }

    private ListObject transformBodySymbols(ListObject body, List<AtomObject> fromParameters, Set<AtomObject> fromClosure) {
        DefaultVisitor visitor = new DefaultVisitor() {
            @Override
            public LispObject visit(AtomObject obj) {
                for (int i = 0; i < fromParameters.size(); i++) {
                    if (obj == fromParameters.get(i)) {
                        return getParameterObj(i);
                    }
                }
                
                if (fromClosure.contains(obj)) {
                    return getValueFromClosure(obj);
                }
                return obj;
            }

            @Override
            public LispObject visit(ListObject obj) {
                if (obj == ListObject.NIL) {
                    return obj;
                }
                Optional<AtomObject> head = obj.car().asAtom();

                if (head.isEmpty()) {
                    return super.visit(obj);
                }

                if (head.get().specialForm() == SpecialFormEnum.LAMBDA) {
                    return obj;
                }

                return super.visit(obj);
            }

            private LispObject getParameterObj(int i) {
                Supplier<LispObject> getter = () -> {
                    return environment.stackTop().parameterAt(i);
                };
                Consumer<LispObject> setter = (obj) -> {
                    environment.stackTop().setParameterAt(i, obj);
                };
                return new ComputedLispObject(getter, setter);
            }

            private LispObject getValueFromClosure(AtomObject obj) {
                Environment closureEnv = environment.stackTop().getEnv();
                
                return closureEnv
                        .lookupValue(obj);
            }

        };

        return body.accept(visitor).asList().get();
    }

    private Environment upvaluesObj(LambdaMeta meta) {
        Environment env = new MapEnvironment();
        
        for (AtomObject id: meta.getClosureInfo().keySet()) {
            int parIndex = meta.getClosureInfo().get(id);
            if (parIndex >= 0) {
                CellObject cell = new CellObject();
                cell.set(environment.stackTop().parameterAt(parIndex));
                
                env.set(id, cell);
            } else {
                LispObject cell = environment.stackTop().getEnv().lookupValue(id);
                
                env.set(id, cell);
            }
        }
        
        return env;
    }

    private Supplier<LispObject> evaluateBody(ListObject body) {
        List<Supplier<LispObject>> suppliers = new ArrayList<>();

        for (LispObject obj : body) {
            suppliers.add(obj.accept(evaluator));
        }

        return () -> {
            LispObject returnValue = null;

            for (Supplier<LispObject> supplier : suppliers) {
                returnValue = supplier.get();
            }

            return returnValue;
        };
    }

}
