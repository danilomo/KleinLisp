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
import net.sourceforge.kleinlisp.objects.PVectorObject;

/**
 * A Seq implementation that wraps a PVectorObject with a starting index. This provides efficient
 * sequential access to persistent vectors by simply incrementing an index rather than copying data.
 *
 * @author Danilo Oliveira
 */
public final class IndexedSeq implements Seq {

  private final PVectorObject vector;
  private final int index;

  public IndexedSeq(PVectorObject vector) {
    this(vector, 0);
  }

  public IndexedSeq(PVectorObject vector, int index) {
    this.vector = vector;
    this.index = index;
  }

  @Override
  public LispObject first() {
    if (index >= vector.length()) {
      return null;
    }
    return vector.get(index);
  }

  @Override
  public Seq rest() {
    if (index >= vector.length()) {
      return this;
    }
    return new IndexedSeq(vector, index + 1);
  }

  @Override
  public boolean isEmpty() {
    return index >= vector.length();
  }

  /** Returns the underlying PVectorObject. */
  public PVectorObject getVector() {
    return vector;
  }

  /** Returns the current index in the vector. */
  public int getIndex() {
    return index;
  }
}
