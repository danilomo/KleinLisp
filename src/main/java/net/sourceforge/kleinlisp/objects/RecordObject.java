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

/**
 * Represents an instance of an R7RS record type.
 *
 * <p>A record is a structured data type with named fields defined by a record type. Each record
 * instance holds values for all fields defined in its type.
 *
 * @author Danilo Oliveira
 */
public final class RecordObject implements LispObject {

  private final RecordTypeObject recordType;
  private final LispObject[] fieldValues;

  /**
   * Creates a new record instance.
   *
   * @param recordType The record type definition
   * @param fieldValues The values for all fields
   */
  public RecordObject(RecordTypeObject recordType, LispObject[] fieldValues) {
    this.recordType = recordType;
    this.fieldValues = fieldValues.clone(); // Defensive copy
  }

  /** Returns the record type of this instance. */
  public RecordTypeObject getRecordType() {
    return recordType;
  }

  /** Returns the type name. */
  public AtomObject getTypeName() {
    return recordType.getTypeName();
  }

  /**
   * Gets the value of a field by index.
   *
   * @param fieldIndex The index of the field
   * @return The field value
   */
  public LispObject getField(int fieldIndex) {
    if (fieldIndex < 0 || fieldIndex >= fieldValues.length) {
      throw new LispArgumentError(
          "Field index out of bounds: "
              + fieldIndex
              + " for record type "
              + recordType.getTypeName());
    }
    return fieldValues[fieldIndex];
  }

  /**
   * Sets the value of a mutable field by index.
   *
   * @param fieldIndex The index of the field
   * @param value The new value
   */
  public void setField(int fieldIndex, LispObject value) {
    if (fieldIndex < 0 || fieldIndex >= fieldValues.length) {
      throw new LispArgumentError(
          "Field index out of bounds: "
              + fieldIndex
              + " for record type "
              + recordType.getTypeName());
    }
    if (!recordType.isFieldMutable(fieldIndex)) {
      throw new LispArgumentError(
          "Cannot mutate immutable field at index "
              + fieldIndex
              + " in record type "
              + recordType.getTypeName());
    }
    fieldValues[fieldIndex] = value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("#<");
    sb.append(recordType.getTypeName());
    for (int i = 0; i < fieldValues.length; i++) {
      sb.append(" ");
      sb.append(fieldValues[i]);
    }
    sb.append(">");
    return sb.toString();
  }

  @Override
  public Object asObject() {
    return this;
  }

  @Override
  public <T> T asObject(Class<T> clazz) {
    if (clazz.isAssignableFrom(this.getClass())) {
      return (T) this;
    }
    return null;
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
