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
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * REPL implementation that communicates over a TCP socket.
 *
 * <p>Each instance handles a single client connection with its own isolated Lisp environment.
 *
 * @author Danilo Oliveira
 */
public class SocketRepl implements Runnable {

  private static final String PROMPT = "kleinlisp> ";
  private static final String CONTINUATION = "... ";

  private final Socket socket;
  private final Lisp lisp;
  private final BufferedReader reader;
  private final PrintWriter writer;

  /**
   * Creates a new socket REPL for the given connection.
   *
   * @param socket the client socket
   * @param lisp the Lisp instance to use for evaluation
   * @throws IOException if streams cannot be created
   */
  public SocketRepl(Socket socket, Lisp lisp) throws IOException {
    this.socket = socket;
    this.lisp = lisp;
    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new PrintWriter(socket.getOutputStream(), true);
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

  @Override
  public void run() {
    try {
      runRepl();
    } catch (IOException e) {
      // Client disconnected, which is normal
    } finally {
      closeSocket();
    }
  }

  private void runRepl() throws IOException {
    while (!socket.isClosed()) {
      try {
        String input = readExpression();
        if (input == null) {
          // EOF - client disconnected
          break;
        }

        if (input.trim().isEmpty()) {
          continue;
        }

        LispObject result = lisp.evaluate(input);
        if (result != null && !isVoid(result)) {
          writer.println(result);
        } else {
          writer.println("#<void>");
        }
        writer.flush();
      } catch (LispRuntimeException e) {
        writer.println("Error: " + e.getMessage());
        if (e.getLispStackTrace() != null && !e.getLispStackTrace().isEmpty()) {
          writer.println("Stack trace:");
          for (LispEnvironment.FunctionRef ref : e.getLispStackTrace()) {
            writer.println("  at " + ref);
          }
        }
        writer.flush();
      } catch (Exception e) {
        writer.println("Error: " + e.getMessage());
        writer.flush();
      }
    }
  }

  private String readExpression() throws IOException {
    StringBuilder sb = new StringBuilder();
    int parenBalance = 0;
    boolean firstLine = true;

    while (true) {
      writer.print(firstLine ? PROMPT : CONTINUATION);
      writer.flush();

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

  private void closeSocket() {
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      // Ignore close errors
    }
  }

  /**
   * Returns the Lisp instance used by this REPL.
   *
   * @return the Lisp instance
   */
  public Lisp getLisp() {
    return lisp;
  }
}
