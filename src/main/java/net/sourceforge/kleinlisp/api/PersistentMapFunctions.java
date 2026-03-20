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
import net.sourceforge.kleinlisp.objects.PMapObject;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

/**
 * Persistent map functions for KleinLisp. These functions provide Clojure-like persistent map
 * operations with structural sharing.
 */
public class PersistentMapFunctions {

  /**
   * Creates a persistent map from key-value pairs. (p-map) creates empty map (p-map k1 v1 k2 v2
   * ...) creates map with entries
   */
  public static LispObject pMap(LispObject[] params) {
    if (params.length == 0) {
      return PMapObject.EMPTY;
    }
    if (params.length % 2 != 0) {
      throw new LispArgumentError("p-map requires an even number of arguments (key-value pairs)");
    }
    PMap<LispObject, LispObject> map = HashTreePMap.empty();
    for (int i = 0; i < params.length; i += 2) {
      map = map.plus(params[i], params[i + 1]);
    }
    return new PMapObject(map);
  }

  /** Returns the value associated with the key, or NIL if not found. (p-map-get m key) */
  public static LispObject pMapGet(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-get requires a persistent map as first argument, got: " + describeType(params[0]));
    }
    if (params.length > 2) {
      // Optional default value
      return map.getOrDefault(params[1], params[2]);
    }
    return map.get(params[1]);
  }

  /**
   * Returns a new map with the key-value pair added or replaced. (p-map-assoc m k v) or
   * (p-map-assoc m k1 v1 k2 v2 ...) Does not modify the original map.
   */
  public static LispObject pMapAssoc(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-assoc requires a persistent map as first argument, got: "
              + describeType(params[0]));
    }
    if ((params.length - 1) % 2 != 0) {
      throw new LispArgumentError("p-map-assoc requires key-value pairs");
    }
    for (int i = 1; i < params.length; i += 2) {
      map = map.assoc(unwrap(params[i]), unwrap(params[i + 1]));
    }
    return map;
  }

  /**
   * Returns a new map with the key removed. (p-map-dissoc m key) or (p-map-dissoc m k1 k2 ...) Does
   * not modify the original map.
   */
  public static LispObject pMapDissoc(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-dissoc requires a persistent map as first argument, got: "
              + describeType(params[0]));
    }
    for (int i = 1; i < params.length; i++) {
      map = map.dissoc(unwrap(params[i]));
    }
    return map;
  }

  /** Returns true if the map contains the key. (p-map-contains? m key) */
  public static LispObject pMapContains(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-contains? requires a persistent map as first argument, got: "
              + describeType(params[0]));
    }
    return map.containsKey(unwrap(params[1])) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Returns a list of all keys. (p-map-keys m) */
  public static LispObject pMapKeys(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-keys requires a persistent map, got: " + describeType(params[0]));
    }
    return map.keys();
  }

  /** Returns a list of all values. (p-map-vals m) */
  public static LispObject pMapVals(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-vals requires a persistent map, got: " + describeType(params[0]));
    }
    return map.vals();
  }

  /** Returns a list of [key value] pairs. (p-map-entries m) */
  public static LispObject pMapEntries(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-entries requires a persistent map, got: " + describeType(params[0]));
    }
    return map.entries();
  }

  /** Returns the number of entries in the map. (p-map-size m) */
  public static LispObject pMapSize(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-size requires a persistent map, got: " + describeType(params[0]));
    }
    return IntObject.valueOf(map.size());
  }

  /**
   * Merges two or more persistent maps. Later maps override earlier ones. (p-map-merge m1 m2 ...)
   */
  public static LispObject pMapMerge(LispObject[] params) {
    if (params.length == 0) {
      return PMapObject.EMPTY;
    }
    PMapObject result = asPMap(params[0]);
    if (result == null) {
      throw new LispArgumentError(
          "p-map-merge requires persistent maps, got: " + describeType(params[0]));
    }
    for (int i = 1; i < params.length; i++) {
      PMapObject other = asPMap(params[i]);
      if (other == null) {
        throw new LispArgumentError(
            "p-map-merge requires persistent maps, argument "
                + (i + 1)
                + " is: "
                + describeType(params[i]));
      }
      result = result.merge(other);
    }
    return result;
  }

  /** Converts a persistent map to a flat list of alternating keys and values. (p-map->list m) */
  public static LispObject pMapToList(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map->list requires a persistent map, got: " + describeType(params[0]));
    }
    return map.toList();
  }

  /**
   * Converts a list of key-value pairs to a persistent map. (list->p-map l) The list should be (k1
   * v1 k2 v2 ...) or ((k1 v1) (k2 v2) ...)
   */
  public static LispObject listToPMap(LispObject[] params) {
    if (params[0] == ListObject.NIL) {
      return PMapObject.EMPTY;
    }
    ListObject list = params[0].asList();
    if (list == null) {
      throw new LispArgumentError("list->p-map requires a list");
    }

    PMap<LispObject, LispObject> map = HashTreePMap.empty();

    // Check if first element is a list (alist format)
    if (list.car().asList() != null && list.car().asList() != ListObject.NIL) {
      // Format: ((k1 v1) (k2 v2) ...)
      for (LispObject entry : list) {
        ListObject pair = entry.asList();
        if (pair == null || pair.length() < 2) {
          throw new LispArgumentError("list->p-map: each entry must be a list of (key value)");
        }
        map = map.plus(pair.car(), pair.cdr().car());
      }
    } else {
      // Format: (k1 v1 k2 v2 ...)
      ListObject ptr = list;
      while (ptr != ListObject.NIL) {
        LispObject key = ptr.car();
        ptr = ptr.cdr();
        if (ptr == ListObject.NIL) {
          throw new LispArgumentError("list->p-map: odd number of elements");
        }
        LispObject value = ptr.car();
        ptr = ptr.cdr();
        map = map.plus(key, value);
      }
    }

    return new PMapObject(map);
  }

  /** Tests if the value is a persistent map. (p-map? x) */
  public static LispObject isPMap(LispObject[] params) {
    return (asPMap(params[0]) != null) ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /** Tests if the persistent map is empty. (p-map-empty? m) */
  public static LispObject isPMapEmpty(LispObject[] params) {
    PMapObject map = asPMap(params[0]);
    if (map == null) {
      throw new LispArgumentError(
          "p-map-empty? requires a persistent map, got: " + describeType(params[0]));
    }
    return map.isEmpty() ? BooleanObject.TRUE : BooleanObject.FALSE;
  }

  /**
   * Unwraps a LispObject if it's a CellObject. CellObjects are used to wrap values in closures and
   * need to be unwrapped before type checks.
   */
  private static LispObject unwrap(LispObject obj) {
    if (obj instanceof CellObject) {
      return ((CellObject) obj).get();
    }
    return obj;
  }

  /**
   * Unwraps and casts to PMapObject, or returns null if not a PMapObject. Handles CellObject
   * wrapping.
   */
  private static PMapObject asPMap(LispObject obj) {
    LispObject unwrapped = unwrap(obj);
    if (unwrapped instanceof PMapObject) {
      return (PMapObject) unwrapped;
    }
    return null;
  }

  /** Returns a human-readable description of a LispObject's type for error messages. */
  private static String describeType(LispObject obj) {
    // Unwrap CellObjects for better error messages
    obj = unwrap(obj);
    if (obj == null) {
      return "null";
    }
    if (obj == ListObject.NIL) {
      return "'() (empty list)";
    }
    if (obj instanceof PMapObject) {
      return "persistent map";
    }
    if (obj instanceof net.sourceforge.kleinlisp.objects.PSetObject) {
      return "persistent set";
    }
    if (obj instanceof net.sourceforge.kleinlisp.objects.PVectorObject) {
      return "persistent vector";
    }
    if (obj.asList() != null) {
      return "list";
    }
    if (obj.asInt() != null) {
      return "integer (" + obj.asInt().value + ")";
    }
    if (obj.asDouble() != null) {
      return "number (" + obj.asDouble().value + ")";
    }
    if (obj.asString() != null) {
      return "string (\"" + obj.asString().value() + "\")";
    }
    if (obj.asAtom() != null) {
      return "symbol (" + obj.asAtom() + ")";
    }
    if (obj instanceof net.sourceforge.kleinlisp.objects.BooleanObject) {
      return obj.truthiness() ? "#t (true)" : "#f (false)";
    }
    if (obj.asFunction() != null) {
      return "function";
    }
    return obj.getClass().getSimpleName();
  }
}
