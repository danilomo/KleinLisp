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

import java.util.*;

/**
 * Registry for R7RS library definitions.
 *
 * <p>This registry tracks both standard R7RS libraries and user-defined libraries. In KleinLisp's
 * backwards-compatible mode (Option B), this is primarily used for validation and compatibility:
 *
 * <ul>
 *   <li>Standard libraries like (scheme base) are pre-registered but always available globally
 *   <li>User libraries can be defined with define-library
 *   <li>Import statements validate against registered libraries in R7RS mode
 * </ul>
 *
 * @author Danilo Oliveira
 */
public class LibraryRegistry {

  private final Map<String, Library> libraries = new HashMap<>();

  public LibraryRegistry() {
    registerStandardLibraries();
  }

  /**
   * Registers a library in the registry.
   *
   * @param library the library to register
   */
  public void register(Library library) {
    String key = libraryNameToKey(library.getName());
    libraries.put(key, library);
  }

  /**
   * Checks if a library with the given name exists.
   *
   * @param libraryName the library name as a list (e.g., ["scheme", "base"])
   * @return true if the library is registered, false otherwise
   */
  public boolean exists(List<Object> libraryName) {
    String key = libraryNameToKey(libraryName);
    return libraries.containsKey(key);
  }

  /**
   * Retrieves a library by name.
   *
   * @param libraryName the library name as a list
   * @return the library, or null if not found
   */
  public Library get(List<Object> libraryName) {
    String key = libraryNameToKey(libraryName);
    return libraries.get(key);
  }

  /**
   * Converts a library name to a string key for map storage.
   *
   * @param libraryName the library name as a list
   * @return the key string
   */
  private String libraryNameToKey(List<Object> libraryName) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < libraryName.size(); i++) {
      if (i > 0) sb.append(".");
      sb.append(libraryName.get(i));
    }
    return sb.toString();
  }

  /**
   * Registers all standard R7RS libraries.
   *
   * <p>Note: In KleinLisp, all these functions are loaded globally at startup. This registration is
   * for compatibility and validation purposes.
   */
  private void registerStandardLibraries() {
    // Register each standard library with its R7RS exports
    registerSchemeBase();
    registerSchemeCaseLambda();
    registerSchemeChar();
    registerSchemeCxr();
    registerSchemeEval();
    registerSchemeFile();
    registerSchemeInexact();
    registerSchemeLazy();
    registerSchemeLoad();
    registerSchemeProcessContext();
    registerSchemeRead();
    registerSchemeRepl();
    registerSchemeTime();
    registerSchemeWrite();

    // Note: (scheme complex) is not supported by KleinLisp
  }

  /**
   * Helper method to register a standard library with given name parts and exports.
   *
   * @param parts the library name parts (e.g., "scheme", "base")
   * @param exports the exported symbols
   */
  private void registerStdLib(String[] parts, String... exports) {
    List<Object> name = Arrays.asList((Object[]) parts);
    Set<String> exportSet = new HashSet<>(Arrays.asList(exports));
    Library lib = new Library(name, exportSet, null);
    register(lib);
  }

  /**
   * Returns all registered library names.
   *
   * @return set of library name keys
   */
  public Set<String> getAllLibraryNames() {
    return new HashSet<>(libraries.keySet());
  }

  /**
   * Returns the number of registered libraries.
   *
   * @return the library count
   */
  public int size() {
    return libraries.size();
  }

  // ===================================================================================
  // Standard Library Export Registration (R7RS Appendix A)
  // ===================================================================================

  /** Registers (scheme base) with all R7RS exports. R7RS Section 6: Standard Procedures */
  private void registerSchemeBase() {
    registerStdLib(
        new String[] {"scheme", "base"},
        // Booleans
        "not",
        "boolean?",
        "boolean=?",
        // Equivalence
        "eq?",
        "eqv?",
        "equal?",
        // Numbers
        "+",
        "-",
        "*",
        "/",
        "=",
        "<",
        ">",
        "<=",
        ">=",
        "number?",
        "complex?",
        "real?",
        "rational?",
        "integer?",
        "exact?",
        "inexact?",
        "exact-integer?",
        "zero?",
        "positive?",
        "negative?",
        "odd?",
        "even?",
        "max",
        "min",
        "abs",
        "floor",
        "ceiling",
        "truncate",
        "round",
        "quotient",
        "remainder",
        "modulo",
        "floor-quotient",
        "floor-remainder",
        "floor/",
        "truncate-quotient",
        "truncate-remainder",
        "truncate/",
        "gcd",
        "lcm",
        "numerator",
        "denominator",
        "floor-remainder",
        "truncate-remainder",
        "rationalize",
        "square",
        "exact-integer-sqrt",
        "expt",
        "sqrt",
        "number->string",
        "string->number",
        // Lists and pairs
        "pair?",
        "cons",
        "car",
        "cdr",
        "set-car!",
        "set-cdr!",
        "caar",
        "cadr",
        "cdar",
        "cddr",
        "null?",
        "list?",
        "make-list",
        "list",
        "length",
        "append",
        "reverse",
        "list-tail",
        "list-ref",
        "list-set!",
        "list-copy",
        "memq",
        "memv",
        "member",
        "assq",
        "assv",
        "assoc",
        // Symbols
        "symbol?",
        "symbol=?",
        "symbol->string",
        "string->symbol",
        // Characters
        "char?",
        "char=?",
        "char<?",
        "char>?",
        "char<=?",
        "char>=?",
        "char->integer",
        "integer->char",
        // Strings
        "string?",
        "make-string",
        "string",
        "string-length",
        "string-ref",
        "string-set!",
        "string=?",
        "string<?",
        "string>?",
        "string<=?",
        "string>=?",
        "substring",
        "string-append",
        "string->list",
        "list->string",
        "string-copy",
        "string-copy!",
        "string-fill!",
        // Vectors
        "vector?",
        "make-vector",
        "vector",
        "vector-length",
        "vector-ref",
        "vector-set!",
        "vector->list",
        "list->vector",
        "vector->string",
        "string->vector",
        "vector-copy",
        "vector-copy!",
        "vector-append",
        "vector-fill!",
        // Bytevectors
        "bytevector?",
        "make-bytevector",
        "bytevector",
        "bytevector-length",
        "bytevector-u8-ref",
        "bytevector-u8-set!",
        "bytevector-copy",
        "bytevector-copy!",
        "bytevector-append",
        "utf8->string",
        "string->utf8",
        // Control features
        "procedure?",
        "apply",
        "map",
        "for-each",
        "string-map",
        "string-for-each",
        "vector-map",
        "vector-for-each",
        "call-with-current-continuation",
        "call/cc",
        "values",
        "call-with-values",
        "dynamic-wind",
        // Exceptions
        "error",
        "error-object?",
        "error-object-message",
        "error-object-irritants",
        "read-error?",
        "file-error?",
        "raise",
        "raise-continuable",
        "with-exception-handler",
        // Environments and evaluation
        "environment",
        "scheme-report-environment",
        "null-environment",
        "interaction-environment",
        // Input and output
        "input-port?",
        "output-port?",
        "textual-port?",
        "binary-port?",
        "port?",
        "input-port-open?",
        "output-port-open?",
        "current-input-port",
        "current-output-port",
        "current-error-port",
        "close-port",
        "close-input-port",
        "close-output-port",
        "open-input-string",
        "open-output-string",
        "get-output-string",
        "open-input-bytevector",
        "open-output-bytevector",
        "get-output-bytevector",
        "read-char",
        "peek-char",
        "read-line",
        "eof-object?",
        "eof-object",
        "char-ready?",
        "read-string",
        "read-u8",
        "peek-u8",
        "u8-ready?",
        "read-bytevector",
        "read-bytevector!",
        "write-char",
        "newline",
        "write-string",
        "write-u8",
        "write-bytevector",
        "flush-output-port",
        // System interface
        "features",
        "command-line",
        "exit",
        "emergency-exit",
        "get-environment-variable",
        "get-environment-variables",
        "current-second",
        "current-jiffy",
        "jiffies-per-second",
        // Note: Special forms like define, lambda, if are not included
        // as they are handled specially and not looked up as functions
        "_" // Placeholder symbol for pattern matching
        );
  }

  private void registerSchemeCaseLambda() {
    registerStdLib(new String[] {"scheme", "case-lambda"}, "case-lambda");
  }

  private void registerSchemeChar() {
    registerStdLib(
        new String[] {"scheme", "char"},
        "char-alphabetic?",
        "char-numeric?",
        "char-whitespace?",
        "char-upper-case?",
        "char-lower-case?",
        "char-upcase",
        "char-downcase",
        "char-foldcase",
        "digit-value",
        "char-ci=?",
        "char-ci<?",
        "char-ci>?",
        "char-ci<=?",
        "char-ci>=?",
        "string-ci=?",
        "string-ci<?",
        "string-ci>?",
        "string-ci<=?",
        "string-ci>=?",
        "string-upcase",
        "string-downcase",
        "string-foldcase");
  }

  private void registerSchemeCxr() {
    registerStdLib(
        new String[] {"scheme", "cxr"},
        "caaar",
        "caadr",
        "cadar",
        "caddr",
        "cdaar",
        "cdadr",
        "cddar",
        "cdddr",
        "caaaar",
        "caaadr",
        "caadar",
        "caaddr",
        "cadaar",
        "cadadr",
        "caddar",
        "cadddr",
        "cdaaar",
        "cdaadr",
        "cdadar",
        "cdaddr",
        "cddaar",
        "cddadr",
        "cdddar",
        "cddddr");
  }

  private void registerSchemeEval() {
    registerStdLib(new String[] {"scheme", "eval"}, "eval");
  }

  private void registerSchemeFile() {
    registerStdLib(
        new String[] {"scheme", "file"},
        "call-with-input-file",
        "call-with-output-file",
        "open-input-file",
        "open-output-file",
        "open-binary-input-file",
        "open-binary-output-file",
        "with-input-from-file",
        "with-output-to-file",
        "file-exists?",
        "delete-file");
  }

  private void registerSchemeInexact() {
    registerStdLib(
        new String[] {"scheme", "inexact"},
        "acos",
        "asin",
        "atan",
        "cos",
        "sin",
        "tan",
        "exp",
        "log",
        "finite?",
        "infinite?",
        "nan?");
  }

  private void registerSchemeLazy() {
    registerStdLib(
        new String[] {"scheme", "lazy"},
        "delay",
        "delay-force",
        "force",
        "make-promise",
        "promise?");
  }

  private void registerSchemeLoad() {
    registerStdLib(new String[] {"scheme", "load"}, "load");
  }

  private void registerSchemeProcessContext() {
    registerStdLib(
        new String[] {"scheme", "process-context"},
        "command-line",
        "exit",
        "emergency-exit",
        "get-environment-variable",
        "get-environment-variables");
  }

  private void registerSchemeRead() {
    registerStdLib(new String[] {"scheme", "read"}, "read");
  }

  private void registerSchemeRepl() {
    registerStdLib(new String[] {"scheme", "repl"}, "interaction-environment");
  }

  private void registerSchemeTime() {
    registerStdLib(
        new String[] {"scheme", "time"}, "current-second", "current-jiffy", "jiffies-per-second");
  }

  private void registerSchemeWrite() {
    registerStdLib(
        new String[] {"scheme", "write"}, "write", "write-shared", "write-simple", "display");
  }
}
