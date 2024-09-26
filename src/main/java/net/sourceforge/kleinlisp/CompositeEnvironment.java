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

public class CompositeEnvironment implements Environment {
  private final Environment left;
  private final Environment right;

  public CompositeEnvironment(Environment left, Environment right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public LispObject lookupValue(AtomObject name) {
    if (left.exists(name)) {
      return left.lookupValue(name);
    }

    return right.lookupValue(name);
  }

  @Override
  public void set(AtomObject name, LispObject obj) {
    left.set(name, obj);
  }

  @Override
  public boolean exists(AtomObject name) {
    return left.exists(name) || right.exists(name);
  }

  @Override
  public String toString() {
    return "CompositeEnvironment{" + "left=" + left + ", right=" + right + '}';
  }
}
