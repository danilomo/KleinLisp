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

import java.util.List;
import java.util.Set;

/**
 * Represents an R7RS library definition.
 *
 * <p>A library consists of:
 *
 * <ul>
 *   <li>Name: a list of symbols/integers (e.g., (scheme base) or (mylib utils 1))
 *   <li>Exports: set of symbol names that are publicly accessible
 *   <li>Body: executable code and definitions within the library
 * </ul>
 *
 * <p>Note: In KleinLisp's backwards-compatible implementation (Option B), libraries are primarily
 * used for R7RS source compatibility. All standard library functions are loaded globally at
 * startup, regardless of imports.
 *
 * @author Danilo Oliveira
 */
public class Library {

  private final List<Object> name;
  private final Set<String> exports;
  private final LispObject body;

  /**
   * Creates a new library definition.
   *
   * @param name the library name as a list (e.g., ["scheme", "base"])
   * @param exports set of exported symbol names
   * @param body the library body (typically a begin form)
   */
  public Library(List<Object> name, Set<String> exports, LispObject body) {
    this.name = name;
    this.exports = exports;
    this.body = body;
  }

  /**
   * Returns the library name as a list.
   *
   * @return the library name
   */
  public List<Object> getName() {
    return name;
  }

  /**
   * Returns the library name as a string for display (e.g., "(scheme base)").
   *
   * @return the library name as a string
   */
  public String getNameString() {
    StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < name.size(); i++) {
      if (i > 0) sb.append(" ");
      sb.append(name.get(i));
    }
    sb.append(")");
    return sb.toString();
  }

  /**
   * Returns the set of exported symbols.
   *
   * @return the exports
   */
  public Set<String> getExports() {
    return exports;
  }

  /**
   * Returns the library body.
   *
   * @return the body
   */
  public LispObject getBody() {
    return body;
  }

  /**
   * Checks if this library exports the given symbol.
   *
   * @param symbol the symbol to check
   * @return true if exported, false otherwise
   */
  public boolean exports(String symbol) {
    return exports.contains(symbol);
  }

  @Override
  public String toString() {
    return "Library" + getNameString();
  }
}
