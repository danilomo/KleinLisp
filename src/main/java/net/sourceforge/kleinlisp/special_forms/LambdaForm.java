/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sourceforge.kleinlisp.special_forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.CompositeEnvironment;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.ClosureVisitor.LambdaMeta;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.CellObject;
import net.sourceforge.kleinlisp.objects.ComputedLispObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IdentifierObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * @author Danilo Oliveira
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

  public class LambdaFunction implements Function {

    private final Supplier<LispObject> body;
    private final LambdaMeta meta;
    private final Environment env;

    public LambdaFunction(Supplier<LispObject> body, LambdaMeta meta, Environment env) {
      this.body = body;
      this.meta = meta;
      this.env = env;
    }

    @Override
    public LispObject evaluate(LispObject[] parameters) {
      Environment closureEnv = upvaluesObj(parameters);

      Environment cenv;

      if (closureEnv == null) {
        cenv = env;
      } else if (env == null) {
        cenv = closureEnv;
      } else {
        cenv = new CompositeEnvironment(closureEnv, env);
      }

      environment.stackPush(parameters, cenv);
      LispObject result = body.get();
      environment.stackPop();

      return result;
    }

    public LispObject evaluateTailCall(LispObject[] parameters) {
      environment.setStackTop(parameters);

      return body.get();
    }

    private Environment upvaluesObj(LispObject[] parameters) {
      if (meta.getClosureInfo().isEmpty()) {
        return null;
      }

      Environment closureEnv = new MapEnvironment();

      for (AtomObject id : meta.getClosureInfo().keySet()) {
        int parIndex = meta.getClosureInfo().get(id);
        if (parIndex >= 0) {
          CellObject cell = new CellObject();
          cell.set(parameters[parIndex]);

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
    ListObject orig = obj.asList();
    IdentifierObject id = orig.car().asIdentifier();

    ListObject list = orig.cdr();
    LambdaMeta meta = orig.getMeta(LambdaMeta.class);

    ListObject body = list.cdr();
    Set<AtomObject> fromClosure = new HashSet<>(meta.getParent().getClosureInfo().keySet());
    fromClosure.addAll(meta.getClosureInfo().keySet());

    ListObject transformed = transformBodySymbols(body, meta.getParameters(), fromClosure);

    Supplier<LispObject> functionSupplier = evaluateBody(transformed);

    return () -> {
      Environment env = null;

      if (!environment.isStackEmpty()) {
        env = environment.stackTop().getEnv();
      }

      LambdaFunction function = new LambdaFunction(functionSupplier, meta, env);
      FunctionObject functionObj = new FunctionObject(function);

      functionObj.setIdentifier(id);

      return functionObj;
    };
  }

  private ListObject transformBodySymbols(
      ListObject body, List<AtomObject> fromParameters, Set<AtomObject> fromClosure) {
    DefaultVisitor visitor =
        new DefaultVisitor() {

          @Override
          public LispObject visit(IdentifierObject obj) {
            LispObject result = obj.asAtom().accept(this);

            if (result.asAtom() != null) {
              return new IdentifierObject(
                  result.asAtom(), obj.getSource(), obj.getLine(), obj.getCol());
            }

            return result;
          }

          @Override
          public LispObject visit(AtomObject obj) {

            if (fromClosure.contains(obj)) {
              return getValueFromClosure(obj);
            }

            for (int i = 0; i < fromParameters.size(); i++) {
              if (obj == fromParameters.get(i)) {
                return getParameterObj(i);
              }
            }

            return obj;
          }

          @Override
          public LispObject visit(ListObject obj) {
            if (obj == ListObject.NIL) {
              return obj;
            }

            AtomObject head = obj.car().asAtom();

            if (head == null) {
              return super.visit(obj);
            }

            if (head.specialForm() == SpecialFormEnum.LAMBDA) {
              return obj;
            }

            return super.visit(obj);
          }

          private LispObject getParameterObj(int i) {
            Supplier<LispObject> getter =
                () -> {
                  return environment.stackTop().parameterAt(i);
                };
            Consumer<LispObject> setter =
                (obj) -> {
                  environment.stackTop().setParameterAt(i, obj);
                };
            return new ComputedLispObject(getter, setter);
          }

          private LispObject getValueFromClosure(AtomObject id) {
            Supplier<LispObject> getter =
                () -> {
                  return environment.stackTop().getEnv().lookupValue(id);
                };
            Consumer<LispObject> setter =
                (obj) -> {
                  environment.stackTop().getEnv().lookupValue(id).set(obj);
                };
            return new ComputedLispObject(getter, setter);
          }
        };

    return body.accept(visitor).asList();
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
