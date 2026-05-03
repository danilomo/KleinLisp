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
import net.sourceforge.kleinlisp.evaluator.ContinuationException;

/**
 * An escape continuation captured by call/cc. When invoked as a procedure, throws a
 * ContinuationException that unwinds to the matching call/cc boundary.
 *
 * <p>Implements LispObject and delegates function dispatch to an inner FunctionObject so it
 * participates normally in function calls, procedure?, and apply, while remaining identifiable via
 * instanceof for continuation?.
 */
public class EscapeContinuationObject implements LispObject {

  private final FunctionObject functionObject;

  public EscapeContinuationObject(Object key) {
    this.functionObject =
        new FunctionObject(
            params -> {
              LispObject value = (params != null && params.length > 0) ? params[0] : ListObject.NIL;
              throw new ContinuationException(key, value);
            });
  }

  @Override
  public FunctionObject asFunction() {
    return functionObject;
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
    return visitor.visit(functionObject);
  }

  @Override
  public String toString() {
    return "#<continuation>";
  }
}
