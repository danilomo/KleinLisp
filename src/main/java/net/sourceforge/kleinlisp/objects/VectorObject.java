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

import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/** Mutable vector (array) type for KleinLisp. Provides O(1) indexed access. */
public final class VectorObject implements LispObject {

  private final LispObject[] elements;

  /** Creates a vector of the given size, initialized with nil. */
  public VectorObject(int size) {
    this.elements = new LispObject[size];
    for (int i = 0; i < size; i++) {
      this.elements[i] = ListObject.NIL;
    }
  }

  /** Creates a vector of the given size, initialized with the given fill value. */
  public VectorObject(int size, LispObject fill) {
    this.elements = new LispObject[size];
    for (int i = 0; i < size; i++) {
      this.elements[i] = fill;
    }
  }

  /** Creates a vector from an array of elements. */
  public VectorObject(LispObject[] elements) {
    this.elements = elements.clone();
  }

  /** Returns the length of the vector. */
  public int length() {
    return elements.length;
  }

  /** Returns the element at the given index. */
  public LispObject ref(int index) {
    if (index < 0 || index >= elements.length) {
      throw new LispArgumentError(
          "vector-ref: index out of bounds: " + index + " for vector of length " + elements.length);
    }
    return elements[index];
  }

  /** Sets the element at the given index. */
  public void set(int index, LispObject value) {
    if (index < 0 || index >= elements.length) {
      throw new LispArgumentError(
          "vector-set!: index out of bounds: "
              + index
              + " for vector of length "
              + elements.length);
    }
    elements[index] = value;
  }

  /** Converts the vector to a list. */
  public ListObject toList() {
    if (elements.length == 0) {
      return ListObject.NIL;
    }
    return (ListObject) ListObject.fromList(elements);
  }

  /** Returns a copy of the internal array. */
  public LispObject[] toArray() {
    return elements.clone();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("#(");
    for (int i = 0; i < elements.length; i++) {
      if (i > 0) {
        sb.append(" ");
      }
      sb.append(elements[i]);
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public Object asObject() {
    return elements;
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
}
