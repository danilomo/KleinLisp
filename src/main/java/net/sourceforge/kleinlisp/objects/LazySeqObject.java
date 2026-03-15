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

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.Seq;

/**
 * A LispObject wrapper for lazy sequences. This allows lazy sequences (like file line readers) to
 * be first-class Lisp values that can be passed around and used with seq operations.
 *
 * @author Danilo Oliveira
 */
public final class LazySeqObject implements LispObject {

  private final Seq seq;
  private final String description;

  public LazySeqObject(Seq seq) {
    this(seq, "lazy-seq");
  }

  public LazySeqObject(Seq seq, String description) {
    this.seq = seq;
    this.description = description;
  }

  @Override
  public Object asObject() {
    return seq;
  }

  @Override
  public boolean truthiness() {
    return !seq.isEmpty();
  }

  @Override
  public Seq asSeq() {
    if (seq.isEmpty()) {
      return null;
    }
    return seq;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    if (seq.isEmpty()) {
      return "()";
    }
    // Don't force the entire sequence - just show it's a lazy seq
    return "#<" + description + ">";
  }

  /** Returns the underlying Seq. */
  public Seq getSeq() {
    return seq;
  }
}
