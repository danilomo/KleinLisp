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
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * Represents a keyword in Scheme syntax (#:keyword). Keywords are self-evaluating symbols used
 * primarily for named arguments.
 *
 * <p>Keywords are callable as functions for map lookup: (#:key my-map) returns (p-map-get my-map
 * #:key) (#:key my-map default) returns (p-map-get my-map #:key default)
 *
 * @author Danilo Oliveira
 */
public final class KeywordObject implements LispObject, Function {

  private final String name;

  public KeywordObject(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return "#:" + name;
  }

  @Override
  public Object asObject() {
    return this;
  }

  @Override
  public boolean truthiness() {
    return true;
  }

  @Override
  public KeywordObject asKeyword() {
    return this;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    KeywordObject that = (KeywordObject) obj;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * Makes keywords callable for map lookup. (#:key my-map) returns value for :key in map (#:key
   * my-map default) returns value or default
   */
  @Override
  public LispObject evaluate(LispObject[] parameters) {
    if (parameters.length == 0) {
      throw new LispArgumentError("Keyword lookup requires a map argument");
    }
    LispObject coll = parameters[0];
    LispObject defaultVal = parameters.length > 1 ? parameters[1] : ListObject.NIL;

    if (coll instanceof PMapObject) {
      return ((PMapObject) coll).getOrDefault(this, defaultVal);
    }

    // Also work with regular association lists
    ListObject list = coll.asList();
    if (list != null && list != ListObject.NIL) {
      // Try as alist: ((key val) (key val) ...)
      for (LispObject entry : list) {
        ListObject pair = entry.asList();
        if (pair != null && pair.car().equals(this)) {
          return pair.cdr().car();
        }
      }
    }

    return defaultVal;
  }

  @Override
  public FunctionObject asFunction() {
    return new FunctionObject(this);
  }
}
