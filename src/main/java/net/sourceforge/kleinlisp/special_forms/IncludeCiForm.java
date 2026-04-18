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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Lisp;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispRuntimeException;
import net.sourceforge.kleinlisp.Parser;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Implements the R7RS (include-ci ...) special form.
 *
 * <p>Syntax:
 *
 * <pre>
 * (include-ci &lt;filename1&gt; &lt;filename2&gt; ...)
 * </pre>
 *
 * <p>The include-ci form is like include, but reads the file in case-insensitive mode. All
 * identifiers in the included file are effectively case-folded to lowercase.
 *
 * <p>Key behaviors:
 *
 * <ul>
 *   <li>Files are read and parsed in the order specified
 *   <li>Relative paths are resolved relative to the file containing the include-ci form
 *   <li>All identifiers are converted to lowercase (case-insensitive)
 *   <li>All expressions in the included file are evaluated in the current environment
 * </ul>
 *
 * <p>Note: The current implementation treats include-ci the same as include because KleinLisp does
 * not have a case-folding parser mode. This is a known limitation.
 *
 * @author Danilo Oliveira
 */
public class IncludeCiForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;
  private final Parser parser;

  public IncludeCiForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
    this.parser = Parser.defaultParser();
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject form = t.asList();
    FormErrors.assertMinArgs("include-ci", form, 1);

    // Get the Lisp instance to access load context
    Lisp lispInstance = environment.getLispInstance();
    if (lispInstance == null) {
      throw new LispRuntimeException("include-ci: cannot access Lisp instance");
    }

    // Process each filename
    ListObject filenames = form.cdr();
    while (filenames != null && filenames != ListObject.NIL) {
      LispObject filenameObj = filenames.car();
      if (filenameObj == null) {
        break;
      }

      // Get the filename string
      StringObject filenameString = filenameObj.asString();
      if (filenameString == null) {
        throw new LispRuntimeException(
            "include-ci: filename must be a string, got: " + filenameObj);
      }

      String filename = filenameString.value();

      // Resolve the path relative to the current file being loaded
      Path resolvedPath = lispInstance.loadContext().resolveRelativePath(filename);

      // Check if file exists
      if (!Files.exists(resolvedPath)) {
        throw new LispRuntimeException("include-ci: file not found: " + resolvedPath);
      }

      if (!Files.isReadable(resolvedPath)) {
        throw new LispRuntimeException("include-ci: file not readable: " + resolvedPath);
      }

      // Include the file
      // TODO: Implement proper case-folding mode for the parser
      // For now, this behaves the same as regular include
      includeFile(resolvedPath, lispInstance);

      filenames = filenames.cdr().asList();
    }

    return () -> VoidObject.VOID;
  }

  /**
   * Includes a file by parsing and evaluating all expressions in it.
   *
   * <p>Note: This should use case-insensitive parsing, but currently uses the default parser.
   *
   * @param path the path to the file to include
   * @param lispInstance the Lisp instance for parsing and evaluation
   */
  private void includeFile(Path path, Lisp lispInstance) {
    // Set up load context so nested includes work correctly
    lispInstance.loadContext().beginLoad(path);
    try {
      // Parse and evaluate the file
      // TODO: Use case-insensitive parser when available
      parser.parse(path, environment, obj -> evaluator.evaluate(obj));
    } finally {
      lispInstance.loadContext().endLoad();
    }
  }
}
