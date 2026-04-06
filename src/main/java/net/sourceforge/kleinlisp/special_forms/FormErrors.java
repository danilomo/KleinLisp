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

import net.sourceforge.kleinlisp.LispException;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Utility class for generating Guile-compatible error messages for special forms. Error formats
 * follow Guile Scheme conventions: "<form>: bad <form> in form <full-form>"
 *
 * @author Danilo Oliveira
 */
public final class FormErrors {

  private FormErrors() {}

  /** Syntax error for malformed special form. Format: "<form>: bad <form> in form <full-form>" */
  public static LispException badForm(String formName, LispObject form) {
    return new LispException(formName + ": bad " + formName + " in form " + form);
  }

  /** Asserts that the form has at least minArgs arguments (not counting the form name itself). */
  public static void assertMinArgs(String formName, ListObject form, int minArgs) {
    int actual = form.cdr().length();
    if (actual < minArgs) {
      throw badForm(formName, form);
    }
  }

  /** Asserts that the form has between min and max arguments (inclusive). */
  public static void assertArgRange(String formName, ListObject form, int min, int max) {
    int actual = form.cdr().length();
    if (actual < min || actual > max) {
      throw badForm(formName, form);
    }
  }

  /** Validates that bindings is a list of (symbol value) pairs. */
  public static void validateBindings(String formName, LispObject bindings, LispObject fullForm) {
    if (bindings == null) {
      throw badForm(formName, fullForm);
    }

    // nil is a valid empty bindings list
    if (bindings == ListObject.NIL) {
      return;
    }

    ListObject bindingsList = bindings.asList();
    if (bindingsList == null) {
      throw badForm(formName, fullForm);
    }

    // Validate each binding is a proper (symbol value) pair
    for (LispObject binding : bindingsList) {
      ListObject tuple = binding.asList();
      if (tuple == null || tuple.length() < 2) {
        throw badForm(formName, fullForm);
      }

      // First element must be a symbol (atom or identifier)
      LispObject first = tuple.car();
      if (first.asAtom() == null && first.asIdentifier() == null) {
        throw badForm(formName, fullForm);
      }
    }
  }

  /**
   * Validates that the parameter list is valid (list of symbols or improper list ending in rest).
   */
  public static void validateParameters(String formName, LispObject params, LispObject fullForm) {
    if (params == null) {
      throw badForm(formName, fullForm);
    }

    // Single symbol for rest-only parameter: (lambda args body)
    if (params.asAtom() != null || params.asIdentifier() != null) {
      return;
    }

    // nil is a valid empty parameter list
    if (params == ListObject.NIL) {
      return;
    }

    ListObject paramsList = params.asList();
    if (paramsList == null) {
      throw badForm(formName, fullForm);
    }

    // Validate each parameter is a symbol
    for (LispObject param : paramsList) {
      if (param.asAtom() == null && param.asIdentifier() == null) {
        // Check for improper list (rest parameter)
        if (param.asList() != null) {
          throw badForm(formName, fullForm);
        }
      }
    }
  }
}
