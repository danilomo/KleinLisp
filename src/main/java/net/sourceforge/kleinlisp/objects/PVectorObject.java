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
import org.pcollections.PVector;
import org.pcollections.TreePVector;

/**
 * Persistent vector type for KleinLisp. Based on PCollections TreePVector, providing immutable
 * vectors with efficient structural sharing. Similar to Clojure vectors.
 *
 * <p>Vectors are callable as functions: (my-vec index) returns the element at index (my-vec index
 * default) returns the element or default if out of bounds
 */
public final class PVectorObject implements LispObject, Function, Iterable<LispObject> {

  public static final PVectorObject EMPTY = new PVectorObject(TreePVector.empty());

  private final PVector<LispObject> vector;

  /** Creates a persistent vector from a PVector. */
  public PVectorObject(PVector<LispObject> vector) {
    this.vector = vector;
  }

  /** Creates a persistent vector from elements. */
  public static PVectorObject of(LispObject... elements) {
    PVector<LispObject> vec = TreePVector.empty();
    for (LispObject elem : elements) {
      vec = vec.plus(elem);
    }
    return new PVectorObject(vec);
  }

  /** Returns the underlying PVector. */
  public PVector<LispObject> getVector() {
    return vector;
  }

  /** Returns the length of the vector. */
  public int length() {
    return vector.size();
  }

  /** Returns the element at the given index. */
  public LispObject get(int index) {
    if (index < 0 || index >= vector.size()) {
      throw new LispArgumentError(
          "p-vec-ref: index out of bounds: " + index + " for vector of length " + vector.size());
    }
    return vector.get(index);
  }

  /** Returns a new vector with the element at index replaced. */
  public PVectorObject assoc(int index, LispObject value) {
    if (index < 0 || index >= vector.size()) {
      throw new LispArgumentError(
          "p-vec-assoc: index out of bounds: " + index + " for vector of length " + vector.size());
    }
    return new PVectorObject(vector.with(index, value));
  }

  /** Returns a new vector with the element added at the end. */
  public PVectorObject conj(LispObject value) {
    return new PVectorObject(vector.plus(value));
  }

  /** Returns a new vector with multiple elements added at the end. */
  public PVectorObject conjAll(Iterable<LispObject> values) {
    PVector<LispObject> result = vector;
    for (LispObject value : values) {
      result = result.plus(value);
    }
    return new PVectorObject(result);
  }

  /** Returns a new vector without the last element. */
  public PVectorObject pop() {
    if (vector.isEmpty()) {
      throw new LispArgumentError("p-vec-pop: cannot pop from empty vector");
    }
    return new PVectorObject(vector.minus(vector.size() - 1));
  }

  /** Returns the last element. */
  public LispObject peek() {
    if (vector.isEmpty()) {
      return ListObject.NIL;
    }
    return vector.get(vector.size() - 1);
  }

  /** Returns a subvector from start (inclusive) to end (exclusive). */
  public PVectorObject subvec(int start, int end) {
    if (start < 0 || end > vector.size() || start > end) {
      throw new LispArgumentError(
          "p-vec-subvec: invalid range ["
              + start
              + ", "
              + end
              + ") for vector of length "
              + vector.size());
    }
    return new PVectorObject(TreePVector.from(vector.subList(start, end)));
  }

  /** Converts the vector to a list. */
  public ListObject toList() {
    if (vector.isEmpty()) {
      return ListObject.NIL;
    }
    LispObject[] elements = new LispObject[vector.size()];
    for (int i = 0; i < vector.size(); i++) {
      elements[i] = vector.get(i);
    }
    return (ListObject) ListObject.fromList(elements);
  }

  /** Concatenates two persistent vectors. */
  public PVectorObject concat(PVectorObject other) {
    return new PVectorObject(vector.plusAll(other.vector));
  }

  @Override
  public Iterator<LispObject> iterator() {
    return vector.iterator();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < vector.size(); i++) {
      if (i > 0) {
        sb.append(" ");
      }
      sb.append(vector.get(i));
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public Object asObject() {
    return vector;
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
    if (!(obj instanceof PVectorObject)) return false;
    PVectorObject other = (PVectorObject) obj;
    return vector.equals(other.vector);
  }

  @Override
  public int hashCode() {
    return vector.hashCode();
  }

  /**
   * Makes the vector callable as a function. (my-vec index) returns element at index (my-vec index
   * default) returns element or default if out of bounds
   */
  @Override
  public LispObject evaluate(LispObject[] parameters) {
    if (parameters.length == 0) {
      throw new LispArgumentError("Vector lookup requires an index");
    }
    IntObject idx = parameters[0].asInt();
    if (idx == null) {
      throw new LispArgumentError("Vector index must be an integer");
    }
    LispObject defaultVal = parameters.length > 1 ? parameters[1] : ListObject.NIL;
    if (idx.value < 0 || idx.value >= vector.size()) {
      return defaultVal;
    }
    return vector.get(idx.value);
  }

  @Override
  public FunctionObject asFunction() {
    return new FunctionObject(this);
  }
}
