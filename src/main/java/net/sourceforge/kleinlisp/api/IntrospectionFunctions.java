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
package net.sourceforge.kleinlisp.api;

import java.nio.file.Path;
import java.util.Set;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Introspection functions for examining the Lisp environment.
 *
 * @author Danilo Oliveira
 */
public class IntrospectionFunctions {

  private final LispEnvironment environment;
  private final Lisp lisp;

  public IntrospectionFunctions(LispEnvironment environment, Lisp lisp) {
    this.environment = environment;
    this.lisp = lisp;
  }

  /**
   * Returns a list of all symbol names defined in the environment.
   *
   * <p>(environment-symbols) → (symbol1 symbol2 ...)
   */
  public LispObject environmentSymbols(LispObject[] params) {
    Set<String> symbols = environment.getDefinedSymbols();
    ListObject result = ListObject.NIL;
    for (String symbol : symbols) {
      result = new ListObject(new StringObject(symbol), result);
    }
    return result;
  }

  /**
   * Returns the arity of a procedure.
   *
   * <p>(procedure-arity proc) → integer or -1 for varargs/unknown
   */
  public LispObject procedureArity(LispObject[] params) {
    if (params.length < 1) {
      throw new LispArgumentError("procedure-arity requires a procedure argument");
    }

    LispObject proc = params[0];
    FunctionObject funcObj = proc.asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("procedure-arity requires a procedure, got: " + proc);
    }

    // For now, return -1 (unknown/varargs) since we don't have easy access to arity info
    // Lambda functions store arity in their meta, but it's not easily accessible from here
    return IntObject.valueOf(-1);
  }

  /**
   * Returns the name of a procedure, or #f if unnamed.
   *
   * <p>(procedure-name proc) → string or #f
   */
  public LispObject procedureName(LispObject[] params) {
    if (params.length < 1) {
      throw new LispArgumentError("procedure-name requires a procedure argument");
    }

    LispObject proc = params[0];
    FunctionObject funcObj = proc.asFunction();
    if (funcObj == null) {
      throw new LispArgumentError("procedure-name requires a procedure, got: " + proc);
    }

    String name = funcObj.functionName();
    if (name == null || name.equals("*unamed function*")) {
      return BooleanObject.FALSE;
    }
    return new StringObject(name);
  }

  /**
   * Loads and evaluates a Scheme file.
   *
   * <p>(load filename) → void
   *
   * <p>The filename is resolved as follows:
   *
   * <ul>
   *   <li>If absolute, it is used as-is
   *   <li>If relative, it is resolved relative to the current working directory
   * </ul>
   *
   * <p>This follows standard R7RS/Guile/Chicken semantics where load resolves paths relative to the
   * application's current working directory, not the source file.
   *
   * <p>Cyclical references are detected and will raise an error.
   *
   * @see #loadRelative(LispObject[]) for loading relative to the current source file
   */
  public LispObject load(LispObject[] params) {
    if (params.length < 1) {
      throw new LispArgumentError("load requires a filename argument");
    }

    StringObject filenameObj = params[0].asString();
    if (filenameObj == null) {
      throw new LispArgumentError("load requires a string filename, got: " + params[0]);
    }

    String filename = filenameObj.value();
    Path path = lisp.loadContext().resolveCwdPath(filename);
    lisp.executeWithLoadContext(path);

    return VoidObject.VOID;
  }

  /**
   * Loads and evaluates a Scheme file relative to the current source file.
   *
   * <p>(load-relative filename) → void
   *
   * <p>The filename is resolved as follows:
   *
   * <ul>
   *   <li>If absolute, it is used as-is
   *   <li>If relative, it is resolved relative to the directory of the file that called
   *       load-relative, or the current working directory if called from the REPL
   * </ul>
   *
   * <p>This follows Chicken Scheme's load-relative semantics, which is useful for organizing
   * multi-file projects where files reference each other based on their relative positions.
   *
   * <p>Cyclical references are detected and will raise an error.
   *
   * @see #load(LispObject[]) for standard load semantics (relative to CWD)
   */
  public LispObject loadRelative(LispObject[] params) {
    if (params.length < 1) {
      throw new LispArgumentError("load-relative requires a filename argument");
    }

    StringObject filenameObj = params[0].asString();
    if (filenameObj == null) {
      throw new LispArgumentError("load-relative requires a string filename, got: " + params[0]);
    }

    String filename = filenameObj.value();
    Path path = lisp.loadContext().resolveRelativePath(filename);
    lisp.executeWithLoadContext(path);

    return VoidObject.VOID;
  }

  /**
   * Returns the pathname of the file currently being loaded, or #f if not loading any file.
   *
   * <p>(current-load-pathname) → string or #f
   *
   * <p>This is useful for resolving paths relative to the current file, or for debugging purposes.
   */
  public LispObject currentLoadPathname(LispObject[] params) {
    if (params.length != 0) {
      throw new LispArgumentError("current-load-pathname takes no arguments");
    }

    Path currentPath = lisp.loadContext().getCurrentLoadPath();
    if (currentPath == null) {
      return BooleanObject.FALSE;
    }
    return new StringObject(currentPath.toString());
  }

  /**
   * Returns the current interaction environment.
   *
   * <p>(interaction-environment) → environment
   *
   * <p>This is the environment used by the REPL and contains all loaded definitions.
   */
  public LispObject interactionEnvironment(LispObject[] params) {
    if (params.length != 0) {
      throw new LispArgumentError("interaction-environment takes no arguments");
    }

    return new net.sourceforge.kleinlisp.objects.EnvironmentObject(
        environment, "interaction-environment");
  }

  /**
   * Returns an R5RS/R7RS compatible environment.
   *
   * <p>(scheme-report-environment version) → environment
   *
   * <p>The version should be 5 or 7. Since KleinLisp loads all functions globally and implements
   * most R5RS/R7RS features, this returns the current environment.
   *
   * <p>Note: This is a simplified implementation. A full R7RS implementation would provide separate
   * environments for R5RS (version 5) and R7RS (version 7).
   */
  public LispObject schemeReportEnvironment(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError("scheme-report-environment requires a version argument");
    }

    IntObject versionObj = params[0].asInt();
    if (versionObj == null) {
      throw new LispArgumentError(
          "scheme-report-environment requires an integer version, got: " + params[0]);
    }

    int version = versionObj.value();
    if (version != 5 && version != 7) {
      throw new LispArgumentError(
          "scheme-report-environment only supports version 5 or 7, got: " + version);
    }

    return new net.sourceforge.kleinlisp.objects.EnvironmentObject(
        environment, "scheme-report-environment(" + version + ")");
  }

  /**
   * Returns a null environment containing only special forms.
   *
   * <p>(null-environment version) → environment
   *
   * <p>The version should be 5 or 7. This environment contains only syntax (special forms) without
   * any built-in procedures.
   *
   * <p>Note: This is a simplified implementation that returns the current environment. A full
   * implementation would create a restricted environment with only special forms.
   */
  public LispObject nullEnvironment(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError("null-environment requires a version argument");
    }

    IntObject versionObj = params[0].asInt();
    if (versionObj == null) {
      throw new LispArgumentError(
          "null-environment requires an integer version, got: " + params[0]);
    }

    int version = versionObj.value();
    if (version != 5 && version != 7) {
      throw new LispArgumentError("null-environment only supports version 5 or 7, got: " + version);
    }

    // Note: In a full implementation, this would create a minimal environment
    // with only special forms. For now, we return the current environment.
    return new net.sourceforge.kleinlisp.objects.EnvironmentObject(
        environment, "null-environment(" + version + ")");
  }

  /**
   * Evaluates an expression in the given environment.
   *
   * <p>(eval expr) → result
   *
   * <p>(eval expr environment) → result
   *
   * <p>If no environment is provided, the current environment is used. If an environment is
   * provided (from interaction-environment, scheme-report-environment, or null-environment), the
   * expression is evaluated in that environment.
   */
  public LispObject eval(LispObject[] params) {
    if (params.length < 1 || params.length > 2) {
      throw new LispArgumentError("eval requires 1 or 2 arguments (expression [environment])");
    }

    LispObject expr = params[0];

    // If environment is provided, validate it
    if (params.length == 2) {
      net.sourceforge.kleinlisp.objects.EnvironmentObject envObj =
          params[1].asObject(net.sourceforge.kleinlisp.objects.EnvironmentObject.class);
      if (envObj == null) {
        throw new LispArgumentError("eval requires an environment object, got: " + params[1]);
      }
      // Note: Currently we don't actually switch environments because the Lisp.evaluate()
      // method doesn't support it. This is a simplified implementation that validates
      // the environment object but evaluates in the current environment.
    }

    return lisp.evaluate(expr);
  }
}
