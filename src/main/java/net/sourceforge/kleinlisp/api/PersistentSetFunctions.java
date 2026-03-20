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

import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.CellObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.PSetObject;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

/**
 * Persistent set functions for KleinLisp. These functions provide Clojure-like persistent set
 * operations with structural sharing.
 */
public class PersistentSetFunctions {

  /**
   * Creates a persistent set from elements. (p-set) creates empty set (p-set e1 e2 ...) creates set
   * with elements
   */
  public static LispObject pSet(LispObject[] params) {
    if (params.length == 0) {
      return PSetObject.EMPTY;
    }
    return PSetObject.of(params);
  }

  /** Returns true if the set contains the element. (p-set-contains? s elem) */
  public static LispObject pSetContains(LispObject[] params) {
    PSetObject set = asPSet(params[0]);
    if (set == null) {
      throw new LispArgumentError("p-set-contains? requires a persistent set as first argument");
    }
    return set.contains(unwrap(params[1])) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /**
   * Returns a new set with the element added. (p-set-conj s elem) or (p-set-conj s e1 e2 ...) Does
   * not modify the original set.
   */
  public static LispObject pSetConj(LispObject[] params) {
    PSetObject set = asPSet(params[0]);
    if (set == null) {
      throw new LispArgumentError("p-set-conj requires a persistent set as first argument");
    }
    for (int i = 1; i < params.length; i++) {
      set = set.conj(unwrap(params[i]));
    }
    return set;
  }

  /**
   * Returns a new set with the element removed. (p-set-disj s elem) or (p-set-disj s e1 e2 ...)
   * Does not modify the original set.
   */
  public static LispObject pSetDisj(LispObject[] params) {
    PSetObject set = asPSet(params[0]);
    if (set == null) {
      throw new LispArgumentError("p-set-disj requires a persistent set as first argument");
    }
    for (int i = 1; i < params.length; i++) {
      set = set.disj(unwrap(params[i]));
    }
    return set;
  }

  /** Returns the union of two or more sets. (p-set-union s1 s2 ...) */
  public static LispObject pSetUnion(LispObject[] params) {
    if (params.length == 0) {
      return PSetObject.EMPTY;
    }
    PSetObject result = asPSet(params[0]);
    if (result == null) {
      throw new LispArgumentError("p-set-union requires persistent sets");
    }
    for (int i = 1; i < params.length; i++) {
      PSetObject other = asPSet(params[i]);
      if (other == null) {
        throw new LispArgumentError("p-set-union requires persistent sets");
      }
      result = result.union(other);
    }
    return result;
  }

  /** Returns the intersection of two or more sets. (p-set-intersection s1 s2 ...) */
  public static LispObject pSetIntersection(LispObject[] params) {
    if (params.length == 0) {
      return PSetObject.EMPTY;
    }
    PSetObject result = asPSet(params[0]);
    if (result == null) {
      throw new LispArgumentError("p-set-intersection requires persistent sets");
    }
    for (int i = 1; i < params.length; i++) {
      PSetObject other = asPSet(params[i]);
      if (other == null) {
        throw new LispArgumentError("p-set-intersection requires persistent sets");
      }
      result = result.intersection(other);
    }
    return result;
  }

  /**
   * Returns the difference of two sets (elements in first but not second). (p-set-difference s1 s2)
   */
  public static LispObject pSetDifference(LispObject[] params) {
    PSetObject set1 = asPSet(params[0]);
    if (set1 == null) {
      throw new LispArgumentError("p-set-difference requires a persistent set as first argument");
    }
    PSetObject set2 = asPSet(params[1]);
    if (set2 == null) {
      throw new LispArgumentError("p-set-difference requires a persistent set as second argument");
    }
    return set1.difference(set2);
  }

  /** Returns true if s1 is a subset of s2. (p-set-subset? s1 s2) */
  public static LispObject pSetSubset(LispObject[] params) {
    PSetObject set1 = asPSet(params[0]);
    if (set1 == null) {
      throw new LispArgumentError("p-set-subset? requires a persistent set as first argument");
    }
    PSetObject set2 = asPSet(params[1]);
    if (set2 == null) {
      throw new LispArgumentError("p-set-subset? requires a persistent set as second argument");
    }
    return set1.isSubset(set2) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Returns true if s1 is a superset of s2. (p-set-superset? s1 s2) */
  public static LispObject pSetSuperset(LispObject[] params) {
    PSetObject set1 = asPSet(params[0]);
    if (set1 == null) {
      throw new LispArgumentError("p-set-superset? requires a persistent set as first argument");
    }
    PSetObject set2 = asPSet(params[1]);
    if (set2 == null) {
      throw new LispArgumentError("p-set-superset? requires a persistent set as second argument");
    }
    return set1.isSuperset(set2) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Returns the number of elements in the set. (p-set-size s) */
  public static LispObject pSetSize(LispObject[] params) {
    PSetObject set = asPSet(params[0]);
    if (set == null) {
      throw new LispArgumentError("p-set-size requires a persistent set");
    }
    return IntObject.valueOf(set.size());
  }

  /** Converts a persistent set to a list. (p-set->list s) */
  public static LispObject pSetToList(LispObject[] params) {
    PSetObject set = asPSet(params[0]);
    if (set == null) {
      throw new LispArgumentError("p-set->list requires a persistent set");
    }
    return set.toList();
  }

  /** Converts a list to a persistent set. (list->p-set l) */
  public static LispObject listToPSet(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return PSetObject.EMPTY;
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("list->p-set requires a list");
    }
    PSet<LispObject> set = HashTreePSet.empty();
    for (LispObject elem : list) {
      set = set.plus(elem);
    }
    return new PSetObject(set);
  }

  /** Tests if the value is a persistent set. (p-set? x) */
  public static LispObject isPSet(LispObject[] params) {
    return (asPSet(params[0]) != null) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the persistent set is empty. (p-set-empty? s) */
  public static LispObject isPSetEmpty(LispObject[] params) {
    PSetObject set = asPSet(params[0]);
    if (set == null) {
      throw new LispArgumentError("p-set-empty? requires a persistent set");
    }
    return set.isEmpty() ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Unwraps a LispObject if it's a CellObject. */
  private static LispObject unwrap(LispObject obj) {
    if (obj instanceof CellObject) {
      return ((CellObject) obj).get();
    }
    return obj;
  }

  /** Unwraps and casts to PSetObject, or returns null if not a PSetObject. */
  private static PSetObject asPSet(LispObject obj) {
    LispObject unwrapped = unwrap(obj);
    if (unwrapped instanceof PSetObject) {
      return (PSetObject) unwrapped;
    }
    return null;
  }
}
