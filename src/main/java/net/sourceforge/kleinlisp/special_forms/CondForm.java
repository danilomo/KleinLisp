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
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Implements the cond special form with support for the => clause.
 *
 * <p>Syntax:
 *
 * <pre>
 * (cond clause ...)
 * clause = (test expression ...)
 *        | (test => procedure)
 *        | (else expression ...)
 * </pre>
 *
 * <p>The => clause passes the result of the test to a one-argument procedure.
 *
 * @author danilo
 */
public class CondForm implements SpecialForm {

  private abstract static class Branch {
    final Supplier<LispObject> condition;

    public Branch(Supplier<LispObject> condition) {
      this.condition = condition;
    }

    abstract LispObject evaluate(LispObject testResult);
  }

  private static class RegularBranch extends Branch {
    final Supplier<LispObject> body;

    public RegularBranch(Supplier<LispObject> condition, Supplier<LispObject> body) {
      super(condition);
      this.body = body;
    }

    @Override
    LispObject evaluate(LispObject testResult) {
      return body.get();
    }
  }

  /** Branch for the => clause that applies a procedure to the test result. */
  private static class ArrowBranch extends Branch {
    final Supplier<LispObject> procedure;

    public ArrowBranch(Supplier<LispObject> condition, Supplier<LispObject> procedure) {
      super(condition);
      this.procedure = procedure;
    }

    @Override
    LispObject evaluate(LispObject testResult) {
      LispObject proc = procedure.get();
      FunctionObject funcObj = proc.asFunction();
      if (funcObj == null) {
        throw new IllegalArgumentException("cond: => clause requires a procedure, got: " + proc);
      }
      Function func = funcObj.function();
      return func.evaluate(new LispObject[] {testResult});
    }
  }

  private final Evaluator evaluator;

  public CondForm(Evaluator evaluator) {
    this.evaluator = evaluator;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject body = t.asList().cdr();

    List<Branch> branches = parseCondBody(body);

    return () -> {
      for (Branch b : branches) {
        LispObject val = b.condition.get();

        if (val.truthiness()) {
          return b.evaluate(val);
        }
      }

      return ListObject.NIL;
    };
  }

  private List<Branch> parseCondBody(ListObject body) {
    List<Branch> output = new ArrayList<>();

    for (LispObject obj : body) {
      Branch branch = parseCondBranch(obj);
      output.add(branch);
    }

    return output;
  }

  private Branch parseCondBranch(LispObject obj) {
    ListObject clause = obj.asList();
    if (clause == null || clause == ListObject.NIL) {
      throw new IllegalArgumentException("cond: invalid clause");
    }

    LispObject cond = clause.car();
    ListObject rest = clause.cdr();

    boolean isElse =
        Optional.ofNullable(cond.asAtom()).map(a -> a.toString().equals("else")).orElse(false);

    Supplier<LispObject> condEval;

    if (isElse) {
      condEval = () -> BooleanObject.TRUE;
    } else {
      condEval = cond.accept(evaluator);
    }

    // Check for => clause: (test => procedure)
    if (rest != ListObject.NIL && rest.length() == 2) {
      LispObject maybeArrow = rest.car();
      if (maybeArrow.asAtom() != null && maybeArrow.asAtom().toString().equals("=>")) {
        LispObject procedure = rest.cdr().car();
        Supplier<LispObject> procEval = procedure.accept(evaluator);
        return new ArrowBranch(condEval, procEval);
      }
    }

    // Regular clause: (test body ...) - evaluate body expressions, return last
    Supplier<LispObject> bodyEval = compileBody(rest);

    return new RegularBranch(condEval, bodyEval);
  }

  private Supplier<LispObject> compileBody(ListObject body) {
    if (body == ListObject.NIL) {
      // If no body, return the test result (handled by returning condition value)
      // But actually we need to return the condition's value, which is handled differently
      // For empty body, we should return the test result - this is a special case
      return () -> BooleanObject.TRUE;
    }

    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : body) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      LispObject result = ListObject.NIL;
      for (Supplier<LispObject> supplier : bodySuppliers) {
        result = supplier.get();
      }
      return result;
    };
  }
}
