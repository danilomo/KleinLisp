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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Interactive REPL (Read-Eval-Print Loop) for KleinLisp.
 *
 * @author Danilo Oliveira
 */
public class Repl {

  private static final String PROMPT = "kleinlisp> ";
  private static final String CONTINUATION = "... ";

  private final Lisp lisp;
  private final BufferedReader reader;

  public Repl() {
    this.lisp = new Lisp();
    this.reader = new BufferedReader(new InputStreamReader(System.in));
    loadGeiserSupport();
  }

  public Repl(Lisp lisp) {
    this.lisp = lisp;
    this.reader = new BufferedReader(new InputStreamReader(System.in));
    loadGeiserSupport();
  }

  private void loadGeiserSupport() {
    try {
      InputStream geiserInit = getClass().getResourceAsStream("/geiser/kleinlisp.scm");
      if (geiserInit != null) {
        String initCode = new String(geiserInit.readAllBytes(), StandardCharsets.UTF_8);
        lisp.evaluate(initCode);
        geiserInit.close();
      }
    } catch (IOException e) {
      // Silently ignore if geiser support file is not available
    } catch (Exception e) {
      // Silently ignore evaluation errors in init file
    }
  }

  public void run() {
    System.out.println("KleinLisp REPL");
    System.out.println("Type expressions to evaluate. Press Ctrl+D to exit.");
    System.out.println();
    System.out.flush();

    while (true) {
      try {
        String input = readExpression();
        if (input == null) {
          // EOF
          System.out.println();
          System.out.flush();
          break;
        }

        if (input.trim().isEmpty()) {
          continue;
        }

        LispObject result = lisp.evaluate(input);
        if (result != null && !isVoid(result)) {
          System.out.println(result);
        } else {
          // Print marker for void results so Geiser knows evaluation completed
          System.out.println("#<void>");
        }
        System.out.flush();
      } catch (LispRuntimeException e) {
        System.err.println("Error: " + e.getMessage());
        if (e.getLispStackTrace() != null && !e.getLispStackTrace().isEmpty()) {
          System.err.println("Stack trace:");
          for (LispEnvironment.FunctionRef ref : e.getLispStackTrace()) {
            System.err.println("  at " + ref);
          }
        }
        System.err.flush();
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        System.err.flush();
      }
    }
  }

  private String readExpression() throws IOException {
    StringBuilder sb = new StringBuilder();
    int parenBalance = 0;
    boolean firstLine = true;

    while (true) {
      System.out.print(firstLine ? PROMPT : CONTINUATION);
      System.out.flush();

      String line = reader.readLine();
      if (line == null) {
        return sb.length() > 0 ? sb.toString() : null;
      }

      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(line);

      parenBalance += countParentheses(line);

      if (parenBalance <= 0 && sb.toString().trim().length() > 0) {
        return sb.toString();
      }

      firstLine = false;
    }
  }

  private int countParentheses(String line) {
    int balance = 0;
    boolean inString = false;
    boolean escape = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (escape) {
        escape = false;
        continue;
      }

      if (c == '\\') {
        escape = true;
        continue;
      }

      if (c == '"') {
        inString = !inString;
        continue;
      }

      if (inString) {
        continue;
      }

      // Skip line comments
      if (c == ';') {
        break;
      }

      if (c == '(' || c == '[') {
        balance++;
      } else if (c == ')' || c == ']') {
        balance--;
      }
    }

    return balance;
  }

  private boolean isVoid(LispObject obj) {
    return obj.getClass().getSimpleName().equals("VoidObject");
  }

  public Lisp getLisp() {
    return lisp;
  }
}
