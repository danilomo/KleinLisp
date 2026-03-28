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

import java.util.ArrayDeque;
import java.util.Deque;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * R7RS Parameter object for dynamically-scoped variables.
 *
 * <p>A parameter is a procedure that can be called with:
 *
 * <ul>
 *   <li>No arguments: returns current value
 *   <li>One argument: sets the value (after passing through converter)
 * </ul>
 *
 * <p>parameterize temporarily binds new values in a dynamic scope.
 */
public final class ParameterObject implements LispObject, Function {

  private final Deque<LispObject> valueStack; // Stack for parameterize
  private final Function converter; // Optional converter function
  private FunctionObject functionWrapper; // Cached wrapper for asFunction()

  public ParameterObject(LispObject initialValue, Function converter) {
    this.valueStack = new ArrayDeque<>();
    this.converter = converter;

    // Apply converter to initial value if present
    if (converter != null) {
      initialValue = converter.evaluate(new LispObject[] {initialValue});
    }
    this.valueStack.push(initialValue);
  }

  public ParameterObject(LispObject initialValue) {
    this(initialValue, null);
  }

  /** Get current parameter value. */
  public LispObject getValue() {
    return valueStack.peek();
  }

  /** Set parameter value (applies converter if present). */
  public void setValue(LispObject value) {
    if (converter != null) {
      value = converter.evaluate(new LispObject[] {value});
    }
    // Replace top of stack
    valueStack.pop();
    valueStack.push(value);
  }

  /** Push a new value for parameterize (applies converter). */
  public void pushValue(LispObject value) {
    if (converter != null) {
      value = converter.evaluate(new LispObject[] {value});
    }
    valueStack.push(value);
  }

  /** Pop value after parameterize scope ends. */
  public void popValue() {
    if (valueStack.size() > 1) {
      valueStack.pop();
    }
  }

  @Override
  public LispObject evaluate(LispObject[] parameters) {
    if (parameters.length == 0) {
      return getValue();
    } else if (parameters.length == 1) {
      setValue(parameters[0]);
      return VoidObject.VOID;
    } else {
      throw new LispArgumentError("parameter: expected 0 or 1 arguments, got " + parameters.length);
    }
  }

  @Override
  public FunctionObject asFunction() {
    if (functionWrapper == null) {
      functionWrapper = new FunctionObject(this);
    }
    return functionWrapper;
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
    return "#<parameter: " + getValue() + ">";
  }
}
