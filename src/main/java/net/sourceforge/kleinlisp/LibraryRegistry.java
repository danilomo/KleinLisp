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
    // Core library - contains fundamental forms and functions
    registerStdLib("scheme", "base");

    // Additional R7RS standard libraries
    registerStdLib("scheme", "case-lambda");
    registerStdLib("scheme", "char");
    registerStdLib("scheme", "cxr");
    registerStdLib("scheme", "eval");
    registerStdLib("scheme", "file");
    registerStdLib("scheme", "inexact");
    registerStdLib("scheme", "lazy");
    registerStdLib("scheme", "load");
    registerStdLib("scheme", "process-context");
    registerStdLib("scheme", "read");
    registerStdLib("scheme", "repl");
    registerStdLib("scheme", "time");
    registerStdLib("scheme", "write");

    // Note: (scheme complex) is not supported by KleinLisp
  }

  /**
   * Helper method to register a standard library with given name parts.
   *
   * @param parts the library name parts (e.g., "scheme", "base")
   */
  private void registerStdLib(String... parts) {
    List<Object> name = Arrays.asList((Object[]) parts);
    // Empty exports and body - these are marker libraries only
    Library lib = new Library(name, new HashSet<>(), null);
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
}
