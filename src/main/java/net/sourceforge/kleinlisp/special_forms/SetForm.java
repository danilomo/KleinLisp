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

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ComputedLispObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Implements set! special form.
 *
 * <p>Handles setting variables in different scopes:
 *
 * <ul>
 *   <li>ComputedLispObject: For lambda parameters and closure variables (transformed symbols)
 *   <li>Let/Do environment: For let-bound and do loop variables
 *   <li>Global environment: For top-level definitions
 * </ul>
 *
 * @author danilo
 */
public class SetForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public SetForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject list = obj.asList().cdr();

    LispObject symbol = list.car();
    Supplier<LispObject> value = list.cdr().car().accept(evaluator);

    // Check if symbol has been transformed to a ComputedLispObject (lambda params/closures)
    if (symbol instanceof ComputedLispObject) {
      return () -> {
        LispObject val = value.get();
        symbol.set(val);
        return VoidObject.VOID;
      };
    }

    // Get the atom for environment lookup
    AtomObject atom = symbol.asAtom();
    if (atom == null && symbol.asIdentifier() != null) {
      atom = symbol.asIdentifier().asAtom();
    }

    if (atom == null) {
      throw new IllegalArgumentException("set!: expected identifier, got " + symbol);
    }

    final AtomObject finalAtom = atom;

    return () -> {
      LispObject val = value.get();

      // First check if variable is in let/do environment stack
      LispObject letValue = environment.lookupInLetEnvStack(finalAtom);
      if (letValue != null) {
        environment.setInLetEnvStack(finalAtom, val);
      } else {
        // Fall back to global environment
        environment.set(finalAtom, val);
      }

      return VoidObject.VOID;
    };
  }
}
