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
package net.sourceforge.kleinlisp.evaluator;

import net.sourceforge.kleinlisp.objects.FunctionObject;

/**
 * One entry in the dynamic-wind extent stack: a (before, after) thunk pair together with its parent
 * frame (forming a singly-linked list). The {@code depth} field allows O(1) comparison of stack
 * depths when finding the common ancestor of two extents.
 */
public final class WindFrame {
  public final FunctionObject before;
  public final FunctionObject after;
  public final WindFrame parent;
  public final int depth;

  public WindFrame(FunctionObject before, FunctionObject after, WindFrame parent) {
    this.before = before;
    this.after = after;
    this.parent = parent;
    this.depth = (parent == null) ? 0 : parent.depth + 1;
  }
}
