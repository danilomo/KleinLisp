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
package net.sourceforge.kleinlisp;

import java.nio.file.Paths;

/**
 * KleinLisp main entry point.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>No args → start REPL
 *   <li>--repl → start REPL (explicit)
 *   <li>file.scm → execute Scheme file
 *   <li>-e &lt;expr&gt; → evaluate expression and print result
 * </ul>
 *
 * @author Danilo Oliveira
 */
public class Main {
  public static void main(String[] args) throws Exception {
    if (args.length == 0 || (args.length == 1 && args[0].equals("--repl"))) {
      // Start REPL
      Repl repl = new Repl();
      repl.run();
    } else if (args.length >= 2 && args[0].equals("-e")) {
      // Evaluate expression
      StringBuilder expr = new StringBuilder();
      for (int i = 1; i < args.length; i++) {
        if (i > 1) {
          expr.append(" ");
        }
        expr.append(args[i]);
      }
      Lisp lisp = new Lisp();
      LispObject result = lisp.evaluate(expr.toString());
      if (result != null) {
        System.out.println(result);
      }
    } else if (args[0].endsWith(".scm") || args[0].endsWith(".ss") || args[0].endsWith(".lisp")) {
      // Execute script file
      String script = args[0];
      Lisp lisp = new Lisp();
      lisp.execute(Paths.get(script));
    } else {
      // Treat as script file anyway for backward compatibility
      String script = args[0];
      Lisp lisp = new Lisp();
      lisp.execute(Paths.get(script));
    }
  }
}
