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
import net.sourceforge.kleinlisp.special_forms.LambdaForm.LambdaFunction;

/**
 * Special marker object returned by tail calls to signal the trampoline. Instead of recursively
 * calling the function, this object carries the function and parameters for the next iteration.
 */
public class TailCallObject implements LispObject {

  private final LambdaFunction function;
  private final LispObject[] parameters;

  public TailCallObject(LambdaFunction function, LispObject[] parameters) {
    this.function = function;
    this.parameters = parameters;
  }

  public LambdaFunction getFunction() {
    return function;
  }

  public LispObject[] getParameters() {
    return parameters;
  }

  @Override
  public Object asObject() {
    return this;
  }

  @Override
  public <T> T accept(LispVisitor<T> visitor) {
    throw new UnsupportedOperationException("TailCallObject should not be visited");
  }

  @Override
  public boolean truthiness() {
    return true;
  }

  @Override
  public String toString() {
    return "#<tail-call>";
  }
}
