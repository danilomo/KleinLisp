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

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * @author danilo
 */
public class ComputedLispObject implements LispObject {

  private Supplier<LispObject> getter;
  private Consumer<LispObject> setter;
  private LispObject cache = null;

  public ComputedLispObject(Supplier<LispObject> getter, Consumer<LispObject> setter) {
    this.getter = getter;
    this.setter = setter;
  }

  public ComputedLispObject(Supplier<LispObject> getter) {
    this(getter, null);
  }

  public Supplier<LispObject> getComputed() {
    return getter;
  }

  @Override
  public Object asObject() {
    return getObj().asObject();
  }

  @Override
  public boolean truthiness() {
    return getObj().truthiness();
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public void set(LispObject value) {
    setter.accept(value);
  }

  @Override
  public boolean error() {
    return getObj().error();
  }

  private LispObject getObj() {
    if (setter != null) {
      return getter.get();
    }

    if (cache == null) {
      cache = getter.get();
    }

    return cache;
  }
}
