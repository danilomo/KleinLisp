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

import java.util.Iterator;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.Seq;

/**
 * A Seq implementation that wraps a Java Iterator. This is used for sets and other iterables that
 * don't have indexed access. The iterator is consumed lazily - elements are only fetched when
 * needed.
 *
 * <p>Note: Because iterators are stateful and can only be consumed once, calling rest() on an
 * IteratorSeq will cache the first element and then return a new IteratorSeq wrapping the same
 * iterator.
 *
 * @author Danilo Oliveira
 */
public final class IteratorSeq implements Seq {

  private final Iterator<LispObject> iterator;
  private LispObject first;
  private boolean firstCached;
  private Seq rest;
  private boolean restComputed;
  private boolean empty;
  private boolean emptyComputed;

  public IteratorSeq(Iterator<LispObject> iterator) {
    this.iterator = iterator;
    this.firstCached = false;
    this.restComputed = false;
    this.emptyComputed = false;
  }

  private IteratorSeq(LispObject first, Iterator<LispObject> iterator) {
    this.iterator = iterator;
    this.first = first;
    this.firstCached = true;
    this.restComputed = false;
    this.empty = false;
    this.emptyComputed = true;
  }

  @Override
  public LispObject first() {
    ensureFirstCached();
    return first;
  }

  @Override
  public Seq rest() {
    if (!restComputed) {
      ensureFirstCached();
      if (empty) {
        rest = this;
      } else if (iterator.hasNext()) {
        rest = new IteratorSeq(iterator.next(), iterator);
      } else {
        rest = new EmptySeq();
      }
      restComputed = true;
    }
    return rest;
  }

  @Override
  public boolean isEmpty() {
    if (!emptyComputed) {
      ensureFirstCached();
    }
    return empty;
  }

  private void ensureFirstCached() {
    if (!firstCached) {
      if (iterator.hasNext()) {
        first = iterator.next();
        empty = false;
      } else {
        first = null;
        empty = true;
      }
      firstCached = true;
      emptyComputed = true;
    }
  }

  /** An empty sequence singleton for use when the iterator is exhausted. */
  private static final class EmptySeq implements Seq {
    @Override
    public LispObject first() {
      return null;
    }

    @Override
    public Seq rest() {
      return this;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }
  }
}
