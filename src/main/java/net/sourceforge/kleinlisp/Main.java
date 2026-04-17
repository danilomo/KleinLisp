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

import java.io.IOException;
import java.nio.file.Paths;

/**
 * KleinLisp main entry point.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>No args → start REPL
 *   <li>--repl → start REPL (explicit)
 *   <li>--r7rs → enable R7RS mode (can be combined with other options)
 *   <li>--listen [port] → start socket REPL server (default port: 37146)
 *   <li>file.scm → execute Scheme file
 *   <li>-e &lt;expr&gt; → evaluate expression and print result
 * </ul>
 *
 * <p>This class is designed for extension. Subclasses can override:
 *
 * <ul>
 *   <li>{@link #createLisp()} - to provide a custom Lisp instance
 *   <li>{@link #configureLisp(Lisp)} - to add custom functions and configure the environment
 *   <li>{@link #createRepl(Lisp)} - to provide a custom REPL instance
 *   <li>{@link #getBanner()} - to customize the REPL banner
 * </ul>
 *
 * <p>Example usage in an extending project:
 *
 * <pre>{@code
 * public class MyMain extends Main {
 *     public static void main(String[] args) throws Exception {
 *         new MyMain().run(args);
 *     }
 *
 *     @Override
 *     protected void configureLisp(Lisp lisp) {
 *         super.configureLisp(lisp);
 *         // Add custom functions
 *         lisp.getEnvironment().define("my-func", new MyFunction());
 *     }
 * }
 * }</pre>
 *
 * @author Danilo Oliveira
 */
public class Main {

  private boolean r7rsMode = false;

  public static void main(String[] args) throws Exception {
    new Main().run(args);
  }

  /**
   * Main entry point for running KleinLisp. This method parses arguments and delegates to the
   * appropriate handler methods.
   *
   * @param args command line arguments
   * @throws Exception if an error occurs during execution
   */
  public void run(String[] args) throws Exception {
    // Parse flags and filter non-flag arguments
    String[] processedArgs = parseFlags(args);

    if (processedArgs.length == 0
        || (processedArgs.length == 1 && processedArgs[0].equals("--repl"))) {
      runRepl();
    } else if (processedArgs.length >= 1 && processedArgs[0].equals("--listen")) {
      int port =
          processedArgs.length > 1
              ? Integer.parseInt(processedArgs[1])
              : SocketReplServer.DEFAULT_PORT;
      runSocketServer(port);
    } else if (processedArgs.length >= 2 && processedArgs[0].equals("-e")) {
      runExpression(processedArgs);
    } else {
      runScript(processedArgs[0]);
    }
  }

  /**
   * Parses command line flags and returns the remaining arguments.
   *
   * @param args the original command line arguments
   * @return arguments with flags removed
   */
  private String[] parseFlags(String[] args) {
    java.util.List<String> remaining = new java.util.ArrayList<>();
    for (String arg : args) {
      if (arg.equals("--r7rs")) {
        r7rsMode = true;
      } else {
        remaining.add(arg);
      }
    }
    return remaining.toArray(new String[0]);
  }

  /**
   * Starts the interactive REPL.
   *
   * @throws Exception if an error occurs during REPL execution
   */
  protected void runRepl() throws Exception {
    Lisp lisp = createAndConfigureLisp();
    Repl repl = createRepl(lisp);
    String banner = getBanner();
    if (banner != null && !banner.isEmpty()) {
      System.out.println(banner);
      System.out.println();
      System.out.flush();
    }
    repl.run();
  }

  /**
   * Evaluates an expression from command line arguments.
   *
   * @param args command line arguments where args[1..] contains the expression
   * @throws Exception if an error occurs during evaluation
   */
  protected void runExpression(String[] args) throws Exception {
    StringBuilder expr = new StringBuilder();
    for (int i = 1; i < args.length; i++) {
      if (i > 1) {
        expr.append(" ");
      }
      expr.append(args[i]);
    }
    Lisp lisp = createAndConfigureLisp();
    LispObject result = lisp.evaluate(expr.toString());
    if (result != null) {
      System.out.println(result);
    }
  }

  /**
   * Executes a script file.
   *
   * @param scriptPath path to the script file
   * @throws Exception if an error occurs during script execution
   */
  protected void runScript(String scriptPath) throws Exception {
    Lisp lisp = createAndConfigureLisp();
    lisp.execute(Paths.get(scriptPath));
  }

  /**
   * Starts a socket REPL server that accepts TCP connections.
   *
   * <p>Each connection gets its own isolated Lisp environment. This enables Emacs/Geiser to connect
   * via geiser-connect.
   *
   * @param port the TCP port to listen on
   * @throws IOException if the server cannot start
   */
  protected void runSocketServer(int port) throws IOException {
    SocketReplServer server = new SocketReplServer(port, this::createAndConfigureLisp);
    System.out.println("KleinLisp REPL server listening on port " + port);
    System.out.println("Connect with: M-x connect-to-kleinlisp");
    System.out.println("Or use: nc localhost " + port);
    System.out.println("Press Ctrl+C to stop the server.");
    System.out.flush();
    server.start();
  }

  /**
   * Creates and configures a new Lisp instance. This method calls {@link #createLisp()} followed by
   * {@link #configureLisp(Lisp)}.
   *
   * @return a configured Lisp instance
   */
  protected final Lisp createAndConfigureLisp() {
    Lisp lisp = createLisp();
    configureLisp(lisp);
    return lisp;
  }

  /**
   * Creates a new Lisp instance. Override this method to provide a custom Lisp instance with
   * different initialization parameters.
   *
   * @return a new Lisp instance
   */
  protected Lisp createLisp() {
    return new Lisp(r7rsMode);
  }

  /**
   * Configures the Lisp environment. Override this method to add custom functions, load additional
   * libraries, or modify the environment.
   *
   * <p>Example:
   *
   * <pre>{@code
   * @Override
   * protected void configureLisp(Lisp lisp) {
   *     super.configureLisp(lisp);
   *     lisp.getEnvironment().define("my-func", args -> {
   *         // custom implementation
   *         return result;
   *     });
   * }
   * }</pre>
   *
   * @param lisp the Lisp instance to configure
   */
  protected void configureLisp(Lisp lisp) {
    // Default implementation does nothing.
    // Subclasses can override to add custom configuration.
  }

  /**
   * Creates a REPL instance. Override this method to provide a custom REPL implementation.
   *
   * @param lisp the configured Lisp instance
   * @return a REPL instance
   */
  protected Repl createRepl(Lisp lisp) {
    return new Repl(lisp);
  }

  /**
   * Returns the banner to display when starting the REPL. Override this method to customize the
   * welcome message.
   *
   * @return the banner text, or null/empty to display no banner
   */
  protected String getBanner() {
    String banner = "KleinLisp REPL";
    if (r7rsMode) {
      banner += " (R7RS mode)";
    }
    banner += "\nType expressions to evaluate. Press Ctrl+D to exit.";
    return banner;
  }
}
