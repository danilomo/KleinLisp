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

import net.sourceforge.kleinlisp.objects.AtomObject;

/**
 * Optimized environment for closures that capture exactly two variables. Avoids HashMap overhead by
 * storing bindings directly in fields.
 */
public class InlineEnvironment2 implements Environment {

  private final AtomObject name1;
  private LispObject value1;
  private final AtomObject name2;
  private LispObject value2;

  public InlineEnvironment2(
      AtomObject name1, LispObject value1, AtomObject name2, LispObject value2) {
    this.name1 = name1;
    this.value1 = value1;
    this.name2 = name2;
    this.value2 = value2;
  }

  @Override
  public LispObject lookupValue(AtomObject name) {
    if (name1 == name) {
      return value1;
    }
    if (name2 == name) {
      return value2;
    }
    return null;
  }

  @Override
  public LispObject lookupValueOrNull(AtomObject name) {
    if (name1 == name) {
      return value1;
    }
    if (name2 == name) {
      return value2;
    }
    return null;
  }

  @Override
  public void set(AtomObject name, LispObject obj) {
    if (name1 == name) {
      value1 = obj;
    } else if (name2 == name) {
      value2 = obj;
    }
  }

  @Override
  public boolean exists(AtomObject name) {
    return name1 == name || name2 == name;
  }

  @Override
  public String toString() {
    return "InlineEnvironment2{" + name1 + "=" + value1 + ", " + name2 + "=" + value2 + "}";
  }
}
