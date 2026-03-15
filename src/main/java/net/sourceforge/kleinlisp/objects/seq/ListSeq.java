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
package net.sourceforge.kleinlisp.objects.seq;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.Seq;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * A Seq implementation that wraps a ListObject (cons cell).
 * This is the most straightforward implementation as lists are already
 * inherently sequential.
 *
 * @author Danilo Oliveira
 */
public final class ListSeq implements Seq {

  private final ListObject list;

  public ListSeq(ListObject list) {
    this.list = list;
  }

  @Override
  public LispObject first() {
    if (list == ListObject.NIL) {
      return null;
    }
    return list.car();
  }

  @Override
  public Seq rest() {
    if (list == ListObject.NIL) {
      return this;
    }
    return new ListSeq(list.cdr());
  }

  @Override
  public boolean isEmpty() {
    return list == ListObject.NIL;
  }

  /**
   * Returns the underlying ListObject.
   */
  public ListObject getList() {
    return list;
  }
}
