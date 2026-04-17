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

import java.util.*;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.*;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Implements the R7RS (define-library ...) special form.
 *
 * <p>Syntax:
 *
 * <pre>
 * (define-library &lt;library-name&gt;
 *   &lt;library-declaration&gt; ...)
 *
 * &lt;library-declaration&gt; ::= (export &lt;export-spec&gt; ...)
 *                          | (import &lt;import-set&gt; ...)
 *                          | (begin &lt;command-or-definition&gt; ...)
 *                          | (include &lt;filename&gt; ...)
 *                          | (include-ci &lt;filename&gt; ...)
 *                          | (cond-expand &lt;ce-clause&gt; ...)
 * </pre>
 *
 * <p>Backwards-Compatible Implementation (Option B):
 *
 * <ul>
 *   <li>Registers user-defined libraries in the LibraryRegistry
 *   <li>Evaluates the library body in the global environment
 *   <li>Tracks exported symbols for validation purposes
 *   <li>All definitions become globally available (no true encapsulation)
 * </ul>
 *
 * <p>This allows users to organize code into libraries while maintaining backwards compatibility.
 *
 * @author Danilo Oliveira
 */
public class DefineLibraryForm implements SpecialForm {

  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public DefineLibraryForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject form = t.asList();
    FormErrors.assertMinArgs("define-library", form, 2);

    // Extract library name
    LispObject libraryNameObj = form.cdr().car();
    ListObject libraryNameList = libraryNameObj.asList();
    if (libraryNameList == null) {
      throw new LispRuntimeException(
          "define-library: library name must be a list: " + libraryNameObj);
    }

    List<Object> libraryName = extractLibraryName(libraryNameList);

    // Process library declarations
    Set<String> exports = new HashSet<>();
    List<LispObject> bodyForms = new ArrayList<>();

    ListObject declarations = form.cdr().cdr();
    while (declarations != null && declarations != ListObject.NIL) {
      LispObject declaration = declarations.car();
      if (declaration == null) {
        break;
      }
      processDeclaration(declaration, exports, bodyForms);
      declarations = declarations.cdr().asList();
    }

    // Create the library body (wrap all forms in a begin)
    LispObject body = createBeginForm(bodyForms);

    // Register the library
    Library library = new Library(libraryName, exports, body);
    environment.getLibraryRegistry().register(library);

    // Evaluate the library body in the global environment
    // (in true R7RS, this would create a separate environment)
    return () -> {
      if (body != null) {
        evaluator.evaluate(body);
      }
      return VoidObject.VOID;
    };
  }

  /**
   * Processes a single library declaration.
   *
   * @param declaration the declaration to process
   * @param exports the set to add exports to
   * @param bodyForms the list to add body forms to
   */
  private void processDeclaration(
      LispObject declaration, Set<String> exports, List<LispObject> bodyForms) {
    ListObject declList = declaration.asList();
    if (declList == null) {
      throw new LispRuntimeException("define-library: invalid declaration: " + declaration);
    }

    LispObject first = declList.car();
    String keyword = first.asAtom() != null ? first.asAtom().toString() : null;

    if (keyword == null) {
      throw new LispRuntimeException("define-library: invalid declaration: " + declaration);
    }

    switch (keyword) {
      case "export":
        processExport(declList, exports);
        break;
      case "import":
        // Import declarations are processed but don't affect the body
        // in backwards-compatible mode (all functions are global)
        processImport(declList);
        break;
      case "begin":
        processBegin(declList, bodyForms);
        break;
      case "include":
      case "include-ci":
        throw new LispRuntimeException("define-library: " + keyword + " is not yet implemented");
      case "cond-expand":
        // Process cond-expand by adding it to the body
        bodyForms.add(declaration);
        break;
      default:
        throw new LispRuntimeException("define-library: unknown declaration: " + keyword);
    }
  }

  /**
   * Processes an (export ...) declaration.
   *
   * @param exportDecl the export declaration
   * @param exports the set to add exports to
   */
  private void processExport(ListObject exportDecl, Set<String> exports) {
    ListObject symbols = exportDecl.cdr();
    while (symbols != null && symbols != ListObject.NIL) {
      LispObject symbol = symbols.car();
      if (symbol == null) {
        break;
      }
      if (symbol.asAtom() == null) {
        throw new LispRuntimeException("define-library: export requires symbols: " + symbol);
      }
      exports.add(symbol.asAtom().toString());
      symbols = symbols.cdr().asList();
    }
  }

  /**
   * Processes an (import ...) declaration.
   *
   * @param importDecl the import declaration
   */
  private void processImport(ListObject importDecl) {
    // In backwards-compatible mode, imports are validated but don't
    // actually affect what's available (everything is global)
    // We could call ImportForm here, but it's simpler to just validate
    Lisp lispInstance = environment.getLispInstance();
    boolean r7rsMode = lispInstance != null && lispInstance.isR7rsMode();

    if (r7rsMode) {
      // Validate import sets
      ListObject imports = importDecl.cdr();
      while (imports != null && imports != ListObject.NIL) {
        LispObject importSet = imports.car();
        if (importSet == null) {
          break;
        }
        validateImportSet(importSet);
        imports = imports.cdr().asList();
      }
    }
  }

  /**
   * Validates an import set (simple library name validation).
   *
   * @param importSet the import set to validate
   */
  private void validateImportSet(LispObject importSet) {
    ListObject importList = importSet.asList();
    if (importList == null) {
      throw new LispRuntimeException("define-library: invalid import set: " + importSet);
    }

    LispObject first = importList.car();
    String firstSymbol = first.asAtom() != null ? first.asAtom().toString() : null;

    if (firstSymbol != null
        && (firstSymbol.equals("only")
            || firstSymbol.equals("except")
            || firstSymbol.equals("prefix")
            || firstSymbol.equals("rename"))) {
      // It's a filtered import - recursively validate the nested import
      LispObject nested = importList.cdr().car();
      validateImportSet(nested);
    } else {
      // It's a library name - validate it exists
      List<Object> name = extractLibraryName(importList);
      if (!environment.getLibraryRegistry().exists(name)) {
        throw new LispRuntimeException(
            "define-library: library not found: " + formatLibraryName(name));
      }
    }
  }

  /**
   * Processes a (begin ...) declaration.
   *
   * @param beginDecl the begin declaration
   * @param bodyForms the list to add forms to
   */
  private void processBegin(ListObject beginDecl, List<LispObject> bodyForms) {
    ListObject forms = beginDecl.cdr();
    while (forms != null && forms != ListObject.NIL) {
      LispObject form = forms.car();
      if (form == null) {
        break;
      }
      bodyForms.add(form);
      forms = forms.cdr().asList();
    }
  }

  /**
   * Creates a (begin ...) form from a list of body forms.
   *
   * @param bodyForms the forms to wrap
   * @return the begin form, or null if no forms
   */
  private LispObject createBeginForm(List<LispObject> bodyForms) {
    if (bodyForms.isEmpty()) {
      return null;
    }

    // Build the list in reverse order for proper cons cell construction
    LispObject result = ListObject.NIL;
    for (int i = bodyForms.size() - 1; i >= 0; i--) {
      result = new ListObject(bodyForms.get(i), result);
    }

    // Prepend the 'begin' symbol
    return new ListObject(environment.atomOf("begin"), result);
  }

  /**
   * Extracts a library name from a list into a Java List.
   *
   * @param libraryName the library name as a ListObject
   * @return the library name as a List
   */
  private List<Object> extractLibraryName(ListObject libraryName) {
    List<Object> result = new ArrayList<>();
    ListObject current = libraryName;

    while (current != null && current != ListObject.NIL) {
      LispObject elem = current.car();
      if (elem == null) {
        break;
      }
      if (elem.asAtom() != null) {
        result.add(elem.asAtom().toString());
      } else if (elem.asInt() != null) {
        result.add(elem.asInt());
      } else {
        throw new LispRuntimeException("define-library: invalid library name component: " + elem);
      }
      current = current.cdr().asList();
    }

    if (result.isEmpty()) {
      throw new LispRuntimeException("define-library: empty library name");
    }

    return result;
  }

  /**
   * Formats a library name for display.
   *
   * @param name the library name as a list
   * @return the formatted name
   */
  private String formatLibraryName(List<Object> name) {
    StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < name.size(); i++) {
      if (i > 0) sb.append(" ");
      sb.append(name.get(i));
    }
    sb.append(")");
    return sb.toString();
  }
}
