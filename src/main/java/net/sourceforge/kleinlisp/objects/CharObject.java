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

import net.sourceforge.kleinlisp.LispVisitor;

/**
 * Represents a Scheme character object (R7RS character type).
 *
 * @author daolivei
 */
public final class CharObject implements net.sourceforge.kleinlisp.LispObject {

  private final char value;

  public CharObject(char value) {
    this.value = value;
  }

  public char getValue() {
    return value;
  }

  @Override
  public Object asObject() {
    return value;
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
  public String toString() {
    switch (value) {
      case ' ':
        return "#\\space";
      case '\n':
        return "#\\newline";
      case '\t':
        return "#\\tab";
      case '\r':
        return "#\\return";
      case '\0':
        return "#\\null";
      case '\007':
        return "#\\alarm";
      case '\b':
        return "#\\backspace";
      case '\033':
        return "#\\escape";
      case '\177':
        return "#\\delete";
      default:
        return "#\\" + value;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    CharObject that = (CharObject) obj;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Character.hashCode(value);
  }
}
