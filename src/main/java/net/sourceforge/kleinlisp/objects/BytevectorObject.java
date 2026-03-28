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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * R7RS Bytevector - mutable sequence of bytes (0-255).
 *
 * @author daolivei
 */
public class BytevectorObject implements LispObject {

  private final byte[] data;

  public BytevectorObject(int size) {
    this.data = new byte[size];
  }

  public BytevectorObject(int size, int fill) {
    this.data = new byte[size];
    Arrays.fill(data, (byte) fill);
  }

  public BytevectorObject(byte[] data) {
    this.data = data.clone();
  }

  public BytevectorObject(int[] values) {
    this.data = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
      if (values[i] < 0 || values[i] > 255) {
        throw new IllegalArgumentException("bytevector value out of range: " + values[i]);
      }
      data[i] = (byte) values[i];
    }
  }

  public int length() {
    return data.length;
  }

  public int get(int index) {
    if (index < 0 || index >= data.length) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }
    return data[index] & 0xFF; // Convert to unsigned
  }

  public void set(int index, int value) {
    if (index < 0 || index >= data.length) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }
    if (value < 0 || value > 255) {
      throw new IllegalArgumentException("Value out of range: " + value);
    }
    data[index] = (byte) value;
  }

  public byte[] getBytes() {
    return data.clone();
  }

  public BytevectorObject copy() {
    return new BytevectorObject(data);
  }

  public BytevectorObject copy(int start) {
    return copy(start, data.length);
  }

  public BytevectorObject copy(int start, int end) {
    if (start < 0 || end > data.length || start > end) {
      throw new IndexOutOfBoundsException("Invalid range: " + start + " to " + end);
    }
    return new BytevectorObject(Arrays.copyOfRange(data, start, end));
  }

  public void copyTo(BytevectorObject target, int at, int start, int end) {
    if (start < 0 || end > data.length || start > end) {
      throw new IndexOutOfBoundsException("Invalid source range");
    }
    if (at < 0 || at + (end - start) > target.data.length) {
      throw new IndexOutOfBoundsException("Invalid target position");
    }
    System.arraycopy(data, start, target.data, at, end - start);
  }

  public static BytevectorObject append(BytevectorObject... bytevectors) {
    int totalLength = 0;
    for (BytevectorObject bv : bytevectors) {
      totalLength += bv.length();
    }

    byte[] result = new byte[totalLength];
    int pos = 0;
    for (BytevectorObject bv : bytevectors) {
      System.arraycopy(bv.data, 0, result, pos, bv.data.length);
      pos += bv.data.length;
    }
    return new BytevectorObject(result);
  }

  public String toUtf8String() {
    return new String(data, StandardCharsets.UTF_8);
  }

  public static BytevectorObject fromUtf8String(String s) {
    return new BytevectorObject(s.getBytes(StandardCharsets.UTF_8));
  }

  public static BytevectorObject fromUtf8String(String s, int start, int end) {
    return new BytevectorObject(s.substring(start, end).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Object asObject() {
    return data.clone();
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
  public String toString() {
    StringBuilder sb = new StringBuilder("#u8(");
    for (int i = 0; i < data.length; i++) {
      if (i > 0) sb.append(" ");
      sb.append(data[i] & 0xFF);
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    BytevectorObject that = (BytevectorObject) obj;
    return Arrays.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }
}
