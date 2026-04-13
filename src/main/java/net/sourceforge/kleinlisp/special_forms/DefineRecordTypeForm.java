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
package net.sourceforge.kleinlisp.special_forms;

import java.util.*;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispException;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.*;

/**
 * Implements the R7RS define-record-type special form.
 *
 * <p>Syntax: (define-record-type name (constructor field ...) predicate (field accessor [mutator])
 * ...)
 *
 * <p>Example: (define-record-type point (make-point x y) point? (x point-x) (y point-y
 * set-point-y!))
 *
 * @author Danilo Oliveira
 */
public class DefineRecordTypeForm implements SpecialForm {

  private final LispEnvironment environment;

  public DefineRecordTypeForm(LispEnvironment environment) {
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject form = t.asList();
    FormErrors.assertMinArgs("define-record-type", form, 3);

    // Parse: (define-record-type NAME (CONSTRUCTOR args...) PREDICATE (field accessor [mutator])
    // ...)
    ListObject rest = form.cdr();

    // 1. Type name
    AtomObject typeName = extractAtom("define-record-type", rest.car(), form);
    rest = rest.cdr();

    // 2. Constructor clause: (constructor-name arg1 arg2 ...)
    ListObject constructorClause = extractList("define-record-type", rest.car(), form);
    AtomObject constructorName = extractAtom("define-record-type", constructorClause.car(), form);
    List<AtomObject> constructorArgs = new ArrayList<>();
    for (LispObject arg : constructorClause.cdr()) {
      constructorArgs.add(extractAtom("define-record-type", arg, form));
    }
    rest = rest.cdr();

    // 3. Predicate name
    AtomObject predicateName = extractAtom("define-record-type", rest.car(), form);
    rest = rest.cdr();

    // 4. Field clauses: (field-name accessor [mutator])
    List<FieldSpec> fields = new ArrayList<>();
    Map<AtomObject, Integer> fieldIndexMap = new HashMap<>();

    for (LispObject fieldClause : rest) {
      ListObject fieldList = extractList("define-record-type", fieldClause, form);
      if (fieldList.length() < 2 || fieldList.length() > 3) {
        throw FormErrors.badForm("define-record-type", form);
      }

      AtomObject fieldName = extractAtom("define-record-type", fieldList.car(), form);
      AtomObject accessor = extractAtom("define-record-type", fieldList.cdr().car(), form);
      AtomObject mutator =
          fieldList.length() == 3
              ? extractAtom("define-record-type", fieldList.cdr().cdr().car(), form)
              : null;

      int fieldIndex = fields.size();
      fieldIndexMap.put(fieldName, fieldIndex);
      fields.add(new FieldSpec(fieldName, accessor, mutator));
    }

    // 5. Build constructor arg indices mapping
    int[] constructorArgIndices = new int[constructorArgs.size()];
    for (int i = 0; i < constructorArgs.size(); i++) {
      AtomObject argName = constructorArgs.get(i);
      Integer fieldIndex = fieldIndexMap.get(argName);
      if (fieldIndex == null) {
        throw new LispException(
            "define-record-type: constructor argument "
                + argName
                + " does not match any field in "
                + form);
      }
      constructorArgIndices[i] = fieldIndex;
    }

    // 6. Build mutable field indices set
    Set<Integer> mutableFieldIndices = new HashSet<>();
    for (int i = 0; i < fields.size(); i++) {
      if (fields.get(i).mutator != null) {
        mutableFieldIndices.add(i);
      }
    }

    // 7. Create RecordTypeObject
    List<AtomObject> fieldNames = new ArrayList<>();
    for (FieldSpec field : fields) {
      fieldNames.add(field.fieldName);
    }
    RecordTypeObject recordType =
        new RecordTypeObject(typeName, fieldNames, constructorArgIndices, mutableFieldIndices);

    // 8. Register constructor function
    environment.set(
        constructorName,
        new FunctionObject(
            new Function() {
              @Override
              public LispObject evaluate(LispObject[] params) {
                if (params.length != constructorArgs.size()) {
                  throw new LispArgumentError(
                      constructorName
                          + ": expected "
                          + constructorArgs.size()
                          + " arguments, got "
                          + params.length);
                }
                // Map constructor arguments to field values
                LispObject[] fieldValues = new LispObject[fields.size()];
                for (int i = 0; i < fieldValues.length; i++) {
                  fieldValues[i] = ListObject.NIL; // Default value
                }
                for (int i = 0; i < params.length; i++) {
                  fieldValues[constructorArgIndices[i]] = params[i];
                }
                return recordType.createInstance(fieldValues);
              }
            }));

    // 9. Register predicate function
    environment.set(
        predicateName,
        new FunctionObject(
            new Function() {
              @Override
              public LispObject evaluate(LispObject[] params) {
                if (params.length != 1) {
                  throw new LispArgumentError(
                      predicateName + ": expected 1 argument, got " + params.length);
                }
                RecordObject record = params[0].asObject(RecordObject.class);
                if (record != null
                    && record.getRecordType().getTypeName().value().equals(typeName.value())) {
                  return BooleanObject.TRUE;
                }
                return BooleanObject.FALSE;
              }
            }));

    // 10. Register accessor and mutator functions
    for (int i = 0; i < fields.size(); i++) {
      FieldSpec field = fields.get(i);
      final int fieldIndex = i;

      // Register accessor
      environment.set(
          field.accessor,
          new FunctionObject(
              new Function() {
                @Override
                public LispObject evaluate(LispObject[] params) {
                  if (params.length != 1) {
                    throw new LispArgumentError(
                        field.accessor + ": expected 1 argument, got " + params.length);
                  }
                  RecordObject record = params[0].asObject(RecordObject.class);
                  if (record == null
                      || !record.getRecordType().getTypeName().value().equals(typeName.value())) {
                    throw new LispArgumentError(
                        field.accessor + ": expected record of type " + typeName);
                  }
                  return record.getField(fieldIndex);
                }
              }));

      // Register mutator if present
      if (field.mutator != null) {
        environment.set(
            field.mutator,
            new FunctionObject(
                new Function() {
                  @Override
                  public LispObject evaluate(LispObject[] params) {
                    if (params.length != 2) {
                      throw new LispArgumentError(
                          field.mutator + ": expected 2 arguments, got " + params.length);
                    }
                    RecordObject record = params[0].asObject(RecordObject.class);
                    if (record == null
                        || !record.getRecordType().getTypeName().value().equals(typeName.value())) {
                      throw new LispArgumentError(
                          field.mutator + ": expected record of type " + typeName);
                    }
                    record.setField(fieldIndex, params[1]);
                    return VoidObject.VOID;
                  }
                }));
      }
    }

    return () -> VoidObject.VOID;
  }

  private AtomObject extractAtom(String formName, LispObject obj, LispObject fullForm) {
    AtomObject atom = obj.asAtom();
    if (atom == null) {
      IdentifierObject identifier = obj.asIdentifier();
      if (identifier != null) {
        atom = identifier.asAtom();
      }
    }
    if (atom == null) {
      throw FormErrors.badForm(formName, fullForm);
    }
    return atom;
  }

  private ListObject extractList(String formName, LispObject obj, LispObject fullForm) {
    ListObject list = obj.asList();
    if (list == null) {
      throw FormErrors.badForm(formName, fullForm);
    }
    return list;
  }

  private static class FieldSpec {
    final AtomObject fieldName;
    final AtomObject accessor;
    final AtomObject mutator; // null if immutable

    FieldSpec(AtomObject fieldName, AtomObject accessor, AtomObject mutator) {
      this.fieldName = fieldName;
      this.accessor = accessor;
      this.mutator = mutator;
    }
  }
}
