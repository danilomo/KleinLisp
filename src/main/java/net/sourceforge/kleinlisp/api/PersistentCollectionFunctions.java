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
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.PMapObject;
import net.sourceforge.kleinlisp.objects.PSetObject;
import net.sourceforge.kleinlisp.objects.PVectorObject;

/**
 * Generic functions that work across all persistent collection types, similar to Clojure's
 * polymorphic collection operations.
 *
 * <p>These provide a unified API: - (get coll key [default]) - works on p-map, p-vec, p-set -
 * (assoc coll key val ...) - works on p-map, p-vec - (conj coll val ...) - works on p-map, p-vec,
 * p-set - (dissoc coll key ...) - works on p-map, p-set - (contains? coll key) - works on p-map,
 * p-set, p-vec - (count coll) - works on all - (empty? coll) - works on all
 */
public class PersistentCollectionFunctions {

  /**
   * Generic get operation. (get coll key) or (get coll key default)
   *
   * <p>- For p-map: returns value for key, or default/nil - For p-vec: returns element at index, or
   * default/nil - For p-set: returns the element if present, or default/nil
   */
  public static LispObject get(LispObject[] params) {
    LispObject coll = params[0];
    LispObject key = params[1];
    LispObject defaultVal = params.length > 2 ? params[2] : ListObject.NIL;

    if (coll instanceof PMapObject) {
      PMapObject map = (PMapObject) coll;
      return map.getOrDefault(key, defaultVal);
    }

    if (coll instanceof PVectorObject) {
      PVectorObject vec = (PVectorObject) coll;
      IntObject idx = key.asInt();
      if (idx == null) {
        return defaultVal;
      }
      if (idx.value < 0 || idx.value >= vec.length()) {
        return defaultVal;
      }
      return vec.get(idx.value);
    }

    if (coll instanceof PSetObject) {
      PSetObject set = (PSetObject) coll;
      return set.contains(key) ? key : defaultVal;
    }

    throw new LispArgumentError(
        "get: expected a persistent collection, got " + coll.getClass().getSimpleName());
  }

  /**
   * Polymorphic assoc operation.
   *
   * <p>For persistent collections (Clojure-style): (assoc coll key val) or (assoc coll k1 v1 k2 v2
   * ...) - For p-map: associates key with value - For p-vec: sets element at index (index must be
   * valid or equal to length for append)
   *
   * <p>For association lists (Scheme-style): (assoc key alist) - Searches for key in alist, returns
   * the pair or #f
   */
  public static LispObject assoc(LispObject[] params) {
    LispObject first = params[0];

    // If first argument is a persistent collection, use Clojure-style assoc
    if (first instanceof PMapObject || first instanceof PVectorObject) {
      if ((params.length - 1) % 2 != 0) {
        throw new LispArgumentError("assoc: requires key-value pairs for persistent collections");
      }

      if (first instanceof PMapObject) {
        PMapObject map = (PMapObject) first;
        for (int i = 1; i < params.length; i += 2) {
          map = map.assoc(params[i], params[i + 1]);
        }
        return map;
      }

      if (first instanceof PVectorObject) {
        PVectorObject vec = (PVectorObject) first;
        for (int i = 1; i < params.length; i += 2) {
          IntObject idx = params[i].asInt();
          if (idx == null) {
            throw new LispArgumentError("assoc: vector index must be an integer");
          }
          // Allow appending at the end (Clojure behavior)
          if (idx.value == vec.length()) {
            vec = vec.conj(params[i + 1]);
          } else {
            vec = vec.assoc(idx.value, params[i + 1]);
          }
        }
        return vec;
      }
    }

    // Otherwise, delegate to Scheme-style alist assoc
    return ListFunctions.assoc(params);
  }

  /**
   * Generic conj (conjoin) operation. (conj coll val) or (conj coll v1 v2 ...)
   *
   * <p>- For p-vec: adds elements at the end - For p-set: adds elements - For p-map: val must be a
   * 2-element list [key value]
   */
  public static LispObject conj(LispObject[] params) {
    LispObject coll = params[0];

    if (coll instanceof PVectorObject) {
      PVectorObject vec = (PVectorObject) coll;
      for (int i = 1; i < params.length; i++) {
        vec = vec.conj(params[i]);
      }
      return vec;
    }

    if (coll instanceof PSetObject) {
      PSetObject set = (PSetObject) coll;
      for (int i = 1; i < params.length; i++) {
        set = set.conj(params[i]);
      }
      return set;
    }

    if (coll instanceof PMapObject) {
      PMapObject map = (PMapObject) coll;
      for (int i = 1; i < params.length; i++) {
        ListObject pair = params[i].asList();
        if (pair == null || pair.length() != 2) {
          throw new LispArgumentError("conj: for p-map, each value must be a (key value) pair");
        }
        map = map.assoc(pair.car(), pair.cdr().car());
      }
      return map;
    }

    throw new LispArgumentError(
        "conj: expected a persistent collection, got " + coll.getClass().getSimpleName());
  }

  /**
   * Generic dissoc (disassociate) operation. (dissoc coll key) or (dissoc coll k1 k2 ...)
   *
   * <p>- For p-map: removes keys - For p-set: removes elements (same as disj)
   */
  public static LispObject dissoc(LispObject[] params) {
    LispObject coll = params[0];

    if (coll instanceof PMapObject) {
      PMapObject map = (PMapObject) coll;
      for (int i = 1; i < params.length; i++) {
        map = map.dissoc(params[i]);
      }
      return map;
    }

    if (coll instanceof PSetObject) {
      PSetObject set = (PSetObject) coll;
      for (int i = 1; i < params.length; i++) {
        set = set.disj(params[i]);
      }
      return set;
    }

    throw new LispArgumentError(
        "dissoc: expected a p-map or p-set, got " + coll.getClass().getSimpleName());
  }

  /**
   * Generic contains? operation. (contains? coll key)
   *
   * <p>- For p-map: checks if key exists - For p-set: checks if element exists - For p-vec: checks
   * if index is valid
   */
  public static LispObject contains(LispObject[] params) {
    LispObject coll = params[0];
    LispObject key = params[1];

    if (coll instanceof PMapObject) {
      PMapObject map = (PMapObject) coll;
      return map.containsKey(key) ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    if (coll instanceof PSetObject) {
      PSetObject set = (PSetObject) coll;
      return set.contains(key) ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    if (coll instanceof PVectorObject) {
      PVectorObject vec = (PVectorObject) coll;
      IntObject idx = key.asInt();
      if (idx == null) {
        return BooleanObject.FALSE;
      }
      return (idx.value >= 0 && idx.value < vec.length())
          ? BooleanObject.TRUE
          : BooleanObject.FALSE;
    }

    throw new LispArgumentError(
        "contains?: expected a persistent collection, got " + coll.getClass().getSimpleName());
  }

  /**
   * Generic count operation. (count coll)
   *
   * <p>Works on p-map, p-vec, p-set, and also regular lists.
   */
  public static LispObject count(LispObject[] params) {
    LispObject coll = params[0];

    if (coll instanceof PVectorObject) {
      return IntObject.valueOf(((PVectorObject) coll).length());
    }

    if (coll instanceof PMapObject) {
      return IntObject.valueOf(((PMapObject) coll).size());
    }

    if (coll instanceof PSetObject) {
      return IntObject.valueOf(((PSetObject) coll).size());
    }

    // Also support regular lists for convenience
    if (coll == ListObject.NIL) {
      return IntObject.valueOf(0);
    }

    ListObject list = coll.asList();
    if (list != null) {
      return IntObject.valueOf(list.length());
    }

    throw new LispArgumentError(
        "count: expected a collection, got " + coll.getClass().getSimpleName());
  }

  /**
   * Generic empty? operation. (empty? coll)
   *
   * <p>Works on all persistent collections and regular lists.
   */
  public static LispObject isEmpty(LispObject[] params) {
    LispObject coll = params[0];

    if (coll instanceof PVectorObject) {
      return ((PVectorObject) coll).length() == 0 ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    if (coll instanceof PMapObject) {
      return ((PMapObject) coll).isEmpty() ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    if (coll instanceof PSetObject) {
      return ((PSetObject) coll).isEmpty() ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    if (coll == ListObject.NIL) {
      return BooleanObject.TRUE;
    }

    ListObject list = coll.asList();
    if (list != null) {
      return list == ListObject.NIL ? BooleanObject.TRUE : BooleanObject.FALSE;
    }

    throw new LispArgumentError(
        "empty?: expected a collection, got " + coll.getClass().getSimpleName());
  }

  /**
   * Generic into operation. (into to-coll from-coll)
   *
   * <p>Pours elements from from-coll into to-coll using conj.
   */
  public static LispObject into(LispObject[] params) {
    LispObject toColl = params[0];
    LispObject fromColl = params[1];

    // Get elements from source collection
    Iterable<LispObject> elements;

    if (fromColl instanceof PVectorObject) {
      elements = (PVectorObject) fromColl;
    } else if (fromColl instanceof PSetObject) {
      elements = (PSetObject) fromColl;
    } else if (fromColl == ListObject.NIL) {
      return toColl; // Nothing to add
    } else if (fromColl.asList() != null) {
      elements = fromColl.asList();
    } else if (fromColl instanceof PMapObject) {
      // For maps, iterate over entries as lists
      ListObject entries = ((PMapObject) fromColl).entries();
      if (entries == ListObject.NIL) {
        return toColl;
      }
      elements = entries;
    } else {
      throw new LispArgumentError(
          "into: source must be a collection, got " + fromColl.getClass().getSimpleName());
    }

    // Conj each element into target
    if (toColl instanceof PVectorObject) {
      PVectorObject vec = (PVectorObject) toColl;
      for (LispObject elem : elements) {
        vec = vec.conj(elem);
      }
      return vec;
    }

    if (toColl instanceof PSetObject) {
      PSetObject set = (PSetObject) toColl;
      for (LispObject elem : elements) {
        set = set.conj(elem);
      }
      return set;
    }

    if (toColl instanceof PMapObject) {
      PMapObject map = (PMapObject) toColl;
      for (LispObject elem : elements) {
        ListObject pair = elem.asList();
        if (pair == null || pair.length() < 2) {
          throw new LispArgumentError(
              "into: for p-map target, each element must be a (key value) pair");
        }
        map = map.assoc(pair.car(), pair.cdr().car());
      }
      return map;
    }

    throw new LispArgumentError(
        "into: target must be a persistent collection, got " + toColl.getClass().getSimpleName());
  }

  /** Check if a value is any persistent collection type. (persistent? x) */
  public static LispObject isPersistent(LispObject[] params) {
    LispObject obj = params[0];
    return (obj instanceof PVectorObject || obj instanceof PMapObject || obj instanceof PSetObject)
        ? BooleanObject.TRUE
        : BooleanObject.FALSE;
  }
}
