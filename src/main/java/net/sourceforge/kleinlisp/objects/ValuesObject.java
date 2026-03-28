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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.Seq;

/**
 * Represents multiple return values in R7RS Scheme.
 *
 * <p>When used in a single-value context, only the first value is used. When used with
 * call-with-values, all values are passed to the consumer.
 */
public class ValuesObject implements LispObject {

  private final LispObject[] values;

  public ValuesObject(LispObject[] values) {
    this.values = values;
  }

  public ValuesObject(List<LispObject> values) {
    this.values = values.toArray(new LispObject[0]);
  }

  public LispObject[] getValues() {
    return values;
  }

  public int count() {
    return values.length;
  }

  public LispObject get(int index) {
    if (index < 0 || index >= values.length) {
      throw new IndexOutOfBoundsException("Value index out of bounds: " + index);
    }
    return values[index];
  }

  /**
   * When a ValuesObject is used in a single-value context, return the first value (or void if
   * empty).
   */
  public LispObject primaryValue() {
    if (values.length == 0) {
      return VoidObject.VOID;
    }
    return values[0];
  }

  @Override
  public Object asObject() {
    return primaryValue().asObject();
  }

  @Override
  public boolean truthiness() {
    // A ValuesObject with at least one value is truthy if its primary value is truthy
    if (values.length == 0) {
      return false;
    }
    return values[0].truthiness();
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  // Delegate single-value context methods to the primary value

  @Override
  public IntObject asInt() {
    return primaryValue().asInt();
  }

  @Override
  public DoubleObject asDouble() {
    return primaryValue().asDouble();
  }

  @Override
  public ListObject asList() {
    return primaryValue().asList();
  }

  @Override
  public FunctionObject asFunction() {
    return primaryValue().asFunction();
  }

  @Override
  public AtomObject asAtom() {
    return primaryValue().asAtom();
  }

  @Override
  public StringObject asString() {
    return primaryValue().asString();
  }

  @Override
  public IdentifierObject asIdentifier() {
    return primaryValue().asIdentifier();
  }

  @Override
  public KeywordObject asKeyword() {
    return primaryValue().asKeyword();
  }

  @Override
  public CellObject asCell() {
    return primaryValue().asCell();
  }

  @Override
  public Seq asSeq() {
    return primaryValue().asSeq();
  }

  @Override
  public String toString() {
    if (values.length == 0) {
      return ""; // No values
    }
    if (values.length == 1) {
      return values[0].toString();
    }
    return Arrays.stream(values).map(LispObject::toString).collect(Collectors.joining("\n"));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ValuesObject that = (ValuesObject) obj;
    return Arrays.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }
}
