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

import java.util.Map;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

/**
 * Persistent map type for KleinLisp. Based on PCollections HashTreePMap, providing immutable maps
 * with efficient structural sharing. Similar to Clojure maps.
 *
 * <p>Maps are callable as functions: (my-map key) returns the value for key (my-map key default)
 * returns the value or default if not found
 */
public final class PMapObject implements LispObject, Function {

  public static final PMapObject EMPTY = new PMapObject(HashTreePMap.empty());

  private final PMap<LispObject, LispObject> map;

  /** Creates a persistent map from a PMap. */
  public PMapObject(PMap<LispObject, LispObject> map) {
    this.map = map;
  }

  /** Returns the underlying PMap. */
  public PMap<LispObject, LispObject> getMap() {
    return map;
  }

  /** Returns the number of entries in the map. */
  public int size() {
    return map.size();
  }

  /** Returns true if the map is empty. */
  public boolean isEmpty() {
    return map.isEmpty();
  }

  /** Returns the value associated with the key, or NIL if not found. */
  public LispObject get(LispObject key) {
    LispObject value = map.get(key);
    return value != null ? value : ListObject.NIL;
  }

  /** Returns the value associated with the key, or the default if not found. */
  public LispObject getOrDefault(LispObject key, LispObject defaultValue) {
    LispObject value = map.get(key);
    return value != null ? value : defaultValue;
  }

  /** Returns true if the map contains the key. */
  public boolean containsKey(LispObject key) {
    return map.containsKey(key);
  }

  /** Returns a new map with the key-value pair added or replaced. */
  public PMapObject assoc(LispObject key, LispObject value) {
    return new PMapObject(map.plus(key, value));
  }

  /** Returns a new map with multiple key-value pairs added. */
  public PMapObject assocAll(PMapObject other) {
    return new PMapObject(map.plusAll(other.map));
  }

  /** Returns a new map with the key removed. */
  public PMapObject dissoc(LispObject key) {
    return new PMapObject(map.minus(key));
  }

  /** Returns a new map with multiple keys removed. */
  public PMapObject dissocAll(Iterable<LispObject> keys) {
    PMap<LispObject, LispObject> result = map;
    for (LispObject key : keys) {
      result = result.minus(key);
    }
    return new PMapObject(result);
  }

  /** Merges another map into this one. Values from other take precedence. */
  public PMapObject merge(PMapObject other) {
    return new PMapObject(map.plusAll(other.map));
  }

  /** Returns a list of all keys. */
  public ListObject keys() {
    if (map.isEmpty()) {
      return ListObject.NIL;
    }
    LispObject[] keys = map.keySet().toArray(new LispObject[0]);
    return (ListObject) ListObject.fromList(keys);
  }

  /** Returns a list of all values. */
  public ListObject vals() {
    if (map.isEmpty()) {
      return ListObject.NIL;
    }
    LispObject[] values = map.values().toArray(new LispObject[0]);
    return (ListObject) ListObject.fromList(values);
  }

  /** Returns a list of [key value] pairs as lists. */
  public ListObject entries() {
    if (map.isEmpty()) {
      return ListObject.NIL;
    }
    LispObject[] entries = new LispObject[map.size()];
    int i = 0;
    for (Map.Entry<LispObject, LispObject> entry : map.entrySet()) {
      entries[i++] = ListObject.fromList(new LispObject[] {entry.getKey(), entry.getValue()});
    }
    return (ListObject) ListObject.fromList(entries);
  }

  /** Converts to a flat list of alternating keys and values. */
  public ListObject toList() {
    if (map.isEmpty()) {
      return ListObject.NIL;
    }
    LispObject[] elements = new LispObject[map.size() * 2];
    int i = 0;
    for (Map.Entry<LispObject, LispObject> entry : map.entrySet()) {
      elements[i++] = entry.getKey();
      elements[i++] = entry.getValue();
    }
    return (ListObject) ListObject.fromList(elements);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<LispObject, LispObject> entry : map.entrySet()) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      sb.append(entry.getKey()).append(" ").append(entry.getValue());
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public Object asObject() {
    return map;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PMapObject)) return false;
    PMapObject other = (PMapObject) obj;
    return map.equals(other.map);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  /**
   * Makes the map callable as a function. (my-map key) returns value for key (my-map key default)
   * returns value or default if not found
   */
  @Override
  public LispObject evaluate(LispObject[] parameters) {
    if (parameters.length == 0) {
      throw new LispArgumentError("Map lookup requires a key");
    }
    LispObject key = parameters[0];
    LispObject defaultVal = parameters.length > 1 ? parameters[1] : ListObject.NIL;
    return getOrDefault(key, defaultVal);
  }

  @Override
  public FunctionObject asFunction() {
    return new FunctionObject(this);
  }
}
