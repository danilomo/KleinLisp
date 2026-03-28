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

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * @author daolivei
 */
public final class JavaObject implements LispObject {

  private final Object object;

  public JavaObject(Object object) {
    this.object = object;
  }

  public Object object() {
    return object;
  }

  @Override
  public String toString() {
    return "Object: " + object.toString();
  }

  @Override
  public Object asObject() {
    return object;
  }

  @Override
  public <T> T asObject(Class<T> clazz) {
    if (clazz.isAssignableFrom(object.getClass())) {
      return (T) object;
    } else {
      return null;
    }
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

  /**
   * Delegates equality to the wrapped Java object's equals() method. Two JavaObjects are equal if
   * their wrapped objects are equal according to Object.equals().
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof JavaObject)) {
      return false;
    }
    JavaObject other = (JavaObject) obj;
    if (object == null) {
      return other.object == null;
    }
    return object.equals(other.object);
  }

  /** Delegates hashCode to the wrapped Java object's hashCode() method. */
  @Override
  public int hashCode() {
    return object == null ? 0 : object.hashCode();
  }

  /** Returns true if the wrapped object implements Comparable. */
  public boolean isComparable() {
    return object instanceof Comparable;
  }

  /**
   * Compares this JavaObject with another, if both wrap Comparable objects of compatible types.
   * Returns negative if this < other, zero if equal, positive if this > other.
   *
   * @throws ClassCastException if the wrapped objects are not mutually comparable
   * @throws IllegalStateException if the wrapped object is not Comparable
   */
  @SuppressWarnings("unchecked")
  public int compareTo(JavaObject other) {
    if (!(object instanceof Comparable)) {
      throw new IllegalStateException("Wrapped object is not Comparable: " + object.getClass());
    }
    return ((Comparable<Object>) object).compareTo(other.object);
  }
}
