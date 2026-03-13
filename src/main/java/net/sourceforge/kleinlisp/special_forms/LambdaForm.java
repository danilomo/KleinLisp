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

      int capturedCount = closureInfo.size();

      // Use inline environments for 1-2 captured variables
      if (capturedCount == 1) {
        Map.Entry<AtomObject, Integer> entry = closureInfo.entrySet().iterator().next();
        AtomObject id = entry.getKey();
        int parIndex = entry.getValue();

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
          value = env.lookupValue(id);
        }

        return new InlineEnvironment1(id, value);
      }

      if (capturedCount == 2) {
        Iterator<Map.Entry<AtomObject, Integer>> it = closureInfo.entrySet().iterator();

        Map.Entry<AtomObject, Integer> e1 = it.next();
        AtomObject id1 = e1.getKey();
        int parIndex1 = e1.getValue();
        LispObject value1;
        if (parIndex1 >= 0) {
          CellObject cell = new CellObject();
          cell.set(parameters[parIndex1]);
          value1 = cell;
        } else if (parIndex1 < -1) {
          int regularParamCount = -(parIndex1 + 2);
          value1 = buildRestList(parameters, regularParamCount);
        } else {
          value1 = env.lookupValue(id1);
        }

        Map.Entry<AtomObject, Integer> e2 = it.next();
        AtomObject id2 = e2.getKey();
        int parIndex2 = e2.getValue();
        LispObject value2;
        if (parIndex2 >= 0) {
          CellObject cell = new CellObject();
          cell.set(parameters[parIndex2]);
          value2 = cell;
        } else if (parIndex2 < -1) {
          int regularParamCount = -(parIndex2 + 2);
          value2 = buildRestList(parameters, regularParamCount);
        } else {
          value2 = env.lookupValue(id2);
        }

        return new InlineEnvironment2(id1, value1, id2, value2);
      }

      // Use IndexedEnvironment for 3+ captured variables - O(1) slot access
      Map<AtomObject, Integer> slotIndices = meta.getClosureSlotIndices();
      LispObject[] slots = new LispObject[slotIndices.size()];

      for (Map.Entry<AtomObject, Integer> entry : slotIndices.entrySet()) {
        AtomObject id = entry.getKey();
        int slotIndex = entry.getValue();
        int parIndex = closureInfo.get(id);

        if (parIndex >= 0) {
          CellObject cell = new CellObject();
          cell.set(parameters[parIndex]);
          slots[slotIndex] = cell;
        } else if (parIndex < -1) {
          int regularParamCount = -(parIndex + 2);
          slots[slotIndex] = buildRestList(parameters, regularParamCount);
        } else {
          slots[slotIndex] = env.lookupValue(id);
        }
      }

      return new IndexedEnvironment(slots, slotIndices, null);
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

      LambdaFunction function = new LambdaFunction(functionSupplier, meta, env);
      FunctionObject functionObj = new FunctionObject(function);

      functionObj.setIdentifier(id);

      return functionObj;
    };
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
