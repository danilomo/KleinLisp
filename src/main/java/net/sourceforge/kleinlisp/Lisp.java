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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.sourceforge.kleinlisp.evaluator.Evaluator;

/**
 * @author daolivei
 */
public class Lisp {

  private final LispEnvironment environment;
  private final Parser parser;
  private final Evaluator evaluator;
  private final LoadContext loadContext;

  public Lisp() {
    environment = new LispEnvironment();
    parser = Parser.defaultParser();
    evaluator = new Evaluator(environment);
    loadContext = new LoadContext();
    environment.registerIntrospectionFunctions(this);
  }

  public LispObject parse(String expression) {
    AtomicReference<LispObject> ref = new AtomicReference<>();
    Consumer<LispObject> consumer = ref::set;
    parser.parse(expression, environment, consumer);

    return ref.get();
  }

  public LispObject evaluate(LispObject obj) {
    try {
      return evaluator.evaluate(obj);
    } catch (LispRuntimeException ex) {
      ex.setLispStackTrace(environment().getFunctionCalls());
      throw ex;
    }
  }

  public LispObject evaluate(String expression) {
    AtomicReference<LispObject> ref = new AtomicReference<>();
    Consumer<LispObject> consumer =
        obj -> {
          LispObject response = evaluate(obj);
          ref.set(response);
        };
    parser.parse(expression, environment, consumer);

    return ref.get();
  }

  /**
   * Evaluates a Scheme expression with a source path context. This sets up the load context so that
   * relative paths in (load ...) calls within the code are resolved relative to the source path's
   * directory.
   *
   * <p>This is useful when you have code as a string but want (load ...) calls within it to work
   * correctly relative to a known source file location.
   *
   * @param expression the Scheme expression(s) to evaluate
   * @param sourcePath the path to use as the context for resolving relative loads
   * @return the result of evaluating the last expression
   */
  public LispObject evaluate(String expression, Path sourcePath) {
    Path resolvedPath = sourcePath.toAbsolutePath().normalize();
    loadContext.beginLoad(resolvedPath);
    try {
      return evaluate(expression);
    } finally {
      loadContext.endLoad();
    }
  }

  /**
   * Executes a Scheme file. Relative paths in (load ...) calls within the file will be resolved
   * relative to this file's directory.
   *
   * @param path the path to the file to execute
   */
  public void execute(Path path) {
    executeWithLoadContext(path);
  }

  /**
   * Executes a Scheme file with load context tracking. This method:
   *
   * <ul>
   *   <li>Resolves relative paths relative to the current file being loaded
   *   <li>Detects and prevents cyclical load references
   *   <li>Updates the current-load-pathname during execution
   * </ul>
   *
   * @param path the path to the file to execute
   * @throws LispRuntimeException if the file cannot be found or a cycle is detected
   */
  public void executeWithLoadContext(Path path) {
    Path resolvedPath = path.toAbsolutePath().normalize();

    if (!Files.exists(resolvedPath)) {
      throw new LispRuntimeException("Cannot load file: " + resolvedPath + " (file not found)");
    }

    if (!Files.isReadable(resolvedPath)) {
      throw new LispRuntimeException("Cannot load file: " + resolvedPath + " (not readable)");
    }

    loadContext.beginLoad(resolvedPath);
    try {
      parser.parse(resolvedPath, environment, this::evaluate);
    } finally {
      loadContext.endLoad();
    }
  }

  /**
   * Returns the load context for tracking file loading operations.
   *
   * @return the load context
   */
  public LoadContext loadContext() {
    return loadContext;
  }

  public LispEnvironment environment() {
    return environment;
  }

  public Evaluator evaluator() {
    return evaluator;
  }
}
