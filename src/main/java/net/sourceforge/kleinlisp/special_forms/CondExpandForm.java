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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispException;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Implements R7RS cond-expand for feature-based conditional expansion.
 *
 * <p>Syntax: (cond-expand clause ...)
 *
 * <p>Each clause is: (feature-requirement expr ...) where feature-requirement can be:
 *
 * <ul>
 *   <li>feature-identifier - matches if feature is present
 *   <li>(and requirement ...) - matches if all requirements match
 *   <li>(or requirement ...) - matches if any requirement matches
 *   <li>(not requirement) - matches if requirement doesn't match
 *   <li>(library library-name) - matches if library is available (always true in KleinLisp)
 *   <li>else - always matches (must be last clause)
 * </ul>
 *
 * @author Danilo Oliveira
 */
public class CondExpandForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;
  private final Set<String> features;

  public CondExpandForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
    this.features = buildFeatureSet();
  }

  private Set<String> buildFeatureSet() {
    Set<String> featureSet = new HashSet<>();
    featureSet.add("r7rs");
    featureSet.add("kleinlisp");
    featureSet.add("java");

    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      featureSet.add("windows");
    } else if (os.contains("mac")) {
      featureSet.add("macosx");
      featureSet.add("posix");
    } else if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
      featureSet.add("posix");
    }

    return featureSet;
  }

  @Override
  public Supplier<LispObject> apply(LispObject obj) {
    ListObject form = obj.asList();
    FormErrors.assertMinArgs("cond-expand", form, 1);

    ListObject clauses = form.cdr();

    for (LispObject clauseObj : clauses) {
      ListObject clause = clauseObj.asList();
      if (clause == null || clause == ListObject.NIL) {
        throw FormErrors.badForm("cond-expand", form);
      }

      LispObject requirement = clause.car();
      ListObject body = clause.cdr();

      if (matchesRequirement(requirement)) {
        if (body == ListObject.NIL) {
          return () -> VoidObject.VOID;
        }
        return evaluateBody(body);
      }
    }

    throw new LispException("cond-expand: no matching clause");
  }

  private boolean matchesRequirement(LispObject requirement) {
    // Check for 'else' symbol
    AtomObject atom = requirement.asAtom();
    if (atom != null) {
      String name = atom.toString();
      if ("else".equals(name)) {
        return true;
      }
      return features.contains(name);
    }

    // Check for identifier (unbound symbol)
    if (requirement.asIdentifier() != null) {
      String name = requirement.asIdentifier().toString();
      if ("else".equals(name)) {
        return true;
      }
      return features.contains(name);
    }

    // Check for compound requirement: (and ...), (or ...), (not ...), (library ...)
    ListObject reqList = requirement.asList();
    if (reqList == null || reqList == ListObject.NIL) {
      throw new LispException("cond-expand: invalid feature requirement: " + requirement);
    }

    LispObject head = reqList.car();
    String headName = getSymbolName(head);

    if (headName == null) {
      throw new LispException("cond-expand: invalid feature requirement: " + requirement);
    }

    switch (headName) {
      case "and":
        return matchesAnd(reqList.cdr());
      case "or":
        return matchesOr(reqList.cdr());
      case "not":
        return matchesNot(reqList.cdr());
      case "library":
        return matchesLibrary(reqList.cdr());
      default:
        throw new LispException("cond-expand: unknown feature requirement operator: " + headName);
    }
  }

  private String getSymbolName(LispObject obj) {
    AtomObject atom = obj.asAtom();
    if (atom != null) {
      return atom.toString();
    }
    if (obj.asIdentifier() != null) {
      return obj.asIdentifier().toString();
    }
    return null;
  }

  private boolean matchesAnd(ListObject requirements) {
    if (requirements == ListObject.NIL) {
      return true;
    }
    for (LispObject req : requirements) {
      if (!matchesRequirement(req)) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesOr(ListObject requirements) {
    if (requirements == ListObject.NIL) {
      return false;
    }
    for (LispObject req : requirements) {
      if (matchesRequirement(req)) {
        return true;
      }
    }
    return false;
  }

  private boolean matchesNot(ListObject requirements) {
    if (requirements == ListObject.NIL || requirements.length() != 1) {
      throw new LispException("cond-expand: not requires exactly one argument");
    }
    return !matchesRequirement(requirements.car());
  }

  private boolean matchesLibrary(ListObject librarySpec) {
    // In KleinLisp, all standard libraries are always available since everything
    // is loaded globally. Return true for any library specification.
    return true;
  }

  private Supplier<LispObject> evaluateBody(ListObject body) {
    // Wrap body in (begin ...) for evaluation
    ListObject beginForm = new ListObject(environment.atomOf("begin"), body);
    return beginForm.accept(evaluator);
  }
}
