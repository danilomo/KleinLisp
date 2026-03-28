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

import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * R7RS Promise object for lazy evaluation.
 *
 * <p>A promise wraps an expression that will be evaluated on first force. The result is cached for
 * subsequent forces.
 */
public final class PromiseObject implements LispObject {

  private Supplier<LispObject> computation; // The unevaluated computation (null after forced)
  private LispObject value; // Cached result (null until forced)
  private boolean forced; // Has this promise been forced?

  /**
   * Create a promise from an unevaluated computation.
   *
   * @param computation the supplier that will compute the value when forced
   */
  public PromiseObject(Supplier<LispObject> computation) {
    this.computation = computation;
    this.value = null;
    this.forced = false;
  }

  /**
   * Create a promise from an already-evaluated value (for make-promise).
   *
   * @param value the already-computed value
   */
  public PromiseObject(LispObject value) {
    this.computation = null;
    this.value = value;
    this.forced = true;
  }

  /**
   * Force the promise, evaluating if necessary. Returns the cached value on subsequent calls.
   *
   * <p>Note: Unlike some lazy Scheme implementations, standard R7RS force does NOT automatically
   * force nested promises. If the expression evaluates to a promise, that promise is returned as-is
   * (not forced).
   *
   * @return the forced value
   */
  public LispObject force() {
    if (!forced) {
      // Evaluate the expression
      value = computation.get();

      // Clear reference to allow garbage collection
      computation = null;
      forced = true;
    }
    return value;
  }

  /**
   * Check if this promise has been forced.
   *
   * @return true if the promise has been forced
   */
  public boolean isForced() {
    return forced;
  }

  @Override
  public Object asObject() {
    return this;
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
    if (forced) {
      return "#<promise (forced): " + value + ">";
    }
    return "#<promise>";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    PromiseObject that = (PromiseObject) obj;

    // Force both promises and compare values
    return force().equals(that.force());
  }

  @Override
  public int hashCode() {
    return force().hashCode();
  }
}
