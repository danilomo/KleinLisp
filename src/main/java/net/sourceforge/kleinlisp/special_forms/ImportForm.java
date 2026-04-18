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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.*;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * Implements the R7RS (import ...) special form.
 *
 * <p>Syntax:
 *
 * <pre>
 * (import &lt;import-set&gt; ...)
 *
 * &lt;import-set&gt; ::= &lt;library-name&gt;
 *               | (only &lt;import-set&gt; &lt;identifier&gt; ...)
 *               | (except &lt;import-set&gt; &lt;identifier&gt; ...)
 *               | (prefix &lt;import-set&gt; &lt;identifier&gt;)
 *               | (rename &lt;import-set&gt; (&lt;identifier1&gt; &lt;identifier2&gt;) ...)
 * </pre>
 *
 * <p>Backwards-Compatible Implementation (Option B):
 *
 * <ul>
 *   <li>In non-R7RS mode: This is a no-op. All functions are already loaded globally.
 *   <li>In R7RS mode: Validates that libraries exist in the registry, but still doesn't actually
 *       restrict access since all functions are global.
 * </ul>
 *
 * <p>This provides R7RS source compatibility while maintaining backwards compatibility with
 * existing code.
 *
 * @author Danilo Oliveira
 */
public class ImportForm implements SpecialForm {

  private final LispEnvironment environment;

  public ImportForm(LispEnvironment environment) {
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject form = t.asList();
    FormErrors.assertMinArgs("import", form, 1);

    // Check if R7RS mode is enabled
    Lisp lispInstance = environment.getLispInstance();
    boolean r7rsMode = lispInstance != null && lispInstance.isR7rsMode();

    // Process each import set
    ListObject importSets = form.cdr();
    while (importSets != null && importSets != ListObject.NIL) {
      LispObject importSet = importSets.car();
      if (importSet == null) {
        break;
      }
      processImportSet(importSet, r7rsMode);
      importSets = importSets.cdr().asList();
    }

    return () -> VoidObject.VOID;
  }

  /**
   * Processes a single import set.
   *
   * @param importSet the import set to process
   * @param r7rsMode whether R7RS mode is enabled
   */
  private void processImportSet(LispObject importSet, boolean r7rsMode) {
    // Import set can be either a library name or a filtered import
    ListObject importList = importSet.asList();
    if (importList == null) {
      throw new LispRuntimeException("import: invalid import set: " + importSet);
    }

    LispObject first = importList.car();
    String firstSymbol = first.asAtom() != null ? first.asAtom().toString() : null;

    if (firstSymbol != null) {
      // Check for import modifiers
      switch (firstSymbol) {
        case "only":
          processOnlyImport(importList, r7rsMode);
          break;
        case "except":
          processExceptImport(importList, r7rsMode);
          break;
        case "prefix":
          processPrefixImport(importList, r7rsMode);
          break;
        case "rename":
          processRenameImport(importList, r7rsMode);
          break;
        default:
          // It's a library name
          processLibraryImport(importList, r7rsMode);
          break;
      }
    } else {
      throw new LispRuntimeException("import: invalid import set: " + importSet);
    }
  }

  /**
   * Processes a direct library import: (scheme base)
   *
   * @param libraryName the library name as a list
   * @param r7rsMode whether R7RS mode is enabled
   */
  private void processLibraryImport(ListObject libraryName, boolean r7rsMode) {
    List<Object> nameList = extractLibraryName(libraryName);

    if (r7rsMode) {
      // Validate that the library exists
      if (!environment.getLibraryRegistry().exists(nameList)) {
        throw new LispRuntimeException("import: library not found: " + formatLibraryName(nameList));
      }
    }
    // In backwards-compatible mode, this is a no-op
    // All functions are already loaded globally
  }

  /**
   * Processes an (only ...) import.
   *
   * @param importForm the import form (only <import-set> <identifier> ...)
   * @param r7rsMode whether R7RS mode is enabled
   */
  private void processOnlyImport(ListObject importForm, boolean r7rsMode) {
    FormErrors.assertMinArgs("only", importForm, 2);
    LispObject nestedImport = importForm.cdr().car();

    // In R7RS mode, validate that requested symbols exist in the library
    if (r7rsMode) {
      // Get the library being imported
      Library library = resolveLibrary(nestedImport);

      if (library != null) {
        // Extract the symbols to import (everything after the nested import)
        List<String> requestedSymbols = extractSymbolList(importForm.cdr().cdr());

        // Validate each symbol exists in the library's exports
        for (String symbol : requestedSymbols) {
          if (!library.exports(symbol)) {
            throw new LispRuntimeException(
                "import: symbol '"
                    + symbol
                    + "' not exported by library "
                    + library.getNameString());
          }
        }
      }
    }

    // Recursively process the nested import set
    processImportSet(nestedImport, r7rsMode);
    // In backwards-compatible mode, we don't actually filter anything
  }

  /**
   * Processes an (except ...) import.
   *
   * @param importForm the import form (except <import-set> <identifier> ...)
   * @param r7rsMode whether R7RS mode is enabled
   */
  private void processExceptImport(ListObject importForm, boolean r7rsMode) {
    FormErrors.assertMinArgs("except", importForm, 2);
    LispObject nestedImport = importForm.cdr().car();

    // In R7RS mode, validate that excluded symbols exist in the library
    if (r7rsMode) {
      // Get the library being imported
      Library library = resolveLibrary(nestedImport);

      if (library != null) {
        // Extract the symbols to exclude
        List<String> excludedSymbols = extractSymbolList(importForm.cdr().cdr());

        // Validate each symbol exists in the library's exports
        for (String symbol : excludedSymbols) {
          if (!library.exports(symbol)) {
            throw new LispRuntimeException(
                "import: cannot exclude '"
                    + symbol
                    + "' - not exported by library "
                    + library.getNameString());
          }
        }
      }
    }

    processImportSet(nestedImport, r7rsMode);
  }

  /**
   * Processes a (prefix ...) import.
   *
   * @param importForm the import form
   * @param r7rsMode whether R7RS mode is enabled
   */
  private void processPrefixImport(ListObject importForm, boolean r7rsMode) {
    FormErrors.assertArgRange("prefix", importForm, 2, 2);
    LispObject nestedImport = importForm.cdr().car();
    processImportSet(nestedImport, r7rsMode);
  }

  /**
   * Processes a (rename ...) import.
   *
   * @param importForm the import form (rename <import-set> (<old> <new>) ...)
   * @param r7rsMode whether R7RS mode is enabled
   */
  private void processRenameImport(ListObject importForm, boolean r7rsMode) {
    FormErrors.assertMinArgs("rename", importForm, 2);
    LispObject nestedImport = importForm.cdr().car();

    // In R7RS mode, validate that renamed symbols exist in the library
    if (r7rsMode) {
      // Get the library being imported
      Library library = resolveLibrary(nestedImport);

      if (library != null) {
        // Extract and validate rename pairs
        ListObject renamePairs = importForm.cdr().cdr().asList();
        while (renamePairs != null && renamePairs != ListObject.NIL) {
          LispObject pairObj = renamePairs.car();
          if (pairObj == null) break;

          ListObject pair = pairObj.asList();
          if (pair == null) {
            throw new LispRuntimeException("import: invalid rename pair: " + pairObj);
          }

          // Get the old symbol name (first element of pair)
          LispObject oldSymbolObj = pair.car();
          if (oldSymbolObj == null || oldSymbolObj.asAtom() == null) {
            throw new LispRuntimeException("import: invalid symbol in rename: " + oldSymbolObj);
          }

          String oldSymbol = oldSymbolObj.asAtom().toString();

          // Validate the old symbol exists in the library
          if (!library.exports(oldSymbol)) {
            throw new LispRuntimeException(
                "import: cannot rename '"
                    + oldSymbol
                    + "' - not exported by library "
                    + library.getNameString());
          }

          renamePairs = renamePairs.cdr().asList();
        }
      }
    }

    processImportSet(nestedImport, r7rsMode);
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
        throw new LispRuntimeException("import: invalid library name component: " + elem);
      }
      current = current.cdr().asList();
    }

    if (result.isEmpty()) {
      throw new LispRuntimeException("import: empty library name");
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

  /**
   * Resolves an import set to the underlying library. Handles nested filters like (only (prefix
   * (scheme base) foo:) +)
   *
   * @param importSet the import set to resolve
   * @return the library, or null if not found
   */
  private Library resolveLibrary(LispObject importSet) {
    ListObject importList = importSet.asList();
    if (importList == null) {
      return null;
    }

    LispObject first = importList.car();
    String firstSymbol = first.asAtom() != null ? first.asAtom().toString() : null;

    if (firstSymbol != null) {
      // Check for import modifiers - recursively resolve nested filters
      switch (firstSymbol) {
        case "only":
        case "except":
        case "prefix":
        case "rename":
          // These all have the nested import as the second element
          LispObject nestedImport = importList.cdr().car();
          return resolveLibrary(nestedImport);
        default:
          // It's a library name - extract and lookup
          List<Object> libraryName = extractLibraryName(importList);
          return environment.getLibraryRegistry().get(libraryName);
      }
    }

    return null;
  }

  /**
   * Extracts a list of symbols from a ListObject. Used to extract identifier lists from import
   * filters.
   *
   * @param symbolList the list of symbols
   * @return list of symbol names as strings
   */
  private List<String> extractSymbolList(ListObject symbolList) {
    List<String> symbols = new ArrayList<>();
    ListObject current = symbolList;

    while (current != null && current != ListObject.NIL) {
      LispObject elem = current.car();
      if (elem == null) {
        break;
      }

      if (elem.asAtom() != null) {
        symbols.add(elem.asAtom().toString());
      } else {
        throw new LispRuntimeException("import: expected symbol, got: " + elem);
      }

      current = current.cdr().asList();
    }

    return symbols;
  }
}
