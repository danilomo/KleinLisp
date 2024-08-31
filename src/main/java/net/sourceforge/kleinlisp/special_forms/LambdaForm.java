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
import net.sourceforge.kleinlisp.CompositeEnvironment;
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

    public static final MapEnvironment EMPTY_ENV = new MapEnvironment();

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

        @Override
        public String toString() {
            return "MapEnvironment{" + "map=" + map + '}';
        }
    }

    private class LambdaFunction implements Function {

        private final Supplier<LispObject> body;
        private final LambdaMeta meta;
        private final Environment env;

        public LambdaFunction(Supplier<LispObject> body, LambdaMeta meta, Environment env) {
            this.body = body;
            this.meta = meta;
            this.env = env;
        }

        @Override
        public LispObject evaluate(List<LispObject> parameters) {            
            Environment closureEnv = upvaluesObj(parameters);
            
            Environment cenv;
            
            if (closureEnv == null) {
                cenv = env;
            } else {
                cenv = new CompositeEnvironment(closureEnv, env);
            }
            
            environment.stackPush(parameters, cenv);

            LispObject result = body.get();
            environment.stackPop();

            return result;
        }

        private Environment upvaluesObj(List<LispObject> parameters) {
            if (meta.getClosureInfo().isEmpty()) {
                return null;
            }
            
            Environment closureEnv = new MapEnvironment();
            
            for (AtomObject id : meta.getClosureInfo().keySet()) {
                int parIndex = meta.getClosureInfo().get(id);
                if (parIndex >= 0) {
                    CellObject cell = new CellObject();
                    cell.set(parameters.get(parIndex));

                    closureEnv.set(id, cell);
                } else {
                    LispObject cell = env.lookupValue(id);

                    cell.set(cell);
                }
            }
            
            return closureEnv;
        }

        @Override
        public String toString() {
            return "LambdaFunction{" + "body=" + meta.getRepr() + ", meta=" + meta + ", env=" + env + '}';
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

        ListObject transformed = transformBodySymbols(
                body,
                meta.getParameters(),
                meta.getParent().getClosureInfo().keySet());

        Supplier<LispObject> functionSupplier = evaluateBody(transformed);

        return () -> {
            Environment env = new MapEnvironment();
            
            if (!environment.isStackEmpty()) {
                env = environment.stackTop().getEnv();
            }
            
            LambdaFunction function = new LambdaFunction(functionSupplier, meta, env);

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

            private LispObject getValueFromClosure(AtomObject id) {
                Supplier<LispObject> getter = () -> {
                    return environment.stackTop().getEnv().lookupValue(id);
                };
                Consumer<LispObject> setter = (obj) -> {
                    //environment.stackTop().getEnv().set(id, obj);
                    environment.stackTop().getEnv().lookupValue(id).set(obj);
                };
                return new ComputedLispObject(getter, setter);
            }

        };

        return body.accept(visitor).asList().get();
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
