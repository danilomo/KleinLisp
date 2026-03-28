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
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Implements R7RS do loop.
 *
 * <pre>
 * (do ((var init step) ...)
 *     (test result ...)
 *   body ...)
 * </pre>
 *
 * @author danilo
 */
public class DoForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public DoForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject list = obj.asList().cdr(); // Skip "do" keyword

    if (list.length() < 2) {
      throw new IllegalArgumentException("do: requires at least bindings and test clause");
    }

    // Parse bindings: ((var init step) ...)
    LispObject bindingsObj = list.car();
    if (bindingsObj.asList() == null) {
      throw new IllegalArgumentException("do: bindings must be a list");
    }
    ListObject bindingsList = bindingsObj.asList();

    // Parse test clause: (test result ...)
    LispObject testClauseObj = list.cdr().car();
    if (testClauseObj.asList() == null || testClauseObj.asList() == ListObject.NIL) {
      throw new IllegalArgumentException("do: test clause must be a non-empty list");
    }
    ListObject testClause = testClauseObj.asList();

    LispObject testExpr = testClause.car();
    ListObject resultExprs = testClause.cdr();

    // Body expressions (optional)
    ListObject bodyExprs = list.cdr().cdr();

    // Parse variable bindings
    List<AtomObject> varNames = new ArrayList<>();
    List<Supplier<LispObject>> initSuppliers = new ArrayList<>();
    List<Supplier<LispObject>> stepSuppliers = new ArrayList<>(); // null means no step

    for (LispObject binding : bindingsList) {
      if (binding.asList() == null) {
        throw new IllegalArgumentException("do: each binding must be a list");
      }
      ListObject bindingList = binding.asList();

      if (bindingList.length() < 2 || bindingList.length() > 3) {
        throw new IllegalArgumentException(
            "do: binding must have 2 or 3 elements (var init [step])");
      }

      LispObject varObj = bindingList.car();
      AtomObject atom = varObj.asAtom();
      if (atom == null && varObj.asIdentifier() != null) {
        atom = varObj.asIdentifier().asAtom();
      }
      if (atom == null) {
        throw new IllegalArgumentException("do: variable must be an identifier");
      }
      varNames.add(atom);
      initSuppliers.add(bindingList.cdr().car().accept(evaluator));

      if (bindingList.length() == 3) {
        stepSuppliers.add(bindingList.cdr().cdr().car().accept(evaluator));
      } else {
        stepSuppliers.add(null); // No step expression
      }
    }

    // Compile test expression
    Supplier<LispObject> testSupplier = testExpr.accept(evaluator);

    // Compile result expressions
    List<Supplier<LispObject>> resultSuppliers = new ArrayList<>();
    for (LispObject expr : resultExprs) {
      resultSuppliers.add(expr.accept(evaluator));
    }

    // Compile body expressions
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    for (LispObject expr : bodyExprs) {
      bodySuppliers.add(expr.accept(evaluator));
    }

    return () -> {
      // Create a new environment for loop variables
      Environment loopEnv = new LambdaForm.MapEnvironment();

      // Initialize variables - all init expressions are evaluated in the outer environment
      for (int i = 0; i < varNames.size(); i++) {
        LispObject initValue = initSuppliers.get(i).get();
        loopEnv.set(varNames.get(i), initValue);
      }

      // Push the loop environment onto the stack
      environment.pushLetEnv(loopEnv);

      try {
        // Main loop
        while (true) {
          // Evaluate test
          LispObject testResult = testSupplier.get();
          if (testResult.truthiness()) {
            // Test is true - evaluate result expressions and return
            LispObject result = VoidObject.VOID;
            for (Supplier<LispObject> supplier : resultSuppliers) {
              result = supplier.get();
            }
            return result;
          }

          // Test is false - evaluate body
          for (Supplier<LispObject> supplier : bodySuppliers) {
            supplier.get();
          }

          // Compute step values (all in parallel using current values)
          LispObject[] newValues = new LispObject[varNames.size()];
          for (int i = 0; i < varNames.size(); i++) {
            if (stepSuppliers.get(i) != null) {
              newValues[i] = stepSuppliers.get(i).get();
            } else {
              newValues[i] = loopEnv.lookupValue(varNames.get(i));
            }
          }

          // Update variables with new values
          for (int i = 0; i < varNames.size(); i++) {
            loopEnv.set(varNames.get(i), newValues[i]);
          }
        }
      } finally {
        // Pop the loop environment
        environment.popLetEnv();
      }
    };
  }
}
