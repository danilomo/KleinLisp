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

import java.util.Iterator;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

/**
 * Persistent set type for KleinLisp. Based on PCollections HashTreePSet, providing immutable sets
 * with efficient structural sharing. Similar to Clojure sets.
 *
 * <p>Sets are callable as functions: (my-set element) returns the element if present, nil otherwise
 * (my-set element default) returns the element or default if not found
 */
public final class PSetObject implements LispObject, Function, Iterable<LispObject> {

  public static final PSetObject EMPTY = new PSetObject(HashTreePSet.empty());

  private final PSet<LispObject> set;

  /** Creates a persistent set from a PSet. */
  public PSetObject(PSet<LispObject> set) {
    this.set = set;
  }

  /** Creates a persistent set from elements. */
  public static PSetObject of(LispObject... elements) {
    PSet<LispObject> s = HashTreePSet.empty();
    for (LispObject elem : elements) {
      s = s.plus(elem);
    }
    return new PSetObject(s);
  }

  /** Returns the underlying PSet. */
  public PSet<LispObject> getSet() {
    return set;
  }

  /** Returns the number of elements in the set. */
  public int size() {
    return set.size();
  }

  /** Returns true if the set is empty. */
  public boolean isEmpty() {
    return set.isEmpty();
  }

  /** Returns true if the set contains the element. */
  public boolean contains(LispObject element) {
    return set.contains(element);
  }

  /** Returns a new set with the element added. */
  public PSetObject conj(LispObject element) {
    return new PSetObject(set.plus(element));
  }

  /** Returns a new set with multiple elements added. */
  public PSetObject conjAll(Iterable<LispObject> elements) {
    PSet<LispObject> result = set;
    for (LispObject element : elements) {
      result = result.plus(element);
    }
    return new PSetObject(result);
  }

  /** Returns a new set with the element removed. */
  public PSetObject disj(LispObject element) {
    return new PSetObject(set.minus(element));
  }

  /** Returns a new set with multiple elements removed. */
  public PSetObject disjAll(Iterable<LispObject> elements) {
    PSet<LispObject> result = set;
    for (LispObject element : elements) {
      result = result.minus(element);
    }
    return new PSetObject(result);
  }

  /** Returns the union of this set with another. */
  public PSetObject union(PSetObject other) {
    return new PSetObject(set.plusAll(other.set));
  }

  /** Returns the intersection of this set with another. */
  public PSetObject intersection(PSetObject other) {
    PSet<LispObject> result = HashTreePSet.empty();
    for (LispObject element : set) {
      if (other.set.contains(element)) {
        result = result.plus(element);
      }
    }
    return new PSetObject(result);
  }

  /** Returns the difference of this set with another (elements in this but not other). */
  public PSetObject difference(PSetObject other) {
    PSet<LispObject> result = set;
    for (LispObject element : other.set) {
      result = result.minus(element);
    }
    return new PSetObject(result);
  }

  /** Returns true if this is a subset of other. */
  public boolean isSubset(PSetObject other) {
    for (LispObject element : set) {
      if (!other.set.contains(element)) {
        return false;
      }
    }
    return true;
  }

  /** Returns true if this is a superset of other. */
  public boolean isSuperset(PSetObject other) {
    return other.isSubset(this);
  }

  /** Converts the set to a list. */
  public ListObject toList() {
    if (set.isEmpty()) {
      return ListObject.NIL;
    }
    LispObject[] elements = set.toArray(new LispObject[0]);
    return (ListObject) ListObject.fromList(elements);
  }

  @Override
  public Iterator<LispObject> iterator() {
    return set.iterator();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("#{");
    boolean first = true;
    for (LispObject element : set) {
      if (!first) {
        sb.append(" ");
      }
      first = false;
      sb.append(element);
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public Object asObject() {
    return set;
  }

  @Override
  public boolean truthiness() {
    return true;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public boolean error() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PSetObject)) return false;
    PSetObject other = (PSetObject) obj;
    return set.equals(other.set);
  }

  @Override
  public int hashCode() {
    return set.hashCode();
  }

  /**
   * Makes the set callable as a function. (my-set element) returns element if present, nil
   * otherwise (my-set element default) returns element or default if not found
   */
  @Override
  public LispObject evaluate(LispObject[] parameters) {
    if (parameters.length == 0) {
      throw new LispArgumentError("Set lookup requires an element");
    }
    LispObject element = parameters[0];
    LispObject defaultVal = parameters.length > 1 ? parameters[1] : ListObject.NIL;
    return set.contains(element) ? element : defaultVal;
  }

  @Override
  public FunctionObject asFunction() {
    return new FunctionObject(this);
  }
}
