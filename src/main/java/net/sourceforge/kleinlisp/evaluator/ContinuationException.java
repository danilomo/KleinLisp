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

import net.sourceforge.kleinlisp.LispObject;

/**
 * Thrown when an escape continuation is invoked. Extends Error rather than RuntimeException so that
 * existing catch(RuntimeException) handlers (e.g. in GuardForm) do not intercept it — it propagates
 * up to the matching call/cc boundary.
 *
 * <p>Stack trace generation is suppressed for performance since this is used for control flow.
 */
public class ContinuationException extends Error {

  private final Object key;
  private final LispObject value;

  public ContinuationException(Object key, LispObject value) {
    super(null, null, true, false);
    this.key = key;
    this.value = value;
  }

  public Object getKey() {
    return key;
  }

  public LispObject getValue() {
    return value;
  }
}
