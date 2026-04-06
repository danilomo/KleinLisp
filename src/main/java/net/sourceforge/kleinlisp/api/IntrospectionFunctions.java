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
   *   <li>If relative, it is resolved relative to the directory of the file that called load, or
   *       the current working directory if called from the REPL
   * </ul>
   *
   * <p>Cyclical references are detected and will raise an error.
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
    Path path = lisp.loadContext().resolvePath(filename);
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
   * Evaluates an expression.
   *
   * <p>(eval expr) → result
   */
  public LispObject eval(LispObject[] params) {
    if (params.length < 1) {
      throw new LispArgumentError("eval requires an expression argument");
    }

    return lisp.evaluate(params[0]);
  }
}
