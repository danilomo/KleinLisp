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

import java.util.List;
import java.util.Set;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * Represents an R7RS record type definition created by define-record-type.
 *
 * <p>A record type defines the structure of records, including field names, constructor parameters,
 * and which fields are mutable.
 *
 * @author Danilo Oliveira
 */
public final class RecordTypeObject implements LispObject {

  private final AtomObject typeName;
  private final List<AtomObject> fieldNames;
  private final int[] constructorArgIndices; // Maps constructor arg position to field index
  private final Set<Integer> mutableFieldIndices;

  /**
   * Creates a new record type definition.
   *
   * @param typeName The name of the record type
   * @param fieldNames The list of field names in order
   * @param constructorArgIndices Maps constructor argument positions to field indices
   * @param mutableFieldIndices Set of field indices that are mutable
   */
  public RecordTypeObject(
      AtomObject typeName,
      List<AtomObject> fieldNames,
      int[] constructorArgIndices,
      Set<Integer> mutableFieldIndices) {
    this.typeName = typeName;
    this.fieldNames = fieldNames;
    this.constructorArgIndices = constructorArgIndices;
    this.mutableFieldIndices = mutableFieldIndices;
  }

  /** Returns the type name. */
  public AtomObject getTypeName() {
    return typeName;
  }

  /** Returns the list of field names. */
  public List<AtomObject> getFieldNames() {
    return fieldNames;
  }

  /** Returns the number of fields. */
  public int getFieldCount() {
    return fieldNames.size();
  }

  /** Returns the mapping from constructor argument indices to field indices. */
  public int[] getConstructorArgIndices() {
    return constructorArgIndices;
  }

  /** Returns the set of mutable field indices. */
  public Set<Integer> getMutableFieldIndices() {
    return mutableFieldIndices;
  }

  /** Checks if a field at the given index is mutable. */
  public boolean isFieldMutable(int fieldIndex) {
    return mutableFieldIndices.contains(fieldIndex);
  }

  /**
   * Creates a new instance of this record type with the given field values.
   *
   * @param fieldValues The values for all fields
   * @return A new RecordObject instance
   */
  public RecordObject createInstance(LispObject[] fieldValues) {
    if (fieldValues.length != fieldNames.size()) {
      throw new IllegalArgumentException(
          "Expected "
              + fieldNames.size()
              + " field values for record type "
              + typeName
              + ", got "
              + fieldValues.length);
    }
    return new RecordObject(this, fieldValues);
  }

  @Override
  public String toString() {
    return "#<record-type " + typeName + ">";
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
