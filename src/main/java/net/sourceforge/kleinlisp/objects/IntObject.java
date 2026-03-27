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

import net.sourceforge.kleinlisp.LispVisitor;

/**
 * @author daolivei
 */
public final class IntObject implements NumericObject {

  private static final int CACHE_LOW = -128;
  private static final int CACHE_HIGH = 1024;
  private static final IntObject[] CACHE;

  static {
    CACHE = new IntObject[CACHE_HIGH - CACHE_LOW + 1];
    for (int i = 0; i < CACHE.length; i++) {
      CACHE[i] = new IntObject(CACHE_LOW + i, false);
    }
  }

  public final int value;

  private IntObject(int value, boolean unused) {
    this.value = value;
  }

  public IntObject(int value) {
    this.value = value;
  }

  public static IntObject valueOf(int value) {
    if (value >= CACHE_LOW && value <= CACHE_HIGH) {
      return CACHE[value - CACHE_LOW];
    }
    return new IntObject(value);
  }

  public int value() {
    return value;
  }

  @Override
  public int toInt() {
    return value;
  }

  @Override
  public double toDouble() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

  @Override
  public Object asObject() {
    return value;
  }

  @Override
  public IntObject asInt() {
    return this;
  }

  @Override
  public DoubleObject asDouble() {
    return new DoubleObject(value);
  }

  @Override
  public boolean truthiness() {
    return !(value == 0);
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
    if (!(obj instanceof IntObject)) return false;
    IntObject that = (IntObject) obj;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(value);
  }

  public boolean isExact() {
    return true;
  }
}
