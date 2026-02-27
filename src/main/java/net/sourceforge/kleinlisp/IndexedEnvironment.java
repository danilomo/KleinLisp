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
package net.sourceforge.kleinlisp;

import java.util.Map;
import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 * Environment that supports O(1) indexed access for closure variables. Each captured variable is
 * assigned a slot index for fast lookup.
 */
public class IndexedEnvironment implements Environment {

  private final LispObject[] slots;
  private final Map<AtomObject, Integer> indexMap;
  private final Environment parent;

  /**
   * Creates an indexed environment with the given slots.
   *
   * @param slots Array of captured values
   * @param indexMap Map from atom to slot index for fallback lookup
   * @param parent Parent environment for non-indexed lookups
   */
  public IndexedEnvironment(
      LispObject[] slots, Map<AtomObject, Integer> indexMap, Environment parent) {
    this.slots = slots;
    this.indexMap = indexMap;
    this.parent = parent;
  }

  /** Direct indexed access - O(1) lookup. */
  public LispObject getSlot(int index) {
    return slots[index];
  }

  /** Direct indexed write - O(1). */
  public void setSlot(int index, LispObject value) {
    slots[index] = value;
  }

  @Override
  public LispObject lookupValue(AtomObject name) {
    Integer index = indexMap.get(name);
    if (index != null) {
      return slots[index];
    }
    return parent != null ? parent.lookupValue(name) : null;
  }

  @Override
  public LispObject lookupValueOrNull(AtomObject name) {
    Integer index = indexMap.get(name);
    if (index != null) {
      return slots[index];
    }
    return parent != null ? parent.lookupValueOrNull(name) : null;
  }

  @Override
  public void set(AtomObject name, LispObject obj) {
    Integer index = indexMap.get(name);
    if (index != null) {
      slots[index] = obj;
    } else if (parent != null) {
      parent.set(name, obj);
    }
  }

  @Override
  public boolean exists(AtomObject name) {
    if (indexMap.containsKey(name)) {
      return true;
    }
    return parent != null && parent.exists(name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("IndexedEnvironment{slots=[");
    for (int i = 0; i < slots.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(slots[i]);
    }
    sb.append("]}");
    return sb.toString();
  }
}
