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
package net.sourceforge.kleinlisp.api;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VectorObject;

/** Equality predicate functions for KleinLisp. */
public class EqualityFunctions {

  /**
   * Tests object identity (same reference). (eq? a b) Returns #t if a and b are the same object in
   * memory.
   */
  public static LispObject eq(LispObject[] params) {
    LispObject a = params[0];
    LispObject b = params[1];

    // Handle nil cases
    if (a == null && b == null) {
      return BooleanObject.TRUE;
    }
    if (a == null || b == null) {
      return BooleanObject.FALSE;
    }

    // Reference equality
    if (a == b) {
      return BooleanObject.TRUE;
    }

    // For AtomObject and IdentifierObject, compare the underlying atoms
    AtomObject atomA = a.asAtom();
    AtomObject atomB = b.asAtom();
    if (atomA != null && atomB != null) {
      return atomA == atomB ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    // IntObject has value caching, so we need to handle it
    IntObject intA = a.asInt();
    IntObject intB = b.asInt();
    if (intA != null && intB != null) {
      // eq? should check if they're the same cached instance
      return a == b ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    return BooleanObject.FALSE;
  }

  /**
   * Tests equivalence for primitive values. (eqv? a b) Returns #t if a and b are eq?, or if they
   * are numbers with the same value, or both are booleans with the same value.
   */
  public static LispObject eqv(LispObject[] params) {
    LispObject a = params[0];
    LispObject b = params[1];

    // Handle nil cases
    if (a == null && b == null) {
      return BooleanObject.TRUE;
    }
    if (a == null || b == null) {
      return BooleanObject.FALSE;
    }

    // Reference equality (covers many cases including booleans and cached ints)
    if (a == b) {
      return BooleanObject.TRUE;
    }

    // Compare symbols by identity
    AtomObject atomA = a.asAtom();
    AtomObject atomB = b.asAtom();
    if (atomA != null && atomB != null) {
      return atomA == atomB ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    // Compare integers by value
    IntObject intA = a.asInt();
    IntObject intB = b.asInt();
    if (intA != null && intB != null) {
      return intA.value == intB.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    // Compare doubles by value
    DoubleObject doubleA = a.asDouble();
    DoubleObject doubleB = b.asDouble();
    if (doubleA != null && doubleB != null) {
      return doubleA.value == doubleB.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    // Mixed number comparison (int vs double)
    if (intA != null && doubleB != null) {
      return (double) intA.value == doubleB.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    }
    if (doubleA != null && intB != null) {
      return doubleA.value == (double) intB.value ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    return BooleanObject.FALSE;
  }

  /**
   * Tests deep structural equality. (equal? a b) Returns #t if a and b are eqv?, or if they are
   * strings with the same characters, or lists/vectors with equal elements.
   */
  public static LispObject equal(LispObject[] params) {
    return deepEqual(params[0], params[1]) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Helper method for deep equality comparison. */
  private static boolean deepEqual(LispObject a, LispObject b) {
    // Handle nil cases
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }

    // Reference equality
    if (a == b) {
      return true;
    }

    // Compare nil/empty list
    if (a == ListObject.NIL && b == ListObject.NIL) {
      return true;
    }
    if (a == ListObject.NIL || b == ListObject.NIL) {
      // One is NIL but the other is not
      if (a == ListObject.NIL && b.asList() != null) {
        return false;
      }
      if (b == ListObject.NIL && a.asList() != null) {
        return false;
      }
      return false;
    }

    // Compare symbols
    AtomObject atomA = a.asAtom();
    AtomObject atomB = b.asAtom();
    if (atomA != null && atomB != null) {
      return atomA == atomB;
    }

    // Compare integers
    IntObject intA = a.asInt();
    IntObject intB = b.asInt();
    if (intA != null && intB != null) {
      return intA.value == intB.value;
    }

    // Compare doubles
    DoubleObject doubleA = a.asDouble();
    DoubleObject doubleB = b.asDouble();
    if (doubleA != null && doubleB != null) {
      return doubleA.value == doubleB.value;
    }

    // Mixed number comparison
    if (intA != null && doubleB != null) {
      return (double) intA.value == doubleB.value;
    }
    if (doubleA != null && intB != null) {
      return doubleA.value == (double) intB.value;
    }

    // Compare strings by content
    StringObject strA = a.asString();
    StringObject strB = b.asString();
    if (strA != null && strB != null) {
      return strA.value().equals(strB.value());
    }

    // Compare lists element by element
    ListObject listA = a.asList();
    ListObject listB = b.asList();
    if (listA != null && listB != null) {
      if (listA.length() != listB.length()) {
        return false;
      }
      while (listA != ListObject.NIL && listB != ListObject.NIL) {
        if (!deepEqual(listA.car(), listB.car())) {
          return false;
        }
        listA = listA.cdr();
        listB = listB.cdr();
      }
      return listA == ListObject.NIL && listB == ListObject.NIL;
    }

    // Compare vectors element by element
    if (a instanceof VectorObject && b instanceof VectorObject) {
      VectorObject vecA = (VectorObject) a;
      VectorObject vecB = (VectorObject) b;
      if (vecA.length() != vecB.length()) {
        return false;
      }
      for (int i = 0; i < vecA.length(); i++) {
        if (!deepEqual(vecA.ref(i), vecB.ref(i))) {
          return false;
        }
      }
      return true;
    }

    return false;
  }
}
