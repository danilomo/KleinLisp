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

import java.util.Arrays;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;

/**
 * R7RS Error object created by the error procedure.
 *
 * @author daolivei
 */
public final class ErrorObject implements LispObject {

  private final String message;
  private final LispObject[] irritants;
  private final ErrorType errorType;

  public ErrorObject(String message) {
    this(message, new LispObject[0], ErrorType.GENERIC);
  }

  public ErrorObject(String message, LispObject[] irritants) {
    this(message, irritants, ErrorType.GENERIC);
  }

  public ErrorObject(String message, LispObject[] irritants, ErrorType errorType) {
    this.message = message;
    this.irritants = irritants;
    this.errorType = errorType;
  }

  public String getMessage() {
    return message;
  }

  public LispObject[] getIrritants() {
    return irritants;
  }

  public ListObject getIrritantsList() {
    return (ListObject) ListObject.fromList(Arrays.asList(irritants));
  }

  public ErrorType getErrorType() {
    return errorType;
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
    StringBuilder sb = new StringBuilder("#<error: ");
    sb.append(message);
    if (irritants.length > 0) {
      sb.append(" ");
      for (int i = 0; i < irritants.length; i++) {
        if (i > 0) sb.append(" ");
        sb.append(irritants[i]);
      }
    }
    sb.append(">");
    return sb.toString();
  }

  @Override
  public boolean error() {
    return true;
  }
}
