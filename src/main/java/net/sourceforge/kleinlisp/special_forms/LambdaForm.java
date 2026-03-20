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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.CompositeEnvironment;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.IndexedEnvironment;
import net.sourceforge.kleinlisp.InlineEnvironment1;
import net.sourceforge.kleinlisp.InlineEnvironment2;
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
import net.sourceforge.kleinlisp.objects.TailCallObject;

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
    public LispObject lookupValueOrNull(AtomObject name) {
      return map.get(name);
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

      // Trampoline loop for TCO: handle tail calls without recursion
      while (result instanceof TailCallObject) {
        TailCallObject tailCall = (TailCallObject) result;
        LambdaFunction targetFunc = tailCall.getFunction();
        LispObject[] newParams = tailCall.getParameters();

        // Reuse the stack frame by updating parameters
        environment.setStackTop(newParams);
        result = targetFunc.body.get();
      }

      environment.stackPop();

      return result;
    }

    public LispObject evaluateTailCall(LispObject[] parameters) {
      environment.setStackTop(parameters);

      return body.get();
    }

    public Supplier<LispObject> getBody() {
      return body;
    }

    private Environment upvaluesObj(LispObject[] parameters) {
      Map<AtomObject, Integer> closureInfo = meta.getClosureInfo();

      if (closureInfo.isEmpty()) {
        return null;
      }

      // Filter out let-bound closure variables - they'll be captured when inner lambdas are created
      Set<AtomObject> letBoundClosureVars = meta.getLetBoundClosureVars();

      // Count non-let-bound variables
      int capturedCount = 0;
      for (AtomObject id : closureInfo.keySet()) {
        if (!letBoundClosureVars.contains(id)) {
          capturedCount++;
        }
      }

      if (capturedCount == 0) {
        return null;
      }

      // Build environment with only non-let-bound variables
      // Use MapEnvironment for simplicity (can optimize later if needed)
      MapEnvironment captured = new MapEnvironment();

      for (Map.Entry<AtomObject, Integer> entry : closureInfo.entrySet()) {
        AtomObject id = entry.getKey();
        int parIndex = entry.getValue();

        // Skip let-bound closure variables
        if (letBoundClosureVars.contains(id)) {
          continue;
        }

        LispObject value;
        if (parIndex >= 0) {
          CellObject cell = new CellObject();
          cell.set(parameters[parIndex]);
          value = cell;
        } else if (parIndex < -1) {
          // Rest parameter: parIndex = -(regularParamCount + 2)
          int regularParamCount = -(parIndex + 2);
          value = buildRestList(parameters, regularParamCount);
        } else {
          value = lookupClosureValue(id);
        }

        captured.set(id, value);
      }

      return captured;
    }

    /**
     * Look up a closure variable value from all available scopes. First checks the let environment
     * stack (for let-bound variables), then falls back to the parent closure environment. Values
     * from let bindings are wrapped in CellObject to ensure proper capture since the let
     * environment will be popped after the let form finishes.
     */
    private LispObject lookupClosureValue(AtomObject id) {
      // First check the let environment stack (for let-bound variables)
      LispObject letValue = environment.lookupInLetEnvStack(id);
      if (letValue != null) {
        // Wrap in CellObject to capture the value (let env will be popped later)
        CellObject cell = new CellObject();
        cell.set(letValue);
        return cell;
      }

      // Fall back to parent closure environment
      if (env != null) {
        return env.lookupValue(id);
      }

      // Fall back to global environment
      return environment.lookupValue(id);
    }

    private LispObject buildRestList(LispObject[] parameters, int startIndex) {
      if (startIndex >= parameters.length) {
        return ListObject.NIL;
      }
      ListObject result = ListObject.NIL;
      for (int i = parameters.length - 1; i >= startIndex; i--) {
        result = new ListObject(parameters[i], result);
      }
      return result;
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

    ListObject transformed =
        transformBodySymbols(body, meta.getParameters(), fromClosure, meta.getRestParameter());

    Supplier<LispObject> functionSupplier = evaluateBody(transformed);

    return () -> {
      Environment env = null;

      if (!environment.isStackEmpty()) {
        env = environment.stackTop().getEnv();
      }

      // Capture let-bound variables at lambda creation time.
      // These variables are stored in the let environment stack which will be popped
      // after the let form finishes, so we need to capture their values now.
      Environment letCapturedEnv = captureLetBoundVariables(fromClosure);
      if (letCapturedEnv != null) {
        if (env == null) {
          env = letCapturedEnv;
        } else {
          env = new CompositeEnvironment(letCapturedEnv, env);
        }
      }

      LambdaFunction function = new LambdaFunction(functionSupplier, meta, env);
      FunctionObject functionObj = new FunctionObject(function);

      functionObj.setIdentifier(id);

      return functionObj;
    };
  }

  /**
   * Captures let-bound variables that are referenced in closures. This is called at lambda creation
   * time to ensure that let-bound values are captured before the let environment is popped.
   *
   * @param closureVars The set of variables that may be captured from closure
   * @return An environment containing the captured let-bound variables, or null if none
   */
  private Environment captureLetBoundVariables(Set<AtomObject> closureVars) {
    if (!environment.hasLetEnv()) {
      return null;
    }

    MapEnvironment captured = null;

    for (AtomObject var : closureVars) {
      LispObject value = environment.lookupInLetEnvStack(var);
      if (value != null) {
        if (captured == null) {
          captured = new MapEnvironment();
        }
        // Wrap in CellObject to ensure proper closure semantics
        CellObject cell = new CellObject();
        cell.set(value);
        captured.set(var, cell);
      }
    }

    return captured;
  }

  private ListObject transformBodySymbols(
      ListObject body,
      List<AtomObject> fromParameters,
      Set<AtomObject> fromClosure,
      AtomObject restParameter) {
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
            // Check if this is the rest parameter (must be before closure check)
            if (restParameter != null && obj == restParameter) {
              return getRestParameterObj(fromParameters.size());
            }

            // Check closures (important for set! to work)
            if (fromClosure.contains(obj)) {
              return getValueFromClosure(obj);
            }

            // Check regular parameters
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

            // Handle let forms specially - don't transform binding names
            if (head.specialForm() == SpecialFormEnum.LET) {
              return visitLetForm(obj);
            }

            return super.visit(obj);
          }

          /**
           * Visit a let form, transforming binding values and body but not binding names.
           */
          private LispObject visitLetForm(ListObject obj) {
            // let form: (let ((var1 val1) (var2 val2) ...) body...)
            ListObject list = obj.cdr();
            LispObject bindingsObj = list.car();
            ListObject body = list.cdr();

            // Transform binding values (but not names)
            List<LispObject> newBindings = new ArrayList<>();
            if (bindingsObj.asList() != null) {
              for (LispObject binding : bindingsObj.asList()) {
                ListObject tuple = binding.asList();
                if (tuple != null && tuple.length() >= 2) {
                  LispObject name = tuple.car();  // Keep name as-is
                  LispObject valueExpr = tuple.cdr().car();
                  LispObject newValueExpr = valueExpr.accept(this);

                  // Rebuild binding tuple with original name
                  newBindings.add(new ListObject(name, new ListObject(newValueExpr, ListObject.NIL)));
                }
              }
            }

            // Transform body
            List<LispObject> newBody = new ArrayList<>();
            for (LispObject expr : body) {
              newBody.add(expr.accept(this));
            }

            // Rebuild bindings list
            ListObject newBindingsList = ListObject.NIL;
            for (int i = newBindings.size() - 1; i >= 0; i--) {
              newBindingsList = new ListObject(newBindings.get(i), newBindingsList);
            }

            // Rebuild body list
            ListObject newBodyList = ListObject.NIL;
            for (int i = newBody.size() - 1; i >= 0; i--) {
              newBodyList = new ListObject(newBody.get(i), newBodyList);
            }

            // Rebuild let form
            return new ListObject(obj.car(), new ListObject(newBindingsList, newBodyList));
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

          private LispObject getRestParameterObj(int startIndex) {
            Supplier<LispObject> getter =
                () -> {
                  LispObject[] params = environment.stackTop().getParameters();
                  if (startIndex >= params.length) {
                    return ListObject.NIL;
                  }
                  // Build a list from remaining parameters
                  ListObject result = ListObject.NIL;
                  for (int i = params.length - 1; i >= startIndex; i--) {
                    result = new ListObject(params[i], result);
                  }
                  return result;
                };
            Consumer<LispObject> setter =
                (obj) -> {
                  // Rest parameter is read-only
                  throw new UnsupportedOperationException("Cannot set rest parameter");
                };
            return new ComputedLispObject(getter, setter);
          }

          private LispObject getValueFromClosure(AtomObject id) {
            Supplier<LispObject> getter =
                () -> {
                  // First check the let environment stack (for let-bound variables)
                  LispObject letValue = environment.lookupInLetEnvStack(id);
                  if (letValue != null) {
                    return letValue;
                  }
                  // Fall back to closure environment
                  Environment env = environment.stackTop().getEnv();
                  if (env != null) {
                    return env.lookupValue(id);
                  }
                  // Fall back to global
                  return environment.lookupValue(id);
                };
            Consumer<LispObject> setter =
                (obj) -> {
                  // First check the let environment stack
                  LispObject letValue = environment.lookupInLetEnvStack(id);
                  if (letValue != null) {
                    environment.setInLetEnvStack(id, obj);
                    return;
                  }
                  // Fall back to closure environment
                  Environment env = environment.stackTop().getEnv();
                  if (env != null) {
                    env.lookupValue(id).set(obj);
                  }
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
