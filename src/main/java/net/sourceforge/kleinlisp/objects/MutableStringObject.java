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
 * Mutable string object for R7RS compliance. Wraps a StringBuilder to allow string mutation
 * operations like string-set!, string-copy!, and string-fill!.
 *
 * @author Danilo Oliveira
 */
public final class MutableStringObject implements LispObject {

  private final StringBuilder value;

  public MutableStringObject(int length, char fill) {
    this.value = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      this.value.append(fill);
    }
  }

  public MutableStringObject(String value) {
    this.value = new StringBuilder(value);
  }

  public MutableStringObject(StringBuilder value) {
    this.value = value;
  }

  /** Returns the current string value. */
  public String value() {
    return value.toString();
  }

  /** Returns the length of the string. */
  public int length() {
    return value.length();
  }

  /** Returns the character at the specified index. */
  public char charAt(int index) {
    return value.charAt(index);
  }

  /** Sets the character at the specified index. */
  public void setCharAt(int index, char ch) {
    value.setCharAt(index, ch);
  }

  /** Fills the entire string with the specified character. */
  public void fill(char ch) {
    for (int i = 0; i < value.length(); i++) {
      value.setCharAt(i, ch);
    }
  }

  /** Fills a range of the string with the specified character. */
  public void fill(char ch, int start, int end) {
    for (int i = start; i < end; i++) {
      value.setCharAt(i, ch);
    }
  }

  /**
   * Copies characters from another string into this string.
   *
   * @param at the index in this string where copying begins
   * @param from the source string
   * @param start the start index in the source string
   * @param end the end index in the source string
   */
  public void copyFrom(int at, String from, int start, int end) {
    int destIndex = at;
    for (int srcIndex = start; srcIndex < end; srcIndex++) {
      if (destIndex >= value.length()) {
        break;
      }
      value.setCharAt(destIndex++, from.charAt(srcIndex));
    }
  }

  @Override
  public String toString() {
    return "\"" + value.toString() + "\"";
  }

  @Override
  public Object asObject() {
    return value.toString();
  }

  @Override
  public boolean truthiness() {
    // R7RS: Only #f is false. Empty strings are truthy.
    return true;
  }

  @Override
  public StringObject asString() {
    // Allow MutableStringObject to be used where StringObject is expected
    return new StringObject(value.toString());
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
    if (obj instanceof MutableStringObject) {
      MutableStringObject that = (MutableStringObject) obj;
      return value.toString().equals(that.value.toString());
    }
    if (obj instanceof StringObject) {
      StringObject that = (StringObject) obj;
      return value.toString().equals(that.value());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return value.toString().hashCode();
  }
}
